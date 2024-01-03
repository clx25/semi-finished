package com.semifinished.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;
import java.util.Set;


@Getter
@Setter
public class DataSourceConfig extends HikariDataSource {

    /**
     * 映射关系
     */
    private Mapping mapping;

    /**
     * 排除字段
     */
    private Map<String, Set<String>> excludes;


    @Getter
    @Setter
    public static class Mapping {
        /**
         * 是否开启表名与字段映射
         * 如果开启了该功能，未匹配的将会提示异常
         */
        private boolean enable;

        /**
         * 表名映射
         */
        private Map<String, String> table;

        /**
         * 字段名映射
         */
        private Map<String, Map<String, String>> column;
    }
}
