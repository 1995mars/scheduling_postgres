package com.mars.scheduling.service;
import com.mars.scheduling.entity.Schedule;
import com.mars.scheduling.job.ScheduleJob;
import com.mars.scheduling.repository.ScheduleRepository;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class ScheduleService {

    @Autowired
    private Scheduler scheduler;

    @Autowired
    private ScheduleRepository scheduleRepository;

    public void addSchedule(Schedule schedule) throws SchedulerException {
        System.out.println(schedule);
        System.out.println(schedule.getCronExpression());
        JobDetail jobDetail = JobBuilder.newJob(ScheduleJob.class)
                .withIdentity(schedule.getId().toString())
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity(schedule.getId().toString())
                .withSchedule(CronScheduleBuilder.cronSchedule(schedule.getCronExpression()))
                .startAt(Date.from(schedule.getValidStartDate().atZone(ZoneId.systemDefault()).toInstant()))
                .endAt(Date.from(schedule.getValidEndDate().atZone(ZoneId.systemDefault()).toInstant()))
                .build();

        jobDetail.getJobDataMap().put("schedule", schedule);
        scheduler.scheduleJob(jobDetail, trigger);
        schedule.setEnabled(true);
        scheduleRepository.save(schedule);
    }

    public void updateSchedule(Schedule schedule) throws SchedulerException {
        TriggerKey triggerKey = new TriggerKey(schedule.getId().toString());
        JobKey jobKey = new JobKey(schedule.getId().toString());

        if (scheduler.checkExists(jobKey) && scheduler.checkExists(triggerKey)) {
            scheduler.deleteJob(jobKey);
            addSchedule(schedule);
        } else {
            throw new SchedulerException("Schedule does not exist");
        }
    }

    public void removeSchedule(Long scheduleId) throws SchedulerException {
        JobKey jobKey = new JobKey(scheduleId.toString());
        scheduler.deleteJob(jobKey);
        scheduleRepository.deleteById(scheduleId);
    }

    public void pauseSchedule(Long scheduleId) throws SchedulerException {
        JobKey jobKey = new JobKey(scheduleId.toString());
        scheduler.pauseJob(jobKey);
        Schedule schedule = scheduleRepository.getById(scheduleId);
        schedule.setEnabled(false);
        scheduleRepository.save(schedule);
    }

    public void resumeSchedule(Long scheduleId) throws SchedulerException {
        JobKey jobKey = new JobKey(scheduleId.toString());
        scheduler.resumeJob(jobKey);
        Schedule schedule = scheduleRepository.getById(scheduleId);
        schedule.setEnabled(true);
        scheduleRepository.save(schedule);
    }

    public void checkSchedules() {
        List<Schedule> schedules = scheduleRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (Schedule schedule : schedules) {
            if (schedule.isEnabled() && now.isAfter(schedule.getValidStartDate()) && now.isBefore(schedule.getValidEndDate())) {
                try {
                    JobKey jobKey = new JobKey(schedule.getId().toString());
                    scheduler.triggerJob(jobKey);
                } catch (SchedulerException e) {
                    e.printStackTrace();
                }
            } else if (schedule.isEnabled()) {
                try {
                    pauseSchedule(schedule.getId());
                } catch (SchedulerException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<Schedule> getAllSchedules() {
        List<Schedule> schedules = scheduleRepository.findAll();
        return schedules;
    }
}