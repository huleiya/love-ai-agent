package com.figgo.loveaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.transformation.QueryTransformer;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

/**
 * 查询重写器
 */
@Component
@DependsOn("chatClientBuilder")
public class QueryWriter {

    private final QueryTransformer rewriteQueryTransformer;


    public QueryWriter(ChatClient.Builder chatClientBuilder) {
        rewriteQueryTransformer = RewriteQueryTransformer.builder()
                .chatClientBuilder(chatClientBuilder)
                .build();
    }


    public String queryRewrite(String prompt) {
        return rewriteQueryTransformer.transform(new Query(prompt)).text();
    }
}
