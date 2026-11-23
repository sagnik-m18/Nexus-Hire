package com.nexushire.web;

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
import com.nexushire.model.Application;
import com.nexushire.model.ApplicationStatus;
import com.nexushire.model.Job;
import com.nexushire.model.Role;
import com.nexushire.model.User;
import com.nexushire.service.ApplicationService;

import jakarta.servlet.http.HttpSession;

/** Employer pages: see who applied to a job and update each application's status. */
@Controller
public class ApplicantController {

    private static final Logger log = LoggerFactory.getLogger(ApplicantController.class);

    private final ApplicationService applications = new ApplicationService();

    // ---------- Applicants of one job ----------
    @GetMapping("/employer/jobs/{id}/applicants")
    public String applicants(@PathVariable int id, HttpSession session, Model model) {
        User employer = requireEmployer(session);
        try {
            Optional<Job> job = applications.findOwnedJob(employer, id);
            if (job.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found");
            model.addAttribute("job", job.get());
            model.addAttribute("rows", applications.findApplicants(job.get()));
            return "employer-applicants";
        } catch (DatabaseException e) {
            log.error("Could not load applicants for job {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not load the applicants");
        }
    }

    // ---------- Change an application's status ----------
    @PostMapping("/employer/applications/{id}/status")
    public String updateStatus(@PathVariable int id,
                               @RequestParam(defaultValue = "") String status,
                               HttpSession session,
                               RedirectAttributes redirect) {
        User employer = requireEmployer(session);
        try {
            Optional<Application> application = applications.findApplicationForEmployer(employer, id);
            if (application.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found");

            int jobId = application.get().getJobId();
            ApplicationStatus newStatus = parseStatus(status);
            if (newStatus == null) {
                redirect.addFlashAttribute("error", "Please choose a status.");
            } else {
                applications.setStatus(id, newStatus);
                redirect.addFlashAttribute("success", "Status updated.");
            }
            return "redirect:/employer/jobs/" + jobId + "/applicants";
        } catch (DatabaseException e) {
            log.error("Could not update application {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not update the status");
        }
    }

    // ---------- Helpers ----------
    /** Employers can move an application to any status except back to APPLIED. Returns null for anything else. */
    private ApplicationStatus parseStatus(String text) {
        try {
            ApplicationStatus status = ApplicationStatus.valueOf(text);
            return status == ApplicationStatus.APPLIED ? null : status;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /** The login check is done by AuthInterceptor; this makes sure the person is an employer. */
    private User requireEmployer(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != Role.EMPLOYER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only employers can use this page");
        }
        return user;
    }
}
