package mg.federation.agricole.api.repository;

import mg.federation.agricole.api.entity.MemberEntity;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Repository
public class MemberRepository {

    // MODIFICATION 1: findById avec String id
    public Optional<MemberEntity> findById(Connection conn, String id) throws SQLException {
        String sql = "SELECT id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, date_adhesion_federation FROM member WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(map(rs));
            }
            return Optional.empty();
        }
    }

    // MODIFICATION 2: findByIds avec List<String> ids
    public List<MemberEntity> findByIds(Connection conn, List<String> ids) throws SQLException {
        if (ids.isEmpty()) return List.of();
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, date_adhesion_federation FROM member WHERE id IN (" + placeholders + ")";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) {
                stmt.setString(i + 1, ids.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            List<MemberEntity> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        }
    }

    // MODIFICATION 3: insert avec id explicite (plus de RETURNING id, ne retourne rien)
    public void insert(Connection conn, MemberEntity member) throws SQLException {
        String sql = "INSERT INTO member (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, date_adhesion_federation) VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.getId());
            stmt.setString(2, member.getFirstName());
            stmt.setString(3, member.getLastName());
            stmt.setDate(4, Date.valueOf(member.getBirthDate()));
            stmt.setString(5, member.getGender());
            stmt.setString(6, member.getAddress());
            stmt.setString(7, member.getProfession());
            stmt.setString(8, member.getPhoneNumber());
            stmt.setString(9, member.getEmail());
            stmt.setDate(10, Date.valueOf(member.getDateAdhesionFederation()));
            stmt.executeUpdate();
        }
    }

    // MODIFICATION 4: countAll (inchangé - retourne le nombre total de membres)
    public int countAll(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM member";
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        }
    }

    // MODIFICATION 5: map - récupère l'id comme String
    private MemberEntity map(ResultSet rs) throws SQLException {
        MemberEntity m = new MemberEntity();
        m.setId(rs.getString("id"));
        m.setFirstName(rs.getString("first_name"));
        m.setLastName(rs.getString("last_name"));
        m.setBirthDate(rs.getDate("birth_date").toLocalDate());
        m.setGender(rs.getString("gender"));
        m.setAddress(rs.getString("address"));
        m.setProfession(rs.getString("profession"));
        m.setPhoneNumber(rs.getString("phone_number"));
        m.setEmail(rs.getString("email"));
        m.setDateAdhesionFederation(rs.getDate("date_adhesion_federation").toLocalDate());
        return m;
    }
}