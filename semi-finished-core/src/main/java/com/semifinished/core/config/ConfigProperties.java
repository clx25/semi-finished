package com.semifinished.core.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties("semi-finished.core")
public class ConfigProperties {

    private String dataSource = "master";

    /**
     * 在没有指定分页参数时的最大获取行数
     */
    private int maxPageSize = 0;

    /**
     * 分页参数pageSize与pageNum的键名
     */
    private String pageSizeKey = "pageSize";
    private String pageNumKey = "pageNum";


    /**
     * 数据中心id,用于雪花算法
     */
    private long datacenterId = 1L;

    /**
     * 机器标识,用于雪花算法
     */
    private long machineId = 1L;


    /**
     * page参数是否合理化
     * 逻辑是如果pageSize与pageNum超过了数据的最后一页，那么就获取最后一页
     */
    private boolean pageNormalized = true;

    /**
     * 括号规则的key
     * <pre>
     *     "a":{
     *         "value":"b",
     *         "c":"d"
     *     },
     *     "|e":{
     *      "value":"f",
     *      "g":"h:
     *      }
     * </pre>
     * 以上的规则解析为 where (a=b and c=d) or (e=f and g=h)
     */
    private String bracketsKey = "value";

    /**
     * 主键字段
     */
    private String idKey = "id";

    /**
     * 是否自由删除
     * 为false时只能根据id删除
     * 为true时可以根据请求条件删除，注意数据安全
     */
    private boolean freeDelete = false;

    /**
     * 是否逻辑删除
     */
    private boolean logicDelete=false;

    /**
     * 逻辑删除字段，如果开启了逻辑删除又没有该字段，会提示错误
     */
    private String logicDeleteColumn = "deleted";

}
