package zw.codinho.ridehail.chat;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zw.codinho.ridehail.chat.domain.RideChatMessage;
import zw.codinho.ridehail.chat.domain.RideChatMessageRepository;
import zw.codinho.ridehail.chat.domain.RideChatSenderRole;
import zw.codinho.ridehail.chat.rest.CreateRideChatMessageRequest;
import zw.codinho.ridehail.chat.rest.RideChatMessageResponse;
import zw.codinho.ridehail.ride.domain.Ride;
import zw.codinho.ridehail.ride.domain.RideStatus;
import zw.codinho.ridehail.ride.RideService;
import zw.codinho.ridehail.shared.exception.BadRequestException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RideChatService {

    private final RideChatMessageRepository rideChatMessageRepository;
    private final RideService rideService;

    @Transactional
    public RideChatMessageResponse sendMessage(UUID rideId, CreateRideChatMessageRequest request) {
        Ride ride = rideService.requireRideEntity(rideId);
        validateRideChatParticipants(ride, request.senderRole());

        if (ride.getStatus() == RideStatus.CANCELLED) {
            throw new BadRequestException("Chat is unavailable for cancelled rides");
        }

        RideChatMessage message = new RideChatMessage();
        message.setRide(ride);
        message.setSenderRole(request.senderRole());
        message.setSenderName(request.senderName().trim());
        message.setMessage(request.message().trim());

        return toResponse(rideChatMessageRepository.save(message));
    }

    @Transactional(readOnly = true)
    public List<RideChatMessageResponse> getMessages(UUID rideId) {
        Ride ride = rideService.requireRideEntity(rideId);
        validateRideChatParticipants(ride, RideChatSenderRole.RIDER);
        validateRideChatParticipants(ride, RideChatSenderRole.DRIVER);

        return rideChatMessageRepository.findAllByRideIdOrderByCreatedAtAsc(rideId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateRideChatParticipants(Ride ride, RideChatSenderRole senderRole) {
        if (senderRole == RideChatSenderRole.RIDER && ride.getRider() == null) {
            throw new BadRequestException("This ride has no rider assigned");
        }
        if (senderRole == RideChatSenderRole.DRIVER && ride.getDriver() == null) {
            throw new BadRequestException("Driver chat is unavailable until a driver is assigned");
        }
    }

    private RideChatMessageResponse toResponse(RideChatMessage message) {
        return new RideChatMessageResponse(
                message.getId(),
                message.getRide().getId(),
                message.getSenderRole(),
                message.getSenderName(),
                message.getMessage(),
                message.getCreatedAt());
    }
}
