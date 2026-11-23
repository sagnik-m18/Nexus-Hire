package com.nexushire.model;

import java.time.LocalDateTime;

public class Job {
    private int id;
    private int employerId;
    private String title;
    private String company;
    private String location;
    private double salary;
    private String description;
    private JobStatus status = JobStatus.PENDING;
    private LocalDateTime postedAt;

    public Job() {}

    public Job(int employerId, String title, String company, String location, double salary, String description) {
        this.employerId = employerId;
        this.title = title;
        this.company = company;
        this.location = location;
        this.salary = salary;
        this.description = description;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getEmployerId() { return employerId; }
    public void setEmployerId(int employerId) { this.employerId = employerId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public double getSalary() { return salary; }
    public void setSalary(double salary) { this.salary = salary; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public JobStatus getStatus() { return status; }
    public void setStatus(JobStatus status) { this.status = status; }
    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }

    @Override
    public String toString() {
        return title + " at " + company + " (" + location + ") [" + status + "]";
    }
}
