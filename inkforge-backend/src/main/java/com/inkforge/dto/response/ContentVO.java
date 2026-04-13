package com.inkforge.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ContentVO {
    private Long id;
    private Long userId;
    private String username;
    private String avatar;
    private String title;
    private String content;
    private String type;
    private String status;
    private String visibility;
    private Integer viewCount;
    private Integer likeCount;
    private Integer favoriteCount; // 收藏总数（排行用）
    private String coverImage;     // 封面图URL
    private LocalDateTime createTime;
    private List<TagVO> tags;
    private Integer commentCount;
    private Boolean favorited; // 当前用户是否收藏
    private Boolean liked;     // 当前用户是否点赞
}
