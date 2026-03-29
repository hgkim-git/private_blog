package io.github.hgkimer.privateblog.util;

import io.jsonwebtoken.Jwts.SIG;
import io.jsonwebtoken.io.Encoders;
import javax.crypto.SecretKey;

public class JwtSecretGenerator {

  public static String generateSecret() {
    SecretKey secretKey = SIG.HS384.key().build();
    return Encoders.BASE64.encode(secretKey.getEncoded());
  }
}
