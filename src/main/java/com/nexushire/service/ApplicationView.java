package com.nexushire.service;

import com.nexushire.model.Application;
import com.nexushire.model.Job;
import com.nexushire.model.JobSeeker;
import com.nexushire.model.User;

/** One application with its job and (for employers) the person who applied, so a page can show everything. */
public class ApplicationView {

    private final Application application;
    private final Job job;
    private final User seeker;

    public ApplicationView(Application application, Job job) {
        this(application, job, null);
    }

    public ApplicationView(Application application, Job job, User seeker) {
        this.application = application;
        this.job = job;
        this.seeker = seeker;
    }

    public Application getApplication() { return application; }
    public Job getJob() { return job; }
    public User getSeeker() { return seeker; }

    /** The applicant's resume text, or null if they have none. */
    public String getResume() {
        return seeker instanceof JobSeeker js ? js.getResume() : null;
    }
}
