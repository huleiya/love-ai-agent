package com.figgo.loveaiagent.agent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * ReAct（Resoning and Acting）模式的代理抽象类
 * 实现了思考-行动的循环模式
 */
@Data
@Slf4j
public abstract class ReActAgent extends BaseAgent{

    /**
     * 执行单个步骤：思考和行动
     *
     * @return 步骤执行结果
     */
    @Override
    public String step() {
        try {
            boolean shouldAct = think();
            if (!shouldAct) {
                return "思考完成 - 无需行动";
            }
            return act();
        } catch (Exception e) {
            // 记录异常日志 Executing step 2 error, e
            log.error(String.format("Error Executing step %d / %d, %s",getCurrentStep(),getMaxSteps(),e.getMessage()), e);
            return String.format("步骤 %d 执行失败: %s", getCurrentStep(), e.getMessage());
        }
    }

    /**
     * 处理当前状态并决定下一步行动
     *
     * @return 是否需要执行行动，true表示需要执行，false表示不需要执行
     */
    public abstract boolean think();

    /**
     * 执行决定的行动 - 工具调用
     *
     * @return 步骤执行结果
     */
    public abstract String act();
}
