package com.github.vimcmd.abook.commons.payload;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class ChapterResponse {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String bookIdentity;
    private String url;
    private String title;
}
