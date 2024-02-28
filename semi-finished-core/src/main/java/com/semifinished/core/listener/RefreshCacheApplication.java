package com.semifinished.core.listener;


import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 用于刷新配置缓存
 */
@Component
public class RefreshCacheApplication extends ApplicationContextEvent {
    private final ApplicationContext applicationContext;

    public RefreshCacheApplication(ApplicationContext source) {
        super(source);
        this.applicationContext = source;
    }

    /**
     * 发布{@link RefreshCacheApplication}事件
     */
    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent() {
        applicationContext.publishEvent(new RefreshCacheApplication(applicationContext));
    }
}
