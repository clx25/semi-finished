package com.semifinished.scheduling;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.scheduling.task.CronTask;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class TestTask implements CronTask {
    @Override
    public void task(JsonNode args) {
        System.out.println("xxxxxx" + LocalDateTime.now().toLocalTime());
    }
}
