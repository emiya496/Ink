package com.inkforge.service.ai.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inkforge.service.ai.AIProvider;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * DeepSeek AI Provider（兼容 OpenAI 格式）
 */
public class DeepSeekProvider implements AIProvider {

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public DeepSeekProvider(String apiKey, String baseUrl, String model) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
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
            String requestBody = objectMapper.writeValueAsString(java.util.Map.of(
                "model", model,
                "messages", java.util.List.of(
                    java.util.Map.of("role", "user", "content", prompt)
                ),
                "temperature", 0.7,
                "max_tokens", 2048
            ));

            Request request = new Request.Builder()
                    .url(baseUrl + "/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("AI API调用失败: " + response.code() + " " + response.message());
                }
                String responseBody = response.body().string();
                JsonNode root = objectMapper.readTree(responseBody);
                return root.path("choices").get(0).path("message").path("content").asText();
            }
        } catch (IOException e) {
            throw new RuntimeException("AI API网络错误: " + e.getMessage(), e);
        }
    }

    @Override
    public String getModelName() {
        return model;
    }
}
