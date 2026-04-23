package mg.federation.agricole.api.repository;

import mg.federation.agricole.api.entity.CollectivityEntity;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.Optional;

@Repository
public class CollectivityRepository {

    public void insert(Connection conn, CollectivityEntity collectivity) throws SQLException {
        String sql = "INSERT INTO collectivity (id, location, specialite_agricole, annual_dues_amount, date_creation, federation_approval, name, number) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, collectivity.getId());
            stmt.setString(2, collectivity.getLocation());
            stmt.setString(3, collectivity.getSpecialiteAgricole());
            stmt.setInt(4, collectivity.getAnnualDuesAmount());
            stmt.setDate(5, Date.valueOf(collectivity.getDateCreation()));
            stmt.setBoolean(6, collectivity.getFederationApproval());
            stmt.setString(7, collectivity.getName());
            if (collectivity.getNumber() != null) {
                stmt.setInt(8, collectivity.getNumber());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }
            stmt.executeUpdate();
        }
    }

    public Optional<CollectivityEntity> findById(Connection conn, String id) throws SQLException {
        String sql = "SELECT id, location, specialite_agricole, annual_dues_amount, date_creation, federation_approval, name, number " +
                "FROM collectivity WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                CollectivityEntity c = new CollectivityEntity();
                c.setId(rs.getString("id"));
                c.setLocation(rs.getString("location"));
                c.setSpecialiteAgricole(rs.getString("specialite_agricole"));
                c.setAnnualDuesAmount(rs.getInt("annual_dues_amount"));
                c.setDateCreation(rs.getDate("date_creation").toLocalDate());
                c.setFederationApproval(rs.getBoolean("federation_approval"));
                c.setName(rs.getString("name"));
                int number = rs.getInt("number");
                c.setNumber(rs.wasNull() ? null : number);
                return Optional.of(c);
            }
            return Optional.empty();
        }
    }

    public boolean hasNameAndNumber(Connection conn, String id) throws SQLException {
        String sql = "SELECT name, number FROM collectivity WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("name") != null && rs.getObject("number") != null;
            }
            return false;
        }
    }

    public void updateNameAndNumber(Connection conn, String id, String name, Integer number) throws SQLException {
        String sql = "UPDATE collectivity SET name = ?, number = ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            if (number != null) {
                stmt.setInt(2, number);
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, id);
            int updated = stmt.executeUpdate();
            if (updated == 0) throw new SQLException("Collectivity not found");
        }
    }

    // MODIFICATION 5: isNameUsed avec String excludeId
    public boolean isNameUsed(Connection conn, String name, String excludeId) throws SQLException {
        String sql = "SELECT 1 FROM collectivity WHERE name = ? AND id != ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, excludeId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public boolean isNumberUsed(Connection conn, Integer number, String excludeId) throws SQLException {
        String sql = "SELECT 1 FROM collectivity WHERE number = ? AND id != ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, number);
            stmt.setString(2, excludeId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    public Optional<CollectivityEntity> findByIdWithDetails(Connection conn, String id) throws SQLException {
        String sql = "SELECT id, location, name, number, specialite_agricole, annual_dues_amount, date_creation, federation_approval " +
                "FROM collectivity WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                CollectivityEntity entity = new CollectivityEntity();
                entity.setId(rs.getString("id"));
                entity.setLocation(rs.getString("location"));
                entity.setName(rs.getString("name"));
                int number = rs.getInt("number");
                entity.setNumber(rs.wasNull() ? null : number);
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