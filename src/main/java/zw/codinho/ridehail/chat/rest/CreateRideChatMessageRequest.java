package zw.codinho.ridehail.chat.rest;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import zw.codinho.ridehail.chat.domain.RideChatSenderRole;

public record CreateRideChatMessageRequest(
        @NotNull(message = "Sender role is required")
        RideChatSenderRole senderRole,
        @NotBlank(message = "Sender name is required")
        String senderName,
        @NotBlank(message = "Message is required")
        String message
) {
}
