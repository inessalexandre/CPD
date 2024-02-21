import java.time.LocalDateTime;
import java.util.Map;

public class TokenWithExpiration {
    private String token;
    private LocalDateTime expirationDate;

    public TokenWithExpiration(String token, LocalDateTime expirationDate) {
        this.token = token;
        this.expirationDate = expirationDate;
    }

    public String getToken() {
        return token;
    }

    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }

    public boolean hasExpired() {
        return LocalDateTime.now().isAfter(expirationDate);
    }

}