package com.inkforge.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TagVO {
    private Long id;
    private String tagName;
    private String type;
    private String status;
    private Long useCount;
}
