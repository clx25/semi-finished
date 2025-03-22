package com.semifinished.core.config;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.semifinished.core.pojo.Desensitization;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TestDesensitizeConfigurer implements CoreConfigurer {
    @Override
    public void addDesensitize(List<Desensitization> desensitize) {
        Desensitization phone = Desensitization.builder().table("user_sensitive_info")
                .column("phone")
                .left(3)
                .right(4)
                .build();
        desensitize.add(phone);

        Desensitization idCard = Desensitization.builder().table("user_sensitive_info")
                .column("id_card")
                .left(0.17)
                .right(0.17)
                .build();
        desensitize.add(idCard);

        Desensitization address = Desensitization.builder().table("user_sensitive_info")
                .column("address")
                .desensitize(node -> {
                    String addr = node.asText().replaceFirst(".+?(?=市)", "**") // 替换“市”之前的字符为**
                            .replaceFirst("(?<=市).+?(?=区)", "**"); // 替换“市”之后到“区”之前的字符为**
                    return JsonNodeFactory.instance.textNode(addr);
                })
                .build();
        desensitize.add(address);
    }

    @Override
    public void addJsonConfig(Map<String, String> jsonConfig) {
        // CoreConfigurer.super.addJsonConfig(jsonConfig);
    }
}
