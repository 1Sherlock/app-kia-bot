package uz.cosmos.appkiabot.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.cosmos.appkiabot.bot.BotConstant;
import uz.cosmos.appkiabot.bot.KiaBot;
import uz.cosmos.appkiabot.entity.TelegramChat;
import uz.cosmos.appkiabot.entity.enums.TelegramChatStatus;
import uz.cosmos.appkiabot.repository.TelegramChatRepository;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

@Service
public class BotService {
    @Autowired
    KiaBot kiaBot;

    @Autowired
    ButtonService buttonService;

    @Autowired
    TelegramChatRepository telegramChatRepository;

    public DeleteMessage deleteTopMessage(Update update) {
        DeleteMessage deleteMessage = new DeleteMessage();
        if (update.hasMessage()) {
            deleteMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
            deleteMessage.setMessageId(update.getMessage().getMessageId());
        } else if (update.hasCallbackQuery()) {
            deleteMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
            deleteMessage.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        }
        return deleteMessage;
    }

    public void welcomeText(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));
        sendMessage.setText(BotConstant.welcomeTextRu);
        sendMessage.setReplyMarkup(buttonService.languageButton());
        kiaBot.execute(sendMessage);
    }

    public void setLang(Update update, String language) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
        boolean isAdmin = update.getCallbackQuery().getMessage().getChatId().equals(273769261L);

        if (telegramChatRepository.existsByChatId(update.getCallbackQuery().getMessage().getChatId())) {
            TelegramChat byChatId = telegramChatRepository.findByChatId(update.getCallbackQuery().getMessage().getChatId());
            if (byChatId.getPhoneNumber() == null || byChatId.getPhoneNumber().length() == 0) {
                byChatId.setLanguage(language);
                byChatId.setStatus(TelegramChatStatus.REGISTRATION);
                telegramChatRepository.save(byChatId);
                sendMessage.setText(language.equals("ru") ? BotConstant.ENTERPHONENUMBERRU : BotConstant.ENTERPHONENUMBERUZ);
                sendMessage.setReplyMarkup(buttonService.enterNumber(language));
            } else {
                byChatId.setLanguage(language);
                telegramChatRepository.save(byChatId);
                sendMessage.setText((language.equals("ru") ? BotConstant.LANGUAGECHANGEDRU + BotConstant.MENUTEXTRU : BotConstant.LANGUAGECHANGEDUZ + BotConstant.MENUTEXTUZ));
                sendMessage.setReplyMarkup(buttonService.menuButton(language, isAdmin));
            }
        } else {
            TelegramChat telegramChat = new TelegramChat(update.getCallbackQuery().getMessage().getChatId(), language, TelegramChatStatus.REGISTRATION, "", "");
            telegramChatRepository.save(telegramChat);
            sendMessage.setText(language.equals("ru") ? BotConstant.ENTERPHONENUMBERRU : BotConstant.ENTERPHONENUMBERUZ);
            sendMessage.setReplyMarkup(buttonService.enterNumber(language));
        }

        kiaBot.execute(sendMessage);
    }

    public void sendConfirmation(TelegramChat byChatId, Contact contact) throws TelegramApiException {
        boolean isAdmin = byChatId.getChatId().equals(273769261L);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(byChatId.getChatId()));
        sendMessage.setText(byChatId.getLanguage().equals("ru") ? BotConstant.REGISTRATIONSUCCESSRU : BotConstant.REGISTRATIONSUCCESSUZ);
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove(true);
        sendMessage.setReplyMarkup(replyKeyboardRemove);

        byChatId.setPhoneNumber(contact.getPhoneNumber());
        byChatId.setFullName(contact.getFirstName() + " " + contact.getLastName());
        byChatId.setStatus(TelegramChatStatus.OTHER);

        telegramChatRepository.save(byChatId);
        kiaBot.execute(sendMessage);

        sendMessage.setText((byChatId.getLanguage().equals("ru") ? BotConstant.MENUTEXTRU : BotConstant.MENUTEXTUZ));
        sendMessage.setReplyMarkup(buttonService.menuButton(byChatId.getLanguage(), isAdmin));
        kiaBot.execute(sendMessage);
    }

    public void sendLocation(Update update, String lang) throws TelegramApiException {
        boolean isAdmin = update.getCallbackQuery().getMessage().getChatId().equals(273769261L);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
        sendMessage.setText(lang.equals("ru") ? BotConstant.LOCATIONTEXTRU : BotConstant.LOCATIONTEXTUZ);
        kiaBot.execute(sendMessage);
        SendLocation location = new SendLocation(String.valueOf(update.getCallbackQuery().getMessage().getChatId()),41.279389, 69.253818);
        location.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
        kiaBot.execute(location);

        sendMessage.setText((lang.equals("ru") ? BotConstant.MENUTEXTRU : BotConstant.MENUTEXTUZ));
        sendMessage.setReplyMarkup(buttonService.menuButton(lang, isAdmin));
        kiaBot.execute(sendMessage);
    }
}
