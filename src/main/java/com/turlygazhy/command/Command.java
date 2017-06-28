package com.turlygazhy.command;

import com.turlygazhy.Bot;
import com.turlygazhy.dao.DaoFactory;
import com.turlygazhy.dao.GoalDao;
import com.turlygazhy.dao.ScriptExecutor;
import com.turlygazhy.dao.impl.*;
import com.turlygazhy.entity.Message;
import com.turlygazhy.entity.Task;
import com.turlygazhy.entity.User;
import com.turlygazhy.entity.WaitingType;
import org.telegram.telegrambots.api.methods.ParseMode;
import org.telegram.telegrambots.api.methods.send.SendContact;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.methods.send.SendPhoto;
import org.telegram.telegrambots.api.methods.send.SendVoice;
import org.telegram.telegrambots.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.api.objects.CallbackQuery;
import org.telegram.telegrambots.api.objects.Contact;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Yerassyl_Turlygazhy on 11/27/2016.
 */
public abstract class Command {
    protected long id;
    protected long messageId;

    protected DaoFactory factory = DaoFactory.getFactory();
    protected UserDao userDao = factory.getUserDao();
    protected MessageDao messageDao = factory.getMessageDao();
    protected KeyboardMarkUpDao keyboardMarkUpDao = factory.getKeyboardMarkUpDao();
    protected ButtonDao buttonDao = factory.getButtonDao();
    protected CommandDao commandDao = factory.getCommandDao();
    protected ConstDao constDao = factory.getConstDao();
    protected MemberDao memberDao = factory.getMemberDao();
    protected KeyWordDao keyWordDao = factory.getKeyWordDao();
    protected ReservationDao reservationDao = factory.getReservationDao();
    protected GroupDao groupDao = factory.getGroupDao();
    protected GoalDao goalDao = factory.getGoalDao();
    protected ThesisDao thesisDao = factory.getThesisDao();
    protected SavedResultsDao savedResultsDao = factory.getSavedResultsDao();
    protected ScriptExecutor scriptExecutor = factory.getScriptExecutor();
    protected TaskDao taskDao = factory.getTaskDao();

    protected WaitingType waitingType;
    protected org.telegram.telegrambots.api.objects.Message updateMessage;
    protected String updateMessageText;
    protected Long chatId;
    private Bot bot;
    protected String editText = messageDao.getMessageText(120);

