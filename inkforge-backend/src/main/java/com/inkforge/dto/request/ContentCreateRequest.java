package com.inkforge.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ContentCreateRequest {
    @Size(max = 200, message = "标题不能超过200字")
    private String title;

    private String content;

    private String type;

    private String coverImage; // 封面图URL

    @Size(max = 5, message = "最多选择5个标签")
    private List<Long> tagIds;

    // 自定义标签名列表（自动创建）
    @Size(max = 5, message = "最多5个自定义标签")
    private List<String> customTags;

    // true=保存草稿, false/null=正常发布
    private Boolean isDraft = false;
}
