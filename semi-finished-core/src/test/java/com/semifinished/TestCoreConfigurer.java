package com.semifinished;

import com.semifinished.config.CoreConfigurer;
import com.semifinished.pojo.Desensitization;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TestCoreConfigurer implements CoreConfigurer {

    @Override
    public void addDesensitize(List<Desensitization> desensitize) {
        Desensitization build = Desensitization.builder().table("info")
                .column("title")
                .desensitize((title) -> "a" + title + "c")
                .build();
        desensitize.add(build);
    }
}
