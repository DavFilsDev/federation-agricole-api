package mg.federation.agricole.api.repository;

import mg.federation.agricole.api.dto.*;
import mg.federation.agricole.api.entity.ActivityEntity;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Repository
public class StatisticsRepository {

    private final AttendanceRepository attendanceRepository;
    private final ActivityRepository activityRepository;

    public StatisticsRepository(AttendanceRepository attendanceRepository,
                                ActivityRepository activityRepository) {
        this.attendanceRepository = attendanceRepository;
        this.activityRepository = activityRepository;
    }

    // Méthode existante (à garder)
    public List<CollectivityLocalStatistics> getCollectivityLocalStatistics(Connection conn, String collectivityId, LocalDate from, LocalDate to) throws SQLException {
        // Récupérer les membres actifs
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

        // Récupérer les activités de la collectivité dans la période
        List<ActivityEntity> activities = activityRepository.findByCollectivityId(conn, collectivityId);

        // Filtrer les activités qui ont une date d'exécution dans la période
        List<ActivityEntity> activitiesInPeriod = new ArrayList<>();
        for (ActivityEntity activity : activities) {
            LocalDate activityDate = getActivityDate(activity, from, to);
            if (activityDate != null && !activityDate.isBefore(from) && !activityDate.isAfter(to)) {
                activitiesInPeriod.add(activity);
            }
        }

        List<CollectivityLocalStatistics> statistics = new ArrayList<>();

        for (MemberDescription member : members) {
            // Calcul du montant payé (existant)
            BigDecimal earnedAmount = getMemberPaidAmount(conn, member.getId(), collectivityId, from, to);

            // Calcul du montant dû (existant)
            BigDecimal dueAmount = calculateDueAmount(conn, collectivityId, from, to);
            BigDecimal unpaidAmount = dueAmount.subtract(earnedAmount);
            if (unpaidAmount.compareTo(BigDecimal.ZERO) < 0) {
                unpaidAmount = BigDecimal.ZERO;
            }

            // NOUVEAU : Calcul du taux d'assiduité
            BigDecimal assiduityPercentage = calculateMemberAssiduity(conn, member.getId(), activitiesInPeriod);

            CollectivityLocalStatistics stat = new CollectivityLocalStatistics();
            stat.setMemberDescription(member);
            stat.setEarnedAmount(earnedAmount);
            stat.setUnpaidAmount(unpaidAmount);
            stat.setAssiduityPercentage(assiduityPercentage);
            statistics.add(stat);
        }

        return statistics;
    }

    // NOUVELLE MÉTHODE : Calculer le taux d'assiduité d'un membre
    private BigDecimal calculateMemberAssiduity(Connection conn, String memberId, List<ActivityEntity> activities) throws SQLException {
        if (activities == null || activities.isEmpty()) {
            return BigDecimal.ZERO;
        }

        int attendedCount = 0;
        int totalConcernedActivities = 0;

        for (ActivityEntity activity : activities) {
            // Vérifier si le membre est concerné par cette activité
            boolean isConcerned = activity.getMemberOccupationsConcerned() == null ||
                    activity.getMemberOccupationsConcerned().isEmpty();

            if (!isConcerned) {
                // Récupérer l'occupation du membre dans la collectivité
                String memberOccupation = getMemberOccupation(conn, memberId, activity.getCollectivityId());
                if (memberOccupation != null && activity.getMemberOccupationsConcerned() != null) {
                    isConcerned = activity.getMemberOccupationsConcerned().stream()
                            .anyMatch(occ -> occ.name().equals(memberOccupation));
                }
            }

            if (isConcerned) {
                totalConcernedActivities++;
                var attendanceOpt = attendanceRepository.findByActivityIdAndMemberId(conn, activity.getId(), memberId);
                if (attendanceOpt.isPresent() && "ATTENDED".equals(attendanceOpt.get().getStatus().name())) {
                    attendedCount++;
                }
            }
        }

        if (totalConcernedActivities == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal percentage = BigDecimal.valueOf(attendedCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalConcernedActivities), 2, RoundingMode.HALF_UP);

        return percentage;
    }

