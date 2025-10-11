package com.figgo.loveaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.preretrieval.query.expansion.QueryExpander;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 多查询扩展工具组件
 */
@Component
public class MyMultiQueryExpander {

    private final QueryExpander multiQueryExpander;

    public MyMultiQueryExpander(ChatClient.Builder chatClientBuilder) {
        this.multiQueryExpander = MultiQueryExpander.builder()
                .chatClientBuilder(chatClientBuilder)
                .includeOriginal(true)
                .numberOfQueries(3)
                .build();
    }


    public List<Query> expand(Query query) {
        return multiQueryExpander.expand(query);
    }
}
