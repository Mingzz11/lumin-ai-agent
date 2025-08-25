package cn.xhm.luminaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public class RagAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {
    private final VectorStore vectorStore;

    private final String knowledgeSourcePrefix; // 新增：知识库来源前缀


    public RagAdvisor(VectorStore vectorStore) {
        this(vectorStore, "知识库来源");
    }

    // 新增带知识库来源前缀的构造器
    public RagAdvisor(VectorStore vectorStore, String knowledgeSourcePrefix) {
        this.vectorStore = vectorStore;
        this.knowledgeSourcePrefix = knowledgeSourcePrefix;
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        String query = advisedRequest.userText();
        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(5)
                .build();
        List<Document> relevantDocs = vectorStore.similaritySearch(searchRequest);

        // 增强：添加知识库来源信息
        String context = buildContextWithSource(relevantDocs);
        String augmentedQuery = context + "\n\n用户问题: " + query;
        log.info("增强后的查询 =======================>: {}", augmentedQuery);

        AdvisedRequest augmentedRequest = AdvisedRequest.from(advisedRequest)
                .userText(augmentedQuery)
                .build();

        return chain.nextAroundCall(augmentedRequest);
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return Mono.fromCallable(() -> {
                    String query = (String) advisedRequest.userText();
                    SearchRequest searchRequest = SearchRequest.builder()
                            .query(query)
                            .topK(5)
                            .build();
                    List<Document> relevantDocs = vectorStore.similaritySearch(searchRequest);
                    return buildContextWithSource(relevantDocs); // 使用增强的方法
                })
                .flatMapMany(context -> {
                    String augmentedQuery = context + "\n\n用户问题: " + advisedRequest.userText();
                    log.info("增强后的查询 =======================>: {}", augmentedQuery);
                    AdvisedRequest augmentedRequest = AdvisedRequest.from(advisedRequest)
                            .userText(augmentedQuery)
                            .build();

                    return chain.nextAroundStream(augmentedRequest);
                });
    }

    // 增强：添加知识库来源信息
    private String buildContextWithSource(List<Document> documents) {
        StringBuilder sb = new StringBuilder("相关上下文信息：\n");
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            String source = doc.getMetadata().getOrDefault("filename", "未知来源").toString();

            sb.append(i + 1).append(". ")
                    .append(doc.getText())
                    .append("\n[")
                    .append(knowledgeSourcePrefix).append(": ") // 添加来源前缀
                    .append(source)
                    .append("]\n\n");
        }
        return sb.toString();
    }

    @Override
    public String getName() {
        return "自定义 RagAdvisor 顾问";
    }

    @Override
    public int getOrder() {
        return -100;
    }
}
