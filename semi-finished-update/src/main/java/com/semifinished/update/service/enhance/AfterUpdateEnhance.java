package com.semifinished.update.service.enhance;

import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.service.enhance.ServiceEnhance;
import lombok.AllArgsConstructor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public interface AfterUpdateEnhance extends ServiceEnhance {


    /**
     * 与sql的执行位于同一事务中
     *
     * @param executor      SQL执行器
     * @param sqlDefinition SQL定义信息
     */
    default void transactional(SqlAutoExecutor executor, SqlDefinition sqlDefinition) {

    }

    /**
     * 事务提交之后执行
     *
     * @param sqlDefinition SQL定义信息
     */
    default void afterExecute(SqlDefinition sqlDefinition) {
    }


    @AllArgsConstructor
    static class SqlAutoExecutor {
        private final Consumer<SqlDefinition> consumer;
        private final SqlDefinition sqlDefinition;
        /**
         * 是否跳过生成并执行SQL
         */
        private final AtomicBoolean skip = new AtomicBoolean(false);

        /**
         * 生成SQL并执行SQL
         */
        public void exec() {
            if (skip.compareAndSet(false, true)) {
                consumer.accept(sqlDefinition);
            }
        }

        /**
         * 设置最终是否跳过自动执行生成并执行SQL
         *
         * @param skip true 跳过，false 执行
         */
        public void setSkip(boolean skip) {
            this.skip.set(skip);
        }
    }
}
