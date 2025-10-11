package com.figgo.loveimagesearchmcpserver;

import com.figgo.loveimagesearchmcpserver.tools.ImageSearchTool;
import jakarta.annotation.Resource;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class LoveImageSearchMcpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoveImageSearchMcpServerApplication.class, args);
	}

	@Bean
	public ToolCallbackProvider imageSearchToolProvider(ImageSearchTool imageSearchTool) {
		return MethodToolCallbackProvider.builder()
				.toolObjects(imageSearchTool)
				.build();
	}

}
