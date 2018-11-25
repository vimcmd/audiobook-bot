package com.github.vimcmd.abookserver.service.audioknigiclub.parser;

import com.github.vimcmd.abook.commons.payload.BookAuthorResponse;
import com.github.vimcmd.abook.commons.payload.BookMetadataResponse;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * IMPORTANT: here is bug in Java 8 versions prior to 8u141, so jsoup will have javax.net.ssl.SSLHandshakeException
 */
@Component
@Slf4j
public class AudioknigiClubJsoupParser {

    private static final String BOOK_ID_REGEX = "book-circle-progress-(\\d+)";
    private static final Pattern BOOK_ID_PATTERN = Pattern.compile(BOOK_ID_REGEX);

    private static final String LS_SEC_KEY_REGEX = "LIVESTREET_SECURITY_KEY\\s?+=\\s?+'(.*)';";
    private static final Pattern LS_SEC_KEY_PATTERN = Pattern.compile(LS_SEC_KEY_REGEX);

    public BookMetadataResponse getBookMetadata(Connection.Response response) throws IOException {
        BookMetadataResponse book = new BookMetadataResponse();
        Document document = response.parse();
        book.setBid(getBookId(document));
        book.setAuthors(getBookAuthors(document));
        book.setFullTitle(getBookFullTitle(document));
        book.setUrl(response.url().toString());
        return book;
    }

    public String getLiveStreetSecurityKey(Connection.Response response) throws IOException {
        for (Element script : response.parse().getElementsByTag("script")) {
            Matcher m = LS_SEC_KEY_PATTERN.matcher(script.data());
            if (m.find()) {
                return m.group(1);
            }
        }

        throw new IllegalStateException("find LIVESTREET_SECURITY_KEY failed");
    }

    private String getBookId(Document document) {
        String selectClass = document.select("div[class~="
                                                     + BOOK_ID_REGEX
                                                     + "]").attr("class");
        Matcher m = BOOK_ID_PATTERN.matcher(selectClass);
        if (m.find()) {
            return m.group(1);
        }

        throw new IllegalStateException("find book id (bid) failed");
    }

    private List<BookAuthorResponse> getBookAuthors(Document document) {
        String authorFullName = document.select("span[itemprop=author]").text();
        return Collections.singletonList(new BookAuthorResponse(authorFullName));
    }

    private String getBookFullTitle(Document document) {
        return document.select("h1[class=topic-title]").text().trim();
    }
}
