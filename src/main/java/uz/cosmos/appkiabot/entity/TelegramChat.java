package uz.cosmos.appkiabot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.cosmos.appkiabot.entity.enums.TelegramChatStatus;
import uz.cosmos.appkiabot.entity.template.AbstractEntity;

import javax.persistence.Entity;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class TelegramChat  extends AbstractEntity {
    private Long chatId;

    private String language;

    private TelegramChatStatus status;

    private String phoneNumber;

    private String fullName;
}
