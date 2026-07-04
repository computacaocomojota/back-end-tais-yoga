package com.taisyoga.backend.config;

import com.taisyoga.backend.agendamento.Agendamento;
import com.taisyoga.backend.agendamento.AgendamentoRepository;
import com.taisyoga.backend.usuario.Usuario;
import com.taisyoga.backend.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

@Configuration
public class DataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Bean
    CommandLineRunner initDatabase(AgendamentoRepository agendamentoRepository,
                                   UsuarioRepository usuarioRepository,
                                   PasswordEncoder passwordEncoder) {
        return args -> {
            Usuario u1;
            if (usuarioRepository.count() == 0) {
                u1 = new Usuario(
                        "Administrador Taís Yoga",
                        "admin@taisyoga.com.br",
                        passwordEncoder.encode("123456")
                );
                u1 = usuarioRepository.save(u1);
                logger.info("✅ [DataSeeder] Usuário padrão criado (admin@taisyoga.com.br / senha: 123456)");
            } else {
                u1 = usuarioRepository.findAll().get(0);
            }

            List<Agendamento> agendamentosExistentes = agendamentoRepository.findAll();
            List<Agendamento> exemplosParaRemover = agendamentosExistentes.stream()
                    .filter(a -> {
                        String titulo = a.getTituloAula() != null ? a.getTituloAula() : "";
                        String data = a.getDataAula() != null ? a.getDataAula() : "";
                        String obs = a.getObservacoes() != null ? a.getObservacoes() : "";
                        return (titulo.equals("Yoga Relaxante") && data.contains("12")) ||
                               (titulo.equals("Flow Matinal") && data.contains("14")) ||
                               (titulo.equals("Despertar Suave") && data.contains("10")) ||
                               obs.contains("alívio de tensões") ||
                               obs.contains("Prática energizante via Zoom") ||
                               obs.contains("Cancelado por imprevisto");
                    })
                    .toList();
            if (!exemplosParaRemover.isEmpty()) {
                agendamentoRepository.deleteAll(exemplosParaRemover);
                logger.info("🧹 [DataSeeder] {} agendamento(s) de exemplo removido(s) do banco de dados.", exemplosParaRemover.size());
            }
        };
    }
}
