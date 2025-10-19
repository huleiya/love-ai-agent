package com.figgo.loveaiagent.agent;

import cn.hutool.core.util.StrUtil;
import com.figgo.loveaiagent.agent.model.AgentState;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 职责：
 * 1. agent loop
 * 2. 修改状态，确保状态行为一致性，和正常的状态流转
 * 3. 日志记录
 *  3.1 记录每次分步执行的开始
 *  3.2 记录代理运行过程中的异常原因
 * 4. 调用分步执行函数 step
 * 5. 返回最后执行结果
 */
@Slf4j
@Data
public abstract class BaseAgent {
    // 核心属性
    private String name;

    // 提示
    private String systemPrompt;
    private String nextStepPrompt;

    // 状态
    private AgentState state = AgentState.IDLE;

    // 执行控制
    private int maxSteps = 10;
    private int currentStep = 0;

    //LLM
    private ChatClient chatClient;

    // Memory（需要自主维护会话上下文）
    private List<Message> messageList = new ArrayList<>();

    /**
     * 运行代理（输入提示词后，agent自主执行任务的整个过程）
     * @param userPrompt 用户提示词
     * @return 执行结果（包含每一步）
     */
    public String run(String userPrompt) {
        if (this.state != AgentState.IDLE) {
            throw new RuntimeException("Cannot run agent from state: " + this.state);
        }
        if (StrUtil.isBlank(userPrompt)) {
            throw new RuntimeException("Cannot run agent with empty user prompt");
        }
        // 更改状态
        this.state = AgentState.RUNNING;
        // 记录消息上下文
        messageList.add(new UserMessage(userPrompt));
        // 保存结果列表，最后拼接成字符串返回
        List<String> results = new ArrayList<>();
        try {
            for (int i = 0; i < maxSteps && this.state != AgentState.FINISHED; i++) {
                currentStep = i + 1;
                // Executing step 1 / 10
                log.info("Executing step " + currentStep + "/" + maxSteps);
                // 单步执行，日志记录分步结果：Step 1: 模型输出结果
                String stepResult = step();
                String result = "Step "+ currentStep + ": " + stepResult;
                results.add(result);
            }
            // 检查是否超出步骤限制
            if (currentStep >= maxSteps) {
                state = AgentState.FINISHED;
                // Terminated: Reached max steps (10)
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
            return String.join("\n", results);
        } catch (Exception e) {
            state = AgentState.ERROR;
            log.error("Error Executing agent", e);
            return "执行错误: " + e.getMessage();
        } finally {
            // 清理资源
            this.cleanup();
        }
    }

    /**
     * 运行代理（流式输出）
     * @param userPrompt 用户提示词
     * @return 执行结果（包含每一步）
     */
    public SseEmitter runStream(String userPrompt) {
        SseEmitter sseEmitter = new SseEmitter(300000L);// 5分钟超时
        // 使用线程异步处理，避免阻塞主线程 return sseEmitter
        CompletableFuture.runAsync(() -> {
            try {
                if (this.state != AgentState.IDLE) {
                    sseEmitter.send(String.format("错误：无法从【%s状态】运行代理", this.state));
                    sseEmitter.complete();
                    return;
                }
                if (StrUtil.isBlank(userPrompt)) {
                    sseEmitter.send(String.format("错误：不能使用空提示词运行代理"));
                    sseEmitter.complete();
                    return;
                }
            } catch (IOException e) {
                sseEmitter.completeWithError(e);
            }
            // 更改状态
            this.state = AgentState.RUNNING;
            // 记录消息上下文
            messageList.add(new UserMessage(userPrompt));

            try {
                for (int i = 0; i < maxSteps && this.state != AgentState.FINISHED; i++) {
                    currentStep = i + 1;
                    // Executing step 1 / 10
                    log.info("Executing step " + currentStep + "/" + maxSteps);

                    // 单步执行
                    String stepResult = step();
                    String result = "Step "+ currentStep + ": " + stepResult;

                    // 发送每步执行结果
                    sseEmitter.send(result);
                }
                // 检查是否超出步骤限制
                if (currentStep >= maxSteps) {
                    state = AgentState.FINISHED;
                    // 执行结束：达到最大步骤 (10)
                    sseEmitter.send(String.format("执行结束：达到最大步骤 (%s) ", maxSteps));
                }
                // 正常完成
                sseEmitter.complete();
            } catch (Exception e) {
                state = AgentState.ERROR;
                log.error("执行智能体失败", e);
                try {
                    sseEmitter.send("执行错误: " + e.getMessage());
                    sseEmitter.complete();
                } catch (Exception ex) {
                    sseEmitter.completeWithError(ex);
                }
            } finally {
                // 清理资源
                this.cleanup();
            }
        });

        // 设置超时和完成回调
        sseEmitter.onTimeout(() -> {
            this.state = AgentState.ERROR;
            this.cleanup();
            log.warn("SSE connection timed out");
        });

        sseEmitter.onCompletion(() -> {
            if (this.state == AgentState.RUNNING) {
                this.state = AgentState.FINISHED;
            }
            this.cleanup();
            log.info("SSE connection completed");
        });

        return sseEmitter;
    }

    /**
     * 执行单个步骤
     * @return 步骤执行结果
     */
    public abstract String step();
    /**
     * 清理资源
     */
    protected void cleanup() {
        // 子类可以重写此方法来清理资源
    }
}
