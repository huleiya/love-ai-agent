package com.figgo.loveaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自动补充文档关键词元信息 组件
 */
@Component
public class MyKeywordEnricher {

    @Resource
    ChatModel dashScopeChatModel;

    public List<Document> enrichDocuments(List<Document> documents) {
        KeywordMetadataEnricher enricher = new KeywordMetadataEnricher(dashScopeChatModel, 3);
        return enricher.apply(documents);
    }
}
