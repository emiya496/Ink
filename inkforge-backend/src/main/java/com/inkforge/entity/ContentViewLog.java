package com.inkforge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("content_view_log")
public class ContentViewLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long contentId;
    private LocalDateTime viewTime;
}
