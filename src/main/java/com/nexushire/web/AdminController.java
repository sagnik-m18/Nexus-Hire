package com.nexushire.web;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nexushire.exception.DatabaseException;
import com.nexushire.model.Role;
import com.nexushire.model.User;
import com.nexushire.service.AdminService;

import jakarta.servlet.http.HttpSession;

/** Admin pages: all users, all jobs, and removing bad ones. (Approving jobs is in JobController.) */
@Controller
public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

    private final AdminService admin = new AdminService();

    // ---------- Users ----------
    @GetMapping("/admin/users")
    public String users(HttpSession session, Model model) {
        requireAdmin(session);
        try {
            model.addAttribute("users", admin.findAllUsers());
        } catch (DatabaseException e) {
            log.error("Could not load users", e);
            model.addAttribute("users", List.of());
            model.addAttribute("loadError", "Could not load users right now. Please try again.");
        }
        return "admin-users";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        User current = requireAdmin(session);
        try {
            String problem = admin.deleteUser(current, id);
            if (problem == null) redirect.addFlashAttribute("success", "User deleted.");
            else redirect.addFlashAttribute("error", problem);
        } catch (DatabaseException e) {
            log.error("Could not delete user {}", id, e);
            redirect.addFlashAttribute("error", "Could not delete the user right now. Please try again.");
        }
        return "redirect:/admin/users";
    }

    // ---------- Jobs ----------
    @GetMapping("/admin/all-jobs")
    public String allJobs(HttpSession session, Model model) {
        requireAdmin(session);
        try {
            model.addAttribute("jobs", admin.findAllJobs());
        } catch (DatabaseException e) {
            log.error("Could not load jobs", e);
            model.addAttribute("jobs", List.of());
            model.addAttribute("loadError", "Could not load jobs right now. Please try again.");
        }
        return "admin-all-jobs";
    }

    @PostMapping("/admin/jobs/{id}/delete")
    public String deleteJob(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        requireAdmin(session);
        try {
            if (admin.deleteJob(id)) redirect.addFlashAttribute("success", "Job deleted.");
            else redirect.addFlashAttribute("error", "That job no longer exists.");
        } catch (DatabaseException e) {
            log.error("Could not delete job {}", id, e);
            redirect.addFlashAttribute("error", "Could not delete the job right now. Please try again.");
        }
        return "redirect:/admin/all-jobs";
    }

    /** The login check is done by AuthInterceptor; this makes sure the person is an admin. */
    private User requireAdmin(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can use this page");
        }
        return user;
    }
}
