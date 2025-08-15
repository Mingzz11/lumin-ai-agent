package cn.xhm.luminaiagent.advisor;

import cn.xhm.luminaiagent.exception.ProhibitedWordException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 违禁词校验 Advisor
 * 检查用户输入是否包含违禁词
 * 违禁词来源 https://gitee.com/crazypoo/badwords
 */
@Slf4j
public class ProhibitedWordAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    private static final String DEFAULT_PROHIBITED_WORDS_FILE = "prohibited/prohibited-words.txt";
    private final List<String> prohibitedWords;

    public ProhibitedWordAdvisor() {
        this.prohibitedWords = loadProhibitedWordsFromFile(DEFAULT_PROHIBITED_WORDS_FILE);
        log.info("初始化违禁词Advisor，违禁词数量: {}", prohibitedWords.size());
    }

    /**
     * 创建违禁词Advisor，从指定文件读取违禁词列表
     */
    public ProhibitedWordAdvisor(String prohibitedWordsFile) {
        this.prohibitedWords = loadProhibitedWordsFromFile(prohibitedWordsFile);
        log.info("初始化违禁词Advisor，违禁词数量: {}", prohibitedWords.size());
    }


    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        return chain.nextAroundCall(checkRequest(advisedRequest));
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return chain.nextAroundStream(checkRequest(advisedRequest));
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return -100;
    }

    public AdvisedRequest checkRequest(AdvisedRequest advisedRequest) {
        String userText = advisedRequest.userText();
        if(containsProhibitedWord(userText)) {
            log.warn("检测到违禁词在用户输入中: {}", userText);
            throw new ProhibitedWordException("用户输入包含违禁词");
        }
        return advisedRequest;
    }

    private boolean containsProhibitedWord(String userText) {

        if(!StringUtils.hasText(userText)) return false;
        // 遍历违禁词 实际还能优化
        for(String prohibitedWord : prohibitedWords) {
            if(prohibitedWord.toLowerCase().contains(userText.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private List<String> loadProhibitedWordsFromFile(String filePath) {
        try {
            var resource = new ClassPathResource(filePath);
            var reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
            List<String> prohibitedWords = reader
                    .lines()
                    // 过滤空格和空字符串
                    .filter(StringUtils::hasText)
                    .map(String::trim)
                    .toList();
            log.info("从文件 {} 加载违禁词 {} 个", filePath, prohibitedWords.size());
            return prohibitedWords;
        }
        catch (Exception e) {
            log.error("加载违禁词文件失败: {}", filePath, e);
            return new ArrayList<>();
        }
    }
}
