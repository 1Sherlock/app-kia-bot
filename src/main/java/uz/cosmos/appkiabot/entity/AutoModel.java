package uz.cosmos.appkiabot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import uz.cosmos.appkiabot.entity.template.AbstractEntity;

import javax.persistence.Entity;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class AutoModel extends AbstractEntity {
    private String modelId;
    private String name;
    private String photo_sha;
    private Integer minPrice;
    private String pdf;
}
