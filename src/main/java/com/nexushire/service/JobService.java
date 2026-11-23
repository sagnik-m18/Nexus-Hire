package com.nexushire.service;

import java.util.List;
import java.util.Optional;

import com.nexushire.dao.JobDAO;
import com.nexushire.exception.DatabaseException;
import com.nexushire.model.Job;
import com.nexushire.model.JobStatus;
import com.nexushire.model.Role;
import com.nexushire.model.User;

/** Job rules: who can see a job, posting, editing, deleting, and admin approval. The controllers call this; this calls JobDAO. */
public class JobService {

    private final JobDAO jobDAO = new JobDAO();

    /** Approved jobs only, filtered by keyword (title/company) and location. */
    public List<Job> searchApproved(String keyword, String location) throws DatabaseException {
        return jobDAO.search(keyword, location);
    }

    /**
     * Returns the job only if this person is allowed to see it:
     * approved jobs are visible to everyone logged in; pending/rejected ones only to the employer who posted them and admins.
     */
    public Optional<Job> findVisibleTo(int jobId, User viewer) throws DatabaseException {
        Optional<Job> found = jobDAO.findById(jobId);
        if (found.isEmpty()) return found;
        Job job = found.get();
        boolean approved = job.getStatus() == JobStatus.APPROVED;
        boolean owner = viewer.getRole() == Role.EMPLOYER && viewer.getId() == job.getEmployerId();
        boolean admin = viewer.getRole() == Role.ADMIN;
        return (approved || owner || admin) ? found : Optional.empty();
    }

    /** The job, but only if it was posted by this employer. */
    public Optional<Job> findOwned(User employer, int jobId) throws DatabaseException {
        return jobDAO.findById(jobId).filter(job -> job.getEmployerId() == employer.getId());
    }

    /** New jobs start as PENDING until an admin approves them. */
    public Job postJob(User employer, String title, String company, String location,
                       double salary, String description) throws DatabaseException {
        Job job = new Job(employer.getId(), title.trim(), company.trim(), location.trim(),
                          salary, description.trim());
        jobDAO.save(job);
        return job;
    }

    /**
     * Saves the employer's changes. An approved or rejected job goes back to PENDING,
     * so the admin checks the new text before job seekers see it.
     */
    public void updateJob(Job job, String title, String company, String location,
                          double salary, String description) throws DatabaseException {
        job.setTitle(title.trim());
        job.setCompany(company.trim());
        job.setLocation(location.trim());
        job.setSalary(salary);
        job.setDescription(description.trim());
        jobDAO.update(job);
        if (job.getStatus() != JobStatus.PENDING) {
            jobDAO.updateStatus(job.getId(), JobStatus.PENDING);
        }
    }

    /** Deletes the job. The database also removes all applications for it. */
    public boolean deleteJob(int jobId) throws DatabaseException {
        return jobDAO.delete(jobId);
    }

    public List<Job> findByEmployer(User employer) throws DatabaseException {
        return jobDAO.findByEmployer(employer.getId());
    }

    public List<Job> findPending() throws DatabaseException {
        return jobDAO.findByStatus(JobStatus.PENDING);
    }

    /** Admin approves or rejects a job. Returns false if the job no longer exists. */
    public boolean decide(int jobId, JobStatus status) throws DatabaseException {
        if (jobDAO.findById(jobId).isEmpty()) return false;
        return jobDAO.updateStatus(jobId, status);
    }
}
