package mg.federation.agricole.api.repository;

import mg.federation.agricole.api.entity.CollectivityEntity;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public class CollectivityRepository {

    public Long insert(Connection conn, CollectivityEntity collectivity) throws SQLException {
        String sql = "INSERT INTO collectivity (location, specialite_agricole, annual_dues_amount, date_creation, federation_approval) VALUES (?,?,?,?,?) RETURNING id";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, collectivity.getLocation());
            stmt.setString(2, collectivity.getSpecialiteAgricole());
            stmt.setInt(3, collectivity.getAnnualDuesAmount());
            stmt.setDate(4, Date.valueOf(collectivity.getDateCreation()));
            stmt.setBoolean(5, collectivity.getFederationApproval());
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getLong(1);
            }
            throw new SQLException("Insert failed");
        }
    }

    public Optional<CollectivityEntity> findById(Connection conn, Long id) throws SQLException {
        String sql = "SELECT id, location, specialite_agricole, annual_dues_amount, date_creation, federation_approval FROM collectivity WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                CollectivityEntity c = new CollectivityEntity();
                c.setId(rs.getLong("id"));
                c.setLocation(rs.getString("location"));
                c.setSpecialiteAgricole(rs.getString("specialite_agricole"));
                c.setAnnualDuesAmount(rs.getInt("annual_dues_amount"));
                c.setDateCreation(rs.getDate("date_creation").toLocalDate());
                c.setFederationApproval(rs.getBoolean("federation_approval"));
                return Optional.of(c);
            }
            return Optional.empty();
        }
    }

    public boolean hasNameAndNumber(Connection conn, Long id) throws SQLException {
        String sql = "SELECT name, number FROM collectivity WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name") != null && rs.getObject("number") != null;
            }
            return false;
        }
    }

    public void updateNameAndNumber(Connection conn, Long id, String name, Integer number) throws SQLException {
        String sql = "UPDATE collectivity SET name = ?, number = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setInt(2, number);
            stmt.setLong(3, id);
            int updated = stmt.executeUpdate();
            if (updated == 0) throw new SQLException("Collectivity not found");
        }
    }

    public boolean isNameUsed(Connection conn, String name, Long excludeId) throws SQLException {
        String sql = "SELECT 1 FROM collectivity WHERE name = ? AND id != ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setLong(2, excludeId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public boolean isNumberUsed(Connection conn, Integer number, Long excludeId) throws SQLException {
        String sql = "SELECT 1 FROM collectivity WHERE number = ? AND id != ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, number);
            stmt.setLong(2, excludeId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public Optional<CollectivityEntity> findByIdWithDetails(Connection conn, Long id) throws SQLException {
        String sql = "SELECT id, location, name, number, specialite_agricole, annual_dues_amount, date_creation, federation_approval " +
                "FROM collectivity WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                CollectivityEntity entity = new CollectivityEntity();
                entity.setId(rs.getLong("id"));
                entity.setLocation(rs.getString("location"));
                entity.setName(rs.getString("name"));
                entity.setNumber(rs.getInt("number"));
                entity.setSpecialiteAgricole(rs.getString("specialite_agricole"));
                entity.setAnnualDuesAmount(rs.getInt("annual_dues_amount"));
                entity.setDateCreation(rs.getDate("date_creation").toLocalDate());
                entity.setFederationApproval(rs.getBoolean("federation_approval"));
                return Optional.of(entity);
            }
            return Optional.empty();
        }
    }
}