    // Récupérer l'occupation d'un membre dans une collectivité
    private String getMemberOccupation(Connection conn, String memberId, String collectivityId) throws SQLException {
        String sql = "SELECT occupation FROM membership WHERE member_id = ? AND collectivity_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, memberId);
            stmt.setString(2, collectivityId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("occupation");
            }
            return null;
        }
    }

    // Obtenir la date d'une activité (à partir de executiveDate ou calculée depuis la récurrence)
    private LocalDate getActivityDate(ActivityEntity activity, LocalDate from, LocalDate to) {
        if (activity.getExecutiveDate() != null) {
            return activity.getExecutiveDate();
        }

        // Pour les activités récurrentes, trouver la première occurrence dans la période
        // Pour simplifier, on prend la date de création comme référence
        // Dans une implémentation plus complète, il faudrait calculer les occurrences
        if (activity.getCreatedAt() != null) {
            LocalDate createdDate = activity.getCreatedAt().toLocalDate();
            if (!createdDate.isBefore(from) && !createdDate.isAfter(to)) {
                return createdDate;
            }
        }
        return null;
    }

    // Méthodes existantes (à garder)
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
                LocalDate eligibleFrom = rs.getDate("eligible_from").toLocalDate();
                String frequency = rs.getString("frequency");
                BigDecimal amount = rs.getBigDecimal("amount");

                int occurrences = countOccurrencesInPeriod(eligibleFrom, frequency, from, to);
                totalDue = totalDue.add(amount.multiply(BigDecimal.valueOf(occurrences)));
            }
        }
        return totalDue;
    }

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

    // NOUVELLE MÉTHODE : Statistiques globales avec assiduité
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

                BigDecimal percentageDue = calculateCurrentDuePercentage(conn, collectivityId, from, to, totalMembers);
                BigDecimal percentageAssiduity = calculateOverallAssiduityPercentage(conn, collectivityId, from, to, totalMembers);

                CollectivityInformation info = new CollectivityInformation();
                info.setName(rs.getString("name"));
                info.setNumber(rs.getInt("number"));

                CollectivityOverallStatistics stat = new CollectivityOverallStatistics();
                stat.setCollectivityInformation(info);
                stat.setNewMembersNumber(newMembers);
                stat.setOverallMemberCurrentDuePercentage(percentageDue);
                stat.setOverallMemberAssiduityPercentage(percentageAssiduity);

                statistics.add(stat);
            }
        }

        return statistics;
    }

    // NOUVELLE MÉTHODE : Calculer le taux d'assiduité global d'une collectivité
    private BigDecimal calculateOverallAssiduityPercentage(Connection conn, String collectivityId, LocalDate from, LocalDate to, int totalMembers) throws SQLException {
        if (totalMembers == 0) {
            return BigDecimal.ZERO;
        }

        // Récupérer les membres de la collectivité
        String membersSql = "SELECT member_id FROM membership WHERE collectivity_id = ?";
        List<String> memberIds = new ArrayList<>();
        try (PreparedStatement stmt = conn.prepareStatement(membersSql)) {
            stmt.setString(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                memberIds.add(rs.getString("member_id"));
            }
        }

        // Récupérer les activités de la collectivité dans la période
        List<ActivityEntity> activities = activityRepository.findByCollectivityId(conn, collectivityId);
        List<ActivityEntity> activitiesInPeriod = new ArrayList<>();
        for (ActivityEntity activity : activities) {
            LocalDate activityDate = getActivityDate(activity, from, to);
            if (activityDate != null && !activityDate.isBefore(from) && !activityDate.isAfter(to)) {
                activitiesInPeriod.add(activity);
            }
        }

        if (activitiesInPeriod.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal totalAssiduity = BigDecimal.ZERO;
        int validMemberCount = 0;

        for (String memberId : memberIds) {
            BigDecimal memberAssiduity = calculateMemberAssiduity(conn, memberId, activitiesInPeriod);
            if (memberAssiduity.compareTo(BigDecimal.ZERO) >= 0) {
                totalAssiduity = totalAssiduity.add(memberAssiduity);
                validMemberCount++;
            }
        }

        if (validMemberCount == 0) {
            return BigDecimal.ZERO;
        }

        return totalAssiduity.divide(BigDecimal.valueOf(validMemberCount), 2, RoundingMode.HALF_UP);
    }

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

        BigDecimal dueAmount = calculateDueAmount(conn, collectivityId, from, to);
        if (dueAmount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }

        int upToDateCount = 0;
        for (String memberId : memberIds) {
            BigDecimal paid = getMemberPaidAmount(conn, memberId, collectivityId, from, to);
            if (paid.compareTo(dueAmount) >= 0) {
                upToDateCount++;
            }
        }

        return BigDecimal.valueOf(upToDateCount)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(totalMembers), 2, RoundingMode.HALF_UP);
    }
}