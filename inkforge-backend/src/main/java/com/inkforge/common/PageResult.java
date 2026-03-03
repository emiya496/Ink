package com.inkforge.common;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private Long total;
    private List<T> list;
    private Integer page;
    private Integer size;

    public static <T> PageResult<T> of(Long total, List<T> list, Integer page, Integer size) {
        PageResult<T> r = new PageResult<>();
        r.setTotal(total);
        r.setList(list);
        r.setPage(page);
        r.setSize(size);
        return r;
    }
}
