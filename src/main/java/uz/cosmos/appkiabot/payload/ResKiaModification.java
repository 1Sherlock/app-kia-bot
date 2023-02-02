package uz.cosmos.appkiabot.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResKiaModification {
    private String name;
    private String price;
    private List<String> engine;
    private List<String> options;

}
