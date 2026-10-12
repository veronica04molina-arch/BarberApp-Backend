package com.barberapp.barberapp.auth;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

@Service
public class GoogleAuthService {

    private final String clientId;

    public GoogleAuthService(@Value("${GOOGLE_CLIENT_ID}") String clientId) {
        this.clientId = clientId;
    }

    public GoogleUser verificarToken(String token) throws Exception {

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance())
                .setAudience(Collections.singletonList(clientId))
                .build();

        GoogleIdToken idToken = verifier.verify(token);

        if (idToken == null) {
            return null;
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        if (!Boolean.TRUE.equals(payload.getEmailVerified())) {
            return null;
        }

        String nombre = (String) payload.get("name");
        String correo = payload.getEmail();

        return new GoogleUser(nombre, correo);
    }
}
