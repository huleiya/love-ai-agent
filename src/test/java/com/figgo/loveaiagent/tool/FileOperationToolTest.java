package com.figgo.loveaiagent.tool;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileOperationToolTest {

    @Test
    void readFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "test.txt";
        String result = tool.readFile(fileName);
        Assertions.assertNotNull(result);
    }

    @Test
    void writeFile() {
        FileOperationTool tool = new FileOperationTool();
        String fileName = "test.txt";
        String content = "this is a test";
        String result = tool.writeFile(fileName, content);
        Assertions.assertNotNull(result);
    }
}