package com.github.vimcmd.abookserver.service.audioknigiclub.controller;

import com.github.vimcmd.abook.commons.payload.BookMetadataResponse;
import com.github.vimcmd.abook.commons.payload.ChapterRequest;
import com.github.vimcmd.abook.commons.payload.ChapterResponse;
import com.github.vimcmd.abook.commons.payload.PagedResponse;
import com.github.vimcmd.abookserver.service.audioknigiclub.service.AudioknigiClubBookService;
import com.github.vimcmd.abookserver.service.audioknigiclub.service.AudioknigiClubChapterService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("${audiobook.server.requestmapping.api.audioknigi.club}")
@AllArgsConstructor
@Slf4j
public class AudioknigiClubController {

    private AudioknigiClubChapterService chapterService;

    private AudioknigiClubBookService bookService;

    @PostMapping
    public PagedResponse<ChapterResponse> parse(@Valid @RequestBody ChapterRequest request) throws IOException {
        return chapterService.getChapters(request);
    }

    @PostMapping("/meta")
    public BookMetadataResponse getBookMetadata(@RequestBody Map<String,Object> body) throws IOException {
        return bookService.getBookMetadata(body.get("url").toString());
    }

    @PostMapping("/chapters")
    public PagedResponse<ChapterResponse> getChapters(@Valid @RequestBody ChapterRequest request) throws IOException {
        PagedResponse<ChapterResponse> chapters = chapterService.getChapters(request);
        return chapters;
    }
}
