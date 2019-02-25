package com.iphayao.linebot.controller;

import com.iphayao.linebot.flex.*;
import com.iphayao.linebot.helper.RichMenuHelper;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.BeaconEvent;
import com.linecorp.bot.model.event.Event;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.Message;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.response.BotApiResponse;
import com.linecorp.bot.spring.boot.annotation.EventMapping;
import com.linecorp.bot.spring.boot.annotation.LineMessageHandler;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;


@LineMessageHandler
public class LineBotController {

    private Logger LOGGER = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private LineMessagingClient lineMessagingClient;


    @EventMapping //รับข้อความ
    public void handleTextMessage(MessageEvent<TextMessageContent> event) throws IOException {
        handleTextContent(event.getReplyToken(), event, event.getMessage());
        TextMessageContent message = event.getMessage();
    }

    @EventMapping
    public void handleBeaconEvent(BeaconEvent event) throws IOException {

        String replyToken = event.getReplyToken();
        String userId = event.getSource().getUserId();

        String pathImageFlex = new ClassPathResource("richmenu/richmenu-flexs.jpg").getFile().getAbsolutePath();
        String pathConfigFlex = new ClassPathResource("richmenu/richmenu-flexs.yml").getFile().getAbsolutePath();
        RichMenuHelper.createRichMenu(lineMessagingClient, pathConfigFlex, pathImageFlex, userId);
        this.reply(replyToken, new NewsFlexMessageSupplier().get());

        reply(replyToken, Arrays.asList(
                new TextMessage("Test โฆษณา")
        ));
    }

    private void handleTextContent(String replyToken, Event event, TextMessageContent content) throws IOException {
        String text = content.getText();
        String userId = event.getSource().getUserId();

        switch (text) {
            case "Flex": {
                String pathImageFlex = new ClassPathResource("richmenu/richmenu-flexs.jpg").getFile().getAbsolutePath();
                String pathConfigFlex = new ClassPathResource("richmenu/richmenu-flexs.yml").getFile().getAbsolutePath();
                RichMenuHelper.createRichMenu(lineMessagingClient, pathConfigFlex, pathImageFlex, userId);
                break;
            }
            case "Flex Back": {

                RichMenuHelper.deleteRichMenu(lineMessagingClient, userId);
                break;
            }

            case "Flex Restaurant": {
                this.reply(replyToken, new RestaurantFlexMessageSupplier().get());
                break;
            }
            case "Flex Menu": {
                this.reply(replyToken, new RestaurantMenuFlexMessageSupplier().get());
                break;
            }
            case "Flex Receipt": {
                this.reply(replyToken, new ReceiptFlexMessageSupplier().get());
                break;
            }
            case "Flex News": {
                this.reply(replyToken, new NewsFlexMessageSupplier().get());
                break;
            }
            case "Flex Ticket": {
                this.reply(replyToken, new TicketFlexMessageSupplier().get());
                break;
            }
            case "Flex Catalogue": {
                this.reply(replyToken, new CatalogueFlexMessageSupplier().get());
                break;
            }
            default:
        }
    }

    private void replyText(@NonNull String replyToken, @NonNull String message) {
        if (replyToken.isEmpty()) {
            throw new IllegalArgumentException("replyToken is not empty");
        }

        if (message.length() > 1000) {
            message = message.substring(0, 1000 - 1) + "...";
        }
        this.reply(replyToken, new TextMessage(message));
    }

    private void reply(@NonNull String replyToken, @NonNull Message message) {
        reply(replyToken, Collections.singletonList(message));
    }

    private void reply(@NonNull String replyToken, @NonNull List<Message> messages) {
        try {
            BotApiResponse response = lineMessagingClient.replyMessage(
                    new ReplyMessage(replyToken, messages)
            ).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
