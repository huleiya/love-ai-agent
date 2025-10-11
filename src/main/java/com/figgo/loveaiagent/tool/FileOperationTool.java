package com.figgo.loveaiagent.tool;

import cn.hutool.core.io.FileUtil;
import com.figgo.loveaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 文件操作工具类
 */
public class FileOperationTool {

    public final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    /**
     * 读取文件内容
     * @param fileName 文件路径字符串
     * @return
     */
    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of the file to read, without any directory path") String fileName) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    @Tool(description = "Write content to a file")
    public String writeFile(
            @ToolParam(description = "Name of the file to write, without any directory path") String fileName,
            @ToolParam(description = "Content to write to the file") String content) {
        String filePath = FILE_DIR + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(fileName);
            FileUtil.writeUtf8String(content, filePath);
            return "File written successfully to: " + filePath;
        } catch (Exception e) {
            return "Error writing to file: " + e.getMessage();
        }
    }
}
