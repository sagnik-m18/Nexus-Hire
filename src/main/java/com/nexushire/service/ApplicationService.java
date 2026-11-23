package com.nexushire.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.nexushire.dao.ApplicationDAO;
import com.nexushire.dao.JobDAO;
import com.nexushire.dao.UserDAO;
import com.nexushire.exception.DatabaseException;
import com.nexushire.model.Application;
import com.nexushire.model.ApplicationStatus;
import com.nexushire.model.Job;
import com.nexushire.model.JobStatus;
import com.nexushire.model.User;

/** Applying to jobs, the job seeker's applications, and the employer's review of applicants. */
public class ApplicationService {

    private final ApplicationDAO applicationDAO = new ApplicationDAO();
    private final JobDAO jobDAO = new JobDAO();
    private final UserDAO userDAO = new UserDAO();

    // ---------- Job seeker ----------

    /** The job, but only if it is approved (people can only apply to approved jobs). */
    public Optional<Job> findApplyableJob(int jobId) throws DatabaseException {
        return jobDAO.findById(jobId).filter(job -> job.getStatus() == JobStatus.APPROVED);
    }

    public boolean hasApplied(int seekerId, int jobId) throws DatabaseException {
        return applicationDAO.findBySeeker(seekerId).stream().anyMatch(a -> a.getJobId() == jobId);
    }

    /** Saves a new application with status APPLIED. The cover letter is optional. */
    public Application apply(User seeker, int jobId, String coverLetter) throws DatabaseException {
        String letter = (coverLetter == null || coverLetter.isBlank()) ? null : coverLetter.trim();
        Application application = new Application(jobId, seeker.getId(), letter);
        applicationDAO.save(application);
        return application;
    }

    /** All applications of one job seeker (newest first), each with its job. */
    public List<ApplicationView> findForSeeker(User seeker) throws DatabaseException {
        List<ApplicationView> views = new ArrayList<>();
        for (Application application : applicationDAO.findBySeeker(seeker.getId())) {
            Job job = jobDAO.findById(application.getJobId()).orElse(null);
            views.add(new ApplicationView(application, job));
        }
        return views;
    }

    // ---------- Employer ----------

    /** The job, but only if it was posted by this employer. */
    public Optional<Job> findOwnedJob(User employer, int jobId) throws DatabaseException {
        return jobDAO.findById(jobId).filter(job -> job.getEmployerId() == employer.getId());
    }

    /** Everyone who applied to one job (newest first), each with the applicant's details. */
    public List<ApplicationView> findApplicants(Job job) throws DatabaseException {
        List<ApplicationView> views = new ArrayList<>();
        for (Application application : applicationDAO.findByJob(job.getId())) {
            User seeker = userDAO.findById(application.getSeekerId()).orElse(null);
            views.add(new ApplicationView(application, job, seeker));
        }
        return views;
    }

    /** The application, but only if it is for a job posted by this employer. */
    public Optional<Application> findApplicationForEmployer(User employer, int applicationId) throws DatabaseException {
        Optional<Application> application = applicationDAO.findById(applicationId);
        if (application.isEmpty()) return Optional.empty();
        return findOwnedJob(employer, application.get().getJobId()).isPresent() ? application : Optional.empty();
    }

    public boolean setStatus(int applicationId, ApplicationStatus status) throws DatabaseException {
        return applicationDAO.updateStatus(applicationId, status);
    }
}
