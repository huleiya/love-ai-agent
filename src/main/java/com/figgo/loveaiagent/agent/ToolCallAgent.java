package com.figgo.loveaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import com.figgo.loveaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 处理工具调用的基础代理类，具体实现了 think 和 act 方法，可以用作创建实例的父类
 * 这里体现了一点：agent 肯定是要使用工具的，或者说，在每步执行中使用工具的才叫agent
 */
@Data
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class ToolCallAgent extends ReActAgent{
    // 可用的工具
    private final ToolCallback[] availableTools;

    // 保存了工具调用信息的响应（其实就是 AI 响应）
    private ChatResponse toolCallChatResponse;

    // 工具调用管理者
    private final ToolCallingManager toolCallingManager;

    // 禁用内置的工具调用机制，自己维护上下文
    private final ChatOptions chatOptions; // 构建 prompt，用于 think 和 act

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        // 禁用 Spring AI 内置的工具调用机制，自己维护选项和消息上下文
        // TODO: 区分一下 阿里 和 spring ai 内置的工具调用机制在设置参数上的含义，为什么一个true一个false
        // TODO: 通过自己维护 tool calling 上下文，思考一下 spring ai 在工具调用上帮我们做了哪些事
        this.chatOptions = DashScopeChatOptions.builder()
                .withProxyToolCalls(true)
                .build();
    }

    @Override
    public boolean think() {
        // 1. 校验 nextStep 提示词，然后构建 user prompt（包括chatoptions，消息上下文，以及下步执行提示词），自己实现根据 ai响应参数 执行工具调用
        // 并且维护会话上下文
        if (StrUtil.isNotBlank(getNextStepPrompt())) {
            UserMessage userMessage = new UserMessage(getNextStepPrompt());
            getMessageList().add(userMessage);
        }
        List<Message> messageList = getMessageList();
        Prompt prompt = new Prompt(messageList, chatOptions);//构建用户提示词，包括：会话上下文，聊天选项（禁用自动工具调用）
        try {
            // 2. 调用 AI 大模型，获取 AI 回复 和 工具调用参数
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .tools(availableTools)
                    .call()
                    .chatResponse();
            // 保存响应，用于 Act（Act一定调用工具吗）
            this.toolCallChatResponse = chatResponse;
            // 3. 解析 AI 回复消息，包括文本和工具调用列表
            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            String result = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();//拿到本次回复需要调用的工具
            // 输出格式：{悟空智能体的思考: [ai回复文本]}
            //          {悟空智能体选择了 3 个工具来使用}
            // 这里对于工具信息的输出定义为一个多行字符串，\n分隔，使用 stream 对 toolCallList 进行转换得到这个字符串
            //          {"工具名称：图片搜索，参数：query"}
            //          {"工具名称：资源下载，参数：url"}
            // 日志记录 ai回复 和 工具调用提示信息
            log.info(getName() + "的思考: " + result);
            log.info(getName() + "选择了 " + toolCallList.size() + " 个工具来使用");
            String toolCallInfo = toolCallList.stream()
                    .map(toolCall -> String.format("工具名称：%s，参数：%s", toolCall.name(), toolCall.arguments()))
                    .collect(Collectors.joining("\n"));
            log.info(toolCallInfo);
            // 4. 根据 ai 回复里的工具调用列表是否为空分别处理
            if (toolCallList.isEmpty()) {
                // 列表为空，保存助手消息到会话上下文
                getMessageList().add(assistantMessage);
                return false;
            } else {
                // 需要调用工具，无需保存助手消息，因为调用工具时会自动记录
                return true;
            }
        } catch (Exception e) {
            log.error(getName() + "的思考过程遇到了问题: " + e.getMessage());
            getMessageList().add(new AssistantMessage("处理时遇到错误: " + e.getMessage()));
            return false;
        }
    }

    /**
     * 执行工具调用并处理结果
     *
     * @return 执行结果
     */
    @Override
    public String act() {
        // 校验
        if (!this.toolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }
        // 调用工具
        //  构建 prompt，此时会话上下文包括最新的思考结果（调用哪些工具，相应工具参数）
        Prompt prompt = new Prompt(getMessageList(), chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        // 保存最新消息到上下文，conversationHistory 已经包含了助手消息和工具调用返回的结果
        setMessageList(toolExecutionResult.conversationHistory());
        // 当前工具调用的结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String results = toolResponseMessage.getResponses().stream()
                .map(response -> "工具 " + response.name() + " 完成了它的任务！结果: " + response.responseData())
                .collect(Collectors.joining("\n"));
        // 判断是否调用了终止工具
        boolean hasTerminateToolCalled = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
        if (hasTerminateToolCalled) {
            setState(AgentState.FINISHED);
        }
        log.info(results);
        return results;
    }
}
