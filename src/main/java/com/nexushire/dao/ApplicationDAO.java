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
import com.nexushire.model.Application;
import com.nexushire.model.ApplicationStatus;
import com.nexushire.util.DBConnection;

public class ApplicationDAO implements GenericDAO<Application> {

    private static final int MYSQL_DUPLICATE_ENTRY = 1062;

    @Override
    public int save(Application app) throws DatabaseException {
        String sql = "INSERT INTO applications (job_id, seeker_id, cover_letter, status) VALUES (?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, app.getJobId());
            ps.setInt(2, app.getSeekerId());
            ps.setString(3, app.getCoverLetter());
            ps.setString(4, app.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    app.setId(keys.getInt(1));
                    return app.getId();
                }
            }
            throw new DatabaseException("Application was saved but no id was returned");
        } catch (SQLException e) {
            if (e.getErrorCode() == MYSQL_DUPLICATE_ENTRY) {
                throw new DatabaseException("You have already applied for this job", e);
            }
            throw new DatabaseException("Could not save application", e);
        }
    }

    @Override
    public Optional<Application> findById(int id) throws DatabaseException {
        return query("SELECT * FROM applications WHERE id = ?", id).stream().findFirst();
    }

    @Override
    public List<Application> findAll() throws DatabaseException {
        return query("SELECT * FROM applications ORDER BY applied_at DESC", null);
    }

    /** Job seeker's "application status" and "application history" views. */
    public List<Application> findBySeeker(int seekerId) throws DatabaseException {
        return query("SELECT * FROM applications WHERE seeker_id = ? ORDER BY applied_at DESC", seekerId);
    }

    /** Employer's view of applicants for one job. */
    public List<Application> findByJob(int jobId) throws DatabaseException {
        return query("SELECT * FROM applications WHERE job_id = ? ORDER BY applied_at DESC", jobId);
    }

    @Override
    public boolean update(Application app) throws DatabaseException {
        return updateStatus(app.getId(), app.getStatus());
    }

    public boolean updateStatus(int applicationId, ApplicationStatus status) throws DatabaseException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE applications SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, applicationId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Could not update application", e);
        }
    }

    @Override
    public boolean delete(int id) throws DatabaseException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM applications WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Could not delete application", e);
        }
    }

    private List<Application> query(String sql, Integer param) throws DatabaseException {
        List<Application> apps = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (param != null) ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) apps.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load applications", e);
        }
        return apps;
    }

    private Application mapRow(ResultSet rs) throws SQLException {
        Application app = new Application();
        app.setId(rs.getInt("id"));
        app.setJobId(rs.getInt("job_id"));
        app.setSeekerId(rs.getInt("seeker_id"));
        app.setCoverLetter(rs.getString("cover_letter"));
        app.setStatus(ApplicationStatus.valueOf(rs.getString("status")));
        app.setAppliedAt(rs.getTimestamp("applied_at").toLocalDateTime());
        return app;
    }
}
