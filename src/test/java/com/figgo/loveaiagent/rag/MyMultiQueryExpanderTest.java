package com.figgo.loveaiagent.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MyMultiQueryExpanderTest {

    @Resource
    MyMultiQueryExpander myMultiQueryExpander;

    @Test
    void expand() {
        List<Query> expandedQueries = myMultiQueryExpander.expand(new Query("你觉得什么样的人生是有意义的？"));
        Assertions.assertNotNull(expandedQueries);
    }
}