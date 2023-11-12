package com.semifinished.service;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.config.ConfigProperties;
import com.semifinished.jdbc.SqlCombiner;
import com.semifinished.jdbc.SqlDefinition;
import com.semifinished.jdbc.SqlExecutorHolder;
import com.semifinished.jdbc.parser.ParserExecutor;
import com.semifinished.pojo.Page;
import com.semifinished.service.enhance.query.AfterQueryEnhance;
import com.semifinished.service.enhance.query.SelectFinallyEnhance;
import com.semifinished.util.ParamsUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EnhanceService {
    private final ParserExecutor parserExecutor;
    private final SqlExecutorHolder sqlExecutorHolder;
    private final ConfigProperties configProperties;

    @Resource
    private List<AfterQueryEnhance> afterQueryEnhances;

    @Autowired(required = false)
    private SelectFinallyEnhance selectFinallyEnhance;


    /**
     * 不解析参数的查询
     *
     * @param sqlDefinition SQL定义信息
     * @return 查询结果
     */
    public Object selectNoParse(SqlDefinition sqlDefinition) {
        return select(sqlDefinition, false);
    }

    /**
     * 包含参数解析的查询
     *
     * @param params 被解析的参数
     * @return 查询结果
     */
    public Object select(ObjectNode params) {
        if (ParamsUtils.isEmpty(params)) {
            params = JsonNodeFactory.instance.objectNode();
        }
        //解析后的sql定义信息类
        SqlDefinition sqlDefinition = new SqlDefinition(params);
        return select(sqlDefinition, true);
    }

    /**
     * 执行查询规则，该方法会执行{@link AfterQueryEnhance}中的
     * support，beforeParse，afterParse，afterPage，afterQuery方法。
     *
     * @param sqlDefinition SQL定义信息
     * @param parse         是否执行请求参数解析,true执行，false不执行
     * @return 包含查询的结果和根据前端参数解析出的sql定义信息。
     */
    public Object select(SqlDefinition sqlDefinition, boolean parse) {

        //判断此次请求使用的增强
        List<AfterQueryEnhance> enhances = supportEnhances(sqlDefinition);


        //执行增强中的beforeParse方法
        enhances.forEach(enhance -> enhance.beforeParse(sqlDefinition));

        if (parse) {
            //执行参数解析器
            parserExecutor.parse(sqlDefinition);
        }

        //执行增强中的afterParse方法
        enhances.forEach(enhance -> enhance.afterParse(sqlDefinition));


        //执行sql
        Page page = executeSql(sqlDefinition);

        //执行增强的afterQuery方法
        enhances.forEach(enhance -> enhance.afterQuery(page, sqlDefinition));

        //如果没有分页规则，则只返回数据列表
        Object result = resultRow(page, sqlDefinition);


        //执行最终增强
        return selectFinallyEnhance != null ?
                selectFinallyEnhance.beforeReturn(result, sqlDefinition) : result;
    }

    private Object resultRow(Page page, SqlDefinition sqlDefinition) {
        List<ObjectNode> records = page.getRecords();
        int rowStart = sqlDefinition.getRowStart();
        Object result = records;
        if (sqlDefinition.isPage()) {
            result = page;
        }
        if (rowStart < 1) {
            return result;
        }
        int rowEnd = sqlDefinition.getRowEnd();

        if (rowEnd == 0) {
            return records.get(rowStart - 1);
        }

        List<ObjectNode> rows = new ArrayList<>();
        for (int i = 1; i < records.size() + 1; i++) {
            if (rowEnd == rowStart && i == rowStart) {
                rows.add(records.get(i - 1));
                break;
            }
            if (i >= rowStart && i <= rowEnd) {
                rows.add(records.get(i - 1));
            }
        }
        records.clear();
        records.addAll(rows);

        return result;
    }


    /**
     * 执行sql，并返回包含结果数据的Page
     *
     * @param sqlDefinition SQL定义信息
     * @return 查询出的数据，可能包含分页数据
     */
    private Page executeSql(SqlDefinition sqlDefinition) {
        Page page = createPage(sqlDefinition);

        //组装查询SQL并获取
        String sql = SqlCombiner.select(sqlDefinition);

        //执行查询
        List<ObjectNode> objectNodes = sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
                .list(sql, SqlCombiner.getArgs(sqlDefinition));

        page.setRecords(objectNodes);
        page.setSize(objectNodes.size());

        return page;
    }

    /**
     * 设置分页信息
     *
     * @param sqlDefinition SQL定义信息
     */
    private Page createPage(SqlDefinition sqlDefinition) {
        Page page = new Page();
        if (!sqlDefinition.isPage()) {
            return page;
        }
        //分页查询
        int pageNum = sqlDefinition.getPageNum();
        int pageSize = sqlDefinition.getPageSize();

        int total = sqlExecutorHolder.dataSource(sqlDefinition.getDataSource())
                .total(SqlCombiner.creatorSqlWithoutLimit(sqlDefinition), SqlCombiner.getArgs(sqlDefinition));

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
     * 执行增强中的support方法，筛选支持此次请求的增强
     *
     * @param sqlDefinition SQL定义信息
     * @return 支持此次请求的增强方法
     */
    private List<AfterQueryEnhance> supportEnhances(SqlDefinition sqlDefinition) {
        return this.afterQueryEnhances.stream()
                .filter(enhance -> enhance.support(sqlDefinition))
                .collect(Collectors.toList());
    }


}
