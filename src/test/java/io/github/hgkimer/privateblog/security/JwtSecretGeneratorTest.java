package io.github.hgkimer.privateblog.security;

import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.io.Encoders;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;


class JwtSecretGeneratorTest {

  @Test
  public void testGenerateSecretKey() {
    SecretKey secretKey = SIG.HS384.key().build();
    String encodedSecretKey = Encoders.BASE64.encode(secretKey.getEncoded());
    System.out.println(encodedSecretKey);
  }
}