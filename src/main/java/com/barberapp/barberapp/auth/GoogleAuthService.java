package com.barberapp.barberapp.auth;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class GoogleAuthService {

    // Client ID de Google
    private static final String CLIENT_ID =
            "441400428516-7kocd79b52m7rc8avh9maqu6v1370heq.apps.googleusercontent.com";

    public GoogleUser verificarToken(String token) throws Exception {

        GoogleIdTokenVerifier verifier =
                new GoogleIdTokenVerifier.Builder(
                        new NetHttpTransport(),
                        GsonFactory.getDefaultInstance()
                )
                .setAudience(Collections.singletonList(CLIENT_ID))
                .build();

        GoogleIdToken idToken = verifier.verify(token);

        if (idToken == null) {
            return null;
        }

        GoogleIdToken.Payload payload = idToken.getPayload();

        String nombre = (String) payload.get("name");
        String correo = payload.getEmail();

        return new GoogleUser(nombre, correo);
    }

}