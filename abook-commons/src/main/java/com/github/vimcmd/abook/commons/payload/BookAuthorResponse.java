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
public class BookAuthorResponse {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String fullName;
}
