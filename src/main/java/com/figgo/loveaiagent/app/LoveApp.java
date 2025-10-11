package com.figgo.loveaiagent.app;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.figgo.loveaiagent.advisor.MyLoggerAdvisor;
import com.figgo.loveaiagent.chatmemory.FileBasedChatMemory;
import com.figgo.loveaiagent.constant.ChatMemoryConstant;
import com.figgo.loveaiagent.rag.LoveAppRagCustomAdvisorFactory;
import com.figgo.loveaiagent.rag.QueryWriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.ai.rag.retrieval.join.ConcatenationDocumentJoiner;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * 恋爱问答应用，使用 AI 模型回答用户恋爱相关问题，包括：常规问答，基于知识库问答，
 * 同时具备以下相关特性：
 * 1. 多轮对话记忆，提供两种对话记忆存储方案：
 *      - 基于内存
 *      - 基于本地文件系统
 * 2. 自定义请求日志输出
 * 3. 文档向量存储
 * 4. 集成云知识库问答
 * 5. 提供两种向量存储方案：
 *    - 配置一个自定义 vectorstore，在初始化时执行一些流程
 *    - 集成 pgVectorStore
*  6. 结构化输出恋爱报告，将 ai回复 转为结构化输出并用相关 model bean 对象存储
 */
@Component
@Slf4j
public class LoveApp {
    record LoveReport(String title, List<String> suggestions) {}

    private final ChatClient chatClient;

    @Resource
    private VectorStore loveAppVectorStore;

    @Resource
    private Advisor loveAppCloudRagAdvisor;

    //@Resource
    //private VectorStore pgVectorStore;

    @Resource
    private QueryWriter queryWriter;

    private static final String SYSTEM_PROMPT = "扮演深耕恋爱心理领域的专家。开场向用户表明身份，告知用户可倾诉恋爱难题。" +
            "围绕单身、恋爱、已婚三种状态提问：单身状态询问社交圈拓展及追求心仪对象的困扰；" +
            "恋爱状态询问沟通、习惯差异引发的矛盾；已婚状态询问家庭责任与亲属关系处理的问题。" +
            "引导用户详述事情经过、对方反应及自身想法，以便给出专属解决方案。";

    public LoveApp(ChatModel dashscopeChatModel) {
        // 初始化基于内存的对话记忆
//         ChatMemory chatMemory = new InMemoryChatMemory();
        ChatMemory chatMemory = new FileBasedChatMemory(ChatMemoryConstant.DEFAULT_CHAT_MEMORY_SAVE_BASE_DIR);
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        new MyLoggerAdvisor()
//                        new ReReadingAdvisor()
                )
                .build();
    }

    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = response.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();

    }

    public LoveReport doChatWithReport(String message, String chatId) {
        LoveReport loveReport = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + "每次对话都要生成恋爱报告，标题为{用户名}的恋爱报告，内容为建议列表")
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(LoveReport.class);
        log.info("Love Report: {}", loveReport);
        return loveReport;
    }

    /**
     * 基于本地知识库实现恋爱知识问答，具体流程：文档读取->向量存储->文档检索->查询增强
     * @param message
     * @param chatId
     * @return
     */
    public String doChatWithRag(String message, String chatId) {
        String rewriteQueryString = queryWriter.queryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                //应用知识库问答
//                .advisors(LoveAppRagCustomAdvisorFactory.createRetrievalAugmentationAdvisor(loveAppVectorStore, "单身"))
                .advisors(new QuestionAnswerAdvisor(loveAppVectorStore))
                //.advisors(loveAppCloudRagAdvisor)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    @Resource
    ToolCallback[] allTools;

    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .tools(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

    @Resource
    private ToolCallbackProvider toolCallbacks;

    public String doChatWithMcp(String message, String chatId) {

        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .tools(toolCallbacks)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);
        return content;
    }

}
