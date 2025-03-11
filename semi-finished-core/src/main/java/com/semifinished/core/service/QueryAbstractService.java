package com.semifinished.core.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.config.ConfigProperties;
import com.semifinished.core.exception.CodeException;
import com.semifinished.core.facotry.SqlDefinitionFactory;
import com.semifinished.core.jdbc.QuerySqlCombiner;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.core.jdbc.executor.Executor;
import com.semifinished.core.jdbc.parser.ParserExecutor;
import com.semifinished.core.pojo.Page;
import com.semifinished.core.pojo.ResultHolder;
import com.semifinished.core.service.enhance.query.AfterQueryEnhance;
import com.semifinished.core.utils.ParamsUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public abstract class QueryAbstractService {
    @Autowired
    private ParserExecutor parserExecutor;
    @Autowired
    private SqlExecutorHolder sqlExecutorHolder;
    @Autowired
    private ConfigProperties configProperties;
    @Autowired
    private List<AfterQueryEnhance> afterQueryEnhances;
    @Autowired
    private SqlDefinitionFactory sqlDefinitionFactory;
    @Autowired
    private List<Executor> executorList;


    /**
     * 包含参数解析的查询
     *
     * @param params 被解析的参数
     * @return 查询结果
     */

    public Object commonQuery(ObjectNode params) {
        if (ParamsUtils.isEmpty(params)) {
            params = JsonNodeFactory.instance.objectNode();
        }

        //解析后的sql定义信息类
        SqlDefinition sqlDefinition = sqlDefinitionFactory.getSqlDefinition(params);
        return commonQuery(sqlDefinition);
    }

    /**
     * 执行查询规则，该方法会执行{@link AfterQueryEnhance}中的
     * support，beforeParse，afterParse，afterPage，afterQuery方法。
     *
     * @param sqlDefinition SQL定义信息
     * @return 包含查询的结果和根据前端参数解析出的sql定义信息。
     */
    public Object commonQuery(SqlDefinition sqlDefinition) {

        //判断此次请求使用的增强
        List<AfterQueryEnhance> enhances = supportEnhances(sqlDefinition);

        //执行增强中的beforeParse方法
        beforeParse(sqlDefinition, enhances);

        //执行参数解析器
        parse(sqlDefinition);

        //执行增强中的afterParse方法
        afterParse(sqlDefinition, enhances);

        //创建分页信息
        Page page = wrapPage(sqlDefinition);

        //执行查询
        List<ObjectNode> records = query(sqlDefinition);

        ResultHolder resultHolder = new ResultHolder(page, records);

        //执行增强的afterQuery方法
        afterQuery(sqlDefinition, enhances, resultHolder);

        //执行@row规则
        executeRows(resultHolder, sqlDefinition);

        return result(resultHolder, sqlDefinition);
    }

    /**
     * 解析前增强
     *
     * @param sqlDefinition SQL定义信息
     * @param enhances      解析前执行增强
     */
    public void beforeParse(SqlDefinition sqlDefinition, List<AfterQueryEnhance> enhances) {
        enhances.forEach(enhance -> enhance.beforeParse(sqlDefinition));
    }

    /**
     * 解析参数
     *
     * @param sqlDefinition SQL定义信息
     */
    public void parse(SqlDefinition sqlDefinition) {
        parserExecutor.parse(sqlDefinition);
    }

    /**
     * 解析完成后的增强器
     *
     * @param sqlDefinition SQL定义信息
     * @param enhances      增强处理实例列表，用于对查询结果进行后置处理
     */
    public void afterParse(SqlDefinition sqlDefinition, List<AfterQueryEnhance> enhances) {
        enhances.forEach(enhance -> enhance.afterParse(sqlDefinition));
    }

    /**
     * 执行查询
     *
     * @param sqlDefinition SQL定义信息
     * @return 查询返回的结果集
     */
    public List<ObjectNode> query(SqlDefinition sqlDefinition) {
        String dialect = sqlDefinition.getDialect();
        Executor executor = executorList.stream().filter(q -> dialect.equals(q.dialect())).findFirst().orElseThrow(() -> new CodeException("未找到%s对应执行器", dialect));
        return executor.query(sqlDefinition);
    }

    /**
     * 在查询执行后执行增强处理
     *
     * @param sqlDefinition SQL定义对象，包含SQL语句的结构化信息
     * @param enhances      增强处理实例列表，用于对查询结果进行后置处理
     * @param resultHolder  结果容器对象，存储查询结果及处理过程中的中间数据
     */
    public void afterQuery(SqlDefinition sqlDefinition, List<AfterQueryEnhance> enhances, ResultHolder resultHolder) {
        enhances.forEach(enhance -> enhance.afterQuery(resultHolder, sqlDefinition));
    }


    /**
     * 执行@row规则
     *
     * @param resultHolder  包含查询数据与分页数据
     * @param sqlDefinition SQL定义信息
     */
    private void executeRows(ResultHolder resultHolder, SqlDefinition sqlDefinition) {
        List<ObjectNode> records = resultHolder.getRecords();

        //@row开始小于1表示没使用，则直接返回即可
        int rowStart = sqlDefinition.getRowStart();
        if (rowStart == -1) {
            return;
        }
        //执行@row结束规则
        int rowEnd = sqlDefinition.getRowEnd();


        List<ObjectNode> rows = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            //如果@row开始结束数值相同，则只返回@row指定位置的数据
            if (rowEnd == rowStart && i == rowStart) {
                rows.add(records.get(i));
                break;
            }
            //如果@row开始结束数值不相同，则返回@row指定范围的数据
            if (i >= rowStart && i <= rowEnd) {
                rows.add(records.get(i));
            }
        }
        resultHolder.setRecords(rows);
    }

    /**
     * 创建分页对象，即使没有分页的查询，都会先包装到分页对象中，在afterQuery增强后进行拆包
     *
     * @param sqlDefinition SQL定义信息
     * @return 分页对象
     */
    @Nullable
    private Page wrapPage(SqlDefinition sqlDefinition) {
        if (!sqlDefinition.isPage()) {
            return null;
        }

        Page page = new Page();
        //分页查询
        int pageNum = sqlDefinition.getPageNum();
        int pageSize = sqlDefinition.getPageSize();

        int total = sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
                .total(QuerySqlCombiner.creatorSqlWithoutLimit(sqlDefinition), QuerySqlCombiner.getArgs(sqlDefinition));

        //参数合理化
        if (configProperties.isPageNormalized() && total != 0 && (pageNum - 1) * pageSize >= total) {
            pageNum = (int) Math.ceil((double) total / pageSize);
            sqlDefinition.setPageNum(pageNum);
        }

        //设置分页信息
        page.setTotal(total);
        page.setPageNum(pageNum);
        page.setPageSize(pageSize);
        page.setHasNext(total > (pageSize * pageNum));
        page.setHasPre(pageNum > 1);
        return page;
    }

    /**
     * 对返回结果行进行处理，执行@row规则
     *
     * @param resultHolder  包含查询数据与分页数据
     * @param sqlDefinition SQL定义信息
     * @return 分页和@row规则处理后的结果
     */
    private Object result(ResultHolder resultHolder, SqlDefinition sqlDefinition) {
        List<ObjectNode> records = resultHolder.getRecords();

        if (sqlDefinition.isPage()) {
            Page page = resultHolder.getPage();
            page.setRecords(records);
            page.setSize(records.size());
            return page;
        }
        int rowStart = sqlDefinition.getRowStart();
        int rowEnd = sqlDefinition.getRowEnd();
        if (records.isEmpty()) {
            return records;
        }

        return rowStart == rowEnd ? records.get(0) : records;
    }


    /**
     * 执行增强中的support方法，筛选支持此次请求的增强
     *
     * @param sqlDefinition SQL定义信息
     * @return 支持此次请求的增强方法
     */
    public List<AfterQueryEnhance> supportEnhances(SqlDefinition sqlDefinition) {
        return this.afterQueryEnhances.stream()
                .filter(enhance -> enhance.support(sqlDefinition))
                .collect(Collectors.toList());
    }


}
