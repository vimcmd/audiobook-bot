package com.github.vimcmd.abooktelegrambot.bot;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.vimcmd.abook.commons.payload.BookMetadataResponse;
import com.github.vimcmd.abook.commons.payload.ChapterRequest;
import com.github.vimcmd.abook.commons.payload.ChapterResponse;
import com.github.vimcmd.abook.commons.payload.PagedResponse;
import com.github.vimcmd.abook.commons.payload.resttemplatewrappers.ChapterPagedResponseWrapper;
import com.github.vimcmd.abook.commons.util.Util;
import com.google.common.util.concurrent.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class AudioBookBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(AudioBookBot.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final int DEFAULT_PAGE = 1;

    /**
     * rate limited message sender
     */
    private MessageSender sender;

    @Value("${audiobook.telegram.bot.chapter.page.size}")
    private int pageSize;

    @Value("${audiobook.telegram.bot.chapter.page.buttons.max}")
    private int pageButtonsMax;

    @Value("${audiobook.server.api.url}")
    private String audioBookServerApiUrl;

    @Value("${audiobook.telegram.bot.username}")
    private String botUserName;

    @Value("${audiobook.telegram.bot.authorization-token}")
    private String botToken;

    @Value("${audiobook.telegram.bot.chapter.send.pages.inline}")
    private boolean isPagesInline;

    @PostConstruct
    private void initBot() {
        sender = new MessageSender();
    }

    @Override
    public String getBotUsername() {
        return botUserName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String msgText = update.getMessage().getText();
            if (msgText.equals("/start")) {
                // TODO (22.11.2018): welcome message
            } else {
                sendBookChapters(update);
            }
        } else if (update.hasCallbackQuery()) {
            try {
                sendChaptersPagedEditMessage(update);
            } catch (IOException | URISyntaxException e) {
                sendMessage("Something goes wrong (" + e.getLocalizedMessage() + ")", update.getMessage().getChatId());
            }
        }
    }

    private void sendBookChapters(Update update) {
        String bookUrl = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();

        try {
            if (isPagesInline) {
                sendChaptersPageInlineMessage(bookUrl, chatId);
            } else {
                sendChaptersPageInSeparateMessage(bookUrl, chatId);
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            logger.warn(e.getLocalizedMessage() + " ('" + bookUrl + "')", e);
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                sendMessage("Sorry, service '" + Util.getDomainNameUnchecked(bookUrl) + "' not yet supported", chatId);
            } else {
                sendMessage(e.getStatusCode() + " Sorry, book not found in url: " + bookUrl, chatId);
            }
        } catch (MalformedURLException | URISyntaxException e) {
            sendMessage("Not valid book url: '" + bookUrl + "' (" + e.getLocalizedMessage() + ")",
                        update.getMessage().getChatId());
            logger.warn(e.getLocalizedMessage(), e);
        } catch (Exception e) {
            logger.error(e.getLocalizedMessage(), e);
        }
    }

    private void sendChaptersPageInSeparateMessage(String bookUrl, Long chatId) throws MalformedURLException, URISyntaxException {
        int pages = sendChapterMessage(bookUrl, chatId, DEFAULT_PAGE);
        for (int i = DEFAULT_PAGE + 1; i <= pages; i++) {
            sendChapterMessage(bookUrl, chatId, i);
        }
    }

    /**
     * @return total pages
     */
    private int sendChapterMessage(String bookUrl, Long chatId, int page) throws MalformedURLException, URISyntaxException {
        PagedResponse<ChapterResponse> chapters = requestChapters(bookUrl, page, pageSize);
        SendMessage msg = new SendMessage().setChatId(chatId)
                                           .setText(getMessageTextMarkdown(bookUrl, chapters))
                                           .setParseMode(ParseMode.MARKDOWN);

        sender.send(msg);
        return chapters.getTotalPages();
    }

    private void sendChaptersPageInlineMessage(String bookUrl, Long chatId) throws MalformedURLException, URISyntaxException {
        // FIXME (25.11.2018): url shorten service, callback data limited
        PagedResponse<ChapterResponse> chapters = requestChapters(bookUrl, DEFAULT_PAGE, pageSize);
        SendMessage msg = new SendMessage().setChatId(chatId)
                                           .setText(getMessageTextMarkdown(bookUrl, chapters))
                                           .setParseMode(ParseMode.MARKDOWN)
                                           .setReplyMarkup(new InlineKeyboardMarkup()
                                                                   .setKeyboard(getChapterPagesKeyboard(bookUrl,
                                                                                                        chapters)));

        sender.send(msg);
    }

    private void sendChaptersPagedEditMessage(Update update) throws IOException, URISyntaxException {
        String callData = update.getCallbackQuery().getData();
        Long chatId = update.getCallbackQuery().getMessage().getChatId();

        // FIXME (24.11.2018): correct buttons shifting
        Integer messageId = update.getCallbackQuery().getMessage().getMessageId();

        ChapterRequest chapterRequest = MAPPER.readValue(callData, ChapterRequest.class);
        String bookUrl = chapterRequest.getUrl();

        PagedResponse<ChapterResponse> chapters = requestChapters(bookUrl,
                                                                  chapterRequest.getPage(),
                                                                  chapterRequest.getPageSize());

        EditMessageText newMsg = new EditMessageText().setChatId(chatId)
                                                      .setMessageId(messageId)
                                                      .setText(getMessageTextMarkdown(bookUrl, chapters))
                                                      .setParseMode(ParseMode.MARKDOWN)
                                                      .setReplyMarkup(new InlineKeyboardMarkup()
                                                                              .setKeyboard(getChapterPagesKeyboard(
                                                                                      bookUrl,
                                                                                      chapters)));

        sender.send(newMsg);
    }

    private String getMessageTextMarkdown(String bookUrl, PagedResponse<ChapterResponse> chapters) throws URISyntaxException {
        String chaptersText = chapters.getContent()
                                      .stream()
                                      .map(chapter -> Util.getMarkdownUrl(chapter.getTitle(), chapter.getUrl()))
                                      .collect(Collectors.joining("\n"));

        BookMetadataResponse book = requestBook(bookUrl);
        return "*" + book.getFullTitle() + "*\n\n" + chaptersText;
    }

    private List<List<InlineKeyboardButton>> getChapterPagesKeyboard(String bookUrl, PagedResponse<ChapterResponse> chapters) {
        return Collections.singletonList(IntStream.rangeClosed(1, chapters.getTotalPages())
                                                  .mapToObj(page -> {
                                                      ChapterRequest cBack = new ChapterRequest();
                                                      cBack.setUrl(bookUrl);
                                                      cBack.setBid(null);
                                                      cBack.setPage(page);
                                                      cBack.setPageSize(pageSize);

                                                      try {
                                                          return new InlineKeyboardButton()
                                                                  .setText(String.valueOf(page))
                                                                  .setCallbackData(MAPPER.writeValueAsString("TODO"));
                                                      } catch (JsonProcessingException e) {
                                                          logger.warn(e.getLocalizedMessage(), e);
                                                          throw new IllegalStateException(e);
                                                      }
                                                  }).collect(Collectors.toList()));
    }

    private BookMetadataResponse requestBook(String bookUrl) throws URISyntaxException {
        String domainName = Util.getDomainName(bookUrl);
        String apiUrl = audioBookServerApiUrl + "/" + domainName + "/meta";
        logger.debug("request to api: '{}'", apiUrl);
        RestTemplate rest = new RestTemplate();
        ResponseEntity<BookMetadataResponse> responseEntity = rest.exchange(apiUrl,
                                                                            HttpMethod.POST,
                                                                            new HttpEntity<>(
                                                                                    Collections.singletonMap("url", bookUrl)),
                                                                            BookMetadataResponse.class);
        return Objects.requireNonNull((responseEntity).getBody());
    }

    private ChapterPagedResponseWrapper requestChapters(String bookUrl, int page, int pageSize) throws MalformedURLException, URISyntaxException {
        ChapterRequest request = ChapterRequest.of(bookUrl, null, page, pageSize);
        String domainName = Util.getDomainName(bookUrl);
        String apiUrl = audioBookServerApiUrl + "/" + domainName;
        logger.debug("request to api: '{}'", apiUrl);
        RestTemplate rest = new RestTemplate();
        ResponseEntity<ChapterPagedResponseWrapper> responseEntity = rest.exchange(apiUrl,
                                                                                   HttpMethod.POST,
                                                                                   new HttpEntity<>(request),
                                                                                   ChapterPagedResponseWrapper.class);
        return Objects.requireNonNull(responseEntity.getBody());
    }

    private void sendMessage(String text, Long chatId) {
        SendMessage msg = new SendMessage().setChatId(chatId)
                                           .setText(text);

        sender.send(msg);
    }

    private class MessageSender {
        @SuppressWarnings("UnstableApiUsage")
        private RateLimiter rateLimiter = RateLimiter.create(0.9); // bot rate limit ~1sec


        private MessageSender() {
        }

        void send(SendMessage message) {
            rateLimiter.acquire();
            try {
                execute(message);
            } catch (TelegramApiException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }

        void send(EditMessageText message) {
            rateLimiter.acquire();
            try {
                execute(message);
            } catch (TelegramApiException e) {
                logger.error(e.getLocalizedMessage(), e);
            }
        }

    }

}
