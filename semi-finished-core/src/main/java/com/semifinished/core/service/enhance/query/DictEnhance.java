package com.semifinished.core.service.enhance.query;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.semifinished.core.constant.ParserStatus;
import com.semifinished.core.exception.CodeException;
import com.semifinished.core.jdbc.QuerySqlCombiner;
import com.semifinished.core.jdbc.SqlDefinition;
import com.semifinished.core.jdbc.SqlExecutorHolder;
import com.semifinished.core.jdbc.parser.ParserExecutor;
import com.semifinished.core.pojo.Column;
import com.semifinished.core.pojo.Page;
import com.semifinished.core.utils.ParamsUtils;
import lombok.AllArgsConstructor;
import org.apache.commons.math3.util.Pair;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * 表字典查询
 * <pre>
 *     {
 *         "col:":{
 *             "@tb":"tb1",
 *             "@on":"col2"
 *         }
 *     }
 * </pre>
 */
@Component
@Order(-200)
@AllArgsConstructor
public class DictEnhance implements AfterQueryEnhance {

    private final ParserExecutor parserExecutor;
    private final SqlExecutorHolder sqlExecutorHolder;


    @Override
    public void afterQuery(Page page, SqlDefinition sqlDefinition) {
        List<SqlDefinition> dictList = QuerySqlCombiner.dicts(sqlDefinition);
        List<ObjectNode> records = page.getRecords();

        for (SqlDefinition definition : dictList) {
            Pair<String, String> joinOn = definition.getJoinOn();

            //获取关联字段的数据集合
            List<String> args = getArgs(records, joinOn.getFirst());
            if (args.isEmpty()) {
                continue;
            }
            //获取查询的where字段
            String inCol = getColumn(definition, joinOn.getSecond());

            //构建查询的SQL定义
            buildQuery(definition, args, inCol);

            //获取查询SQL
            String sql = QuerySqlCombiner.creatorSqlWithoutLimit(definition);

            //查询数据
            List<ObjectNode> secondRecords = sqlExecutorHolder.dataSource(definition.getDataSource()).list(sql, QuerySqlCombiner.getArgs(definition));

            //合并数据
            combine(definition, records, secondRecords, definition.getRowStart(), definition.getRowEnd());
        }
    }


    /**
     * 获取查询的where字段
     *
     * @param definition SQL定义信息
     * @param onColumn   关联字段
     * @return 查询的where字段
     */
    private static String getColumn(SqlDefinition definition, String onColumn) {
        return definition.getColumns()
                .stream()
                .filter(col -> onColumn.equals(col.getAlias()) || onColumn.equals(col.getColumn()))
                .map(Column::getColumn)
                .findFirst()
                .orElseThrow(() -> new CodeException("表字典查询未找到in查询字段"));
    }


    /**
     * 获取关联字段的数据集合
     *
     * @param records  第一次查询数据集合
     * @param onColumn 关联字段
     * @return 关联字段的数据集合
     */
    private static List<String> getArgs(List<ObjectNode> records, String onColumn) {
        return records
                .stream()
                .map(node -> node.get(onColumn))
                .filter(Objects::nonNull)
                .flatMap(node -> node instanceof ArrayNode
                        ? StreamSupport.stream(node.spliterator(), false).map(JsonNode::asText)
                        : Stream.of(node.asText()))
                .collect(Collectors.toList());
    }


    /**
     * 创建第二次in查询规则
     *
     * @param definition SQL定义信息
     * @param args       查询的数据
     * @param inCol      查询字段
     */
    private void buildQuery(SqlDefinition definition, List<String> args, String inCol) {

        ObjectNode params = JsonNodeFactory.instance.objectNode();
        ArrayNode values = params.withArray("[" + inCol + "]");
        args.forEach(values::add);
        params.put("@", "");

        definition.setStatus(ParserStatus.DICTIONARY.getStatus());
        parserExecutor.parse(params, definition);
    }


    /**
     * 合并数据
     *
     * @param definition 表字典的SQL定义信息
     * @param records    第一次查询数据
     * @param replaces   第二次查询数据
     * @param rowStart   开始行
     * @param rowEnd     结束行
     */
    private void combine(SqlDefinition definition, List<ObjectNode> records, List<ObjectNode> replaces, int rowStart, int rowEnd) {
        Pair<String, String> joinOn = definition.getJoinOn();
        String left = joinOn.getFirst();
        String right = joinOn.getSecond();

        //把关联字段作为key把第一次查询数据转为map，减少循环
        Map<String, List<ObjectNode>> map = new HashMap<>();
        for (ObjectNode record : records) {
            JsonNode key = record.path(left);
            //关联字段的数据可能是上一次表字典的数据，所以需要处理数组的情况
            if (key instanceof ArrayNode) {
                key.forEach(node -> map.computeIfAbsent(node.asText(), k -> new ArrayList<>()).add(record));
                continue;
            }
            map.computeIfAbsent(key.asText(), k -> new ArrayList<>()).add(record);
        }


        for (ObjectNode replace : replaces) {
            List<ObjectNode> targets = map.get(replace.path(right).asText());
            if (targets == null) {
                continue;
            }
            //将第二次查询的数据根据关联字段匹配后合并到第一次查询的数据中
            replace.fields().forEachRemaining(entry -> {
                JsonNode node = entry.getValue();
                for (ObjectNode target : targets) {
                    ArrayNode jsonNodes = target.withArray(entry.getKey());
                    if (node instanceof ArrayNode) {
                        jsonNodes.addAll((ArrayNode) node);
                        return;
                    }
                    jsonNodes.add(node);
                }

            });
        }

        List<Column> columns = definition.getColumns();
        List<String> columnsField = columns.stream().map(col -> ParamsUtils.hasText(col.getAlias(), col.getColumn())).collect(Collectors.toList());

        for (ObjectNode record : records) {

            //实现表字典查询的@row规则，第一次查询的key可能对应第二次查询的多条数据，所以会产生数组
            //这里的@row可以指定返回1条还是多条数据
            for (String field : columnsField) {
                if (rowStart <= 0) {
                    break;
                }
                if (rowEnd == 0) {
                    JsonNode jsonNode = record.remove(field);
                    if (jsonNode != null) {
                        record.set(field, jsonNode.get(rowStart - 1));
                    }
                    continue;
                }
                ArrayNode jsonNodes = record.withArray(field);
                ArrayNode rows = JsonNodeFactory.instance.arrayNode();
                for (int i = 1; i < jsonNodes.size() + 1; i++) {
                    if (i <= rowEnd && i >= rowStart) {
                        rows.add(jsonNodes.get(i - 1));
                    }
                }
                record.set(field, rows);
            }


        }


    }
}
