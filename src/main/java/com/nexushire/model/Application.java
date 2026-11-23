package com.nexushire.model;

import java.time.LocalDateTime;

public class Application {
    private int id;
    private int jobId;
    private int seekerId;
    private String coverLetter;
    private ApplicationStatus status = ApplicationStatus.APPLIED;
    private LocalDateTime appliedAt;

    public Application() {}

    public Application(int jobId, int seekerId, String coverLetter) {
        this.jobId = jobId;
        this.seekerId = seekerId;
        this.coverLetter = coverLetter;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getJobId() { return jobId; }
    public void setJobId(int jobId) { this.jobId = jobId; }
    public int getSeekerId() { return seekerId; }
    public void setSeekerId(int seekerId) { this.seekerId = seekerId; }
    public String getCoverLetter() { return coverLetter; }
    public void setCoverLetter(String coverLetter) { this.coverLetter = coverLetter; }
    public ApplicationStatus getStatus() { return status; }
    public void setStatus(ApplicationStatus status) { this.status = status; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public void setAppliedAt(LocalDateTime appliedAt) { this.appliedAt = appliedAt; }

    @Override
    public String toString() {
        return "Application #" + id + " job=" + jobId + " seeker=" + seekerId + " [" + status + "]";
    }
}
