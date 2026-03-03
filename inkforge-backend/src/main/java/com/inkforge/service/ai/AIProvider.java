package com.inkforge.service.ai;

public interface AIProvider {
    /**
     * 核心调用接口：发送 prompt 获取 AI 回复
     */
    String generate(String prompt);

    /**
     * 获取当前模型名称（用于日志记录）
     */
    String getModelName();
}
