package com.barberapp.barberapp.service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.HtmlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

  private static final Logger logger =
        LoggerFactory.getLogger(EmailService.class);

  private final RestClient restClient;
  private final String brevoApiKey;
  private final String senderEmail;
  private final String frontendUrl;
  private final String backendPublicUrl;

  public EmailService(
      @Value("${BREVO_API_KEY:}") String brevoApiKey,
      @Value("${BREVO_SENDER_EMAIL:}") String senderEmail,
      @Value("${FRONTEND_URL:http://localhost:4200}") String frontendUrl,
      @Value("${BACKEND_PUBLIC_URL:http://localhost:8081}") String backendPublicUrl) {

    this.restClient = RestClient.builder()
        .baseUrl("https://api.brevo.com/v3")
        .build();

    this.brevoApiKey = brevoApiKey;
    this.senderEmail = senderEmail;
    this.frontendUrl = frontendUrl.replaceAll("/+$", "");
    this.backendPublicUrl = backendPublicUrl.replaceAll("/+$", "");
  }

  public void enviarCorreoRecuperacion(String destinatario, String token) {

    if (brevoApiKey.isBlank() || senderEmail.isBlank()) {
      throw new IllegalStateException(
          "Falta configurar BREVO_API_KEY o BREVO_SENDER_EMAIL.");
    }

    String emailCodificado = URLEncoder.encode(
        destinatario, StandardCharsets.UTF_8);

    String tokenCodificado = URLEncoder.encode(
        token, StandardCharsets.UTF_8);

    String urlRecuperacion = frontendUrl
        + "/restablecer-password?token="
        + tokenCodificado
        + "&email="
        + emailCodificado;

    String contenidoHtml = """
        <!DOCTYPE html>
        <html lang="es">
        <head>
          <meta charset="UTF-8">
          <meta name="viewport" content="width=device-width, initial-scale=1.0">
          <title>Restablecer Contraseña - BarberApp</title>
        </head>

        <body style="margin: 0; padding: 0; background-color: #f4f6fb; font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;">

          <table role="presentation" width="100%%" border="0" cellspacing="0" cellpadding="0" style="background-color: #f4f6fb; padding: 40px 10px;">
            <tr>
              <td align="center">

                <table role="presentation" width="100%%" border="0" cellspacing="0" cellpadding="0"
                       style="max-width: 520px; background-color: #ffffff; border-radius: 20px; overflow: hidden; box-shadow: 0 10px 30px rgba(16, 41, 109, 0.08); border: 1px solid #e8ecf5;">

                  <!-- ENCABEZADO -->
                  <tr>
                    <td align="center" style="background-color: #10296d; padding: 36px 20px; text-align: center;">

                      <div style="display: inline-block; background-color: #1f3fbf; padding: 14px; border-radius: 16px;">
                        <img src="%s/images/barberapp-logo.png"
                             alt="BarberApp Logo"
                             width="42"
                             height="42"
                             style="display: block; border: 0;" />
                      </div>

                      <h1 style="color: #ffffff; margin: 16px 0 0 0; font-size: 24px; font-weight: 700;">
                        BarberApp
                      </h1>

                      <p style="color: #a5b4fc; margin: 4px 0 0 0; font-size: 13px;">
                        Sistema de Agendamiento de Citas
                      </p>

                    </td>
                  </tr>

                  <!-- CUERPO -->
                  <tr>
                    <td style="padding: 36px 32px; text-align: left;">

                      <h2 style="color: #07205f; margin: 0 0 12px 0; font-size: 20px; font-weight: 700;">
                        ¿Olvidaste tu contraseña?
                      </h2>

                      <p style="color: #4b5563; font-size: 15px; line-height: 1.6; margin: 0 0 24px 0;">
                        Recibimos una solicitud para restablecer la contraseña de tu cuenta asociada a
                        <strong style="color: #10296d;">%s</strong>.
                        No te preocupes, puedes generar una nueva haciendo clic en el siguiente botón:
                      </p>

                      <!-- BOTÓN -->
                      <table role="presentation" width="100%%" border="0" cellspacing="0" cellpadding="0" style="margin: 28px 0;">
                        <tr>
                          <td align="center">
                            <a href="%s"
                               target="_blank"
                               style="display: inline-block; background-color: #1f3fbf; color: #ffffff; text-decoration: none; font-size: 15px; font-weight: 600; padding: 14px 32px; border-radius: 12px; box-shadow: 0 6px 18px rgba(31, 63, 191, 0.35);">
                              Restablecer mi Contraseña
                            </a>
                          </td>
                        </tr>
                      </table>

                      <!-- CÓDIGO -->
                      <div style="background-color: #f7f8fc; border: 1px dashed #cbd5e1; border-radius: 12px; padding: 16px; text-align: center; margin-bottom: 24px;">

                        <p style="color: #64748b; font-size: 12px; margin: 0 0 6px 0; font-weight: 600; text-transform: uppercase;">
                          Tu código de seguridad temporal
                        </p>

                        <span style="font-family: monospace; font-size: 18px; font-weight: 700; color: #10296d; letter-spacing: 2px;">
                          %s
                        </span>

                      </div>

                      <p style="color: #6b7280; font-size: 13px; line-height: 1.5; margin: 0 0 8px 0;">
                        ⏱️ <strong>Nota:</strong> Este enlace y código expiran en <strong>15 minutos</strong>.
                      </p>

                      <p style="color: #9ca3af; font-size: 12px; line-height: 1.5; margin: 0;">
                        Si tú no solicitaste este cambio, puedes ignorar este mensaje de forma segura.
                      </p>

                    </td>
                  </tr>

                  <!-- PIE DE PÁGINA -->
                  <tr>
                    <td style="padding: 24px 32px; background-color: #fafbfc; text-align: center;">

                      <p style="color: #94a3b8; font-size: 12px; margin: 0 0 6px 0;">
                        Si el botón no funciona, copia y pega el siguiente enlace en tu navegador:
                      </p>

                      <p style="margin: 0 0 16px 0; word-break: break-all;">
                        <a href="%s"
                           style="color: #1f3fbf; font-size: 11px; text-decoration: underline;">
                          %s
                        </a>
                      </p>

                      <p style="color: #cbd5e1; font-size: 11px; margin: 0;">
                        © 2026 BarberApp. Todos los derechos reservados.
                      </p>

                    </td>
                  </tr>

                </table>

              </td>
            </tr>
          </table>

        </body>
        </html>
        """
        .formatted(
            backendPublicUrl,
            HtmlUtils.htmlEscape(destinatario),
            urlRecuperacion,
            token,
            urlRecuperacion,
            urlRecuperacion);

    Map<String, Object> solicitud = Map.of(
        "sender", Map.of(
            "name", "BarberApp",
            "email", senderEmail),
        "to", List.of(
            Map.of("email", destinatario)),
        "subject", "Restablecer tu contraseña - BarberApp",
        "htmlContent", contenidoHtml);

    try {
      restClient.post()
          .uri("/smtp/email")
          .header("api-key", brevoApiKey)
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON)
          .body(solicitud)
          .retrieve()
          .toBodilessEntity();

    } catch (RestClientResponseException e) {

      logger.error(
          "Brevo rechazó el envío. HTTP {}. Respuesta: {}",
          e.getStatusCode(),
          e.getResponseBodyAsString());

      throw new RuntimeException(
          "No se pudo enviar el correo de recuperación mediante Brevo.",
          e);

    } catch (RestClientException e) {

      logger.error(
          "Error de conexión con la API de Brevo.",
          e);

      throw new RuntimeException(
          "No se pudo enviar el correo de recuperación mediante Brevo.",
          e);
    }
  }
}
