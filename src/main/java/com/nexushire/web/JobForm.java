package com.nexushire.web;

import java.util.Objects;

import org.springframework.ui.Model;

/** The text fields of the "post a job" / "edit a job" form, and the rules they must follow. */
public class JobForm {

    // These match the column sizes in database/schema.sql
    static final int MAX_TITLE = 150;
    static final int MAX_COMPANY = 150;
    static final int MAX_LOCATION = 100;
    static final int MAX_DESCRIPTION = 5000;
    static final double MAX_SALARY = 9_999_999_999.0;   // DECIMAL(12,2)

    private final String title;
    private final String company;
    private final String location;
    private final String salary;
    private final String description;
    private double salaryValue;

    public JobForm(String title, String company, String location, String salary, String description) {
        this.title = Objects.requireNonNullElse(title, "");
        this.company = Objects.requireNonNullElse(company, "");
        this.location = Objects.requireNonNullElse(location, "");
        this.salary = Objects.requireNonNullElse(salary, "");
        this.description = Objects.requireNonNullElse(description, "");
    }

    /** Returns a message for the first problem found, or null if everything is fine. */
    public String validate() {
        if (title.isBlank()) return "Please enter a job title.";
        if (title.trim().length() > MAX_TITLE) return "Job title is too long (max " + MAX_TITLE + " characters).";
        if (company.isBlank()) return "Please enter the company name.";
        if (company.trim().length() > MAX_COMPANY) return "Company name is too long (max " + MAX_COMPANY + " characters).";
        if (location.isBlank()) return "Please enter the job location.";
        if (location.trim().length() > MAX_LOCATION) return "Location is too long (max " + MAX_LOCATION + " characters).";
        if (description.isBlank()) return "Please describe the job.";
        if (description.trim().length() > MAX_DESCRIPTION) return "Description is too long (max " + MAX_DESCRIPTION + " characters).";
        if (!salary.isBlank()) {
            try {
                salaryValue = Double.parseDouble(salary.trim().replace(",", ""));
            } catch (NumberFormatException e) {
                return "Salary must be a number, for example 600000.";
            }
            if (Double.isNaN(salaryValue) || Double.isInfinite(salaryValue)) return "Salary must be a number, for example 600000.";
            if (salaryValue < 0) return "Salary cannot be negative.";
            if (salaryValue > MAX_SALARY) return "Salary is too large.";
        }
        return null;
    }

    /** Only valid after validate() returned null. 0 means "not listed". */
    public double getSalaryValue() { return salaryValue; }

    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }

    /** Puts the typed values into the page model so the form can show them again. */
    public void addTo(Model model) {
        model.addAttribute("title", title);
        model.addAttribute("company", company);
        model.addAttribute("location", location);
        model.addAttribute("salary", salary);
        model.addAttribute("description", description);
    }
}
