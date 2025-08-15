package cn.xhm.luminaiagent.advisor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.model.MessageAggregator;
import reactor.core.publisher.Flux;

@Slf4j
public class MyLoggerAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {

    public AdvisedRequest before(AdvisedRequest advisedRequest) {
        log.info("AI Request: {}", advisedRequest.userText());
        return advisedRequest;
    }

    public void observerAfter(AdvisedResponse advisedResponse) {
        log.info("AI Response: {}", advisedResponse.response().getResult().getOutput().getText());
    }

    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        // 1. 处理请求（前置处理）
        advisedRequest = this.before(advisedRequest);

        // 2. 调用链中的下一个Advisor
        AdvisedResponse response = chain.nextAroundCall(advisedRequest);

        // 3. 处理响应（后置处理）
        this.observerAfter(response);

        return response;
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        // 1. 处理请求
        advisedRequest = this.before(advisedRequest);

        // 2. 调用链中的下一个Advisor并处理流式响应
        Flux<AdvisedResponse> advisedResponseFlux = chain.nextAroundStream(advisedRequest);

        return (new MessageAggregator()).aggregateAdvisedResponse(advisedResponseFlux, this::observerAfter);
    }


    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
