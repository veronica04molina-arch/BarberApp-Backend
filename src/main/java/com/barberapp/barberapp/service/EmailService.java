package com.barberapp.barberapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

  private final JavaMailSender mailSender;
  private final String frontendUrl;

  public EmailService(
      JavaMailSender mailSender,
      @Value("${FRONTEND_URL:http://localhost:4200}") String frontendUrl) {
    this.mailSender = mailSender;
    this.frontendUrl = frontendUrl;
  }

  public void enviarCorreoRecuperacion(String destinatario, String token) {

    String urlRecuperacion = frontendUrl + "/restablecer-password?token="
        + token
        + "&email="
        + destinatario;

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

                  <!-- HEADER -->
                  <tr>
                    <td align="center" style="background-color: #10296d; padding: 36px 20px; text-align: center;">

                      <div style="display: inline-block; background-color: #1f3fbf; padding: 14px; border-radius: 16px;">
                        <img src="cid:barberapp-logo"
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
            destinatario,
            urlRecuperacion,
            token,
            urlRecuperacion,
            urlRecuperacion);

    try {
      MimeMessage message = mailSender.createMimeMessage();

      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setTo(destinatario);
      helper.setSubject("🔒 Restablecer tu contraseña - BarberApp");
      helper.setText(contenidoHtml, true);

      // Cargar el logo desde resources/images
      ClassPathResource logo = new ClassPathResource("images/barberapp-logo.png");

      // Insertar el logo dentro del correo
      helper.addInline("barberapp-logo", logo, "image/png");

      mailSender.send(message);

    } catch (MessagingException e) {
      throw new RuntimeException(
          "Error al enviar el correo de recuperación: "
              + e.getMessage());
    }
  }
}
