package com.github.vimcmd.abook.commons.payload;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PagedResponse<T> {
    private List<T> content;

    /**
     * starts from 1
     */
    private int page;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean last;

    /**
     * @param page starts from 1
     */
    public static <T> PagedResponse<T> from(List<T> all, int page, int pageSize) {
        List<List<T>> pages = Lists.partition(all, pageSize);
        List<T> pageContent = pages.get(page - 1);
        PagedResponse<T> pr = new PagedResponse<>();
        pr.setContent(pageContent);
        pr.setPage(page);
        pr.setPageSize(pageSize);
        pr.setTotalElements(all.size());
        pr.setTotalPages(pages.size());
        pr.setLast(page == pages.size());

        return pr;
    }
}
