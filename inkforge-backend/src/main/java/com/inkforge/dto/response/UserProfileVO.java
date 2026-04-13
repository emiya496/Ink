package com.inkforge.dto.response;

import lombok.Data;

@Data
public class UserProfileVO {
    private Long id;
    private String username;
    private String avatar;
    private String bio;
    private long followersCount;
    private long followingCount;
    private long worksCount;
    private boolean following; // 当前访问者是否已关注该用户
}
