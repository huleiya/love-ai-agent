package com.figgo.loveaiagent.rag;

import com.figgo.loveaiagent.constant.RagConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.DocumentReader;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 本地知识库文档读取 组件
 */
@Component
@Slf4j
public class LoveAppDocumentLoader {
    private final ResourcePatternResolver resourcePatternResolver;

    public LoveAppDocumentLoader(ResourcePatternResolver resourcePatternResolver) {
        this.resourcePatternResolver = resourcePatternResolver;
    }

    // 因为只有一个读取方法，所以不定义成员变量
    // 因为 spring ai提供的mdDocumentReader只支持单文件解析读取，而我们要读取某目录下所有md文档，所以需要根据文件路径pattern泛解析多个文件
    public List<Document> readMarkdowns() {
        List<Document> documentList = new ArrayList<>();
        Resource[] resources = null;
        try {
            resources = resourcePatternResolver.getResources(RagConstant.DEFAULT_MARKDOWN_FILE_LOCATION_PATTERN);
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                String status = filename.substring(filename.length() - 6, filename.length() - 4);
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withHorizontalRuleCreateDocument(true)
                        .withIncludeCodeBlock(false)
                        .withIncludeBlockquote(false)
                        .withAdditionalMetadata("filename", filename)
                        .withAdditionalMetadata("status", status)
                        .build();
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource, config);
                documentList.addAll(reader.get());
            }
        } catch (IOException e) {
            log.error("Markdown 文档加载失败", e);
        }
        return documentList;
//        在我构想的代码基础上：多加了 多文件泛解析成多个资源的功能
//        String markdownResource = "classpath:doc/*.md";
//        MarkdownDocumentReaderConfig config = new MarkdownDocumentReaderConfig(MarkdownDocumentReaderConfig.builder()
//                .withIncludeBlockquote(true)
//                .withAdditionalMetadata("fileName", "")
//                .withIncludeCodeBlock(true));
//        DocumentReader mdDocumentReader = new MarkdownDocumentReader(markdownResource, config);
//        documentList.addAll(mdDocumentReader.get());
//        return documentList;

    }
}
