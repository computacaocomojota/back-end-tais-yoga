package com.taisyoga.backend.auth;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    public EmailService(Optional<JavaMailSender> mailSenderOpt) {
        this.mailSender = mailSenderOpt.orElse(null);
    }

    @Async
    public void enviarEmailRecuperacao(String para, String nome, String codigo) {
        if (mailSender == null) {
            logger.warn("JavaMailSender não configurado ou indisponível. O e-mail não será enviado por rede, mas o código foi gerado no banco.");
            return;
        }
        try {
            logger.info("Iniciando envio assíncrono de e-mail de recuperação para: {}", para);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(Objects.requireNonNull(para));
            helper.setSubject("Taís Yoga ✨ Código de Recuperação de Senha");

            String htmlTemplate = """
                    <!DOCTYPE html>
                    <html lang="pt-BR">
                    <head>
                        <meta charset="UTF-8">
                        <link href="https://fonts.googleapis.com/css2?family=Cormorant+Garamond:wght@600;700&family=Open+Sans:wght@400;600&display=swap" rel="stylesheet">
                    </head>
                    <body style="margin: 0; padding: 0; font-family: 'Open Sans', Arial, sans-serif; background-color: #fafafa; color: #333333;">
                        <table role="presentation" width="100%%" border="0" cellpadding="0" cellspacing="0" style="background-color: #fafafa; padding: 40px 15px;">
                            <tr>
                                <td align="center">
                                    <table role="presentation" width="100%%" border="0" cellpadding="0" cellspacing="0" style="max-width: 520px; background-color: #ffffff; border-radius: 24px; border: 1px solid #e8edea; box-shadow: 0 8px 32px rgba(0,0,0,0.06); overflow: hidden;">
                                        <tr>
                                            <td style="background: linear-gradient(135deg, #547c6a 0%%, #436354 100%%); border-bottom: 3px solid #a47e4f; padding: 36px 30px; text-align: center;">
                                                <h1 style="margin: 0; font-family: 'Cormorant Garamond', 'Playfair Display', Georgia, serif; font-size: 30px; font-weight: 700; color: #ffffff; letter-spacing: 1px;">Taís Yoga</h1>
                                                <p style="margin: 6px 0 0; font-family: 'Open Sans', Arial, sans-serif; font-size: 12px; color: rgba(255, 255, 255, 0.9); letter-spacing: 2px; text-transform: uppercase;">Espaço de Yoga &amp; Bem-Estar</p>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style="padding: 40px 32px; text-align: left;">
                                                <h2 style="margin: 0 0 16px; font-family: 'Cormorant Garamond', 'Playfair Display', Georgia, serif; font-size: 24px; font-weight: 700; color: #547c6a;">Olá, %s! ✨</h2>
                                                <p style="margin: 0 0 24px; font-size: 15px; line-height: 1.6; color: #555555;">
                                                    Recebemos uma solicitação para redefinir a senha da sua conta no espaço <strong>Taís Yoga</strong>. Utilize o código de verificação abaixo para continuar:
                                                </p>
                                                <div style="background-color: #f2f6f4; border: 2px dashed #a47e4f; border-radius: 16px; padding: 24px; text-align: center; margin: 28px 0;">
                                                    <span style="font-family: 'Open Sans', Arial, sans-serif; font-size: 34px; font-weight: 700; letter-spacing: 8px; color: #547c6a;">%s</span>
                                                </div>
                                                <p style="margin: 0 0 12px; font-size: 14px; color: #707070;">
                                                    ⏱️ <em>Este código expira em <strong style="color: #333333;">15 minutos</strong>.</em>
                                                </p>
                                                <p style="margin: 0; font-size: 13px; color: #888888; line-height: 1.5;">
                                                    Se você não solicitou a redefinição de senha, por favor ignore este e-mail por segurança.
                                                </p>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td style="background-color: #fafafa; padding: 24px 32px; text-align: center; border-top: 1px solid #e8edea;">
                                                <p style="margin: 0; font-size: 13px; color: #707070;">
                                                    Com carinho, <strong style="color: #547c6a;">Equipe Taís Yoga</strong> ✨
                                                </p>
                                            </td>
                                        </tr>
                                    </table>
                                </td>
                            </tr>
                        </table>
                    </body>
                    </html>
                    """;

            String htmlContent = String.format(htmlTemplate, nome, codigo);
            helper.setText(Objects.requireNonNull(htmlContent), true);
            mailSender.send(message);
            logger.info("E-mail HTML de recuperação enviado com sucesso para: {}", para);
        } catch (Exception e) {
            logger.error("Erro ao tentar enviar e-mail via Gmail SMTP para {}: {}", para, e.getMessage());
        }
    }
}
