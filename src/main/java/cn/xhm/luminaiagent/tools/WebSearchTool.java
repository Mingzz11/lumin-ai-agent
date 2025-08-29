package cn.xhm.luminaiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 网页搜索工具类
 */
public class WebSearchTool {

    // SearchAPI搜索接口地址
    private static final String SEARCH_API_URL = "https://www.searchapi.io/api/v1/search";

    // API密钥
    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * 通过百度搜索查询信息
     */
    @Tool(description = "Search for information from Baidu Search Engine")
    public String searchWeb(@ToolParam(description = "Search query keyword") String query) {
        // 使用网页搜索API进行搜索
        Map<String,  Object> params = new HashMap<>();
        params.put("engine", "baidu");
        params.put("q", query);
        params.put("api_key", apiKey);

        try {
            String response = HttpUtil.get(SEARCH_API_URL, params);

            JSONObject jsonResponse = JSONUtil.parseObj(response);
            JSONArray results = jsonResponse.getJSONArray("organic_results");

            return results.stream().limit(5)
                    .map(Object::toString)
                    .collect(Collectors.joining(","));
        } catch (Exception e) {
            return "Error searching Baidu: " + e.getMessage();
        }
    }
}
