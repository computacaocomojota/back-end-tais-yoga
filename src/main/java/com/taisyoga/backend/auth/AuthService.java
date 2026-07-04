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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
    private final EmailService emailService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AuthenticationManager authenticationManager,
                       EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
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

            emailService.enviarEmailRecuperacao(usuario.getEmail(), usuario.getNome(), codigo);
        } else {
            logger.warn("E-mail não encontrado para redefinição de senha: {}", dto.getEmail());
        }

        Map<String, String> resposta = new HashMap<>();
        resposta.put("mensagem", "Se o e-mail estiver cadastrado em nosso sistema, as instruções para redefinição de senha foram enviadas.");
        return resposta;
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

