package com.inkforge.service.ai;

import com.inkforge.service.ai.impl.ClaudeProvider;
import com.inkforge.service.ai.impl.DeepSeekProvider;
import com.inkforge.service.ai.impl.OpenAIProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AIProviderFactory {

    @Value("${ai.provider}")
    private String providerType;

    @Value("${ai.deepseek.api-key}")
    private String deepseekApiKey;
    @Value("${ai.deepseek.base-url}")
    private String deepseekBaseUrl;
    @Value("${ai.deepseek.model}")
    private String deepseekModel;

    @Value("${ai.openai.api-key}")
    private String openaiApiKey;
    @Value("${ai.openai.base-url}")
    private String openaiBaseUrl;
    @Value("${ai.openai.model}")
    private String openaiModel;

    @Value("${ai.claude.api-key}")
    private String claudeApiKey;
    @Value("${ai.claude.model}")
    private String claudeModel;

    public AIProvider getProvider() {
        return switch (providerType.toLowerCase()) {
            case "deepseek" -> new DeepSeekProvider(deepseekApiKey, deepseekBaseUrl, deepseekModel);
            case "openai"   -> new OpenAIProvider(openaiApiKey, openaiBaseUrl, openaiModel);
            case "claude"   -> new ClaudeProvider(claudeApiKey, claudeModel);
            default -> throw new RuntimeException("Unsupported AI Provider: " + providerType);
        };
    }

    public String getProviderType() {
        return providerType;
    }
}
