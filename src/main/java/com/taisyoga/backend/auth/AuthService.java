package com.taisyoga.backend.auth;

import com.taisyoga.backend.exception.ResourceNotFoundException;
import com.taisyoga.backend.security.JwtService;
import com.taisyoga.backend.usuario.Usuario;
import com.taisyoga.backend.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import jakarta.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.security.SecureRandom;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final JavaMailSender mailSender;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       Optional<JavaMailSender> mailSender) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.mailSender = mailSender.orElse(null);
    }

    @Transactional
    public TokenResponseDTO cadastrar(CadastroRequestDTO dto) {
        logger.info("Realizando cadastro de novo usuário: {}", dto.getEmail());

        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("O e-mail informado já está em uso.");
        }

        Usuario novoUsuario = new Usuario(
                dto.getNome(),
                dto.getEmail(),
                passwordEncoder.encode(dto.getSenha())
        );

        usuarioRepository.save(novoUsuario);

        String jwtToken = jwtService.generateToken(novoUsuario);
        logger.info("Usuário cadastrado com sucesso com ID: {}", novoUsuario.getId());

        return new TokenResponseDTO(novoUsuario.getId(), jwtToken, novoUsuario.getEmail(), novoUsuario.getNome());
    }

    @Transactional(readOnly = true)
    public TokenResponseDTO login(LoginRequestDTO dto) {
        logger.info("Tentativa de login para o usuário: {}", dto.getEmail());

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getEmail(), dto.getSenha())
        );

        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        String jwtToken = jwtService.generateToken(usuario);
        logger.info("Login efetuado com sucesso para: {}", usuario.getEmail());

        return new TokenResponseDTO(usuario.getId(), jwtToken, usuario.getEmail(), usuario.getNome());
    }

    @Transactional
    public Map<String, String> esqueceuSenha(EsqueceuSenhaRequestDTO dto) {
        logger.info("Solicitação de redefinição de senha para: {}", dto.getEmail());

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(dto.getEmail());
        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            String codigo = String.format("%04d", SECURE_RANDOM.nextInt(10000));
            usuario.setCodigoRecuperacao(codigo);
            usuario.setExpiracaoCodigoRecuperacao(LocalDateTime.now().plusMinutes(15));
            usuarioRepository.save(usuario);

            logger.debug("=================================================");
            logger.debug("CÓDIGO DE RECUPERAÇÃO GERADO PARA {}: {}", usuario.getEmail(), codigo);
            logger.debug("=================================================");

            enviarEmailRecuperacao(usuario.getEmail(), usuario.getNome(), codigo);
        } else {
            logger.warn("E-mail não encontrado para redefinição de senha: {}", dto.getEmail());
        }

        Map<String, String> resposta = new HashMap<>();
        resposta.put("mensagem", "Se o e-mail estiver cadastrado em nosso sistema, as instruções para redefinição de senha foram enviadas.");
        return resposta;
    }

    private void enviarEmailRecuperacao(String para, String nome, String codigo) {
        if (mailSender == null) {
            logger.warn("JavaMailSender não configurado ou indisponível. O e-mail não será enviado por rede, mas o código está no log acima.");
            return;
        }
        try {
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
            helper.setText(Objects.requireNonNull(htmlContent), true); // true ativa o modo HTML no e-mail
            mailSender.send(message);
            logger.info("E-mail HTML de recuperação enviado com sucesso para: {}", para);
        } catch (Exception e) {
            logger.error("Erro ao tentar enviar e-mail via Gmail SMTP para {}: {}", para, e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Object> verificarCodigo(VerificarCodigoRequestDTO dto) {
        logger.info("Verificando código de recuperação para: {}", dto.getEmail());
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(dto.getEmail());

        boolean valido = false;
        String mensagem = "Código de verificação inválido ou expirado.";

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            if (usuario.getCodigoRecuperacao() != null &&
                usuario.getCodigoRecuperacao().equals(dto.getCodigo()) &&
                usuario.getExpiracaoCodigoRecuperacao() != null &&
                usuario.getExpiracaoCodigoRecuperacao().isAfter(LocalDateTime.now())) {
                valido = true;
                mensagem = "Código verificado com sucesso.";
            }
        }

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("valido", valido);
        resposta.put("mensagem", mensagem);
        return resposta;
    }

    @Transactional
    public Map<String, String> redefinirSenha(RedefinirSenhaRequestDTO dto) {
        logger.info("Redefinindo senha para: {}", dto.getEmail());
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(dto.getEmail());

        if (usuarioOpt.isPresent()) {
            Usuario usuario = usuarioOpt.get();
            boolean codigoValido = dto.getCodigo() != null &&
                    dto.getCodigo().equals(usuario.getCodigoRecuperacao()) &&
                    usuario.getExpiracaoCodigoRecuperacao() != null &&
                    usuario.getExpiracaoCodigoRecuperacao().isAfter(LocalDateTime.now());

            if (codigoValido) {
                usuario.setSenha(passwordEncoder.encode(dto.getNovaSenha()));
                usuario.setCodigoRecuperacao(null);
                usuario.setExpiracaoCodigoRecuperacao(null);
                usuarioRepository.save(usuario);
                logger.info("Senha redefinida com sucesso no banco para: {}", dto.getEmail());
            } else {
                throw new IllegalArgumentException("Código de verificação inválido ou expirado.");
            }
        } else {
            throw new IllegalArgumentException("Usuário não encontrado.");
        }

        Map<String, String> resposta = new HashMap<>();
        resposta.put("mensagem", "Senha redefinida com sucesso!");
        return resposta;
    }
}

