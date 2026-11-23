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
import com.nexushire.model.Job;
import com.nexushire.model.JobStatus;
import com.nexushire.util.DBConnection;

public class JobDAO implements GenericDAO<Job> {

    @Override
    public int save(Job job) throws DatabaseException {
        String sql = "INSERT INTO jobs (employer_id, title, company, location, salary, description, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, job.getEmployerId());
            ps.setString(2, job.getTitle());
            ps.setString(3, job.getCompany());
            ps.setString(4, job.getLocation());
            ps.setDouble(5, job.getSalary());
            ps.setString(6, job.getDescription());
            ps.setString(7, job.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    job.setId(keys.getInt(1));
                    return job.getId();
                }
            }
            throw new DatabaseException("Job was saved but no id was returned");
        } catch (SQLException e) {
            throw new DatabaseException("Could not save job", e);
        }
    }

    @Override
    public Optional<Job> findById(int id) throws DatabaseException {
        List<Job> found = query("SELECT * FROM jobs WHERE id = ?", String.valueOf(id));
        return found.stream().findFirst();
    }

    @Override
    public List<Job> findAll() throws DatabaseException {
        return query("SELECT * FROM jobs ORDER BY posted_at DESC");
    }

    public List<Job> findByStatus(JobStatus status) throws DatabaseException {
        return query("SELECT * FROM jobs WHERE status = ? ORDER BY posted_at DESC", status.name());
    }

    public List<Job> findByEmployer(int employerId) throws DatabaseException {
        return query("SELECT * FROM jobs WHERE employer_id = ? ORDER BY posted_at DESC", String.valueOf(employerId));
    }

    /** Job seeker search: approved jobs whose title/company matches the keyword and location matches. */
    public List<Job> search(String keyword, String location) throws DatabaseException {
        String k = "%" + (keyword == null ? "" : keyword.trim()) + "%";
        String l = "%" + (location == null ? "" : location.trim()) + "%";
        return query("SELECT * FROM jobs WHERE status = 'APPROVED' AND (title LIKE ? OR company LIKE ?) "
                   + "AND location LIKE ? ORDER BY posted_at DESC", k, k, l);
    }

    @Override
    public boolean update(Job job) throws DatabaseException {
        String sql = "UPDATE jobs SET title = ?, company = ?, location = ?, salary = ?, description = ? WHERE id = ?";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, job.getTitle());
            ps.setString(2, job.getCompany());
            ps.setString(3, job.getLocation());
            ps.setDouble(4, job.getSalary());
            ps.setString(5, job.getDescription());
            ps.setInt(6, job.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Could not update job", e);
        }
    }

    /** Admin approves or rejects a job posting. */
    public boolean updateStatus(int jobId, JobStatus status) throws DatabaseException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("UPDATE jobs SET status = ? WHERE id = ?")) {
            ps.setString(1, status.name());
            ps.setInt(2, jobId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Could not update job status", e);
        }
    }

    @Override
    public boolean delete(int id) throws DatabaseException {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM jobs WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Could not delete job", e);
        }
    }

    private List<Job> query(String sql, String... params) throws DatabaseException {
        List<Job> jobs = new ArrayList<>();
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setString(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) jobs.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DatabaseException("Could not load jobs", e);
        }
        return jobs;
    }

    private Job mapRow(ResultSet rs) throws SQLException {
        Job job = new Job();
        job.setId(rs.getInt("id"));
        job.setEmployerId(rs.getInt("employer_id"));
        job.setTitle(rs.getString("title"));
        job.setCompany(rs.getString("company"));
        job.setLocation(rs.getString("location"));
        job.setSalary(rs.getDouble("salary"));
        job.setDescription(rs.getString("description"));
        job.setStatus(JobStatus.valueOf(rs.getString("status")));
        job.setPostedAt(rs.getTimestamp("posted_at").toLocalDateTime());
        return job;
    }
}
