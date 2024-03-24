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
     * 通用查询开关，true表示启用，false表示关闭
     */
    private boolean commonApiEnable = true;

    /**
     * 外部的api文件夹与jar包所在目录的的相对路径
     */
    private String apiFolder = "SEMI-CONFIG";

    /**
     * 是否逻辑删除
     */
    private boolean logicDelete;

    /**
     * 逻辑删除字段，如果开启了逻辑删除又没有该字段，会提示错误
     */
    private String logicDeleteColumn = "deleted";
}
