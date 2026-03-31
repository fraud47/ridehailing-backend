package zw.codinho.ridehail.chat.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import zw.codinho.ridehail.ride.domain.Ride;
import zw.codinho.ridehail.shared.domain.BaseEntity;

@Getter
@Setter
@Entity
@Table(name = "ride_chat_messages")
public class RideChatMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RideChatSenderRole senderRole;

    @Column(nullable = false)
    private String senderName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;
}
