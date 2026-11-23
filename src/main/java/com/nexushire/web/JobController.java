package com.nexushire.web;

import java.math.BigDecimal;
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
import com.nexushire.model.JobStatus;
import com.nexushire.model.Role;
import com.nexushire.model.User;
import com.nexushire.service.JobService;

import jakarta.servlet.http.HttpSession;

/** Job pages: browse, details, employer posting / editing / deleting / "my jobs", admin approval. */
@Controller
public class JobController {

    private static final Logger log = LoggerFactory.getLogger(JobController.class);

    private final JobService jobs = new JobService();

    // ---------- Browse (everyone logged in) ----------
    @GetMapping("/jobs")
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "") String location,
                       Model model) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("location", location);
        try {
            model.addAttribute("jobs", jobs.searchApproved(keyword, location));
        } catch (DatabaseException e) {
            log.error("Could not load jobs", e);
            model.addAttribute("jobs", List.of());
            model.addAttribute("error", "Could not load jobs right now. Please try again.");
        }
        return "jobs";
    }

    @GetMapping("/jobs/{id}")
    public String detail(@PathVariable int id, HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        try {
            Optional<Job> job = jobs.findVisibleTo(id, user);
            if (job.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found");
            model.addAttribute("job", job.get());
            return "job-detail";
        } catch (DatabaseException e) {
            log.error("Could not load job {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not load the job");
        }
    }

    // ---------- Employer: post a job ----------
    @GetMapping("/employer/jobs/new")
    public String newJobPage(HttpSession session) {
        requireRole(session, Role.EMPLOYER);
        return "job-form";
    }

    @PostMapping("/employer/jobs")
    public String postJob(@RequestParam String title,
                          @RequestParam String company,
                          @RequestParam String location,
                          @RequestParam(defaultValue = "") String salary,
                          @RequestParam String description,
                          HttpSession session,
                          Model model,
                          RedirectAttributes redirect) {
        User employer = requireRole(session, Role.EMPLOYER);

        JobForm form = new JobForm(title, company, location, salary, description);
        form.addTo(model);   // keep what the person typed so they don't have to type it again

        String problem = form.validate();
        if (problem != null) {
            model.addAttribute("error", problem);
            return "job-form";
        }

        try {
            jobs.postJob(employer, title, company, location, form.getSalaryValue(), description);
            redirect.addFlashAttribute("success", "Job submitted. It will appear in the job list after an admin approves it.");
            return "redirect:/employer/jobs";
        } catch (DatabaseException e) {
            log.error("Could not save job", e);
            model.addAttribute("error", "Could not save the job right now. Please try again.");
            return "job-form";
        }
    }

    // ---------- Employer: my jobs ----------
    @GetMapping("/employer/jobs")
    public String myJobs(HttpSession session, Model model) {
        User employer = requireRole(session, Role.EMPLOYER);
        try {
            model.addAttribute("jobs", jobs.findByEmployer(employer));
        } catch (DatabaseException e) {
            log.error("Could not load employer jobs", e);
            model.addAttribute("jobs", List.of());
            model.addAttribute("error", "Could not load your jobs right now. Please try again.");
        }
        return "my-jobs";
    }

    // ---------- Employer: edit a job ----------
    @GetMapping("/employer/jobs/{id}/edit")
    public String editPage(@PathVariable int id, HttpSession session, Model model) {
        User employer = requireRole(session, Role.EMPLOYER);
        Job job = findOwnedJob(employer, id);

        String salaryText = job.getSalary() > 0
                ? BigDecimal.valueOf(job.getSalary()).stripTrailingZeros().toPlainString()
                : "";
        new JobForm(job.getTitle(), job.getCompany(), job.getLocation(), salaryText, job.getDescription()).addTo(model);
        model.addAttribute("jobId", id);
        model.addAttribute("jobStatus", job.getStatus().name());
        return "job-edit";
    }

    @PostMapping("/employer/jobs/{id}")
    public String updateJob(@PathVariable int id,
                            @RequestParam String title,
                            @RequestParam String company,
                            @RequestParam String location,
                            @RequestParam(defaultValue = "") String salary,
                            @RequestParam String description,
                            HttpSession session,
                            Model model,
                            RedirectAttributes redirect) {
        User employer = requireRole(session, Role.EMPLOYER);
        Job job = findOwnedJob(employer, id);
        boolean wasLive = job.getStatus() != JobStatus.PENDING;

        JobForm form = new JobForm(title, company, location, salary, description);
        String problem = form.validate();
        if (problem != null) {
            form.addTo(model);
            model.addAttribute("jobId", id);
            model.addAttribute("jobStatus", job.getStatus().name());
            model.addAttribute("error", problem);
            return "job-edit";
        }

        try {
            jobs.updateJob(job, title, company, location, form.getSalaryValue(), description);
            redirect.addFlashAttribute("success", wasLive
                    ? "Job updated. An admin needs to approve it again before job seekers can see it."
                    : "Job updated.");
            return "redirect:/employer/jobs";
        } catch (DatabaseException e) {
            log.error("Could not update job {}", id, e);
            form.addTo(model);
            model.addAttribute("jobId", id);
            model.addAttribute("jobStatus", job.getStatus().name());
            model.addAttribute("error", "Could not save your changes right now. Please try again.");
            return "job-edit";
        }
    }

    // ---------- Employer: delete a job ----------
    @PostMapping("/employer/jobs/{id}/delete")
    public String deleteJob(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        User employer = requireRole(session, Role.EMPLOYER);
        findOwnedJob(employer, id);   // 404 if it is not this employer's job
        try {
            jobs.deleteJob(id);
            redirect.addFlashAttribute("success", "Job deleted.");
        } catch (DatabaseException e) {
            log.error("Could not delete job {}", id, e);
            redirect.addFlashAttribute("error", "Could not delete the job right now. Please try again.");
        }
        return "redirect:/employer/jobs";
    }

    // ---------- Admin: approve / reject ----------
    @GetMapping("/admin/jobs")
    public String pendingJobs(HttpSession session, Model model) {
        requireRole(session, Role.ADMIN);
        try {
            model.addAttribute("jobs", jobs.findPending());
        } catch (DatabaseException e) {
            log.error("Could not load pending jobs", e);
            model.addAttribute("jobs", List.of());
            model.addAttribute("error", "Could not load pending jobs right now. Please try again.");
        }
        return "admin-jobs";
    }

    @PostMapping("/admin/jobs/{id}/approve")
    public String approve(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        return decide(id, JobStatus.APPROVED, "approved", session, redirect);
    }

    @PostMapping("/admin/jobs/{id}/reject")
    public String reject(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        return decide(id, JobStatus.REJECTED, "rejected", session, redirect);
    }

    private String decide(int id, JobStatus status, String word, HttpSession session, RedirectAttributes redirect) {
        requireRole(session, Role.ADMIN);
        try {
            if (jobs.decide(id, status)) {
                redirect.addFlashAttribute("success", "Job " + word + ".");
            } else {
                redirect.addFlashAttribute("error", "That job no longer exists.");
            }
        } catch (DatabaseException e) {
            log.error("Could not update job {}", id, e);
            redirect.addFlashAttribute("error", "Could not update the job right now. Please try again.");
        }
        return "redirect:/admin/jobs";
    }

    // ---------- Helpers ----------
    /** The login check is done by AuthInterceptor; this makes sure the person has the right role for the page. */
    private User requireRole(HttpSession session, Role role) {
        User user = (User) session.getAttribute("user");
        if (user == null || user.getRole() != role) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have access to this page");
        }
        return user;
    }

    /** Finds a job that belongs to this employer, or answers 404. */
    private Job findOwnedJob(User employer, int id) {
        try {
            return jobs.findOwned(employer, id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Job not found"));
        } catch (DatabaseException e) {
            log.error("Could not load job {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Could not load the job");
        }
    }
}
