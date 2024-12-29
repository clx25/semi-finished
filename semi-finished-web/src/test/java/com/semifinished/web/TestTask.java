package com.semifinished.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.semifinished.scheduling.task.CronTask;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class TestTask implements CronTask {

    private final DateTimeFormatter dateTimeFormatter=DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss");
    @Override
    public void task(JsonNode args) {
        System.out.println(dateTimeFormatter.format(LocalDateTime.now()));
    }
}
