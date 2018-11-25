package com.github.vimcmd.abookserver.service.audioknigiclub.service;

import com.github.vimcmd.abookserver.service.audioknigiclub.parser.AudioknigiClubJsoupParser;
import com.github.vimcmd.abook.commons.payload.BookMetadataResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
@AllArgsConstructor
public class AudioknigiClubBookService {

    private AudioknigiClubJsoupParser parser;

    public BookMetadataResponse getBookMetadata(String bookUrl) throws IOException {
        Connection.Response response = Jsoup.connect(bookUrl).execute();
        return parser.getBookMetadata(response);
    }
}
