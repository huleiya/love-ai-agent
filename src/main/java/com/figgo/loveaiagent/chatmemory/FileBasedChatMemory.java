package com.figgo.loveaiagent.chatmemory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import org.objenesis.strategy.StdInstantiatorStrategy;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 基于本地文件的对话记忆，使用 Kryo 序列化对话消息列表
 * 提供 2 个关键方法：
 *  1. 保存对话消息
 *  2. 加载对话消息
 */
public class FileBasedChatMemory implements ChatMemory {
    private final String BASE_DIR;
    public static final Kryo kryo = new Kryo();

    static {
        kryo.setRegistrationRequired(false);
        // 设置实例化策略，自动注册待序列化对象的 class 类型
        kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
    }

    public FileBasedChatMemory(String dir) {
        this.BASE_DIR = dir;
        File baseDir = new File(dir);
        if (!baseDir.exists()) {
            baseDir.mkdirs();//逐级创建目录
        }
    }

    /*
     * 保存这次的对话到文件中，分两种情况：
     * 1. 没有历史对话文件，直接保存文件
     * 2. 有历史，先取出文件里的历史对话列表，然后拼接这次的对话，再保存文件
     * */
    @Override
    public void add(String conversationId, List<Message> messages) {
        List<Message> conversationHistory = getOrCreateConversation(conversationId);
        conversationHistory.addAll(messages);
        saveConversationFile(conversationId, conversationHistory);
    }

    @Override
    public List<Message> get(String conversationId, int lastN) {
        // 1. 先加载所有历史对话
        // 2. 然后利用 stream api 取出最后 N 条对话，直接跳过 list.size() - lastN 条对话
        List<Message> conversationHistory = getOrCreateConversation(conversationId);
        return conversationHistory.stream()
                .skip(Math.max(0, conversationHistory.size() - lastN))
                .toList();
    }

    @Override
    public void clear(String conversationId) {
        File file = getConversationFile(conversationId);
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * 将消息列表序列化并保存到本地文件中
     * @param conversationId 需要保存的文件名:[{conversationId}.kryo],方便以后按 chatId 查找历史对话
     * @param messages 需要保存的对话历史列表
     */
    private void saveConversationFile(String conversationId, List<Message> messages) {
        File file = getConversationFile(conversationId);//指向一个本地文件路径
        try (Output output = new Output(new FileOutputStream(file))) {
            kryo.writeObject(output, messages);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从文件中反序列化消息列表，若文件不存在则直接返回空列表
     * @param conversationId 通过 chatId 获取文件名
     * @return
     */
    private List<Message> getOrCreateConversation(String conversationId) {
        File file = getConversationFile(conversationId);
        List<Message> messages = new ArrayList<>();
        if (file.exists()) {
            try (Input input = new Input(new FileInputStream(file))) {
                messages = kryo.readObject(input, ArrayList.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return messages;
    }

    /**
     * 返回一个文件名关联 chatId 的文件
     * @param conversationId
     * @return
     */
    private File getConversationFile(String conversationId) {
        return new File(BASE_DIR, conversationId +".kryo");
    }
}
