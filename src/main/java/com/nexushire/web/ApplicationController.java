package com.nexushire.web;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nexushire.exception.DatabaseException;
import com.nexushire.model.Job;
import com.nexushire.model.Role;
import com.nexushire.model.User;
import com.nexushire.service.ApplicationService;

import jakarta.servlet.http.HttpSession;

/** Job seeker pages: apply to a job and see "My applications". */
@Controller
public class ApplicationController {

    private static final Logger log = LoggerFactory.getLogger(ApplicationController.class);
    private static final int MAX_COVER_LETTER = 3000;

    private final ApplicationService applications = new ApplicationService();

    // ---------- Apply page ----------
    @GetMapping("/jobs/{id}/apply")
    public String applyPage(@PathVariable int id, HttpSession session, Model model, RedirectAttributes redirect) {
        User seeker = requireSeeker(session);
        try {
            Optional<Job> job = applications.findApplyableJob(id);
            if (job.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found");
            if (applications.hasApplied(seeker.getId(), id)) {
                redirect.addFlashAttribute("error", "You have already applied for this job.");
                return "redirect:/applications";
            }
            model.addAttribute("job", job.get());
            return "apply";
        } catch (DatabaseException e) {
            log.error("Could not load apply page for job {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not load the job");
        }
    }

    // ---------- Send the application ----------
    @PostMapping("/jobs/{id}/apply")
    public String apply(@PathVariable int id,
                        @RequestParam(defaultValue = "") String coverLetter,
                        HttpSession session,
                        Model model,
                        RedirectAttributes redirect) {
        User seeker = requireSeeker(session);
        try {
            Optional<Job> job = applications.findApplyableJob(id);
            if (job.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found");
            if (applications.hasApplied(seeker.getId(), id)) {
                redirect.addFlashAttribute("error", "You have already applied for this job.");
                return "redirect:/applications";
            }
            if (coverLetter.trim().length() > MAX_COVER_LETTER) {
                model.addAttribute("job", job.get());
                model.addAttribute("coverLetter", coverLetter);
                model.addAttribute("error", "Your cover letter is too long (max " + MAX_COVER_LETTER + " characters).");
                return "apply";
            }
            applications.apply(seeker, id, coverLetter);
            redirect.addFlashAttribute("success", "Application sent. You can follow its status on this page.");
            return "redirect:/applications";
        } catch (DatabaseException e) {
            log.error("Could not save application for job {}", id, e);
            redirect.addFlashAttribute("error", "Could not send your application right now. Please try again.");
            return "redirect:/jobs/" + id + "/apply";
        }
    }

    // ---------- My applications ----------
    @GetMapping("/applications")
    public String myApplications(HttpSession session, Model model) {
        User seeker = requireSeeker(session);
        try {
            model.addAttribute("rows", applications.findForSeeker(seeker));
        } catch (DatabaseException e) {
            log.error("Could not load applications", e);
            model.addAttribute("rows", List.of());
            model.addAttribute("loadError", "Could not load your applications right now. Please try again.");
        }
        return "my-applications";
    }

    // ---------- Helper ----------
    /** The login check is done by AuthInterceptor; this makes sure the person is a job seeker. */
    private User requireSeeker(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.JOB_SEEKER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only job seekers can use this page");
        }
        return user;
    }
}
