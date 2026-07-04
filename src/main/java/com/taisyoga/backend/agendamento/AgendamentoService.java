package com.taisyoga.backend.agendamento;

import com.taisyoga.backend.exception.ResourceNotFoundException;
import com.taisyoga.backend.usuario.Usuario;
import com.taisyoga.backend.usuario.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class AgendamentoService {

    private static final Logger logger = LoggerFactory.getLogger(AgendamentoService.class);

    private final AgendamentoRepository repository;
    private final UsuarioRepository usuarioRepository;

    public AgendamentoService(AgendamentoRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<AgendamentoDTO> listarTodos() {
        logger.info("Buscando agendamentos no banco de dados.");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            logger.info("Filtrando agendamentos para o usuário autenticado: {}", auth.getName());
            return usuarioRepository.findByEmail(auth.getName())
                    .map(u -> listarPorUsuario(u.getId()))
                    .orElse(List.of());
        }
        return repository.findAll().stream()
                .map(AgendamentoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AgendamentoDTO> listarPorUsuario(Long usuarioId) {
        logger.info("Buscando agendamentos para o usuário ID: {}", usuarioId);
        return repository.findByUsuarioId(Objects.requireNonNull(usuarioId)).stream()
                .map(AgendamentoDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AgendamentoDTO buscarPorId(Long id) {
        logger.info("Buscando agendamento por ID: {}", id);
        Agendamento agendamento = repository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + id));
        return AgendamentoDTO.fromEntity(agendamento);
    }

    @Transactional
    public AgendamentoDTO criar(AgendamentoDTO dto) {
        logger.info("Criando novo agendamento para aula: {}", dto.getTituloAula());
        Agendamento entidade = dto.toEntity();
        entidade.setId(null); // Garante a criação de um novo registro

        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(Objects.requireNonNull(dto.getUsuarioId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
            entidade.setUsuario(usuario);
        } else {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                usuarioRepository.findByEmail(auth.getName()).ifPresent(entidade::setUsuario);
            }
        }

        Agendamento salvo = repository.save(Objects.requireNonNull(entidade));
        logger.info("Agendamento criado com sucesso! ID: {}", salvo.getId());
        return AgendamentoDTO.fromEntity(salvo);
    }

    @Transactional
    public AgendamentoDTO atualizar(Long id, AgendamentoDTO dto) {
        logger.info("Atualizando agendamento com ID: {}", id);
        Agendamento existente = repository.findById(Objects.requireNonNull(id))
                .orElseThrow(() -> new ResourceNotFoundException("Agendamento não encontrado com ID: " + id));

        if (dto.getTituloAula() != null) existente.setTituloAula(dto.getTituloAula());
        if (dto.getModalidade() != null) existente.setModalidade(dto.getModalidade());
        if (dto.getDataAula() != null) existente.setDataAula(dto.getDataAula());
        if (dto.getHorario() != null) existente.setHorario(dto.getHorario());
        if (dto.getInstrutor() != null) existente.setInstrutor(dto.getInstrutor());
        if (dto.getStatus() != null) existente.setStatus(dto.getStatus());
        if (dto.getObservacoes() != null) existente.setObservacoes(dto.getObservacoes());

        if (dto.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(Objects.requireNonNull(dto.getUsuarioId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + dto.getUsuarioId()));
            existente.setUsuario(usuario);
        }

        Agendamento atualizado = repository.save(Objects.requireNonNull(existente));
        logger.info("Agendamento ID: {} atualizado com sucesso.", id);
        return AgendamentoDTO.fromEntity(atualizado);
    }

    @Transactional
    public void deletar(Long id) {
        logger.info("Removendo agendamento com ID: {}", id);
        if (!repository.existsById(Objects.requireNonNull(id))) {
            throw new ResourceNotFoundException("Agendamento não encontrado com ID: " + id);
        }
        repository.deleteById(Objects.requireNonNull(id));
        logger.info("Agendamento ID: {} removido com sucesso.", id);
    }
}
