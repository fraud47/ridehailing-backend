package zw.codinho.ridehail.platform;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import zw.codinho.ridehail.platform.domain.PlatformAccount;
import zw.codinho.ridehail.platform.domain.PlatformAccountRepository;
import zw.codinho.ridehail.shared.exception.BadRequestException;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class PlatformAccountService {

    private static final String PRIMARY_ACCOUNT_KEY = "PRIMARY";

    private final PlatformAccountRepository platformAccountRepository;

    @Transactional
    public PlatformAccount captureCommission(BigDecimal commissionAmount) {
        BigDecimal normalizedAmount = normalizeAmount(commissionAmount);
        if (normalizedAmount.signum() <= 0) {
            throw new BadRequestException("Commission amount must be greater than zero");
        }

        PlatformAccount account = getOrCreatePrimaryAccount();
        account.setAvailableBalance(account.getAvailableBalance().add(normalizedAmount));
        account.setTotalCommissionEarned(account.getTotalCommissionEarned().add(normalizedAmount));
        return platformAccountRepository.save(account);
    }

    @Transactional
    public PlatformAccount withdraw(BigDecimal amount) {
        BigDecimal normalizedAmount = normalizeAmount(amount);
        if (normalizedAmount.signum() <= 0) {
            throw new BadRequestException("Withdrawal amount must be greater than zero");
        }

        PlatformAccount account = getOrCreatePrimaryAccount();
        if (account.getAvailableBalance().compareTo(normalizedAmount) < 0) {
            throw new BadRequestException("Platform balance is insufficient for this withdrawal");
        }

        account.setAvailableBalance(account.getAvailableBalance().subtract(normalizedAmount));
        account.setTotalWithdrawn(account.getTotalWithdrawn().add(normalizedAmount));
        return platformAccountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public PlatformAccount getPrimaryAccount() {
        return getOrCreatePrimaryAccount();
    }

    private PlatformAccount getOrCreatePrimaryAccount() {
        return platformAccountRepository.findByAccountKey(PRIMARY_ACCOUNT_KEY)
                .orElseGet(() -> {
                    PlatformAccount account = new PlatformAccount();
                    account.setAccountKey(PRIMARY_ACCOUNT_KEY);
                    return platformAccountRepository.save(account);
                });
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
