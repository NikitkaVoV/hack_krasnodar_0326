package ru.fshs.tour.assistant.persistence.dialog;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiDialogSessionRepository extends JpaRepository<AiDialogSession, UUID> {
}