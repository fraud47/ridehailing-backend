package zw.codinho.ridehail.rider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zw.codinho.ridehail.rider.domain.Rider;
import zw.codinho.ridehail.rider.domain.RiderRepository;
import zw.codinho.ridehail.rider.rest.CreateRiderRequest;
import zw.codinho.ridehail.rider.rest.RiderResponse;
import zw.codinho.ridehail.shared.exception.ConflictException;
import zw.codinho.ridehail.shared.exception.NotFoundException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RiderService {

    private final RiderRepository riderRepository;

    @Transactional
    public RiderResponse createRider(CreateRiderRequest request) {
        riderRepository.findByPhoneNumber(request.phoneNumber())
                .ifPresent(rider -> {
                    throw new ConflictException("A rider already exists with phone number " + request.phoneNumber());
                });

        riderRepository.findByEmailAddress(request.emailAddress())
                .ifPresent(rider -> {
                    throw new ConflictException("A rider already exists with email address " + request.emailAddress());
                });

        Rider rider = new Rider();
        rider.setFullName(request.fullName());
        rider.setPhoneNumber(request.phoneNumber());
        rider.setEmailAddress(request.emailAddress());

        return toResponse(riderRepository.save(rider));
    }

    @Transactional(readOnly = true)
    public RiderResponse getRider(UUID riderId) {
        return riderRepository.findById(riderId)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Rider " + riderId + " was not found"));
    }

    @Transactional(readOnly = true)
    public List<RiderResponse> getRiders() {
        return riderRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public Rider requireRider(UUID riderId) {
        return riderRepository.findById(riderId)
                .orElseThrow(() -> new NotFoundException("Rider " + riderId + " was not found"));
    }

    private RiderResponse toResponse(Rider rider) {
        return new RiderResponse(
                rider.getId(),
                rider.getFullName(),
                rider.getPhoneNumber(),
                rider.getEmailAddress(),
                rider.getRating(),
                rider.getCreatedAt());
    }
}
