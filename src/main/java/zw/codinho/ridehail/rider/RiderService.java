package zw.codinho.ridehail.rider;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zw.codinho.ridehail.admin.rest.WalletBalanceResponse;
import zw.codinho.ridehail.rider.domain.Rider;
import zw.codinho.ridehail.rider.domain.RiderRepository;
import zw.codinho.ridehail.rider.rest.CreateRiderRequest;
import zw.codinho.ridehail.rider.rest.RiderResponse;
import zw.codinho.ridehail.shared.exception.ConflictException;
import zw.codinho.ridehail.shared.exception.NotFoundException;
import zw.codinho.ridehail.wallet.WalletService;
import zw.codinho.ridehail.wallet.domain.WalletOwnerType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RiderService {

    private final RiderRepository riderRepository;
    private final WalletService walletService;

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
        Rider savedRider = riderRepository.save(rider);
        walletService.ensureWallet(WalletOwnerType.RIDER, savedRider.getId());
        return toResponse(savedRider);
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

    @Transactional
    public WalletBalanceResponse depositFunds(UUID riderId, BigDecimal amount) {
        requireRider(riderId);
        return walletService.deposit(WalletOwnerType.RIDER, riderId, amount);
    }

    @Transactional
    public void debitWallet(UUID riderId, BigDecimal amount) {
        requireRider(riderId);
        walletService.withdraw(WalletOwnerType.RIDER, riderId, amount, "Rider wallet balance is insufficient to complete this ride");
    }

    private RiderResponse toResponse(Rider rider) {
        WalletBalanceResponse wallet = walletService.getWallet(WalletOwnerType.RIDER, rider.getId());
        return new RiderResponse(
                rider.getId(),
                rider.getFullName(),
                rider.getPhoneNumber(),
                rider.getEmailAddress(),
                rider.getRating(),
                wallet.balance(),
                rider.getCreatedAt());
    }
}