    public ReplyKeyboard getAfterRejectedKeyboard(int taskId) throws SQLException {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        InlineKeyboardButton closeButton = new InlineKeyboardButton();
        String closeText = messageDao.getMessageText(119);
        closeButton.setText(closeText);
        closeButton.setCallbackData(closeText + taskId);

        InlineKeyboardButton editButton = new InlineKeyboardButton();
        String editText = messageDao.getMessageText(120);
        editButton.setText(editText);
        editButton.setCallbackData(editText + taskId);

        InlineKeyboardButton changeExecutorButton = new InlineKeyboardButton();
        String changeExecutorText = messageDao.getMessageText(121);
        changeExecutorButton.setText(changeExecutorText);
        changeExecutorButton.setCallbackData(changeExecutorText + taskId);

        row.add(closeButton);
        row.add(editButton);
        row2.add(changeExecutorButton);
        rows.add(row);
        rows.add(row2);

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public void informExecutor(Task task) throws SQLException, TelegramApiException { //передача задания
        StringBuilder sb = new StringBuilder();
        sendMessage(80, task.getUserId(), bot);
        if (task.isHasAudio()) {
            bot.sendVoice(new SendVoice()
                    .setVoice(task.getVoiceMessageId())
                    .setChatId(task.getUserId()));
        } else {
            sb.append("<b>").append(messageDao.getMessageText(96)).append("</b>").append(task.getText()).append("\n");
        }
        sb.append("<b>").append(messageDao.getMessageText(98)).append("</b>").append(task.getDeadline()).append("\n");
        User name = userDao.getUserByChatId(chatId);// Добавил запись чтобы -Отправитель заданий Жайык
        String nameAdmin = name.getName();           // Добавил запись чтобы -Отправитель заданий Жайык
        sb.append("<b>").append(messageDao.getMessageText(406)).append("</b>").append(nameAdmin).append("\n");// Добавил запись чтобы -Отправитель заданий Жайык
        sb.append("<b>").append(messageDao.getMessageText(407)).append("</b>").append(task.getId());// Добавил запись чтобы -Отправитель заданий Жайык
        bot.sendMessage(new SendMessage()
                .setChatId(task.getUserId())
                .setText(sb.toString())
                .setReplyMarkup(getTaskKeyboard(task))
                .setParseMode(ParseMode.HTML));
    }

    public InlineKeyboardMarkup getTaskKeyboard(Task task) throws SQLException {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(new InlineKeyboardButton()
                .setText(buttonDao.getButtonText(65))   // Accept
                .setCallbackData(buttonDao.getButtonText(65)+ " " + task.getId()));

        row.add(new InlineKeyboardButton()
                .setText(buttonDao.getButtonText(66))   // Reject
                .setCallbackData(buttonDao.getButtonText(66) + " " + task.getId()));

        rows.add(row);
        keyboard.setKeyboard(rows);

        return keyboard;
    }

    protected ThanksDao thanksDao = factory.getThanksDao();

    protected Command() throws SQLException {
    }

    public InlineKeyboardMarkup getDeadlineKeyboard(int shownDates) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        Date date = new Date();
        date.setDate(date.getDate() + (shownDates * 9));
        List<InlineKeyboardButton> row = null;
        for (int i = 1; i < 10; i++) {
            if (row == null) {
                row = new ArrayList<>();
            }
            InlineKeyboardButton button = new InlineKeyboardButton();
            int dateToString = date.getDate();
            String stringDate;
            if (dateToString > 9) {
                stringDate = String.valueOf(dateToString);
            } else {
                stringDate = "0" + dateToString;
            }
            int monthToString = date.getMonth() + 1;
            String stringMonth;
            if (monthToString > 9) {
                stringMonth = String.valueOf(monthToString);
            } else {
                stringMonth = "0" + monthToString;
            }
            String dateText = stringDate + "." + stringMonth;
            button.setText(dateText);
            button.setCallbackData(dateText);
            row.add(button);
            if (i % 3 == 0) {
                rows.add(row);
                row = null;
            }
            date.setDate(date.getDate() + 1);
        }

        if (shownDates > 0) {
            rows.add(getNextPrevRows(true, true));
        } else {
            rows.add(getNextPrevRows(false, true));
        }


        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public ReplyKeyboard getInlineButton(String text) {
        return getInlineButton(text, text);
    }

    public ReplyKeyboard getInlineButton(String text, String callbackData) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();

        button.setText(text);
        button.setCallbackData(callbackData);

        row.add(button);
        rows.add(row);
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public void initMessage(Update update, Bot bot) throws TelegramApiException, SQLException {
        this.bot = bot;
        updateMessage = update.getMessage();
        if (updateMessage == null) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            updateMessage = callbackQuery.getMessage();
            updateMessageText = callbackQuery.getData();
            String waitText = messageDao.getMessageText(88);
            if (chatId == null) {
                chatId = updateMessage.getChatId();
            }
            try {
                bot.editMessageText(new EditMessageText()
                        .setText(waitText)
                        .setChatId(chatId)
                        .setMessageId(updateMessage.getMessageId())
                );
            } catch (Exception ignored) {
            }
        } else {
            updateMessageText = updateMessage.getText();
            if (chatId == null) {
                chatId = updateMessage.getChatId();
            }
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return is command finished
     */
    public abstract boolean execute(Update update, Bot bot) throws SQLException, TelegramApiException;

    public void setMessageId(long messageId) {
        this.messageId = messageId;
    }

    public long getMessageId() {
        return messageId;
    }

    public void sendMessage(long messageId, long chatId, TelegramLongPollingBot bot) throws SQLException, TelegramApiException {
        sendMessage(messageId, chatId, bot, null);
    }

    public void sendMessage(String text, long chatId, TelegramLongPollingBot bot) throws SQLException, TelegramApiException {
        sendMessage(text, chatId, bot, null);
    }

    public void sendMessage(int messageId) throws SQLException, TelegramApiException {
        sendMessage(messageId, chatId, bot, null);
    }

    public void sendMessage(long messageId, long chatId, TelegramLongPollingBot bot, Contact contact) throws SQLException, TelegramApiException {
        Message message = messageDao.getMessage(messageId);
        SendMessage sendMessage = message.getSendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(keyboardMarkUpDao.select(message.getKeyboardMarkUpId()));
        bot.sendMessage(sendMessage);
        if (contact != null) {
            bot.sendContact(new SendContact()
                    .setChatId(chatId)
                    .setFirstName(contact.getFirstName())
                    .setLastName(contact.getLastName())
                    .setPhoneNumber(contact.getPhoneNumber())
            );
        }
    }

    public void sendMessage(String text, ReplyKeyboard keyboard) throws TelegramApiException {
        bot.sendMessage(new SendMessage()
                .setChatId(chatId)
                .setText(text)
                .setReplyMarkup(keyboard)
                .setParseMode(ParseMode.HTML)
        );
    }

    public ReplyKeyboard getWorkersKeyboard(List<User> users) {
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (User user : users) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(user.getName());
            button.setCallbackData("/id" + user.getId());
            row.add(button);
            rows.add(row);
        }

        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public void sendMessage(long messageId, int keyboardMarkUpId) throws SQLException, TelegramApiException {
        sendMessage(messageId, keyboardMarkUpDao.select(keyboardMarkUpId));
    }

    public void sendMessage(long messageId, ReplyKeyboard keyboard) throws SQLException, TelegramApiException {
        bot.sendMessage(new SendMessage()
                .setChatId(chatId)
                .setText(messageDao.getMessageText(messageId))
                .setReplyMarkup(keyboard)
                .setParseMode(ParseMode.HTML)
        );
    }

    public void sendMessage(String text, long chatId, TelegramLongPollingBot bot, Contact contact) throws SQLException, TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(text);
        sendMessage.setParseMode(ParseMode.HTML);
        bot.sendMessage(sendMessage);
        if (contact != null) {
            bot.sendContact(new SendContact()
                    .setChatId(chatId)
                    .setFirstName(contact.getFirstName())
                    .setLastName(contact.getLastName())
                    .setPhoneNumber(contact.getPhoneNumber())
            );
        }
    }

    public void sendMessage(String text) throws SQLException, TelegramApiException {
        sendMessage(text, chatId, bot);
    }

    public void sendMessageToAdmin(long messageId, TelegramLongPollingBot bot) throws SQLException, TelegramApiException {
        long adminChatId = getAdminChatId();
        sendMessage(messageId, adminChatId, bot);
    }

    public long getAdminChatId() {
        return userDao.getAdminChatId();
    }

    public void sendMessageToAdmin(long messageId, Bot bot, Contact contact) throws SQLException, TelegramApiException {
        long adminChatId = getAdminChatId();
        sendMessage(messageId, adminChatId, bot, contact);
    }

    public void sendMessageToAdmin(String text, TelegramLongPollingBot bot) throws SQLException, TelegramApiException {
        long adminChatId = getAdminChatId();
        sendMessage(text, adminChatId, bot);
    }

    public void sendPhotoToAdmin(String photo, Bot bot) throws TelegramApiException {
        long adminChatId = getAdminChatId();
        bot.sendPhoto(new SendPhoto()
                .setChatId(adminChatId)
                .setPhoto(photo)
        );
    }

    public boolean validateTime(String theTime) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm"); //HH = 24h format
        dateFormat.setLenient(false); //this will not enable 25:67 for example
        try {
            dateFormat.parse(theTime);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }

    protected String prevText = messageDao.getMessageText(89);
    protected String nextText = messageDao.getMessageText(90);

    protected List<InlineKeyboardButton> getNextPrevRows(boolean prev, boolean next) {
        List<InlineKeyboardButton> row = new ArrayList<>();

        if (prev) {
            InlineKeyboardButton prevButton = new InlineKeyboardButton();
            prevButton.setText(prevText);
            prevButton.setCallbackData(prevText);
            row.add(prevButton);
        }
        if (next) {
            InlineKeyboardButton nextButton = new InlineKeyboardButton();
            nextButton.setText(nextText);
            nextButton.setCallbackData(nextText);
            row.add(nextButton);
        }

        return row;
    }
}
