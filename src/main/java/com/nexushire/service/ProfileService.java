package com.nexushire.service;

import java.util.Optional;

import com.nexushire.dao.UserDAO;
import com.nexushire.exception.DatabaseException;
import com.nexushire.model.JobSeeker;
import com.nexushire.model.User;

/** A job seeker's own profile: name and resume. The email and password are not changed here. */
public class ProfileService {

    private final UserDAO userDAO = new UserDAO();

    /** Loads the latest saved version of this job seeker. */
    public Optional<JobSeeker> findSeeker(int userId) throws DatabaseException {
        Optional<User> found = userDAO.findById(userId);
        if (found.isPresent() && found.get() instanceof JobSeeker seeker) {
            return Optional.of(seeker);
        }
        return Optional.empty();
    }

    /** Saves the new name and resume and returns the updated job seeker. An empty resume is stored as "no resume". */
    public JobSeeker updateProfile(int userId, String name, String resume) throws DatabaseException {
        JobSeeker seeker = findSeeker(userId)
                .orElseThrow(() -> new DatabaseException("Job seeker account not found"));
        seeker.setName(name.trim());
        seeker.setResume(resume == null || resume.isBlank() ? null : resume.trim());
        userDAO.update(seeker);
        return seeker;
    }
}
