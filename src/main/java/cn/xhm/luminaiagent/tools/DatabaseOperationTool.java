package cn.xhm.luminaiagent.tools;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 数据库操作工具类
 */
@Component
@Slf4j
public class DatabaseOperationTool {

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public DatabaseOperationTool(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    /**
     * 查询数据（不带参数）
     */
    @Tool(description = "Query data from database with SQL only")
    public String queryData(@ToolParam(description = "SQL query statement") String sql) {
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            return formatResultList(result);
        } catch (Exception e) {
            return "Error executing query: " + e.getMessage();
        }
    }

    /**
     * 查询数据（带参数）
     */
    @Tool(description = "Query data from database with parameters")
    public String queryData(
            @ToolParam(description = "SQL query statement") String sql,
            @ToolParam(description = "Query parameters in JSON format, e.g. {\"id\":1, \"name\":\"test\"}") String params
    ) {
        try {
            Map<String, Object> paramMap = parseParams(params);
            List<Map<String, Object>> result = namedParameterJdbcTemplate.queryForList(sql, paramMap);
            return formatResultList(result);
        } catch (Exception e) {
            return "Error executing query: " + e.getMessage();
        }
    }

    /**
     * 插入数据
     */
    @Tool(description = "Insert data into database")
    public String insertData(
            @ToolParam(description = "SQL insert statement") String sql,
            @ToolParam(description = "Insert parameters in JSON format") String params
    ) {
        try {
            Map<String, Object> paramMap = parseParams(params);
            int rowsAffected = namedParameterJdbcTemplate.update(sql, paramMap);
            return rowsAffected + " row(s) inserted successfully";
        } catch (Exception e) {
            return "Error inserting data: " + e.getMessage();
        }
    }

    /**
     * 更新数据
     */
    @Tool(description = "Update data in database")
    public String updateData(
            @ToolParam(description = "SQL update statement") String sql,
            @ToolParam(description = "Update parameters in JSON format") String params
    ) {
        try {
            Map<String, Object> paramMap = parseParams(params);
            int rowsAffected = namedParameterJdbcTemplate.update(sql, paramMap);
            return rowsAffected + " row(s) updated successfully";
        } catch (Exception e) {
            return "Error updating data: " + e.getMessage();
        }
    }

    /**
     * 删除数据
     */
    @Tool(description = "Delete data from database")
    public String deleteData(
            @ToolParam(description = "SQL delete statement") String sql,
            @ToolParam(description = "Delete parameters in JSON format") String params
    ) {
        try {
            Map<String, Object> paramMap = parseParams(params);
            int rowsAffected = namedParameterJdbcTemplate.update(sql, paramMap);
            return rowsAffected + " row(s) deleted successfully";
        } catch (Exception e) {
            return "Error deleting data: " + e.getMessage();
        }
    }

    /**
     * 解析JSON参数
     */
    private Map<String, Object> parseParams(String paramsJson) {
        if(StrUtil.isEmpty(paramsJson)) return Collections.emptyMap();

        try {
            return objectMapper.readValue(paramsJson, new TypeReference<Map<String, Object>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON params: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    /**
     * 格式化查询结果
     */
    private String formatResultList(List<Map<String, Object>> result) {
        if(result.isEmpty()) return "No data found";

        StringBuilder sb = new StringBuilder();
        // 添加表头
        Map<String, Object> firstRow = result.get(0);
        sb.append(String.join(" | ", firstRow.keySet())).append("\n");

        // 创建分隔线
        sb.append("-".repeat(Math.max(0, sb.length() - 1))).append("\n");

        // 添加数据行
        for (Map<String, Object> row : result) {
            sb.append(row.values().stream()
                            .map(val -> val == null ? "NULL" : val.toString())
                            .reduce((a, b) -> a + " | " + b)
                            .orElse(""))
                    .append("\n");
        }

        return sb.toString();
    }
}
