package uz.cosmos.appkiabot.service;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.cosmos.appkiabot.bot.BotConstant;
import uz.cosmos.appkiabot.payload.ResKiaModel;
import uz.cosmos.appkiabot.payload.ResKiaModelInfo;
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

    public ReplyKeyboard sendModels(List<ResKiaModel> modelList, String lang) {
        InlineKeyboardMarkup replyKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (ResKiaModel resKiaModel : modelList) {
            row.add(generateButton(resKiaModel.getName(), resKiaModel.getName(), "modelInfo#" + resKiaModel.getUrl() + "#", lang));
            rows.add(row);
            row = new ArrayList<>();
        }
        row.add(generateButton(BotConstant.BACKUZ, BotConstant.BACKRU, "gomenu#", lang));
        rows.add(row);

        replyKeyboardMarkup.setKeyboard(rows);
        return replyKeyboardMarkup;
    }

    public ReplyKeyboard autoModelGoBack(String lang, String modelUrl, ResKiaModelInfo modelInfo) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();
        List<InlineKeyboardButton> row3 = new ArrayList<>();
        List<InlineKeyboardButton> row4 = new ArrayList<>();

        for (ResKiaModification autoModification : modelInfo.getCompls()) {
            row.add(generateButton(autoModification.getName(), autoModification.getName(), "automodificationinfo#" + modelUrl + "#" + autoModification.getName(), lang));
            rows.add(row);
            row = new ArrayList<>();
        }

        row.add(generateButton(BotConstant.BACKUZ, BotConstant.BACKRU, "models#" , lang));

        rows.add(row);

        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
}
