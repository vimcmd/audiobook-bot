package com.github.vimcmd.abookserver.service.stub.service;

import com.github.vimcmd.abook.commons.payload.ChapterRequest;
import com.github.vimcmd.abook.commons.payload.ChapterResponse;
import com.github.vimcmd.abook.commons.payload.PagedResponse;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class StubAudioBookChapterService {

    public PagedResponse<ChapterResponse> getChapters(@Valid ChapterRequest request) {
        return PagedResponse.from(getStubChapters(request),
                                  request.getPage(),
                                  request.getPageSize());
    }

    private List<ChapterResponse> getStubChapters(@Valid ChapterRequest book) {
        return IntStream.rangeClosed(1, 40).mapToObj(id -> {
            ChapterResponse chapter = new ChapterResponse();
            chapter.setBookIdentity(book.getUrl());
            chapter.setTitle(UUID.randomUUID().toString());
            chapter.setUrl("https://get.sweetbook.net/acl.mp3");
            return chapter;
        }).collect(Collectors.toList());
    }

}
