package uz.cosmos.appkiabot.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import uz.cosmos.appkiabot.entity.template.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class AutoModification extends AbstractEntity {
    private String modificationId;
    private String name;
    private String price;

    @Column(columnDefinition = "text")
    private String descriptions;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    private AutoModel model;
}
