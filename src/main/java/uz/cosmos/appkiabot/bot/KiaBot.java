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
import uz.cosmos.appkiabot.entity.AutoModification;
import uz.cosmos.appkiabot.entity.TelegramChat;
import uz.cosmos.appkiabot.entity.enums.TelegramChatStatus;
import uz.cosmos.appkiabot.repository.AutoModificationRepository;
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

    @Autowired
    AutoModificationRepository autoModificationRepository;


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

                if (text.equals("/start")) {
                    botService.welcomeText(update);
                } else {
                    TelegramChat chat = telegramChatRepository.findByChatId(update.getMessage().getChatId());
                    if (chat != null){
                        if (chat.getStatus().equals(TelegramChatStatus.SEND_INFO)){
                            if (text.equals(BotConstant.CANCELUZ) || text.equals(BotConstant.CANCELRU)){
                                botService.cancelSaveInfo(update, chat);
                            } else {
                                botService.saveInfo(update, chat);
                            }
                        }else if (chat.getStatus() == TelegramChatStatus.CREDIT_PERIOD ) {
                            if (!isNumeric(text) || Integer.parseInt(text) > 60 || Integer.parseInt(text) <= 0){
                                botService.addCreditSummError(update, false);
                            } else {
                                botService.creditCalculatorMonth(update, chat.getLanguage(), "", false, false);
                            }
                        }
                        else if (chat.getStatus() == TelegramChatStatus.CREDIT_FIRST ) {
                            AutoModification modification = autoModificationRepository.findByModificationId(chat.getSelectedModification());
                            if (!isNumeric(text) || ((double)Integer.parseInt(text) * 100 / Integer.parseInt(modification.getPrice())) < 30 || Integer.parseInt(text) > Integer.parseInt(modification.getPrice())){
                                botService.addCreditFirstError(update);
                            } else {
                                botService.creditCalculatorFirst(update, chat.getLanguage(), "", false);
                            }
                        }
                    }
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
                    botService.setLang(update, data.endsWith("uzbek") ? "uz" : "ru");
                } else if (data.startsWith("location#")) {
                    execute(botService.deleteTopMessage(update));
                    botService.sendLocation(update, data.endsWith("uz") ? "uz" : "ru");
                } else if (data.startsWith("models#")) {
                    execute(botService.deleteTopMessage(update));
                    botService.getModels(update, data.endsWith("uz") ? "uz" : "ru");
                } else if (data.startsWith("modelInfo#")) {
                    execute(botService.deleteTopMessage(update));
                    botService.getModifications(update, data.endsWith("uz") ? "uz" : "ru");
                } else if (data.startsWith("gomenu#")) {
                    execute(botService.deleteTopMessage(update));
                    botService.goMenu(update, data.endsWith("uz") ? "uz" : "ru");
                } else if (data.startsWith("automodificationinfo#")){
                    execute(botService.deleteTopMessage(update));
                    botService.autoModification(update, data.endsWith("uz") ? "uz" : "ru");
                } else if (data.startsWith("update#")) {
                    execute(botService.deleteTopMessage(update));
                    botService.updateData(update, (data.endsWith("uz") ? "uz" : "ru"));
                } else if (data.startsWith("sendapp#")) {
                    execute(botService.deleteTopMessage(update));
                    botService.sendApplication(update, (data.endsWith("uz") ? "uz" : "ru"));
                } else if (data.startsWith("downloadinfo#")){
                    execute(botService.deleteTopMessage(update));
                    botService.sendConfigurationInfo(update, (data.endsWith("uz") ? "uz" : "ru"));
                }else if (data.startsWith("creditmodifications#")){
                    execute(botService.deleteTopMessage(update));
                    botService.addCreditSumm(update, true, true);
                }else if (data.startsWith("avtokreditmonth#")) {
                    execute(botService.deleteTopMessage(update));
                    botService.creditCalculatorMonth(update, (data.endsWith("uz") ? "uz" : "ru"), data, true, false);
                }else if (data.startsWith("avtokreditfirst#")) {
                    execute(botService.deleteTopMessage(update));
                    botService.creditCalculatorFirst(update, (data.endsWith("uz") ? "uz" : "ru"), data, true);
                }else if (data.startsWith("avtokredittype#")) {
                    execute(botService.deleteTopMessage(update));
                    botService.sendType(update, (data.endsWith("uz") ? "uz" : "ru"), data);
                }else if (data.startsWith("avtocreditcalc#")){
                    execute(botService.deleteTopMessage(update));
                    botService.calcCredit(update, (data.endsWith("uz") ? "uz" : "ru"), data);
                } else if (data.startsWith("nasiyamodifications#")){
                    execute(botService.deleteTopMessage(update));
                    botService.nasiaInfo(update, (data.endsWith("uz") ? "uz" : "ru"));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

}
