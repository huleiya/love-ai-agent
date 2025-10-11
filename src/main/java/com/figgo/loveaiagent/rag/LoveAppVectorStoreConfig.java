package com.figgo.loveaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.autoconfigure.vectorstore.redis.RedisVectorStoreAutoConfiguration;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * VectorStore自定义配置类，配置一个基于内存的 SimpleVectorStore，供 rag 使用
 * 并在初始化时执行以下流程：加载本地知识库-> 自动补充关键词元信息 -> 文档切片 -> 向量转换并存储
 */
@Configuration
public class LoveAppVectorStoreConfig {
    @Resource
    LoveAppDocumentLoader loveAppDocumentLoader;

    @Resource
    MyKeywordEnricher myKeywordEnricher;

    @Resource
    private MyTokenTextSplitter myTokenTextSplitter;

    @Bean
    public VectorStore loveAppVectorStore(EmbeddingModel dashscopeEmbeddingModel) {
        // 注入基于内存的 VectorStore, 并在初始化 bean 时加载类路径下所有 md文件，然后以向量形式保存到内存中
        SimpleVectorStore simpleVectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel).build();
        //加载所有 md 文档
        //List<Document>documents  = loveAppDocumentLoader.readMarkdowns();
        // 加载文档后，ai 自动补充关键词元信息
        //List<Document> enrichedDocuments = myKeywordEnricher.enrichDocuments(documents);
        // 加载文档后，存储向量转换前，对文档切片
        //List<Document> splittedDocuments = myTokenTextSplitter.splitCustomized(enrichedDocuments);
        //simpleVectorStore.add(enrichedDocuments);

        return simpleVectorStore;
    }
}
