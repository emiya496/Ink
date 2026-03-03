package com.inkforge.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AiRequest {
    @NotBlank(message = "内容不能为空")
    private String content;
    private String question; // 用于AI问答
    private String style;    // 用于文风润色
}
