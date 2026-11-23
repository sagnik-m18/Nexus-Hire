package com.nexushire.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nexushire.exception.DatabaseException;
import com.nexushire.model.JobSeeker;
import com.nexushire.model.Role;
import com.nexushire.model.User;
import com.nexushire.service.ProfileService;

import jakarta.servlet.http.HttpSession;

/** Job seeker page: change your name and resume. Employers see the resume when you apply. */
@Controller
public class ProfileController {

    private static final Logger log = LoggerFactory.getLogger(ProfileController.class);

    private static final int MAX_NAME = 100;      // users.name is VARCHAR(100)
    private static final int MAX_RESUME = 10000;

    private final ProfileService profiles = new ProfileService();

    @GetMapping("/profile")
    public String profilePage(HttpSession session, Model model) {
        User user = requireSeeker(session);
        try {
            JobSeeker seeker = profiles.findSeeker(user.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found"));
            model.addAttribute("name", seeker.getName());
            model.addAttribute("email", seeker.getEmail());
            model.addAttribute("resume", seeker.getResume() == null ? "" : seeker.getResume());
            return "profile";
        } catch (DatabaseException e) {
            log.error("Could not load profile", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not load your profile");
        }
    }

    @PostMapping("/profile")
    public String saveProfile(@RequestParam String name,
                              @RequestParam(defaultValue = "") String resume,
                              HttpSession session,
                              Model model,
                              RedirectAttributes redirect) {
        User user = requireSeeker(session);

        // Keep what the person typed so they don't have to type it again
        model.addAttribute("name", name);
        model.addAttribute("email", user.getEmail());
        model.addAttribute("resume", resume);

        String problem = null;
        if (name.isBlank()) problem = "Please enter your name.";
        else if (name.trim().length() > MAX_NAME) problem = "Name is too long (max " + MAX_NAME + " characters).";
        else if (resume.trim().length() > MAX_RESUME) problem = "Resume is too long (max " + MAX_RESUME + " characters).";

        if (problem != null) {
            model.addAttribute("error", problem);
            return "profile";
        }

        try {
            JobSeeker updated = profiles.updateProfile(user.getId(), name, resume);
            session.setAttribute("user", updated);   // so the navbar shows the new name
            redirect.addFlashAttribute("success", "Profile saved.");
            return "redirect:/profile";
        } catch (DatabaseException e) {
            log.error("Could not save profile", e);
            model.addAttribute("error", "Could not save your profile right now. Please try again.");
            return "profile";
        }
    }

    /** The login check is done by AuthInterceptor; this makes sure the person is a job seeker. */
    private User requireSeeker(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.JOB_SEEKER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only job seekers can use this page");
        }
        return user;
    }
}
