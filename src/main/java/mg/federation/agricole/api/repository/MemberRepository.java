package mg.federation.agricole.api.repository;

import mg.federation.agricole.api.entity.MemberEntity;
import mg.federation.agricole.api.dto.MemberOccupation;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Repository
public class MemberRepository {

    public Optional<MemberEntity> findById(Connection conn, Long id) throws SQLException {
        String sql = "SELECT id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, date_adhesion_federation FROM member WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(map(rs));
            }
            return Optional.empty();
        }
    }

    public List<MemberEntity> findByIds(Connection conn, List<Long> ids) throws SQLException {
        if (ids.isEmpty()) return List.of();
        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, date_adhesion_federation FROM member WHERE id IN (" + placeholders + ")";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) {
                stmt.setLong(i + 1, ids.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            List<MemberEntity> list = new ArrayList<>();
            while (rs.next()) {
                list.add(map(rs));
            }
            return list;
        }
    }

    public Long insert(Connection conn, MemberEntity member) throws SQLException {
        String sql = "INSERT INTO member (first_name, last_name, birth_date, gender, address, profession, phone_number, email, date_adhesion_federation) VALUES (?,?,?,?,?,?,?,?,?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, member.getFirstName());
            stmt.setString(2, member.getLastName());
            stmt.setDate(3, Date.valueOf(member.getBirthDate()));
            stmt.setString(4, member.getGender());
            stmt.setString(5, member.getAddress());
            stmt.setString(6, member.getProfession());
            stmt.setString(7, member.getPhoneNumber());
            stmt.setString(8, member.getEmail());
            stmt.setDate(9, Date.valueOf(member.getDateAdhesionFederation()));
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Insert failed, no ID returned");
        }
    }

    private MemberEntity map(ResultSet rs) throws SQLException {
        MemberEntity m = new MemberEntity();
        m.setId(rs.getLong("id"));
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