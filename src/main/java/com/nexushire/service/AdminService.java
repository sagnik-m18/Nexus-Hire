package com.nexushire.service;

import java.util.List;
import java.util.Optional;

import com.nexushire.dao.JobDAO;
import com.nexushire.dao.UserDAO;
import com.nexushire.exception.DatabaseException;
import com.nexushire.model.Job;
import com.nexushire.model.Role;
import com.nexushire.model.User;

/** Admin-only actions: list everything and remove bad users or jobs. */
public class AdminService {

    private final UserDAO userDAO = new UserDAO();
    private final JobDAO jobDAO = new JobDAO();

    public List<User> findAllUsers() throws DatabaseException {
        return userDAO.findAll();
    }

    public List<Job> findAllJobs() throws DatabaseException {
        return jobDAO.findAll();
    }

    /**
     * Deletes a user. The database also removes that user's jobs and applications.
     * Returns a message if the delete is not allowed, or null when it worked.
     */
    public String deleteUser(User admin, int userId) throws DatabaseException {
        if (admin.getId() == userId) return "You cannot delete your own account.";
        Optional<User> target = userDAO.findById(userId);
        if (target.isEmpty()) return "That user no longer exists.";
        if (target.get().getRole() == Role.ADMIN) return "Admin accounts cannot be deleted here.";
        userDAO.delete(userId);
        return null;
    }

    /** Deletes a job (and, through the database, all its applications). Returns false if it no longer exists. */
    public boolean deleteJob(int jobId) throws DatabaseException {
        return jobDAO.delete(jobId);
    }
}
