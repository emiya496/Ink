package com.inkforge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("content")
public class Content {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String type;
    private String status;
    private Integer viewCount;
    private Integer likeCount;
    private String coverImage;
    private String visibility; // "public" | "private" | "followers_only"
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
