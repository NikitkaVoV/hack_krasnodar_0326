package ru.fshs.tour.assistant.persistence.dialog;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "ai_dialog_message")
@Getter
@Setter
public class AiDialogMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private AiDialogSession session;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "role", nullable = false, length = 30)
    private String role;

    @Column(name = "message_text", nullable = false, columnDefinition = "text")
    private String messageText;

    @Column(name = "intent", length = 100)
    private String intent;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "entities_json", columnDefinition = "json")
    private JsonNode entitiesJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "system_response_json", columnDefinition = "json")
    private JsonNode systemResponseJson;

    @PrePersist
    void onCreate() {
        createdAt = OffsetDateTime.now();
    }
}
