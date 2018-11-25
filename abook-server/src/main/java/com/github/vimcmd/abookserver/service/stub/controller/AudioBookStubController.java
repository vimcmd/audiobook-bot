package com.github.vimcmd.abookserver.service.stub.controller;

import com.github.vimcmd.abookserver.service.stub.service.StubAudioBookChapterService;
import com.github.vimcmd.abookserver.service.stub.service.StubAudioBookService;
import com.github.vimcmd.abook.commons.payload.BookMetadataResponse;
import com.github.vimcmd.abook.commons.payload.ChapterRequest;
import com.github.vimcmd.abook.commons.payload.ChapterResponse;
import com.github.vimcmd.abook.commons.payload.PagedResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("${audiobook.server.requestmapping.api.stub}")
public class AudioBookStubController {

    private StubAudioBookChapterService chapterService;

    private StubAudioBookService bookService;

    @PostMapping
    public PagedResponse<ChapterResponse> parse(@Valid @RequestBody ChapterRequest request) {
        return chapterService.getChapters(request);
    }

    @PostMapping("/meta")
    public BookMetadataResponse getBookMetadata(String bookUrl) {
        return bookService.getStubBookMetadata(bookUrl);
    }

}
