package com.inkforge.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentVO {
    private Long id;
    private Long contentId;
    private Long userId;
    private String username;
    private String avatar;
    private String content;
    private LocalDateTime createTime;
}
