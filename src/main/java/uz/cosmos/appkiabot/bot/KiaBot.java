package uz.cosmos.appkiabot.bot;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import uz.cosmos.appkiabot.entity.TelegramChat;
import uz.cosmos.appkiabot.entity.enums.TelegramChatStatus;
import uz.cosmos.appkiabot.repository.TelegramChatRepository;
import uz.cosmos.appkiabot.service.BotService;

@Component
public class KiaBot extends TelegramLongPollingBot {
    @Value("${bot.token}")
    private String botToken;

    @Value("${bot.username}")
    private String botUsername;

    @Autowired
    BotService botService;

    @Autowired
    TelegramChatRepository telegramChatRepository;

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }

    @Override
    public String getBotToken() {
        return this.botToken;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                String text = update.getMessage().getText();

                if (text.equals("/start")){
                    botService.welcomeText(update);
                }


            } else if (update.getMessage().hasContact()) {
                Contact contact = update.getMessage().getContact();
                Long chatId = update.getMessage().getChatId();
                TelegramChat byChatId = telegramChatRepository.findByChatId(chatId);

                if (byChatId.getStatus() == TelegramChatStatus.REGISTRATION) {
                    botService.sendConfirmation(byChatId, contact);
                }
            }
        } else if (update.hasCallbackQuery()) {
            String data = update.getCallbackQuery().getData();

            try {

                if (data.startsWith("language#")) {
                    execute(botService.deleteTopMessage(update));
                    botService.setLang(update, data.endsWith("uzbek") ? "uz": "ru");
                } else if (data.startsWith("location#")){
                    execute(botService.deleteTopMessage(update));
                    botService.sendLocation(update, data.endsWith("uz") ? "uz": "ru");
                } else if (data.startsWith("models#")){
                    execute(botService.deleteTopMessage(update));
                    botService.getModels(update, data.endsWith("uz") ? "uz": "ru");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
