package zw.codinho.ridehail.wallet;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zw.codinho.ridehail.admin.rest.WalletBalanceResponse;
import zw.codinho.ridehail.shared.exception.BadRequestException;
import zw.codinho.ridehail.wallet.domain.Wallet;
import zw.codinho.ridehail.wallet.domain.WalletOwnerType;
import zw.codinho.ridehail.wallet.domain.WalletRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class WalletService {

    private static final String DEFAULT_CURRENCY = "USD";

    private final WalletRepository walletRepository;

    @Transactional
    public Wallet ensureWallet(WalletOwnerType ownerType, UUID ownerId) {
        return walletRepository.findByOwnerTypeAndOwnerId(ownerType, ownerId)
                .orElseGet(() -> {
                    Wallet wallet = new Wallet();
                    wallet.setOwnerType(ownerType);
                    wallet.setOwnerId(ownerId);
                    wallet.setCurrency(DEFAULT_CURRENCY);
                    return walletRepository.save(wallet);
                });
    }

    @Transactional
    public WalletBalanceResponse deposit(WalletOwnerType ownerType, UUID ownerId, BigDecimal amount) {
        BigDecimal normalizedAmount = normalizeAmount(amount);
        if (normalizedAmount.signum() <= 0) {
            throw new BadRequestException("Deposit amount must be greater than zero");
        }

        Wallet wallet = ensureWallet(ownerType, ownerId);
        wallet.setBalance(wallet.getBalance().add(normalizedAmount));
        Wallet savedWallet = walletRepository.save(wallet);
        return toResponse(savedWallet);
    }

    @Transactional
    public WalletBalanceResponse withdraw(WalletOwnerType ownerType, UUID ownerId, BigDecimal amount, String insufficientFundsMessage) {
        BigDecimal normalizedAmount = normalizeAmount(amount);
        Wallet wallet = ensureWallet(ownerType, ownerId);
        if (wallet.getBalance().compareTo(normalizedAmount) < 0) {
            throw new BadRequestException(insufficientFundsMessage);
        }

        wallet.setBalance(wallet.getBalance().subtract(normalizedAmount));
        Wallet savedWallet = walletRepository.save(wallet);
        return toResponse(savedWallet);
    }

    @Transactional(readOnly = true)
    public WalletBalanceResponse getWallet(WalletOwnerType ownerType, UUID ownerId) {
        return toResponse(ensureWallet(ownerType, ownerId));
    }

    @Transactional(readOnly = true)
    public boolean hasPositiveBalance(WalletOwnerType ownerType, UUID ownerId) {
        return ensureWallet(ownerType, ownerId).getBalance().compareTo(BigDecimal.ZERO) > 0;
    }

    private WalletBalanceResponse toResponse(Wallet wallet) {
        return new WalletBalanceResponse(wallet.getOwnerId(), wallet.getOwnerType().name(), wallet.getBalance(), wallet.getCurrency());
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
