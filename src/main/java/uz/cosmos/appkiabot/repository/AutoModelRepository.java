package uz.cosmos.appkiabot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.cosmos.appkiabot.entity.AutoModel;

import java.util.List;
import java.util.UUID;

public interface AutoModelRepository extends JpaRepository<AutoModel, UUID> {
    AutoModel findByModelId(String modelId);

}
