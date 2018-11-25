package com.github.vimcmd.abook.commons.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

@Data
@NoArgsConstructor
@ToString
public class ChapterRequest {

    @JsonInclude
    @NotEmpty
    private String url;

    @JsonInclude
    private String bid; // book_id

    @JsonInclude
    @NotNull
    private int pageSize;

    @JsonInclude
    @NotNull
    private int page;

    public static ChapterRequest of(String url, String bid, int page, int pageSize) throws MalformedURLException, URISyntaxException {
        URL u = new URL(url);
        u.toURI();
        ChapterRequest cr = new ChapterRequest();
        cr.setUrl(url);
        cr.setBid(bid);
        cr.setPage(page);
        cr.setPageSize(pageSize);
        return cr;
    }

}

