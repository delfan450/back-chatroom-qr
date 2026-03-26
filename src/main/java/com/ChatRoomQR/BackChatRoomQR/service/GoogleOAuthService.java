package com.ChatRoomQR.BackChatRoomQR.service;

import com.google.auth.oauth2.TokenVerifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class GoogleOAuthService {
    @Value("${google.oauth.client-id}")
    private String googleClientId;

    public Map<String, String> validateTokenAndGetInfo(String idToken) {
        try {
            TokenVerifier verifier = TokenVerifier.newBuilder()
                    .setAudience(googleClientId)
                    .build();

            Map<String, Object> claims = verifier.verify(idToken).getPayload();

            Map<String, String> resultado = new HashMap<>();
            resultado.put("email", (String) claims.get("email"));
            resultado.put("nombre", (String) claims.get("given_name"));
            resultado.put("apellidos", (String) claims.get("family_name"));
            resultado.put("google_id", (String) claims.get("sub"));
            resultado.put("foto", (String) claims.get("picture"));

            return resultado;
        } catch (Exception e) {
            System.err.println("Error validando Google OAuth token: " + e.getMessage());
            return null;
        }
    }
}
