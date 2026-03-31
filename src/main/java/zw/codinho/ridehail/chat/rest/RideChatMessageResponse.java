package zw.codinho.ridehail.chat.rest;

import zw.codinho.ridehail.chat.domain.RideChatSenderRole;

import java.time.OffsetDateTime;
import java.util.UUID;

public record RideChatMessageResponse(
        UUID id,
        UUID rideId,
        RideChatSenderRole senderRole,
        String senderName,
        String message,
        OffsetDateTime createdAt
) {
}
