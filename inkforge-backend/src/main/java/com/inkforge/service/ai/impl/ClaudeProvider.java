package com.inkforge.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkforge.service.ai.AIProvider;
import okhttp3.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Claude (Anthropic) Provider
 */
public class ClaudeProvider implements AIProvider {

    private final String apiKey;
    private final String model;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public ClaudeProvider(String apiKey, String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String generate(String prompt) {
        try {
            String requestBody = objectMapper.writeValueAsString(Map.of(
                "model", model,
                "max_tokens", 2048,
                "messages", List.of(Map.of("role", "user", "content", prompt))
            ));

            Request request = new Request.Builder()
                    .url("https://api.anthropic.com/v1/messages")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Claude API调用失败: " + response.code());
                }
                JsonNode root = objectMapper.readTree(response.body().string());
                return root.path("content").get(0).path("text").asText();
            }
        } catch (IOException e) {
            throw new RuntimeException("Claude API网络错误: " + e.getMessage(), e);
        }
    }

    @Override
    public String getModelName() {
        return model;
    }
}
