package com.inkforge.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.inkforge.common.exception.BusinessException;
import com.inkforge.entity.Chapter;
import com.inkforge.entity.Content;
import com.inkforge.mapper.ChapterMapper;
import com.inkforge.mapper.ContentMapper;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChapterService {

    private final ChapterMapper chapterMapper;
    private final ContentMapper contentMapper;

    public ChapterService(ChapterMapper chapterMapper, ContentMapper contentMapper) {
        this.chapterMapper = chapterMapper;
        this.contentMapper = contentMapper;
    }

    public void addChapter(Long contentId, String title, String chapterContent, Long userId, Integer chapterOrder) {
        Content content = contentMapper.selectById(contentId);
        if (content == null) throw new BusinessException(404, "内容不存在");
        if (!content.getUserId().equals(userId)) throw new BusinessException(403, "无权操作");
        if (!"小说".equals(content.getType())) throw new BusinessException("只有小说类型支持章节");

        int order;
        if (chapterOrder != null) {
            // 补写缺章：使用前端指定的序号
            order = chapterOrder;
        } else {
            // 新增下一章：自动取最大序号+1
            order = chapterMapper.selectList(
                    new LambdaQueryWrapper<Chapter>().eq(Chapter::getContentId, contentId)
                            .orderByDesc(Chapter::getChapterOrder)).stream()
                    .mapToInt(Chapter::getChapterOrder).max().orElse(0) + 1;
        }

        Chapter chapter = new Chapter();
        chapter.setContentId(contentId);
        chapter.setChapterTitle(title);
        chapter.setChapterOrder(order);
        chapter.setChapterContent(chapterContent);
        chapterMapper.insert(chapter);
    }

    public List<Chapter> listByContentId(Long contentId) {
        return chapterMapper.selectList(
                new LambdaQueryWrapper<Chapter>()
                        .eq(Chapter::getContentId, contentId)
                        .orderByAsc(Chapter::getChapterOrder));
    }

    public void updateChapter(Long chapterId, String title, String content, Long userId) {
        Chapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) throw new BusinessException(404, "章节不存在");
        Content c = contentMapper.selectById(chapter.getContentId());
        if (!c.getUserId().equals(userId)) throw new BusinessException(403, "无权操作");

        chapter.setChapterTitle(title);
        chapter.setChapterContent(content);
        chapterMapper.updateById(chapter);
    }

    public void deleteChapter(Long chapterId, Long userId) {
        Chapter chapter = chapterMapper.selectById(chapterId);
        if (chapter == null) throw new BusinessException(404, "章节不存在");
        Content content = contentMapper.selectById(chapter.getContentId());
        if (!content.getUserId().equals(userId)) throw new BusinessException(403, "无权操作");
        chapterMapper.deleteById(chapterId);
    }
}
