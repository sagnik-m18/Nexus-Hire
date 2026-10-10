package com.nexushire.web;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nexushire.exception.DatabaseException;
import com.nexushire.exception.DuplicateEmailException;
import com.nexushire.model.Role;
import com.nexushire.model.User;
import com.nexushire.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** Handles the pages: home, login, register, dashboard, logout. */
@Controller
public class AuthController {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private final AuthService auth = new AuthService();

    // ---------- Home ----------
    @GetMapping("/")
    public String home(HttpSession session) {
        return session.getAttribute("user") == null ? "redirect:/login" : "redirect:/dashboard";
    }

    // ---------- Login ----------
    @GetMapping("/login")
    public String loginPage(HttpSession session) {
        if (session.getAttribute("user") != null) return "redirect:/dashboard";
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpServletRequest request,
                        Model model) {
        model.addAttribute("email", email);

        if (email.isBlank() || password.isEmpty()) {
            model.addAttribute("error", "Please enter your email and password.");
            return "login";
        }

        try {
            Optional<User> user = auth.login(email, password);
            if (user.isEmpty()) {
                // Same message for "no such email" and "wrong password" on purpose
                model.addAttribute("error", "Wrong email or password.");
                return "login";
            }
            request.getSession().invalidate();            // drop any old session
            HttpSession session = request.getSession(true); // start a fresh one
            session.setAttribute("user", user.get());
            return "redirect:/dashboard";
        } catch (DatabaseException e) {
            model.addAttribute("error", "Database problem: " + e.getMessage());
            return "login";
        }
    }

    // ---------- Register ----------
    @GetMapping("/register")
    public String registerPage(HttpSession session) {
        if (session.getAttribute("user") != null) return "redirect:/dashboard";
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String name,
                           @RequestParam String email,
                           @RequestParam String password,
                           @RequestParam String confirm,
                           @RequestParam String role,
                           @RequestParam(defaultValue = "") String resume,
                           Model model,
                           RedirectAttributes redirect) {
        // Keep what the person typed so they don't have to type it again
        model.addAttribute("name", name);
        model.addAttribute("email", email);
        model.addAttribute("role", role);
        model.addAttribute("resume", resume);

        String problem = null;
        if (name.isBlank()) problem = "Please enter your name.";
        else if (!EMAIL.matcher(email.trim()).matches()) problem = "Please enter a valid email.";
        else if (password.length() < 6) problem = "Password must be at least 6 characters.";
        else if (!password.equals(confirm)) problem = "Passwords do not match.";

        if (problem != null) {
            model.addAttribute("error", problem);
            return "register";
        }

        // Only Employer or Job Seeker can register. Admin is never allowed here.
        Role chosen = "EMPLOYER".equals(role) ? Role.EMPLOYER : Role.JOB_SEEKER;
        String resumeText = (chosen == Role.JOB_SEEKER && !resume.isBlank()) ? resume.trim() : null;

        try {
            auth.register(name, email, password, chosen, resumeText);
            redirect.addFlashAttribute("success", "Account created! Please log in.");
            return "redirect:/login";
        } catch (DuplicateEmailException e) {
            model.addAttribute("error", "That email is already registered.");
            return "register";
        } catch (DatabaseException e) {
            model.addAttribute("error", "Database problem: " + e.getMessage());
            return "register";
        }
    }

    // ---------- Dashboard (placeholder for now) ----------
    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/login"; // not logged in
        model.addAttribute("user", user);
        return "dashboard";
    }

    // ---------- Logout ----------
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
