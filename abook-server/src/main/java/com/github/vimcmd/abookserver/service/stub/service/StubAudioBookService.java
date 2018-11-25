package com.github.vimcmd.abookserver.service.stub.service;

import com.github.vimcmd.abook.commons.payload.BookMetadataResponse;
import org.springframework.stereotype.Service;

@Service
public class StubAudioBookService {

    public BookMetadataResponse getStubBookMetadata(String bookUrl) {
        BookMetadataResponse book = new BookMetadataResponse();
        book.setUrl(bookUrl);
        book.setFullTitle("BOOK FULL TITLE");
        return book;
    }



}
