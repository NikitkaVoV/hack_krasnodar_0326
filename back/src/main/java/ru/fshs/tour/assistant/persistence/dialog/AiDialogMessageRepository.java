package ru.fshs.tour.assistant.persistence.dialog;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiDialogMessageRepository extends JpaRepository<AiDialogMessage, UUID> {
}