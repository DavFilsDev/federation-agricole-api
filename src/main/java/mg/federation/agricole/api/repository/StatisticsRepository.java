package mg.federation.agricole.api.repository;

import mg.federation.agricole.api.config.DataSource;
import mg.federation.agricole.api.dto.CollectivityLocalStatistics;
import mg.federation.agricole.api.dto.CollectivityOverallStatistics;
import mg.federation.agricole.api.dto.MemberDescription;
import mg.federation.agricole.api.dto.CollectivityInformation;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Repository
public class StatisticsRepository {

    private final DataSource dataSource; // À injecter

    public StatisticsRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Récupère les statistiques locales d'une collectivité sur une période
     * Utilise le push-down processing (calcul en base)
     */
    public List<CollectivityLocalStatistics> getCollectivityLocalStatistics(Connection conn, String collectivityId, LocalDate from, LocalDate to) throws SQLException {
        // 1. Récupérer les membres actifs de la collectivité
        String membersSql = """
            SELECT m.id, m.first_name, m.last_name, m.email, ms.occupation
            FROM member m
            JOIN membership ms ON ms.member_id = m.id
            WHERE ms.collectivity_id = ?
            ORDER BY m.id
        """;

        List<MemberDescription> members = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(membersSql)) {
            stmt.setString(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                MemberDescription md = new MemberDescription();
                md.setId(rs.getString("id"));
                md.setFirstName(rs.getString("first_name"));
                md.setLastName(rs.getString("last_name"));
                md.setEmail(rs.getString("email"));
                md.setOccupation(rs.getString("occupation"));
                members.add(md);
            }
        }

        // 2. Pour chaque membre, calculer le montant payé sur la période
        List<CollectivityLocalStatistics> statistics = new ArrayList<>();

        for (MemberDescription member : members) {
            // Montant payé sur la période
            String paidSql = """
                SELECT COALESCE(SUM(amount), 0) as total_paid
                FROM transaction
                WHERE member_id = ? AND collectivity_id = ? AND creation_date BETWEEN ? AND ?
            """;

            BigDecimal earnedAmount = BigDecimal.ZERO;
            try (PreparedStatement stmt = conn.prepareStatement(paidSql)) {
                stmt.setString(1, member.getId());
                stmt.setString(2, collectivityId);
                stmt.setDate(3, Date.valueOf(from));
                stmt.setDate(4, Date.valueOf(to));
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    earnedAmount = rs.getBigDecimal("total_paid");
                }
            }

            // Montant dû sur la période (basé sur les cotisations actives)
            BigDecimal dueAmount = calculateDueAmount(conn, collectivityId, from, to);

            // Montant impayé = dû - payé (si positif, sinon 0)
            BigDecimal unpaidAmount = dueAmount.subtract(earnedAmount);
            if (unpaidAmount.compareTo(BigDecimal.ZERO) < 0) {
                unpaidAmount = BigDecimal.ZERO;
            }

