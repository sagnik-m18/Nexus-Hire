package com.nexushire.service;

import java.util.Optional;

import com.nexushire.dao.UserDAO;
import com.nexushire.exception.DatabaseException;
import com.nexushire.model.Employer;
import com.nexushire.model.JobSeeker;
import com.nexushire.model.Role;
import com.nexushire.model.User;
import com.nexushire.util.PasswordUtil;

/** Login and registration rules. The screens call this; this calls the DAO. */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    /** Returns the user if the email and password are correct, otherwise empty. */
    public Optional<User> login(String email, String password) throws DatabaseException {
        Optional<User> found = userDAO.findByEmail(email.trim().toLowerCase());
        if (found.isPresent() && PasswordUtil.verify(password, found.get().getPasswordHash())) {
            return found;
        }
        return Optional.empty();
    }

    /**
     * Creates a new Employer or Job Seeker account.
     * Admins can never register themselves.
     * Throws DuplicateEmailException if the email is already used.
     */
    public User register(String name, String email, String password, Role role, String resume)
            throws DatabaseException {
        String cleanEmail = email.trim().toLowerCase();
        String hash = PasswordUtil.hash(password);
        User user;
        if (role == Role.EMPLOYER) {
            user = new Employer(0, name.trim(), cleanEmail, hash);
        } else if (role == Role.JOB_SEEKER) {
            user = new JobSeeker(0, name.trim(), cleanEmail, hash, resume);
        } else {
            throw new IllegalArgumentException("Admin accounts cannot be self-registered");
        }
        userDAO.save(user);
        return user;
    }
}
