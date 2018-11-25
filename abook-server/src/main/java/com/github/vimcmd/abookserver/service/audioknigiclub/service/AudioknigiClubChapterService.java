package com.github.vimcmd.abookserver.service.audioknigiclub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.vimcmd.abook.commons.payload.ChapterRequest;
import com.github.vimcmd.abook.commons.payload.ChapterResponse;
import com.github.vimcmd.abook.commons.payload.PagedResponse;
import com.github.vimcmd.abookserver.service.audioknigiclub.parser.AudioknigiClubJsoupParser;
import com.github.vimcmd.abookserver.service.audioknigiclub.security.LiveStreetSecurityProvider;
import com.google.common.base.Strings;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AudioknigiClubChapterService {

    private AudioknigiClubJsoupParser parser;

    private AudioknigiClubBookService bookService;

    private LiveStreetSecurityProvider hashProvider;

    @Value("${app.audioknigi.club.chapters.rest}")
    private String chaptersRest;

    public AudioknigiClubChapterService(AudioknigiClubJsoupParser parser, AudioknigiClubBookService bookService, LiveStreetSecurityProvider hashProvider) {
        this.parser = parser;
        this.bookService = bookService;
        this.hashProvider = hashProvider;
    }

    public PagedResponse<ChapterResponse> getChapters(@Valid ChapterRequest request) throws IOException {
        return PagedResponse.from(getAllChapters(request),
                                  request.getPage(),
                                  request.getPageSize());
    }

    private List<ChapterResponse> getAllChapters(@Valid ChapterRequest request) throws IOException {
        String requestUrl = request.getUrl();
        Connection.Response response = Jsoup.connect(requestUrl).execute();

        if (Strings.isNullOrEmpty(request.getBid())) {
            request.setBid(bookService.getBookMetadata(requestUrl).getBid());
        }

        return requestChapterItems(request.getBid(), response)
                .stream()
                .map(item -> {
                    ChapterResponse chapter = new ChapterResponse();
                    chapter.setBookIdentity(response.url().toString());
                    chapter.setTitle(item.get("title").toString());
                    // FIXME (25.11.2018): that's not all - mp3 on 'correct' url still a stub
                    chapter.setUrl(item.get("mp3").toString().replace(" ", "%%20"));
                    return chapter;
                }).collect(Collectors.toList());

    }

    private List<LinkedHashMap> requestChapterItems(String bookId, Connection.Response response) throws IOException {
        String liveStreetSecurityKey = parser.getLiveStreetSecurityKey(response);
        Object hash = hashProvider.getHash(liveStreetSecurityKey);
        String cookies = response.cookies()
                                 .entrySet()
                                 .stream()
                                 .map(e -> e.getKey() + "=" + e.getValue())
                                 .collect(Collectors.joining("; "));

        LinkedMultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("bid", bookId);
        formData.add("hash", hash);
        formData.add("security_ls_key", liveStreetSecurityKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("cookie", cookies);

        HttpEntity requestEntity = new HttpEntity<>(formData, headers);
        RestTemplate rest = new RestTemplate();
        ResponseEntity<ChapterItemsAjaxResponse> responseEntity = rest.exchange(chaptersRest + "/" + bookId,
                                                                                HttpMethod.POST,
                                                                                requestEntity,
                                                                                ChapterItemsAjaxResponse.class);
        log.info("response entity: {}", responseEntity.getBody());
        return Objects.requireNonNull(responseEntity.getBody()).getMappedItems();
    }


    /**
     * post response payload for https://audioknigi.club/ajax/bid/{bid}
     */
    @NoArgsConstructor
    private static class ChapterItemsAjaxResponse {
        private static final ObjectMapper mapper = new ObjectMapper();

        private String aItems;
        private boolean fstate;
        private String sMsgTitle;
        private String sMsg;
        private boolean bStateError;

        @SuppressWarnings("unchecked")
        List<LinkedHashMap> getMappedItems() {
            // items will be null if lombok used
            try {
                return mapper.readValue(Objects.requireNonNull(aItems), List.class);
            } catch (IOException e) {
                return Collections.emptyList();
            }
        }

        public void setaItems(String aItems) {
            this.aItems = aItems;
        }

        public boolean isFstate() {
            return fstate;
        }

        public void setFstate(boolean fstate) {
            this.fstate = fstate;
        }

        public String getsMsgTitle() {
            return sMsgTitle;
        }

        public void setsMsgTitle(String sMsgTitle) {
            this.sMsgTitle = sMsgTitle;
        }

        public String getsMsg() {
            return sMsg;
        }

        public void setsMsg(String sMsg) {
            this.sMsg = sMsg;
        }

        public boolean isbStateError() {
            return bStateError;
        }

        public void setbStateError(boolean bStateError) {
            this.bStateError = bStateError;
        }
    }

}
