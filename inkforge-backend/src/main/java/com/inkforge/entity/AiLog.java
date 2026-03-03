package com.inkforge.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ai_log")
public class AiLog {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String functionType;
    private String modelName;
    private LocalDateTime createTime;
}
