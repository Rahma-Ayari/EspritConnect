package tn.esprit.espritconnect2.Service;

import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class TwoFactorAuthService {

    private final SecretGenerator secretGenerator = new DefaultSecretGenerator(32);
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final CodeVerifier codeVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);

    public String generateNewSecret() {
        return secretGenerator.generate();
    }

    public String generateQrCodeImageUri(String email, String secret) throws QrGenerationException {
        QrData qrData = new QrData.Builder()
                .label(email)
                .secret(secret)
                .issuer("EspritConnect")
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        QrGenerator qrGenerator = new ZxingPngQrGenerator();
        byte[] qrBytes = qrGenerator.generate(qrData);
        String base64Qr = Base64.getEncoder().encodeToString(qrBytes);
        return "data:image/png;base64," + base64Qr;
    }

    public boolean verifyCode(String secret, String code) {
        if (secret == null || code == null || code.isBlank()) {
            return false;
        }
        String normalized = normalizeTotpCode(code);
        if (normalized.length() != 6) {
            return false;
        }
        ((DefaultCodeVerifier) codeVerifier).setAllowedTimePeriodDiscrepancy(1);
        return codeVerifier.isValidCode(secret, normalized);
    }

    public String normalizeTotpCode(String code) {
        return code.replaceAll("\\s+", "").replaceAll("[^0-9]", "");
    }

    public String normalizeBackupCode(String code) {
        return code.trim().toUpperCase().replaceAll("\\s+", "");
    }

    public boolean isBackupCodeFormat(String code) {
        String normalized = normalizeBackupCode(code);
        return normalized.matches("^[A-Z0-9]{4}-[A-Z0-9]{4}$");
    }

    public List<String> generateBackupCodes() {
        List<String> codes = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String part1 = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            String part2 = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
            codes.add(part1 + "-" + part2);
        }
        return codes;
    }
}
