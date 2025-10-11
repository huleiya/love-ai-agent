package com.figgo.loveaiagent.advisor;

import org.jetbrains.annotations.NotNull;
import org.springframework.ai.chat.client.advisor.api.*;
import org.springframework.ai.chat.prompt.PromptTemplate;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.Map;

/**
 * 自定义 Re-Reading Advisor，在初始化 chatclient 或 单次用户查询 时注入，实现让模型重读用户问题，提高推理能力
 */
public class ReReadingAdvisor implements CallAroundAdvisor, StreamAroundAdvisor {
    @Override
    public AdvisedResponse aroundCall(AdvisedRequest advisedRequest, CallAroundAdvisorChain chain) {
        //在调用模型前改写用户提示词
//        advisedRequest = before(advisedRequest);
//        return chain.nextAroundCall(advisedRequest);
        return chain.nextAroundCall(before(advisedRequest));
    }

    @NotNull
    private AdvisedRequest before(AdvisedRequest advisedRequest) {
        String re2template = """
                        {re2_input_query}
                        Read the question again: {re2_input_query}
                        """;
        String re2InputQuery = new PromptTemplate(re2template).render(Map.of("re2_input_query", advisedRequest.userText()));
        return AdvisedRequest.from(advisedRequest)
                .userText(re2InputQuery)
                .build();
    }

    @Override
    public Flux<AdvisedResponse> aroundStream(AdvisedRequest advisedRequest, StreamAroundAdvisorChain chain) {
        return chain.nextAroundStream(before(advisedRequest));
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
