package com.github.vimcmd.abook.commons.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class BookMetadataResponse {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String url;

    @JsonInclude
    private String bid; // book_id

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String title;

    /**
     * Author - Title
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String fullTitle;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<BookAuthorResponse> authors;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<String> genres;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String thumbnailUrl;

}
