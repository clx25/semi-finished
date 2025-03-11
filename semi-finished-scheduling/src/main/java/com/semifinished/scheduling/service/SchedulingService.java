package com.semifinished.scheduling.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.core.utils.Assert;
import com.semifinished.scheduling.task.CronTask;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

@Component
@AllArgsConstructor
public class SchedulingService implements ApplicationListener<ContextRefreshedEvent> {
    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private final Map<String, CronTask> cronTaskMap;
    private final SqlExecutorHolder sqlExecutorHolder;
    private final ConfigProperties configProperties;
    /**
     * 定时任务的cron表达式
     * beanName->cron表达式
     */
    private final Map<String, String> scheduling = new ConcurrentHashMap<>();
    /**
     * 执行中的任务
     * beanName->任务对象
     */
    private final Map<String, ScheduledFuture<?>> scheduledFutures = new ConcurrentHashMap<>();


    /**
     * 启动定时任务
     *
     * @param beanName 任务类的beanName
     * @param cron     定时规则
     * @param args     参数
     */
    public void run(String beanName, String cron, JsonNode args) {
        Assert.notBlank(cron, () -> new ParamsException("cron不能为空"));
        //如果已经执行，那么不需要重复启动
        if (scheduledFutures.get(beanName) != null) {
            scheduling.put(beanName, cron);
            return;
        }
        //cron表达式在启动时添加到scheduling中，关闭时删除
        scheduling.put(beanName, cron);

        ScheduledFuture<?> schedule = threadPoolTaskScheduler.schedule(() -> cronTaskMap.get(beanName).task(args),
                triggerContext -> new CronTrigger(scheduling.get(beanName))
                        .nextExecutionTime(triggerContext)
        );
        scheduledFutures.put(beanName, schedule);
    }

    /**
     * 关闭定时任务
     *
     * @param beanName              任务类的beanName
     * @param mayInterruptIfRunning true直接打断任务，false等待本次任务完成
     */
    public void cancel(String beanName, boolean mayInterruptIfRunning) {
        ScheduledFuture<?> scheduledFuture = scheduledFutures.get(beanName);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(mayInterruptIfRunning);
            scheduledFutures.remove(beanName);
        }
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        String idKey = configProperties.getIdKey();
        List<ObjectNode> objectNodes = sqlExecutorHolder.dataSource().list("select * from semi_scheduling");

        List<String> remove = objectNodes.stream()
                .filter(node -> !cronTaskMap.containsKey(node.get("bean_name").asText()))
                .map(node -> node.get(idKey).asText())
                .collect(Collectors.toList());

        List<ObjectNode> add = cronTaskMap.keySet()
                .stream().filter(beanName -> objectNodes.stream()
                        .noneMatch(node -> beanName.equals(node.get("bean_name").asText())))
                .map(beanName -> {
                    ObjectNode objectNode = JsonNodeFactory.instance.objectNode();
                    objectNode.put("bean_name", beanName);
                    objectNode.put("enable", false);
                    return objectNode;
                })
                .collect(Collectors.toList());
        sqlExecutorHolder.dataSource().transaction(executor -> {
            executor.batchDelete("semi_scheduling", idKey, remove);
            executor.batchInsert("semi_scheduling", add, idKey);
        });


        objectNodes.stream()
                .filter(node -> node.get("enable").asBoolean())
                .filter(node -> cronTaskMap.containsKey(node.get("bean_name").asText()))
                .forEach(node -> {
                    String beanName = node.get("bean_name").asText();
                    JsonNode jsonNode = node.path("args");
                    run(beanName, node.get("cron").asText(), jsonNode);
                });
    }
}