            CollectivityLocalStatistics stat = new CollectivityLocalStatistics();
            stat.setMemberDescription(member);
            stat.setEarnedAmount(earnedAmount);
            stat.setUnpaidAmount(unpaidAmount);
            statistics.add(stat);
        }

        return statistics;
    }

    /**
     * Calcule le montant total dû par un membre sur une période
     * Basé sur les cotisations ACTIVES de sa collectivité
     */
    private BigDecimal calculateDueAmount(Connection conn, String collectivityId, LocalDate from, LocalDate to) throws SQLException {
        String sql = """
            SELECT id, eligible_from, frequency, amount
            FROM membership_fee
            WHERE collectivity_id = ? AND status = 'ACTIVE'
        """;

        BigDecimal totalDue = BigDecimal.ZERO;

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String feeId = rs.getString("id");
                LocalDate eligibleFrom = rs.getDate("eligible_from").toLocalDate();
                String frequency = rs.getString("frequency");
                BigDecimal amount = rs.getBigDecimal("amount");

                int occurrences = countOccurrencesInPeriod(eligibleFrom, frequency, from, to);
                totalDue = totalDue.add(amount.multiply(BigDecimal.valueOf(occurrences)));
            }
        }

        return totalDue;
    }

    /**
     * Compte le nombre d'occurrences d'une cotisation dans une période
     */
    private int countOccurrencesInPeriod(LocalDate eligibleFrom, String frequency, LocalDate from, LocalDate to) {
        if (to.isBefore(eligibleFrom)) {
            return 0;
        }

        LocalDate startDate = from.isBefore(eligibleFrom) ? eligibleFrom : from;
        int count = 0;

        switch (frequency) {
            case "ANNUALLY":
                LocalDate current = startDate;
                while (!current.isAfter(to)) {
                    if (current.getDayOfMonth() == eligibleFrom.getDayOfMonth() &&
                            current.getMonth() == eligibleFrom.getMonth()) {
                        count++;
                    }
                    current = current.plusYears(1);
                }
                break;

            case "MONTHLY":
                LocalDate monthlyCurrent = startDate.withDayOfMonth(eligibleFrom.getDayOfMonth());
                if (monthlyCurrent.isBefore(startDate)) {
                    monthlyCurrent = monthlyCurrent.plusMonths(1);
                }
                while (!monthlyCurrent.isAfter(to)) {
                    count++;
                    monthlyCurrent = monthlyCurrent.plusMonths(1);
                }
                break;

            case "WEEKLY":
                LocalDate weeklyCurrent = startDate;
                while (!weeklyCurrent.isAfter(to)) {
                    if (weeklyCurrent.getDayOfWeek().getValue() == eligibleFrom.getDayOfWeek().getValue()) {
                        count++;
                    }
                    weeklyCurrent = weeklyCurrent.plusDays(1);
                }
                break;

            case "PUNCTUALLY":
                if (!eligibleFrom.isBefore(from) && !eligibleFrom.isAfter(to)) {
                    count = 1;
                }
                break;
        }

        return count;
    }

    /**
     * Récupère les statistiques globales de toutes les collectivités
     */
    public List<CollectivityOverallStatistics> getOverallStatistics(Connection conn, LocalDate from, LocalDate to) throws SQLException {
        String sql = """
            SELECT c.id, c.name, c.number, c.location,
                   (SELECT COUNT(*) FROM membership ms WHERE ms.collectivity_id = c.id AND ms.date_adhesion BETWEEN ? AND ?) as new_members,
                   (SELECT COUNT(*) FROM membership ms WHERE ms.collectivity_id = c.id) as total_members
            FROM collectivity c
            ORDER BY c.id
        """;

        List<CollectivityOverallStatistics> statistics = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(from));
            stmt.setDate(2, Date.valueOf(to));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String collectivityId = rs.getString("id");
                int totalMembers = rs.getInt("total_members");
                int newMembers = rs.getInt("new_members");

                // Calculer le pourcentage de membres à jour
                BigDecimal percentage = calculateCurrentDuePercentage(conn, collectivityId, from, to, totalMembers);

                CollectivityInformation info = new CollectivityInformation();
                info.setName(rs.getString("name"));
                info.setNumber(rs.getInt("number"));

                CollectivityOverallStatistics stat = new CollectivityOverallStatistics();
                stat.setCollectivityInformation(info);
                stat.setNewMembersNumber(newMembers);
                stat.setOverallMemberCurrentDuePercentage(percentage);

                statistics.add(stat);
            }
        }

        return statistics;
    }

    /**
     * Calcule le pourcentage de membres à jour de leurs cotisations
     */
    private BigDecimal calculateCurrentDuePercentage(Connection conn, String collectivityId, LocalDate from, LocalDate to, int totalMembers) throws SQLException {
        if (totalMembers == 0) {
            return BigDecimal.ZERO;
        }

        String membersSql = "SELECT member_id FROM membership WHERE collectivity_id = ?";
        List<String> memberIds = new ArrayList<>();

        try (PreparedStatement stmt = conn.prepareStatement(membersSql)) {
            stmt.setString(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                memberIds.add(rs.getString("member_id"));
            }
        }

        int upToDateCount = 0;

        for (String memberId : memberIds) {
            BigDecimal paid = getMemberPaidAmount(conn, memberId, collectivityId, from, to);
            BigDecimal due = calculateDueAmountForMember(conn, collectivityId, from, to, memberId);

            if (paid.compareTo(due) >= 0) {
                upToDateCount++;
            }
        }

        BigDecimal percentage = BigDecimal.valueOf(upToDateCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalMembers), 2, BigDecimal.ROUND_HALF_UP);

        return percentage;
    }

    private BigDecimal getMemberPaidAmount(Connection conn, String memberId, String collectivityId, LocalDate from, LocalDate to) throws SQLException {
        String sql = """
            SELECT COALESCE(SUM(amount), 0) as total
            FROM transaction
            WHERE member_id = ? AND collectivity_id = ? AND creation_date BETWEEN ? AND ?
        """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, memberId);
            stmt.setString(2, collectivityId);
            stmt.setDate(3, Date.valueOf(from));
            stmt.setDate(4, Date.valueOf(to));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
            return BigDecimal.ZERO;
        }
    }

    private BigDecimal calculateDueAmountForMember(Connection conn, String collectivityId, LocalDate from, LocalDate to, String memberId) throws SQLException {
        // Pour un membre spécifique, le montant dû est le même que pour tous les membres de la collectivité
        // Car les cotisations s'appliquent à tous les membres
        return calculateDueAmount(conn, collectivityId, from, to);
    }
}