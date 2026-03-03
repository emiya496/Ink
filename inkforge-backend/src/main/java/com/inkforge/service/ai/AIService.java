package com.inkforge.service.ai;

import com.inkforge.entity.AiLog;
import com.inkforge.mapper.AiLogMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AIService {

    private final AIProviderFactory factory;
    private final AiLogMapper aiLogMapper;

    public AIService(AIProviderFactory factory, AiLogMapper aiLogMapper) {
        this.factory = factory;
        this.aiLogMapper = aiLogMapper;
    }

    /** AI续写/创作 */
    public String generate(String content, Long userId) {
        String prompt = String.format("""
                你是一位专业的文学创作助手。请根据以下文章片段，进行风格一致的续写，续写长度约200-400字：

                %s

                请直接输出续写内容，不要有任何前缀说明。
                """, content);
        return callAndLog(prompt, "generate", userId);
    }

    /** 文章摘要 */
    public String summary(String content, Long userId) {
        String prompt = String.format("""
                请为以下文章生成一段精准的内容摘要，控制在100-200字以内，要抓住文章核心主旨：

                %s
                """, content);
        return callAndLog(prompt, "summary", userId);
    }

    /** 文章润色 */
    public String polish(String content, Long userId) {
        String prompt = String.format("""
                你是一位专业的文学编辑，请对以下文章进行润色优化，保持原文意思不变，提升语言表达的流畅性、文采和感染力：

                %s

                请直接输出润色后的文章，不要有任何说明。
                """, content);
        return callAndLog(prompt, "polish", userId);
    }

    /** 关键词提取 */
    public String keywords(String content, Long userId) {
        String prompt = String.format("""
                请从以下文章中提取5-10个核心关键词，用逗号分隔，只输出关键词，不要有其他说明：

                %s
                """, content);
        return callAndLog(prompt, "keywords", userId);
    }

    /** 情感分析 */
    public String sentiment(String content, Long userId) {
        String prompt = String.format("""
                请分析以下文章的情感基调。请从以下维度分析并简要说明（总计100-150字）：
                1. 主要情感（如：喜悦、悲伤、愤怒、思念、平静等）
                2. 情感强度（强烈/适中/平淡）
                3. 整体情感走向

                文章内容：
                %s
                """, content);
        return callAndLog(prompt, "sentiment", userId);
    }

    /** 文风分析 */
    public String style(String content, Long userId) {
        String prompt = String.format("""
                请分析以下文章的写作风格，从语言特点、叙事视角、写作技巧等角度进行分析，控制在150-200字：

                %s
                """, content);
        return callAndLog(prompt, "style", userId);
    }

    /** AI问答 */
    public String qa(String content, String question, Long userId) {
        String prompt = String.format("""
                请根据以下文章内容，回答用户的问题。

                文章内容：
                %s

                用户问题：%s

                请结合文章内容给出准确、简洁的回答。
                """, content, question);
        return callAndLog(prompt, "qa", userId);
    }

    private String callAndLog(String prompt, String functionType, Long userId) {
        AIProvider provider = factory.getProvider();
        String result = provider.generate(prompt);
        // 记录AI调用日志
        AiLog log = new AiLog();
        log.setUserId(userId);
        log.setFunctionType(functionType);
        log.setModelName(provider.getModelName());
        log.setCreateTime(LocalDateTime.now());
        aiLogMapper.insert(log);
        return result;
    }
}
