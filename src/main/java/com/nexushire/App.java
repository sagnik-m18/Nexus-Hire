package com.nexushire;

import java.sql.Connection;
import java.util.List;

import com.nexushire.dao.UserDAO;
import com.nexushire.exception.DatabaseException;
import com.nexushire.model.Admin;
import com.nexushire.model.Employer;
import com.nexushire.model.JobSeeker;
import com.nexushire.model.User;
import com.nexushire.util.DBConnection;
import com.nexushire.util.PasswordUtil;

/** Review-1 check: proves the JDBC connection works and seeds the three demo accounts. */
public class App {

    public static void main(String[] args) {
        try (Connection con = DBConnection.getConnection()) {
            System.out.println("Connected to: " + con.getMetaData().getURL());

            UserDAO users = new UserDAO();
            if (users.findAll().isEmpty()) {
                users.save(new Admin(0, "Site Admin", "admin@nexushire.com", PasswordUtil.hash("Admin@123")));
                users.save(new Employer(0, "Demo Employer", "employer@nexushire.com", PasswordUtil.hash("Employer@123")));
                users.save(new JobSeeker(0, "Demo Seeker", "seeker@nexushire.com", PasswordUtil.hash("Seeker@123"), "Sample resume text"));
                System.out.println("Seeded demo accounts.");
            }

            List<User> all = users.findAll();
            for (User u : all) {
                // Polymorphism: each subclass supplies its own dashboard title
                System.out.println(u + " -> " + u.getDashboardTitle());
            }
        } catch (DatabaseException e) {
            System.err.println("Database problem: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Could not connect. Is MySQL running and is the nexushire database created? " + e.getMessage());
        }
    }
}
