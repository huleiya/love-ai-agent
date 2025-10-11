package com.figgo.loveaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 工厂类：创建自定义 Retrieval Augmentation Advisor的工厂
 */
@Slf4j
public class LoveAppRagCustomAdvisorFactory {

    /**
     * 根据传入的 vector store 和 过滤参数status，生成检索增强 advisor，检索特定情感状态的文档，然后增强用户查询，请求 ai 回复
     * @param vectorStore
     * @param status
     * @return
     */
    public static Advisor createRetrievalAugmentationAdvisor(VectorStore vectorStore, String status) {
        // 可以配置 文档检索器 查询重写器
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();
        DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression)
                .topK(3)
                .similarityThreshold(0.5)
                .build();

        return RetrievalAugmentationAdvisor.builder()
                .documentRetriever(retriever)
                .queryAugmenter(LoveAppContextualQueryAugmenterFactory.creatInstance())
                .build();
    }
}
