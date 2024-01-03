package com.semifinished.jdbc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.constant.ParserStatus;
import com.semifinished.jdbc.parser.SelectParamsParser;
import com.semifinished.pojo.AggregationFun;
import com.semifinished.pojo.Column;
import com.semifinished.pojo.ValueCondition;
import com.semifinished.pojo.ValueReplace;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;

/**
 * sql的定义文件，所有解析出来的数据保存到里面，根据这些数据生成sql
 *
 * @see SelectParamsParser
 */
@Getter
@Setter
public class SqlDefinition {
    public static final int GROUP_DISABLE = 0;//关闭group规则
    public static final int GROUP_COVER = 1;//group字段覆盖了查询字段
    public static final int GROUP_NOT_COVER = 2;//group字段未覆盖查询字段

    /**
     * 当前SqlDefinition所处状态，如正在解析子查询，正在解析join查询，正在解析括号
     *
     * @see ParserStatus
     */
    private int status = ParserStatus.NORMAL.getStatus();

    /**
     * 请求参数中指定的查询字段列表
     */
    private List<Column> columns = new ArrayList<>();

    /**
     * 请求参数，数据可能会在解析过程中被修改
     */
    private ObjectNode params;

    /**
     * 原始的请求参数,不应该对这个数据进行修改
     */
    private JsonNode rawParams;
    /**
     * sql类型
     */
    private int type;

    /**
     * 数据源名称，是在配置中的名称，不是数据库的名称
     */
    private String dataSource;

    /**
     * 当前登录用户的信息
     */
    private ObjectNode user;

    /**
     * 查询的主表
     */
    private String table;

    /**
     * 子查询SQL信息，与主表同时存在时，主表名作为别名
     */
    private SqlDefinition subTable;

    /**
     * 是否去重
     */
    private boolean distinct;

    /**
     * group规则状态
     */
    private int groupStatus = GROUP_DISABLE;
    /**
     * 字典查询
     */
    private List<SqlDefinition> dict;
    /**
     * 聚合函数
     * Pair<函数,别名>
     */
    private List<AggregationFun> aggregationFuns;
    /**
     * 别名
     */
    private List<Column> alias;

    /**
     * 不合法的别名
     */
    private List<Column> illegalAlias;

    /**
     * 从结果排除字段
     */
    private List<Column> excludeColumns;


    /**
     * where查询条件/修改，新增的字段值
     */
    private List<ValueCondition> valueCondition;

    /**
     * join类型，left join 或者 inner join
     */
    private String joinType;

    /**
     * join的on字段
     */
    private Pair<String, String> joinOn;

    /**
     * join查询sql片段,拼接的时候按下标从小到大排列
     * e.g. left join table_2 on table_1.id=table_2.table_1_id
     */
    private List<SqlDefinition> join;


    /**
     * order by代码片段
     */
    private String orderFragment;

    private int pageSize;
    private int pageNum;

    /**
     * 在没有指定分页参数时的最大获取行数
     */
    private int maxPageSize;

    /**
     * 获取指定序号或范围的数据
     */
    private int rowStart;
    private int rowEnd;

    /**
     * group by
     */
    private List<Column> groupBy;


    /**
     * 数据替换规则
     */
    private List<ValueReplace> valueReplaces;

    /**
     * 扩展参数
     */
    private ObjectNode expand;

    public SqlDefinition() {

    }

    public SqlDefinition(ObjectNode params) {
        this(null, params);
    }

    public SqlDefinition(String table, ObjectNode params) {
        setParams(params);
        this.table = table;
    }

    /**
     * 是否分页
     *
     * @return 返回true表示分页，返回false表示不分页
     */
    public boolean isPage() {
        return (pageSize | pageNum) != 0;
    }

    public void setParams(ObjectNode params) {
        if (params == null) {
            this.params = JsonNodeFactory.instance.objectNode();
            return;
        }
        this.params = params;
        this.rawParams = params.deepCopy();
    }


    public void addColumn(String table, String column, String alias) {
        if (this.columns == null) {
            this.columns = new ArrayList<>();
        }
        this.columns.add(new Column(table, column, alias));
    }

    public void addColumn(String table, List<String> columns) {
        if (this.columns == null) {
            this.columns = new ArrayList<>();
        }
        for (String column : columns) {
            this.columns.add(new Column(table, column, null));
        }
    }


    public void addValueCondition(ValueCondition valueCondition) {
        if (this.valueCondition == null) {
            this.valueCondition = new ArrayList<>();
        }
        this.valueCondition.add(valueCondition);
    }


    public void addAlias(String table, String column, String alias) {
        if (this.alias == null) {
            this.alias = new ArrayList<>();
        }
        this.alias.add(new Column(table, column, alias));
    }

    public void addIllegalAlias(String table, String alias, String illegalAlias) {
        if (this.illegalAlias == null) {
            this.illegalAlias = new ArrayList<>();
        }
        this.illegalAlias.add(new Column(table, alias, illegalAlias));
    }


    public void addExcludeColumns(String table, String... excludeColumns) {
        if (this.excludeColumns == null) {
            this.excludeColumns = new ArrayList<>();
        }
        for (String column : excludeColumns) {
            this.excludeColumns.add(new Column(table, column, null));
        }
    }


    public void addGroupBy(String table, String... columns) {
        if (this.groupBy == null) {
            this.groupBy = new ArrayList<>();
        }
        for (String column : columns) {
            this.groupBy.add(new Column(table, column));
        }
    }

    public void addAggregationFun(String table, String column, String funcPattern, String alias) {
        if (this.aggregationFuns == null) {
            this.aggregationFuns = new ArrayList<>();
        }
        this.aggregationFuns.add(new AggregationFun(table, column, funcPattern, alias));

    }

    public void addJoin(SqlDefinition sqlDefinition) {
        if (this.join == null) {
            this.join = new ArrayList<>();
        }
        this.join.add(sqlDefinition);
    }


    public void addDict(SqlDefinition sqlDefinition) {
        if (this.dict == null) {
            this.dict = new ArrayList<>();
        }
        this.dict.add(sqlDefinition);
    }


    public void addReplace(String table, String column, String pattern) {
        if (valueReplaces == null) {
            valueReplaces = new ArrayList<>();
        }
        valueReplaces.add(new ValueReplace(table, column, pattern));
    }

}
