package com.semifinished.scheduling.task;

import com.fasterxml.jackson.databind.JsonNode;

public interface CronTask {
    void task(JsonNode args);
}
