package uz.cosmos.appkiabot.payload;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResKiaModelInfo {
    private String name;
    private String pdf;

    List<ResKiaModification> compls;
}
