package uz.cosmos.appkiabot.service;

import org.apache.commons.io.FileUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import uz.cosmos.appkiabot.bot.BotConstant;
import uz.cosmos.appkiabot.bot.KiaBot;
import uz.cosmos.appkiabot.entity.AutoModel;
import uz.cosmos.appkiabot.entity.AutoModification;
import uz.cosmos.appkiabot.entity.TelegramChat;
import uz.cosmos.appkiabot.entity.enums.TelegramChatStatus;
import uz.cosmos.appkiabot.payload.ResKia;
import uz.cosmos.appkiabot.payload.ResKiaModel;
import uz.cosmos.appkiabot.payload.ResKiaModelInfo;
import uz.cosmos.appkiabot.payload.ResKiaModification;
import uz.cosmos.appkiabot.repository.AutoModelRepository;
import uz.cosmos.appkiabot.repository.AutoModificationRepository;
import uz.cosmos.appkiabot.repository.TelegramChatRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.text.NumberFormat;
import java.time.YearMonth;
import java.util.*;

@Service
public class BotService {
    @Autowired
    KiaBot kiaBot;

    @Autowired
    ButtonService buttonService;

    @Autowired
    TelegramChatRepository telegramChatRepository;

    @Autowired
    RequestService requestService;

    @Autowired
    AutoModelRepository autoModelRepository;

    @Autowired
    AutoModificationRepository autoModificationRepository;

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
                byChatId.setStatus(TelegramChatStatus.OTHER);
                telegramChatRepository.save(byChatId);
                sendMessage.setText((language.equals("ru") ? BotConstant.LANGUAGECHANGEDRU + BotConstant.MENUTEXTRU : BotConstant.LANGUAGECHANGEDUZ + BotConstant.MENUTEXTUZ));
                sendMessage.setReplyMarkup(buttonService.menuButton(language, isAdmin));
            }
        } else {
            TelegramChat telegramChat = new TelegramChat(update.getCallbackQuery().getMessage().getChatId(), language, TelegramChatStatus.REGISTRATION, "", "", "", "", "");
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
        SendLocation location = new SendLocation(String.valueOf(update.getCallbackQuery().getMessage().getChatId()), 38.810577, 65.812521);
        location.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
        kiaBot.execute(location);

        sendMessage.setText((lang.equals("ru") ? BotConstant.MENUTEXTRU : BotConstant.MENUTEXTUZ));
        sendMessage.setReplyMarkup(buttonService.menuButton(lang, isAdmin));
        kiaBot.execute(sendMessage);
    }

    public void getModels(Update update, String lang) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
        sendMessage.setText(lang.equals("ru") ? BotConstant.CHOOSEMODELRU : BotConstant.CHOOSEMODELUZ);
