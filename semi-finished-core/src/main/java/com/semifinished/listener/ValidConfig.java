package com.semifinished.listener;

import com.semifinished.config.ConfigProperties;
import com.semifinished.exception.ParamsException;
import com.semifinished.util.Assert;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(-600)
@AllArgsConstructor
public class ValidConfig implements ApplicationListener<ContextRefreshedEvent> {

    private final ConfigProperties configProperties;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        Assert.isTrue(configProperties.getMaxPageSize()<0,()->new ParamsException("maxPageSize不能小于0"));
        Assert.hasNotText(configProperties.getPageSizeKey(),()->new ParamsException("pageSizeKey不能为空"));
        Assert.hasNotText(configProperties.getPageNumKey(),()->new ParamsException("pageNumKey不能为空"));
        Assert.hasNotText(configProperties.getBracketsKey(),()->new ParamsException("bracketsKey不能为空"));
        Assert.hasNotText(configProperties.getIdKey(),()->new ParamsException("idKey不能为空"));

    }
}
