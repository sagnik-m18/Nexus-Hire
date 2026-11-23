package com.nexushire.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.nexushire.exception.DatabaseException;
import com.nexushire.exception.DuplicateEmailException;
import com.nexushire.model.Admin;
import com.nexushire.model.Employer;
import com.nexushire.model.JobSeeker;
import com.nexushire.model.Role;
import com.nexushire.model.User;
import com.nexushire.util.DBConnection;

public class UserDAO implements GenericDAO<User> {

    private static final int MYSQL_DUPLICATE_ENTRY = 1062;

    @Override
    public int save(User user) throws DatabaseException {
        String sql = "INSERT INTO users (name, email, password_hash, role, resume) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole().name());
            ps.setString(5, user instanceof JobSeeker js ? js.getResume() : null);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getInt(1));
                    return user.getId();
                }
            }
            throw new DatabaseException("User was saved but no id was returned");
        } catch (SQLException e) {
            if (e.getErrorCode() == MYSQL_DUPLICATE_ENTRY) throw new DuplicateEmailException(user.getEmail());
            throw new DatabaseException("Could not save user", e);
        }
    }

    @Override
    public Optional<User> findById(int id) throws DatabaseException {
        return queryOne("SELECT * FROM users WHERE id = ?", String.valueOf(id));
    }

    public Optional<User> findByEmail(String email) throws DatabaseException {
        return queryOne("SELECT * FROM users WHERE email = ?", email);
    }

    @Override
    public List<User> findAll() throws DatabaseException {
        List<User> users = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("SELECT * FROM users ORDER BY id");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(mapRow(rs));
        } catch (SQLException e) {
            throw new DatabaseException("Could not load users", e);
        }
        return users;
    }

    @Override
    public boolean update(User user) throws DatabaseException {
        String sql = "UPDATE users SET name = ?, email = ?, resume = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, user.getName());
            ps.setString(2, user.getEmail());
            ps.setString(3, user instanceof JobSeeker js ? js.getResume() : null);
            ps.setInt(4, user.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e.getErrorCode() == MYSQL_DUPLICATE_ENTRY) throw new DuplicateEmailException(user.getEmail());
            throw new DatabaseException("Could not update user", e);
        }
    }

    @Override
    public boolean delete(int id) throws DatabaseException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM users WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Could not delete user", e);
        }
    }

    private Optional<User> queryOne(String sql, String param) throws DatabaseException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapRow(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not look up user", e);
        }
    }

    /** Builds the right subclass for the stored role. */
    private User mapRow(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        String email = rs.getString("email");
        String hash = rs.getString("password_hash");
        return switch (Role.valueOf(rs.getString("role"))) {
            case ADMIN -> new Admin(id, name, email, hash);
            case EMPLOYER -> new Employer(id, name, email, hash);
            case JOB_SEEKER -> new JobSeeker(id, name, email, hash, rs.getString("resume"));
        };
    }
}