//        ResKia[] groups = requestService.getModels().getTypes();
        List<AutoModel> autoModels = autoModelRepository.findAll();

        sendMessage.setReplyMarkup(buttonService.sendModels(autoModels, lang));
        kiaBot.execute(sendMessage);
    }

    public void getModifications(Update update, String lang) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, TelegramApiException, IOException {
        String data = update.getCallbackQuery().getData();
        String[] split = data.split("#");
        String modelId = split[1];
//        String modelImage = split[2];

//        ResKiaModelInfo modelInfo = requestService.getModelInfo(modelUrl);
        List<AutoModification> autoModifications = autoModificationRepository.findAllByModel_ModelId(modelId);

        AutoModel model = autoModelRepository.findByModelId(modelId);

        if (model.getPhoto_sha().length() > 0) {
            SendPhoto sendPhoto = new SendPhoto();

            sendPhoto.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));

            byte[] image = requestService.getImage(model.getPhoto_sha());
            Files.write(Paths.get("car_image.jpg"), image);
            File file = new File("car_image.jpg");
            FileUtils.writeByteArrayToFile(file, image);
            sendPhoto.setPhoto(new InputFile(file));

            kiaBot.execute(sendPhoto);
        }

        StringBuilder modifications = new StringBuilder();
        for (AutoModification modification : autoModifications) {
            modifications.append(lang.equals("ru") ?
                    BotConstant.NAMERU + "<b>" + modification.getName() + "</b>" + "\n" +
                            BotConstant.PRICERU + "<b>" + getBeautifulNumber(new BigDecimal(modification.getPrice()).setScale(2, RoundingMode.HALF_UP), lang) + "</b>" + "\n" :
                    BotConstant.NAMEUZ + "<b>" + modification.getName() + "</b>" + "\n" +
                            BotConstant.PRICEUZ + "<b>" + getBeautifulNumber(new BigDecimal(modification.getPrice()).setScale(2, RoundingMode.HALF_UP), lang) + "</b>" + "\n").append("\n");
        }
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
        sendMessage.setParseMode("html");

        sendMessage.setText(lang.equals("ru") ?
                BotConstant.NAMERU + "<b>" + model.getName() + "</b>" + "\n\n" +
                        "<b>" + BotConstant.MODIFICATIONSRU + "</b>\n\n" + modifications + "" :
                BotConstant.NAMEUZ + "<b>" + model.getName() + "</b>" + "\n" + "\n" +
                        "<b>" + BotConstant.MODIFICATIONSUZ + "</b>\n\n" + modifications + "");

        sendMessage.setReplyMarkup(buttonService.autoModelGoBack(lang, autoModifications));
        kiaBot.execute(sendMessage);
    }

    public void goMenu(Update update, String language) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
        sendMessage.setText((language.equals("ru") ? BotConstant.MENUTEXTRU : BotConstant.MENUTEXTUZ));
        boolean isAdmin = update.getCallbackQuery().getMessage().getChatId().equals(273769261L);
        sendMessage.setReplyMarkup(buttonService.menuButton(language, isAdmin));
        kiaBot.execute(sendMessage);
    }

    public String getBeautifulNumber(BigDecimal number, String language) {
        Locale locale = new Locale("uz", "UZ");
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
        String format = numberFormat.format(number);

        return language.equals("ru") ? format.replace("soʻm", "сум") : format;
    }

    public String getBeautifulNumber(Integer number) {
        Locale locale = new Locale("uz", "UZ");
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);
        return numberFormat.format(number);
    }

    public void autoModification(Update update, String language) throws TelegramApiException {
        String data = update.getCallbackQuery().getData();

        String[] split = data.split("#");
        String modificationId = split[1];

        AutoModification modification = autoModificationRepository.findByModificationId(modificationId);
        AutoModel model = autoModelRepository.findByModelId(modification.getModel().getModelId());

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
        sendMessage.setParseMode("html");
        sendMessage.setText(
                language.equals("ru") ? BotConstant.ABOUTMODIFICATIONRU + "\n\n" +
                        BotConstant.NAMERU + "<b>" + modification.getName() + "</b>" + "\n" +
                        BotConstant.PRICERU + "<b>" + getBeautifulNumber(new BigDecimal(modification.getPrice()).setScale(2, RoundingMode.HALF_UP), language) + "</b>" + "\n"
                        :
                        BotConstant.ABOUTMODIFICATIONUZ + "\n\n" + BotConstant.NAMEUZ + "<b>" + modification.getName() + "</b>" + "\n" +
                                BotConstant.PRICEUZ + "<b>" + getBeautifulNumber(new BigDecimal(modification.getPrice()).setScale(2, RoundingMode.HALF_UP), language) + "</b>" + "\n"
        );
        sendMessage.setReplyMarkup(buttonService.autoModifications(language, modification, model.getModelId()));
        kiaBot.execute(sendMessage);
    }

    public void updateData(Update update, String language) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, TelegramApiException {
        autoModificationRepository.deleteAll();
        autoModelRepository.deleteAll();

//        List<TelegramChat> users = telegramChatRepository.findAll();
//        for (TelegramChat user : users) {
//            if (user.getStatus().equals(TelegramChatStatus.SEND_MESSAGE))
//                user.setStatus(TelegramChatStatus.OTHER);
//
//            telegramChatRepository.save(user);
//        }
//        bankBinRepository.deleteAll();

//        for (String[] uzcardbin : BotConstant.UZCARDBINS) {
//            bankBinRepository.save(new BankBin(uzcardbin[0], uzcardbin[1]));
//        }

//        for (String[] humobin : BotConstant.HUMOBINS) {
//            bankBinRepository.save(new BankBin(humobin[0], humobin[1]));
//        }

//        List<Card> cards = cardRepository.findAll();
//
//        for (Card card : cards) {
//            List<BankBin> bankBins = bankBinRepository.findAllByBeanStartsWith(card.getPan().substring(0, 6));
//            if (bankBins.size() > 0){
//                card.setBankName(bankBins.get(0).getBankName());
//                cardRepository.save(card);
//            }
//        }


//        kia
        ResKia[] modelsinfo = requestService.getModels().getTypes();
        for (ResKia model : modelsinfo) {
            if (!Objects.equals(model.getName(), "Скоро")) {
                List<ResKiaModel> models1 = model.getModels();
                for (ResKiaModel resKiaModel : models1) {


                    ResKiaModelInfo modelInfo = requestService.getModelInfo(resKiaModel.getUrl());
                    if (resKiaModel.getSoon() == 0 && resKiaModel.getTesting() == 0) {
                        AutoModel save = autoModelRepository.save(new AutoModel(resKiaModel.getName(), resKiaModel.getName(), resKiaModel.getImage(), resKiaModel.getMinPrice() != null ? Integer.parseInt(resKiaModel.getMinPrice()) : 0, modelInfo.getPdf()));

                        for (ResKiaModification compl : modelInfo.getCompls()) {

                            StringBuilder content = new StringBuilder();
                            for (String option : compl.getOptions()) {
                                content.append(option).append("\n");
                            }
                            String price = compl.getPrice();

                            autoModificationRepository.save(new AutoModification(compl.getName() + resKiaModel.getName(), compl.getName(), price, content.toString(), save));
                        }
                    }
                }
            }
        }

        SendMessage sendMessage = new SendMessage(String.valueOf(update.getCallbackQuery().getMessage().getChatId()), "Updated");
        boolean isAdmin = update.getCallbackQuery().getMessage().getChatId().equals(273769261L);
        sendMessage.setReplyMarkup(buttonService.menuButton(language, isAdmin));
        kiaBot.execute(sendMessage);
    }

    public void sendApplication(Update update, String lang) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
        sendMessage.setText(lang.equals("ru") ? BotConstant.SENDFIORU : BotConstant.SENDFIOUZ);
        sendMessage.setReplyMarkup(buttonService.enterFIO(lang));
        TelegramChat chat = telegramChatRepository.findByChatId(update.getCallbackQuery().getMessage().getChatId());
        chat.setStatus(TelegramChatStatus.SEND_INFO);
        telegramChatRepository.save(chat);
        kiaBot.execute(sendMessage);
    }

    public void saveInfo(Update update, TelegramChat chat) throws TelegramApiException {
        boolean isAdmin = chat.getChatId().equals(273769261L);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chat.getChatId()));
        sendMessage.setText(chat.getLanguage().equals("uz") ? BotConstant.SUCCESSAPPUZ : BotConstant.SUCCESSAPPRU);
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove(true);
        sendMessage.setReplyMarkup(replyKeyboardRemove);
        kiaBot.execute(sendMessage);
        sendMessage.setText(chat.getLanguage().equals("ru") ? BotConstant.MENUTEXTRU : BotConstant.MENUTEXTUZ);
        sendMessage.setReplyMarkup(buttonService.menuButton(chat.getLanguage(), isAdmin));
        kiaBot.execute(sendMessage);
    }

    public void cancelSaveInfo(Update update, TelegramChat chat) throws TelegramApiException {
        boolean isAdmin = chat.getChatId().equals(273769261L);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chat.getChatId()));
        sendMessage.setText(chat.getLanguage().equals("uz") ? BotConstant.CANCELLEDUZ : BotConstant.CANCELLEDRU);
        ReplyKeyboardRemove replyKeyboardRemove = new ReplyKeyboardRemove(true);
        sendMessage.setReplyMarkup(replyKeyboardRemove);
        kiaBot.execute(sendMessage);

        chat.setStatus(TelegramChatStatus.OTHER);
        telegramChatRepository.save(chat);

        sendMessage.setText(chat.getLanguage().equals("ru") ? BotConstant.MENUTEXTRU : BotConstant.MENUTEXTUZ);
        sendMessage.setReplyMarkup(buttonService.menuButton(chat.getLanguage(), isAdmin));
        kiaBot.execute(sendMessage);
    }

    public void sendConfigurationInfo(Update update, String lang) throws TelegramApiException {
        String data = update.getCallbackQuery().getData();

        String[] split = data.split("#");
        String modificationId = split[1];

        AutoModification modification = autoModificationRepository.findByModificationId(modificationId);

        SendDocument sendDocument = new SendDocument();
        sendDocument.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
        sendDocument.setDocument(new InputFile(modification.getModel().getPdf()));
        kiaBot.execute(sendDocument);
        autoModification(update, lang);
    }

    public void addCreditSumm(Update update, Boolean isCallback, Boolean isFromAuto) throws TelegramApiException {
        Locale locale = new Locale("uz", "UZ");
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(locale);

        TelegramChat chat = telegramChatRepository.findByChatId(isCallback ? update.getCallbackQuery().getMessage().getChatId() : update.getMessage().getChatId());

        String type = "";

        if (isCallback) {
            String[] split = update.getCallbackQuery().getData().split("#");
            type = split[1];
        } else {
            type = chat.getSelectedModification();
        }


        AutoModification modification = autoModificationRepository.findByModificationId(type);

        String kreditSumm = modification.getPrice();


        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(String.valueOf(chat.getChatId())));
        sendMessage.setText(chat.getLanguage().equals("ru") ? BotConstant.KREDITSUMMINFORU + getBeautifulNumber(new BigDecimal(kreditSumm).setScale(2, RoundingMode.HALF_UP), chat.getLanguage()) + "\n\n" + BotConstant.KREDITPERIODRU : BotConstant.KREDITSUMMINFOUZ + getBeautifulNumber(new BigDecimal(kreditSumm).setScale(2, RoundingMode.HALF_UP), chat.getLanguage()) + "\n\n" + BotConstant.KREDITPERIODUZ);
        sendMessage.setReplyMarkup(buttonService.creditPeriods(chat.getLanguage(), type, modification));

        chat.setStatus(TelegramChatStatus.CREDIT_PERIOD);
        chat.setSelectedModification(type);
        telegramChatRepository.save(chat);

        kiaBot.execute(sendMessage);
    }

    public void creditCalculatorMonth(Update update, String language, String data, Boolean isCallback, Boolean isFromError) throws TelegramApiException {
        TelegramChat chat = telegramChatRepository.findByChatId(isCallback ? update.getCallbackQuery().getMessage().getChatId() : update.getMessage().getChatId());

        String modificationId = "";
        String kreditMonth = "";

        if (isCallback) {
            String[] split = data.split("#");
            modificationId = split[1];
            kreditMonth = split[2];
        } else {
            modificationId = chat.getSelectedModification();
            kreditMonth = update.getMessage().getText();
        }

        if (isFromError) {
            kreditMonth = chat.getSelectedMonth();
        }

        AutoModification modification = autoModificationRepository.findByModificationId(modificationId);
        String kreditSumm = modification.getPrice();

        chat.setSelectedMonth(kreditMonth);
        chat.setStatus(TelegramChatStatus.CREDIT_FIRST);
        telegramChatRepository.save(chat);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setParseMode("html");
        sendMessage.setChatId(String.valueOf(chat.getChatId()));
        sendMessage.setText(language.equals("ru") ? BotConstant.KREDITSUMMINFORU + getBeautifulNumber(new BigDecimal(kreditSumm).setScale(2, RoundingMode.HALF_UP), language) + "\n" +
                BotConstant.KREDITPERIODINFORU + kreditMonth + " " + BotConstant.MONTHRU + "\n\n" +
                (kreditMonth.equals("12") ? BotConstant.KREDITFIRSTSUMMRU50 : BotConstant.KREDITFIRSTSUMMRU)

                : BotConstant.KREDITSUMMINFOUZ + getBeautifulNumber(new BigDecimal(kreditSumm).setScale(2, RoundingMode.HALF_UP), language) + "\n" +
                BotConstant.KREDITPERIODINFOUZ + kreditMonth + " " + BotConstant.MONTHUZ + "\n\n" +
                (kreditMonth.equals("12") ? BotConstant.KREDITFIRSTSUMMUZ50 : BotConstant.KREDITFIRSTSUMMUZ)
        );
        sendMessage.setReplyMarkup(buttonService.creditFirsts(language, modificationId, kreditMonth, modification));

        kiaBot.execute(sendMessage);
    }

    public void creditCalculatorFirst(Update update, String language, String data, Boolean isCallBack) throws TelegramApiException {
        String modificationId = "";
        String kreditMonth = "";
        String kreditFirst = "";

        TelegramChat chat = telegramChatRepository.findByChatId(isCallBack ? update.getCallbackQuery().getMessage().getChatId() : update.getMessage().getChatId());

        if (isCallBack) {
            String[] split = data.split("#");
            modificationId = split[1];
            kreditMonth = split[2];
            kreditFirst = split[3];
        } else {
            modificationId = chat.getSelectedModification();
            kreditMonth = chat.getSelectedMonth();
            kreditFirst = update.getMessage().getText();
        }

        AutoModification modification = autoModificationRepository.findByModificationId(modificationId);

        chat.setStatus(TelegramChatStatus.OTHER);

        String kreditSumm = modification.getPrice();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(chat.getChatId()));
        sendMessage.setText(language.equals("ru") ? BotConstant.KREDITSUMMINFORU + getBeautifulNumber(new BigDecimal(kreditSumm).setScale(2, RoundingMode.HALF_UP), language) + "\n" +
                BotConstant.KREDITPERIODINFORU + kreditMonth + " " + BotConstant.MONTHRU + "\n" +
                BotConstant.KREDITFIRSTSUMMINFORU + (Integer.parseInt(kreditFirst) > 100 ? getBeautifulNumber(Integer.parseInt(kreditFirst)) + "(~" + (BigDecimal.valueOf((double) Integer.parseInt(kreditFirst) * 100 / Integer.parseInt(modification.getPrice())).setScale(0, RoundingMode.HALF_UP)) + "%)" : kreditFirst + "%") + "\n\n" +
                BotConstant.KREDITTYPERU

                : BotConstant.KREDITSUMMINFOUZ + getBeautifulNumber(new BigDecimal(kreditSumm).setScale(2, RoundingMode.HALF_UP), language) + "\n" +
                BotConstant.KREDITPERIODINFOUZ + kreditMonth + " " + BotConstant.MONTHUZ + "\n" +
                BotConstant.KREDITFIRSTSUMMINFOUZ + (Integer.parseInt(kreditFirst) > 100 ? getBeautifulNumber(Integer.parseInt(kreditFirst)) + "(~" + (BigDecimal.valueOf((double) Integer.parseInt(kreditFirst) * 100 / Integer.parseInt(modification.getPrice())).setScale(0, RoundingMode.HALF_UP)) + "%)" : kreditFirst + "%") + "\n\n" +
                BotConstant.KREDITTYPEUZ
        );
        sendMessage.setReplyMarkup(buttonService.creditType(language, modificationId, kreditMonth, kreditFirst, modification));
        kiaBot.execute(sendMessage);
    }

    public void sendType(Update update, String language, String data) throws TelegramApiException {
        String[] split = data.split("#");
        String modificationId = split[1];
        String kreditMonth = split[2];
        String kreditFirst = split[3];
        String kreditType = split[4];

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
        sendMessage.setText(language.equals("ru") ? BotConstant.PAYMENTCREDITTYPERU : BotConstant.PAYMENTCREDITTYPEUZ);
        sendMessage.setReplyMarkup(buttonService.creditPeriodsType(language, modificationId, kreditMonth, kreditFirst, kreditType));
        kiaBot.execute(sendMessage);
    }

    public void calcCredit(Update update, String language, String data) throws TelegramApiException {

        String[] split = data.split("#");
        String modificationId = split[1];
        int kreditMonth = Integer.parseInt(split[2]);
        int kreditFirst = Integer.parseInt(split[3]);
        String creditFormat = split[5];

        AutoModification modification = autoModificationRepository.findByModificationId(modificationId);

        int kreditSumm = Integer.parseInt(modification.getPrice());

        String kreditType = split[4];
        double yearPercent = 0;
        double firstPayment = 0;
        double getKreditSumm = 0;
        double kreditPayment = 0;
        int per = 100;
        double kreditFirstPercent;

        if (kreditFirst > 100) {
            BigDecimal bigDecimal = BigDecimal.valueOf((double) kreditFirst * 100 / Integer.parseInt(modification.getPrice())).setScale(0, RoundingMode.HALF_UP);
            kreditFirstPercent = bigDecimal.doubleValue();
        } else {
            kreditFirstPercent = kreditFirst;
        }

        Date date = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);

        StringBuilder content = new StringBuilder();

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Info");


        if (kreditType.equals("ann")) {
            if (kreditMonth == 12) {

                yearPercent = 0;
            } else if (kreditMonth <= 24) {
                if (kreditFirstPercent >= 30 && kreditFirstPercent < 40) {
                    yearPercent = 17;
                } else if (kreditFirstPercent >= 40 && kreditFirstPercent < 50) {
                    yearPercent = 15;
                } else {
                    yearPercent = 12;
                }
            } else if (kreditMonth <= 36) {
                if (kreditFirstPercent >= 30 && kreditFirstPercent < 40) {
                    yearPercent = 19;
                } else if (kreditFirstPercent >= 40 && kreditFirstPercent < 50) {
                    yearPercent = 17;
                } else {
                    yearPercent = 15;
                }
            } else if (kreditMonth <= 48) {
                if (kreditFirstPercent >= 30 && kreditFirstPercent < 40) {
                    yearPercent = 21;
                } else if (kreditFirstPercent >= 40 && kreditFirstPercent < 50) {
                    yearPercent = 20;
                } else {
                    yearPercent = 18;
                }
            } else {
                if (kreditFirstPercent >= 30 && kreditFirstPercent < 40) {
                    yearPercent = 23;
                } else if (kreditFirstPercent >= 40 && kreditFirstPercent < 50) {
                    yearPercent = 22;
                } else {
                    yearPercent = 20;
                }
            }

            if (kreditFirst > 100) {
                firstPayment = kreditFirst;
            } else {
                firstPayment = (double) kreditSumm * (double) kreditFirst / 100;
            }

            getKreditSumm = kreditSumm - firstPayment;
            kreditPayment = getKreditSumm / kreditMonth;
            double kreditDebt = getKreditSumm;

            double percent = ((double) yearPercent) / 1200;
            double monthPayment;

            if (yearPercent == 0) {
                monthPayment = getKreditSumm / 12;
            } else
                monthPayment = (percent * Math.pow((1 + percent), kreditMonth) / (Math.pow((1 + percent), kreditMonth) - 1)) * getKreditSumm;

            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            Font font = sheet.getWorkbook().createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 14);
            cellStyle.setFont(font);

            int rowNumber = 1;

            Row row = sheet.createRow(rowNumber);

            Cell cellTitle = row.createCell(1);
            cellTitle.setCellStyle(cellStyle);
            cellTitle.setCellValue("Стоимость автомашины");

            Cell cellAuthor = row.createCell(2);
            cellAuthor.setCellValue(getBeautifulNumber(new BigDecimal(kreditSumm).setScale(2, RoundingMode.HALF_UP), "ru"));

            rowNumber++;
            Row row2 = sheet.createRow(rowNumber);

            Cell cellPrice = row2.createCell(1);
            cellPrice.setCellStyle(cellStyle);
            cellPrice.setCellValue("Первоначальный взнос");

            Cell cellFirst = row2.createCell(2);
            cellFirst.setCellValue(kreditFirst > 100 ? getBeautifulNumber(kreditFirst) + "(~" + (BigDecimal.valueOf((double) kreditFirst * 100 / Integer.parseInt(modification.getPrice())).setScale(0, RoundingMode.HALF_UP)) + "%)" : kreditFirst + "%");

            rowNumber++;
            Row row3 = sheet.createRow(rowNumber);

            Cell cellSumm = row3.createCell(1);
            cellSumm.setCellStyle(cellStyle);
            cellSumm.setCellValue("Годовая ставка (%)");

            Cell cellSummInfo = row3.createCell(2);
            cellSummInfo.setCellValue(yearPercent);

            rowNumber++;
            Row row4 = sheet.createRow(rowNumber);

            Cell cellPeriod = row4.createCell(1);
            cellPeriod.setCellStyle(cellStyle);
            cellPeriod.setCellValue("Срок кретида (месяц)");

            Cell cellPeriodInfo = row4.createCell(2);
            cellPeriodInfo.setCellValue(kreditMonth);

            rowNumber++;

            Row row5 = sheet.createRow(++rowNumber);

            Cell cellNumber = row5.createCell(0);
            cellNumber.setCellStyle(cellStyle);
            cellNumber.setCellValue("№");

            Cell cellKredit = row5.createCell(1);
            cellKredit.setCellStyle(cellStyle);
            cellKredit.setCellValue("Выплата кредита");

            Cell cellPercent = row5.createCell(2);
            cellPercent.setCellStyle(cellStyle);
            cellPercent.setCellValue("Выплата процентов");

            Cell cellAll = row5.createCell(3);
            cellAll.setCellStyle(cellStyle);
            cellAll.setCellValue("Общая выплата");

