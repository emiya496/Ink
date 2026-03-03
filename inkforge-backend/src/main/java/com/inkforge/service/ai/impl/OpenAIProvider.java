package com.inkforge.service.ai.impl;

import com.inkforge.service.ai.AIProvider;

/**
 * OpenAI Provider（逻辑同 DeepSeek，仅 base-url 和模型不同）
 */
public class OpenAIProvider extends DeepSeekProvider {
    public OpenAIProvider(String apiKey, String baseUrl, String model) {
        super(apiKey, baseUrl, model);
    }

    @Override
    public String getModelName() {
        return super.getModelName();
    }
}
