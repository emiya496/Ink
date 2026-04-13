package com.inkforge.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.inkforge.entity.Content;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ContentMapper extends BaseMapper<Content> {

    /** 总阅读量排行 TOP10 */
    @Select("SELECT * FROM content " +
            "WHERE status = '正常' AND visibility = 'public' AND (#{type} = '' OR type = #{type}) " +
            "ORDER BY view_count DESC LIMIT 10")
    List<Content> rankByReads(@Param("type") String type);

    /** 近7天阅读量排行 TOP10 */
    @Select("SELECT c.* FROM content c " +
            "LEFT JOIN content_view_log vl ON c.id = vl.content_id " +
            "  AND vl.view_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "WHERE c.status = '正常' AND c.visibility = 'public' AND (#{type} = '' OR c.type = #{type}) " +
            "GROUP BY c.id " +
            "ORDER BY COUNT(vl.id) DESC, c.view_count DESC LIMIT 10")
    List<Content> rankByWeeklyReads(@Param("type") String type);

    /** 点赞量排行 TOP10 */
    @Select("SELECT * FROM content " +
            "WHERE status = '正常' AND visibility = 'public' AND (#{type} = '' OR type = #{type}) " +
            "ORDER BY like_count DESC LIMIT 10")
    List<Content> rankByLikes(@Param("type") String type);

    /** 收藏量排行 TOP10 */
    @Select("SELECT c.* FROM content c " +
            "LEFT JOIN favorite f ON c.id = f.content_id " +
            "WHERE c.status = '正常' AND c.visibility = 'public' AND (#{type} = '' OR c.type = #{type}) " +
            "GROUP BY c.id " +
            "ORDER BY COUNT(f.id) DESC, c.view_count DESC LIMIT 10")
    List<Content> rankByFavorites(@Param("type") String type);

    /** 按周阅读量排序 — 分页 */
    @Select("SELECT c.* FROM content c " +
            "LEFT JOIN content_view_log vl ON c.id = vl.content_id " +
            "  AND vl.view_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "WHERE c.status = '正常' AND c.visibility = 'public' " +
            "AND (#{type} = '' OR c.type = #{type}) " +
            "AND (#{keyword} = '' OR c.title LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{tagId} = 0 OR EXISTS (SELECT 1 FROM content_tag ct WHERE ct.content_id = c.id AND ct.tag_id = #{tagId})) " +
            "GROUP BY c.id " +
            "ORDER BY COUNT(vl.id) DESC, c.view_count DESC " +
            "LIMIT #{offset}, #{size}")
    List<Content> listByWeeklyReads(@Param("type") String type, @Param("keyword") String keyword,
                                    @Param("tagId") long tagId,
                                    @Param("offset") long offset, @Param("size") int size);

    /** 按收藏量排序 — 分页 */
    @Select("SELECT c.* FROM content c " +
            "LEFT JOIN favorite f ON c.id = f.content_id " +
            "WHERE c.status = '正常' AND c.visibility = 'public' " +
            "AND (#{type} = '' OR c.type = #{type}) " +
            "AND (#{keyword} = '' OR c.title LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{tagId} = 0 OR EXISTS (SELECT 1 FROM content_tag ct WHERE ct.content_id = c.id AND ct.tag_id = #{tagId})) " +
            "GROUP BY c.id " +
            "ORDER BY COUNT(f.id) DESC, c.view_count DESC " +
            "LIMIT #{offset}, #{size}")
    List<Content> listByFavorites(@Param("type") String type, @Param("keyword") String keyword,
                                  @Param("tagId") long tagId,
                                  @Param("offset") long offset, @Param("size") int size);

    /** 列表总数（供 weeklyReads / favorites 排序分页用） */
    @Select("SELECT COUNT(*) FROM content " +
            "WHERE status = '正常' AND visibility = 'public' " +
            "AND (#{type} = '' OR type = #{type}) " +
            "AND (#{keyword} = '' OR title LIKE CONCAT('%', #{keyword}, '%')) " +
            "AND (#{tagId} = 0 OR EXISTS (SELECT 1 FROM content_tag ct WHERE ct.content_id = id AND ct.tag_id = #{tagId}))")
    long countList(@Param("type") String type, @Param("keyword") String keyword,
                   @Param("tagId") long tagId);

    /** 近7天最火内容（首页横幅用），无数据时降级到总阅读量第一 */
    @Select("SELECT c.* FROM content c " +
            "LEFT JOIN content_view_log vl ON c.id = vl.content_id " +
            "  AND vl.view_time >= DATE_SUB(NOW(), INTERVAL 7 DAY) " +
            "WHERE c.status = '正常' AND c.visibility = 'public' AND (#{type} = '' OR c.type = #{type}) " +
            "GROUP BY c.id " +
            "ORDER BY COUNT(vl.id) DESC, c.view_count DESC LIMIT 1")
    Content getHotBannerContent(@Param("type") String type);
}
