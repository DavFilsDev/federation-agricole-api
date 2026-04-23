package mg.federation.agricole.api.repository;

import mg.federation.agricole.api.entity.ReferenceEntity;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;

@Repository
public class ReferenceRepository {

    public void insert(Connection conn, ReferenceEntity ref) throws SQLException {
        String sql = "INSERT INTO reference (candidate_id, sponsor_id, relation_nature, sponsorship_date) VALUES (?,?,?,?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, ref.getCandidateId());
            stmt.setString(2, ref.getSponsorId());
            stmt.setString(3, ref.getRelationNature());
            stmt.setDate(4, Date.valueOf(ref.getSponsorshipDate()));
            stmt.executeUpdate();
        }
    }
}