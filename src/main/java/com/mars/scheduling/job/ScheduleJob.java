package com.mars.scheduling.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class ScheduleJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Xử lý công việc
        System.out.println(context + "-" + System.currentTimeMillis());
    }

}
