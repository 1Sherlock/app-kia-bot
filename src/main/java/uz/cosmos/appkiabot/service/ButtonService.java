package uz.cosmos.appkiabot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.cosmos.appkiabot.bot.BotConstant;
import uz.cosmos.appkiabot.entity.AutoModel;
import uz.cosmos.appkiabot.entity.AutoModification;
import uz.cosmos.appkiabot.payload.ResKiaModification;

import java.util.ArrayList;
import java.util.List;

@Service
public class ButtonService {
    public InlineKeyboardButton generateButton(String textUz, String textRu, String callBackData, String language) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(language.equals("ru") ? textRu : textUz);
        button.setCallbackData(callBackData + (language.equals("ru") ? "ru" : "uz"));
        return button;
    }

    public InlineKeyboardButton generateButtonWithUrl(String textUz, String textRu, String language, String url) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(language.equals("ru") ? textRu : textUz);
        button.setUrl(url);
        return button;
    }

    public InlineKeyboardMarkup languageButton() {
        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();
        List<InlineKeyboardButton> rows = new ArrayList<>();
        List<List<InlineKeyboardButton>> inlineKeyboardButtons = new ArrayList<>();

        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(BotConstant.UZBEK);
        button.setCallbackData("language#uzbek");
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText(BotConstant.RUSSIAN);
        button1.setCallbackData("language#russian");

        rows.add(button);
        rows.add(button1);
        inlineKeyboardButtons.add(rows);
        replyKeyboardMarkup.setKeyboard(inlineKeyboardButtons);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboard enterNumber(String language) {
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        KeyboardButton keyboardButton = new KeyboardButton(language.equals("ru") ? BotConstant.SHARENUMBERRU : BotConstant.SHARENUMBERUZ);
        keyboardButton.setRequestContact(true);
        row.add(keyboardButton);
        rows.add(row);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(rows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public InlineKeyboardMarkup menuButton(String language, Boolean isAdmin) {
        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        List<InlineKeyboardButton> row4 = new ArrayList<>();

        if (isAdmin) {
            row4.add(generateButton(BotConstant.UPDATEUZ, BotConstant.UPDATERU, "update#", language));
            rows.add(row4);
        }

        row.add(generateButton(BotConstant.MODELSUZ, BotConstant.MODELSRU, "models#", language));
        rows.add(row);

        row1.add(generateButton(BotConstant.ACTIONSUZ, BotConstant.ACTIONSRU, "actions#", language));
        rows.add(row1);

        row2.add(generateButton(BotConstant.LOCATIONUZ, BotConstant.LOCATIONRU, "location#", language));
        rows.add(row2);

        row3.add(generateButton(BotConstant.YURIDIKUZ, BotConstant.YURIDIKRU, "yuridik#", language));
        rows.add(row3);


        replyKeyboardMarkup.setKeyboard(rows);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboard sendModels(List<AutoModel> modelList, String lang) {
        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (AutoModel resKiaModel : modelList) {
            row.add(generateButton(resKiaModel.getName(), resKiaModel.getName(), "modelInfo#" + resKiaModel.getModelId() + "#", lang));
            rows.add(row);
            row = new ArrayList<>();
        }
        row.add(generateButton(BotConstant.BACKUZ, BotConstant.BACKRU, "gomenu#", lang));
        rows.add(row);

        replyKeyboardMarkup.setKeyboard(rows);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboard autoModelGoBack(String lang, List<AutoModification> modelInfo) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        List<InlineKeyboardButton> row4 = new ArrayList<>();

        for (AutoModification autoModification : modelInfo) {
            row.add(generateButton(autoModification.getName(), autoModification.getName(), "automodificationinfo#" + autoModification.getModificationId() + "#", lang));
            rows.add(row);
            row = new ArrayList<>();
        }

        row.add(generateButton(BotConstant.BACKUZ, BotConstant.BACKRU, "models#", lang));

        rows.add(row);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboard autoModifications(String language, AutoModification modification, String type) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        List<InlineKeyboardButton> row5 = new ArrayList<>();
        List<InlineKeyboardButton> row4 = new ArrayList<>();

        row4.add(generateButton(BotConstant.CREDITCALCULATORUZ, BotConstant.CREDITCALCULATORRU, "creditmodifications#" + modification.getModificationId() + "#", language));
        row2.add(generateButton(BotConstant.SENDAPPUZ, BotConstant.SENDAPPRU, "sendapp#" + modification.getModificationId() + "#", language));
        row3.add(generateButton(BotConstant.NASIYAUZ, BotConstant.NASIYARU, "nasiyamodifications#" + modification.getModificationId() + "#", language));
        row5.add(generateButton(BotConstant.INFOUZ, BotConstant.INFORU, "downloadinfo#" + modification.getModificationId() + "#", language));

        row1.add(generateButton(BotConstant.BACKUZ, BotConstant.BACKRU, "modelInfo#" + type + "#", language));

        rows.add(row4);
        rows.add(row2);
        rows.add(row3);
        rows.add(row5);
        rows.add(row1);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboard enterFIO(String language) {
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();

        KeyboardButton keyboardButton = new KeyboardButton(language.equals("ru") ? BotConstant.CANCELRU : BotConstant.CANCELUZ);
        row.add(keyboardButton);
        rows.add(row);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setKeyboard(rows);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboard creditPeriods(String language, String modificationId, AutoModification modification) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        row2.add(generateButton(12 + " " + BotConstant.MONTHUZ, 12 + " " + BotConstant.MONTHRU, "avtokreditmonth#" + modificationId + "#" + 12 + "#", language));
        rows.add(row2);

        row.add(generateButton(36 + " " + BotConstant.MONTHUZ, 36 + " " + BotConstant.MONTHRU, "avtokreditmonth#" + modificationId + "#" + 36 + "#", language));
        row.add(generateButton(48 + " " + BotConstant.MONTHUZ, 48 + " " + BotConstant.MONTHRU, "avtokreditmonth#" + modificationId + "#" + 48 + "#", language));
        row.add(generateButton(60 + " " + BotConstant.MONTHUZ, 60 + " " + BotConstant.MONTHRU, "avtokreditmonth#" + modificationId + "#" + 60 + "#", language));

        row1.add(generateButton(BotConstant.BACKUZ, BotConstant.BACKRU, "automodificationinfo#" + modificationId + "#", language));

        rows.add(row);
        rows.add(row1);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;

    }

    public ReplyKeyboard creditFirsts(String language, String modificationId, String kreditMonth, AutoModification modification) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();

        if(!kreditMonth.equals("12")){
            row.add(generateButton(30 + "%", 30 + "%", "avtokreditfirst#" + modificationId + "#" + kreditMonth + "#" + 30 + "#", language));
            row.add(generateButton(40 + "%", 40 + "%", "avtokreditfirst#" + modificationId + "#" + kreditMonth + "#" + 40 + "#", language));
        }
        row.add(generateButton(50 + "%", 50 + "%", "avtokreditfirst#" + modificationId + "#" + kreditMonth + "#" + 50 + "#", language));

        row1.add(generateButton(BotConstant.BACKUZ, BotConstant.BACKRU, "creditmodifications#" + modificationId + "#", language));

        rows.add(row);
        rows.add(row1);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboard creditType(String language, String kreditSumm, String kreditMonth, String kreditFirst, AutoModification modification) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();

        if (!kreditMonth.equals("12")){
            row.add(generateButton(BotConstant.DIFFERENSIALUZ, BotConstant.DIFFERENSIALRU, "avtokredittype#" + kreditSumm + "#" + kreditMonth + "#" + kreditFirst + "#diff#", language));
        }

        row.add(generateButton(BotConstant.ANNUITETUZ, BotConstant.ANNUITETRU, "avtokredittype#" + kreditSumm + "#" + kreditMonth + "#" + kreditFirst + "#ann#", language));

        row1.add(generateButton(BotConstant.BACKUZ, BotConstant.BACKRU, "avtokreditmonth#" + kreditSumm + "#" + kreditMonth + "#", language));

        rows.add(row);
        rows.add(row1);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboard creditPeriodsType(String language, String modificationId, String kreditMonth, String kreditFirst, String kreditType) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
//        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        List<InlineKeyboardButton> row4 = new ArrayList<>();

        row1.add(generateButton("Matn ko'rinishida", "В текстовой форме", "avtocreditcalc#" + modificationId + "#" + kreditMonth + "#" + kreditFirst + "#" + kreditType + "#" + "text#", language));
//        row2.add(generateButton("PDF", "PDF", "avtocreditcalc#" + modificationId + "#" + kreditMonth + "#" + kreditFirst + "#" + kreditType + "#" + "pdf#", language));
        row3.add(generateButton("Jadval ko'rinishida", "В табличной форме", "avtocreditcalc#" + modificationId + "#" + kreditMonth + "#" + kreditFirst + "#" + kreditType + "#" + "excel#", language));
        row4.add(generateButton(BotConstant.BACKUZ, BotConstant.BACKRU, "avtokreditfirst#" + modificationId + "#" + kreditMonth + "#" + kreditFirst + "#", language));

        rows.add(row1);
//        rows.add(row2);
        rows.add(row3);
        rows.add(row4);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public ReplyKeyboard goBack(String language, String query) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(generateButton(BotConstant.BACKUZ, BotConstant.BACKRU, query, language));

        rows.add(row);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
}
