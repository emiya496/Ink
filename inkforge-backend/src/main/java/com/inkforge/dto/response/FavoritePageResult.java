package com.inkforge.dto.response;

import com.inkforge.common.PageResult;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class FavoritePageResult extends PageResult<ContentVO> {
    private Long hiddenTotal;
    private Map<String, Long> hiddenByReason;

    public static FavoritePageResult of(
            Long total,
            List<ContentVO> list,
            Integer page,
            Integer size,
            Long hiddenTotal,
            Map<String, Long> hiddenByReason) {
        FavoritePageResult result = new FavoritePageResult();
        result.setTotal(total);
        result.setList(list);
        result.setPage(page);
        result.setSize(size);
        result.setHiddenTotal(hiddenTotal);
        result.setHiddenByReason(hiddenByReason);
        return result;
    }
}