//            rowNumber++;

            for (int i = 0; i < kreditMonth; i++) {
                YearMonth yearMonthObject = YearMonth.of(year, month + 1);
                int daysInMonth = yearMonthObject.lengthOfMonth();

                double paymentPercent = (kreditDebt * yearPercent / 36500) * daysInMonth;
                kreditPayment = monthPayment - paymentPercent;
                kreditDebt -= kreditPayment;

                content.append(i + 1).append(". ").append(language.equals("uz") ? BotConstant.PAYMENTCREDITUZ : BotConstant.PAYMENTCREDITRU).append(getBeautifulNumber(new BigDecimal(kreditPayment).setScale(2, RoundingMode.HALF_UP), "ru")).append("\n").append(language.equals("uz") ? BotConstant.PAYMENTPERCENTUZ : BotConstant.PAYMENTPERCENTRU).append(paymentPercent > 0 ? getBeautifulNumber(new BigDecimal(paymentPercent).setScale(2, RoundingMode.HALF_UP), "ru") : "0").append("\n").append(language.equals("uz") ? BotConstant.PAYMENTALLUZ : BotConstant.PAYMENTALLRU).append(getBeautifulNumber(new BigDecimal(monthPayment).setScale(2, RoundingMode.HALF_UP), "ru")).append("\n\n");

                row = sheet.createRow(++rowNumber);
                int columnCount = 0;

                sheet.autoSizeColumn(columnCount);
                Cell cell = row.createCell(columnCount++);
                cell.setCellValue(i + 1);

                sheet.autoSizeColumn(columnCount);
                Cell cell1 = row.createCell(columnCount++);
                cell1.setCellValue(getBeautifulNumber(new BigDecimal(kreditPayment).setScale(2, RoundingMode.HALF_UP), "ru"));

                sheet.autoSizeColumn(columnCount);
                Cell cell2 = row.createCell(columnCount++);
                cell2.setCellValue(getBeautifulNumber(new BigDecimal(paymentPercent).setScale(2, RoundingMode.HALF_UP), "ru"));

                sheet.autoSizeColumn(columnCount);
                Cell cell3 = row.createCell(columnCount);
                cell3.setCellValue(getBeautifulNumber(new BigDecimal(monthPayment).setScale(2, RoundingMode.HALF_UP), "ru"));


                month++;
                if (month == 12) {
                    month = 0;
                }
            }


        } else if (kreditType.equals("diff")) {
            if (kreditMonth == 12) {
                    yearPercent = 0;
            } else if (kreditMonth <= 24) {
                if (kreditFirstPercent >= 30 && kreditFirstPercent < 40) {
                    yearPercent = 18;
                } else if (kreditFirstPercent >= 40 && kreditFirstPercent < 50) {
                    yearPercent = 16;
                } else {
                    yearPercent = 13;
                }
            } else if (kreditMonth <= 36) {
                    if (kreditFirstPercent >= 30 && kreditFirstPercent < 40) {
                            yearPercent = 20;
                    } else if (kreditFirstPercent >= 40 && kreditFirstPercent < 50) {
                        yearPercent = 19;
                    } else {
                            yearPercent = 16;
                    }
            } else if (kreditMonth <= 48) {
                    if (kreditFirstPercent >= 30 && kreditFirstPercent < 40) {
                        yearPercent = 23;
                    } else if (kreditFirstPercent >= 40 && kreditFirstPercent < 50) {
                        yearPercent = 21;
                    } else {
                            yearPercent = 19;
                    }
            } else {
                    if (kreditFirstPercent >= 30 && kreditFirstPercent < 40) {
                            yearPercent = 24;
                    } else if (kreditFirstPercent >= 40 && kreditFirstPercent < 50) {
                        yearPercent = 23;
                    } else {
                            yearPercent = 21;
                    }
            }

            if (kreditFirst > 100) {
                firstPayment = kreditFirst;
            } else {
                firstPayment = (double) kreditSumm * (double) kreditFirst / 100;
            }
            getKreditSumm = kreditSumm - firstPayment;
            kreditPayment = getKreditSumm / kreditMonth;
            double kreditDebt = getKreditSumm;

            CellStyle cellStyle = sheet.getWorkbook().createCellStyle();
            Font font = sheet.getWorkbook().createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 14);
            cellStyle.setFont(font);

            int rowNumber = 1;

            Row row = sheet.createRow(rowNumber);

            Cell cellTitle = row.createCell(1);
            cellTitle.setCellStyle(cellStyle);
            cellTitle.setCellValue("Стоимость автомашины");

            Cell cellAuthor = row.createCell(2);
            cellAuthor.setCellValue(getBeautifulNumber(new BigDecimal(kreditSumm).setScale(2, RoundingMode.HALF_UP), "ru"));

            rowNumber++;
            Row row2 = sheet.createRow(rowNumber);

            Cell cellPrice = row2.createCell(1);
            cellPrice.setCellStyle(cellStyle);
            cellPrice.setCellValue("Первоначальный взнос");

            Cell cellFirst = row2.createCell(2);
            cellFirst.setCellValue(kreditFirst > 100 ? getBeautifulNumber(kreditFirst) + "(~" + (BigDecimal.valueOf((double) kreditFirst * 100 / Integer.parseInt(modification.getPrice())).setScale(0, RoundingMode.HALF_UP)) + "%)" : kreditFirst + "%");

            rowNumber++;
            Row row3 = sheet.createRow(rowNumber);

            Cell cellSumm = row3.createCell(1);
            cellSumm.setCellStyle(cellStyle);
            cellSumm.setCellValue("Годовая ставка (%)");

            Cell cellSummInfo = row3.createCell(2);
            cellSummInfo.setCellValue(yearPercent);

            rowNumber++;
            Row row4 = sheet.createRow(rowNumber);

            Cell cellPeriod = row4.createCell(1);
            cellPeriod.setCellStyle(cellStyle);
            cellPeriod.setCellValue("Срок кретида (месяц)");

            Cell cellPeriodInfo = row4.createCell(2);
            cellPeriodInfo.setCellValue(kreditMonth);

            rowNumber++;

            Row row5 = sheet.createRow(++rowNumber);

            Cell cellNumber = row5.createCell(0);
            cellNumber.setCellStyle(cellStyle);
            cellNumber.setCellValue("№");

            Cell cellKredit = row5.createCell(1);
            cellKredit.setCellStyle(cellStyle);
            cellKredit.setCellValue("Выплата кредита");

            Cell cellPercent = row5.createCell(2);
            cellPercent.setCellStyle(cellStyle);
            cellPercent.setCellValue("Выплата процентов");

            Cell cellAll = row5.createCell(3);
            cellAll.setCellStyle(cellStyle);
            cellAll.setCellValue("Общая выплата");

            rowNumber++;

            for (int i = 0; i < kreditMonth; i++) {
                YearMonth yearMonthObject = YearMonth.of(year, month + 1);
                int daysInMonth = yearMonthObject.lengthOfMonth();

                double paymentPercent = (kreditDebt * yearPercent / 36500) * daysInMonth;
                kreditDebt -= kreditPayment;

                content.append(i + 1).append(". ").append(language.equals("uz") ? BotConstant.PAYMENTCREDITUZ : BotConstant.PAYMENTCREDITRU).append(getBeautifulNumber(new BigDecimal(kreditPayment).setScale(2, RoundingMode.HALF_UP), "ru")).append("\n").append(language.equals("uz") ? BotConstant.PAYMENTPERCENTUZ : BotConstant.PAYMENTPERCENTRU).append(getBeautifulNumber(new BigDecimal(paymentPercent).setScale(2, RoundingMode.HALF_UP), "ru")).append("\n").append(language.equals("uz") ? BotConstant.PAYMENTALLUZ : BotConstant.PAYMENTALLRU).append(getBeautifulNumber(new BigDecimal(paymentPercent + kreditPayment).setScale(2, RoundingMode.HALF_UP), "ru")).append("\n\n");


                row = sheet.createRow(rowNumber++);
                int columnCount = 0;

                sheet.autoSizeColumn(columnCount);
                Cell cell = row.createCell(columnCount++);
                cell.setCellValue(i + 1);

                sheet.autoSizeColumn(columnCount);
                Cell cell1 = row.createCell(columnCount++);
                cell1.setCellValue(getBeautifulNumber(new BigDecimal(kreditPayment).setScale(2, RoundingMode.HALF_UP), "ru"));

                sheet.autoSizeColumn(columnCount);
                Cell cell2 = row.createCell(columnCount++);
                cell2.setCellValue(getBeautifulNumber(new BigDecimal(paymentPercent).setScale(2, RoundingMode.HALF_UP), "ru"));

                sheet.autoSizeColumn(columnCount);
                Cell cell3 = row.createCell(columnCount);
                cell3.setCellValue(getBeautifulNumber(new BigDecimal(kreditPayment + paymentPercent).setScale(2, RoundingMode.HALF_UP), "ru"));


                month++;
                if (month == 12) {
                    month = 0;
                }
            }


        }

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
        sendMessage.setText(
                (language.equals("uz") ? BotConstant.KREDITSUMMINFOUZ : BotConstant.KREDITSUMMINFORU) + getBeautifulNumber(new BigDecimal(kreditSumm).setScale(2, RoundingMode.HALF_UP), language) + "\n" +
                        (language.equals("uz") ? BotConstant.FIRSTPERCENTUZ : BotConstant.FIRSTPERCENTRU) + (kreditFirst > 100 ? getBeautifulNumber(kreditFirst) + "(~" + (BigDecimal.valueOf((double) kreditFirst * 100 / Integer.parseInt(modification.getPrice())).setScale(0, RoundingMode.HALF_UP)) + "%)" : kreditFirst + "%") + "\n" +
                        (language.equals("uz") ? BotConstant.FIRSTPAYMENTUZ : BotConstant.FIRSTPAYMENTRU) + getBeautifulNumber(new BigDecimal(firstPayment).setScale(2, RoundingMode.HALF_UP), language) + "\n" +
                        (language.equals("uz") ? BotConstant.CREDITSUMMUZ : BotConstant.CREDITSUMMRU) + getBeautifulNumber(new BigDecimal(getKreditSumm).setScale(2, RoundingMode.HALF_UP), language) + "\n" +
                        (language.equals("uz") ? BotConstant.YEARPERCENTUZ : BotConstant.YEARPERCENTRU) + yearPercent + "%\n" +
                        (language.equals("uz") ? BotConstant.KREDITPERIODINFOUZ : BotConstant.KREDITPERIODINFORU) + kreditMonth + "мес.\n\n"
        );
        kiaBot.execute(sendMessage);

        if (creditFormat.equals("text")) {
            if (content.length() > 4096) {
                while (content.length() > 0) {
                    if (content.length() > 4096) {
                        sendMessage.setText(content.toString().substring(0, 4096));
                        content = new StringBuilder(content.substring(4096, content.length()));
                        kiaBot.execute(sendMessage);
                    } else {
                        sendMessage.setText(content.toString());
                        content = new StringBuilder("");
                    }
                }
            } else {
                sendMessage.setText(content.toString());
            }

            sendMessage.setReplyMarkup(buttonService.goBack(language, "avtokredittype#" + modificationId + "#" + kreditMonth + "#" + kreditFirst + "#" + kreditType + "#"));
            kiaBot.execute(sendMessage);
        } else if (creditFormat.equals("excel")) {

            try (FileOutputStream outputStream = new FileOutputStream("avtokredit_grafik.xlsx")) {
                workbook.write(outputStream);

                SendDocument sendDocument = new SendDocument();
                sendDocument.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
                sendDocument.setDocument(new InputFile(new File("avtokredit_grafik.xlsx")));
                sendDocument.setReplyMarkup(buttonService.goBack(language, "avtokredittype#" + modificationId + "#" + kreditMonth + "#" + kreditFirst + "#" + kreditType + "#"));
                kiaBot.execute(sendDocument);
            } catch (IOException e) {
                e.printStackTrace();
                sendMessage.setReplyMarkup(buttonService.goBack(language, "avtokredittype#" + modificationId + "#" + kreditMonth + "#" + kreditFirst + "#" + kreditType + "#"));
                kiaBot.execute(sendMessage);
            }
        } else if (creditFormat.equals("pdf")) {
            sendMessage.setReplyMarkup(buttonService.goBack(language, "avtokredittype#" + modificationId + "#" + kreditMonth + "#" + kreditFirst + "#" + kreditType + "#"));
            kiaBot.execute(sendMessage);
        }
    }

    public void addCreditSummError(Update update, boolean isCallBack) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));

        TelegramChat chat = telegramChatRepository.findByChatId(update.getMessage().getChatId());

        sendMessage.setText(chat.getLanguage().equals("ru") ? BotConstant.CREDITMONTHERRORRU : BotConstant.CREDITMONTHERRORUZ);
        kiaBot.execute(sendMessage);
        this.addCreditSumm(update, false, false);
    }

    public void addCreditFirstError(Update update) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(String.valueOf(update.getMessage().getChatId()));

        TelegramChat chat = telegramChatRepository.findByChatId(update.getMessage().getChatId());

        sendMessage.setText(chat.getLanguage().equals("ru") ? BotConstant.CREDITFIRSTERRORRU : BotConstant.CREDITFIRSTERRORUZ);
        kiaBot.execute(sendMessage);
        this.creditCalculatorMonth(update, chat.getLanguage(), "", false, true);
    }


}
