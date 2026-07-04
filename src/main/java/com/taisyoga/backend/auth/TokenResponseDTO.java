package com.taisyoga.backend.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponseDTO {
    private Long id;
    private String token;
    private String tipo;
    private String email;
    private String nome;

    public TokenResponseDTO(Long id, String token, String email, String nome) {
        this.id = id;
        this.token = token;
        this.tipo = "Bearer";
        this.email = email;
        this.nome = nome;
    }
}
