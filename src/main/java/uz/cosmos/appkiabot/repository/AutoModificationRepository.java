package uz.cosmos.appkiabot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.cosmos.appkiabot.entity.AutoModification;

import java.util.List;
import java.util.UUID;

public interface AutoModificationRepository extends JpaRepository<AutoModification, UUID> {
    List<AutoModification> findAllByModel_ModelId(String modelId);
    AutoModification findByModificationId(String modificationId);
}
