package com.taisyoga.backend.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<TokenResponseDTO> cadastrar(@Valid @RequestBody CadastroRequestDTO dto) {
        TokenResponseDTO response = authService.cadastrar(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDTO> login(@Valid @RequestBody LoginRequestDTO dto) {
        TokenResponseDTO response = authService.login(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/esqueceu-senha")
    public ResponseEntity<Map<String, String>> esqueceuSenha(@Valid @RequestBody EsqueceuSenhaRequestDTO dto) {
        Map<String, String> response = authService.esqueceuSenha(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verificar-codigo")
    public ResponseEntity<Map<String, Object>> verificarCodigo(@Valid @RequestBody VerificarCodigoRequestDTO dto) {
        Map<String, Object> response = authService.verificarCodigo(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Map<String, String>> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequestDTO dto) {
        Map<String, String> response = authService.redefinirSenha(dto);
        return ResponseEntity.ok(response);
    }
}

