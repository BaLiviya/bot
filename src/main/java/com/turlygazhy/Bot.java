package com.turlygazhy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yerassyl_Turlygazhy on 11/24/2016.
 */
public class Bot extends TelegramLongPollingBot {
    private Map<Long, Conversation> conversations = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    public void onUpdateReceived(Update update) {
        Message updateMessage = update.getMessage();
        if (updateMessage == null) {
            updateMessage = update.getCallbackQuery().getMessage();
        }
        Long chatId = updateMessage.getChatId();
        if (chatId < 0) {
            return;
        }
        Conversation conversation = getConversation(update);
        try {
            conversation.handleUpdate(update, this);
        } catch (SQLException | TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private Conversation getConversation(Update update) {
        Message message = update.getMessage();
        if (message == null) {
            message = update.getCallbackQuery().getMessage();
        }
        Long chatId = message.getChatId();
        Conversation conversation = conversations.get(chatId);
        if (conversation == null) {
            logger.info("init new conversation for '{}'", chatId);
            conversation = new Conversation();
            conversations.put(chatId, conversation);
        }
        return conversation;
    }

    public String getBotUsername() {
        return "dev_zhake_bot";
    }

    public String getBotToken() {
//        return "302643839:AAEprqcQzVjV2lVSDFTR2ogKN_xRvA-E8QQ";//@Stgcfjkyfdthbot
        return "405718890:AAHr_1a5Fs6OAXYmdsByXBmiffkovU7sbVA";
//        return "292550349:AAFjomqw9L0X8eMftz0NPVxJ2wh48Vljx84";//bimov_assistant_bot
//        return "376290788:AAHSivLvfESxnoa0UPdV_QJ0IsAG-H3Tbc8";//@TDNbot
    }
}
