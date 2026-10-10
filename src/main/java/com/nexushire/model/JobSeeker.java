package com.nexushire.model;

public class JobSeeker extends User {
    private String resume;

    public JobSeeker(int id, String name, String email, String passwordHash, String resume) {
        super(id, name, email, passwordHash);
        this.resume = resume;
    }
    public String getResume() { return resume; }
    public void setResume(String resume) { this.resume = resume; }

    @Override public Role getRole() { return Role.JOB_SEEKER; }
    @Override public String getDashboardTitle() { return "Job Seeker Dashboard"; }
}
