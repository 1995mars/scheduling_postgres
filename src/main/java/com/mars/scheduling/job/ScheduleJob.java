package com.mars.scheduling.job;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ScheduleJob implements Job {
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        // Xử lý công việc
        try {
            Class<?> clazz = Class.forName("com.mars.scheduling.batch.VNEmployee");
            Method method = clazz.getDeclaredMethod("birthdayBatch");
            method.setAccessible(true);
            Object obj = clazz.newInstance();
            method.invoke(obj);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException |
                 NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
        }
        System.out.println(context + "-" + System.currentTimeMillis());
    }

}
