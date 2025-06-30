This is a single tenant fintech platform. Implementations are explicitly  mocked due to the scope and timeline.

- Fintech's generally can't provision bank accounts except they are partnered with banks or deposit taking institutions.

Testing data.
Valid BVN starts with "2";
Valid OTP is "5431"
Valid date of birth is "1990-10-10" 

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordUtil {

    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String hashPassword(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public static boolean verifyPassword(String rawPassword, String hashedPassword) {
        return encoder.matches(rawPassword, hashedPassword);
    }

    public static void main(String[] args) {
        String raw = "mySecret123";
        String hashed = hashPassword(raw);
        
        System.out.println("Hashed: " + hashed);
        System.out.println("Match? " + verifyPassword("mySecret123", hashed));  // true
    }
}

