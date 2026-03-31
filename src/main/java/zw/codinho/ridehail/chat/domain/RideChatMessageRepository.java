package zw.codinho.ridehail.chat.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RideChatMessageRepository extends JpaRepository<RideChatMessage, UUID> {

    List<RideChatMessage> findAllByRideIdOrderByCreatedAtAsc(UUID rideId);
}
