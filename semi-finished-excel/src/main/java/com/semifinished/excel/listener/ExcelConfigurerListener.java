package com.semifinished.excel.listener;

import com.semifinished.excel.config.ExcelConfigurer;
import com.semifinished.excel.service.ExcelService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ExcelConfigurerListener implements ApplicationListener<ContextRefreshedEvent> {
    private final List<ExcelConfigurer> excelConfigurers;
    private final ExcelService excelService;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (CollectionUtils.isEmpty(excelConfigurers)) {
            return;
        }

        for (ExcelConfigurer excelConfigurer : excelConfigurers) {
            excelConfigurer.addHeaderMap(excelService.getHeaderMap());
        }
    }
}
