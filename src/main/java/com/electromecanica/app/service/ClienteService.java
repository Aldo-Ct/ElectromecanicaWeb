package com.electromecanica.app.service;

import com.electromecanica.app.dto.ClienteDTO;
import com.electromecanica.app.entity.AccionAuditoria;
import com.electromecanica.app.entity.Cliente;
import com.electromecanica.app.entity.Usuario;
import com.electromecanica.app.repository.ClienteRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {
    private final ClienteRepository clienteRepository;
    private final AuditoriaService auditoriaService;

    public ClienteService(ClienteRepository clienteRepository, AuditoriaService auditoriaService) {
        this.clienteRepository = clienteRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<ClienteDTO> listar() {
        return clienteRepository.findAll(Sort.by(Sort.Direction.DESC, "id")).stream().map(this::convertir).toList();
    }

    @Transactional
    public ClienteDTO crear(ClienteDTO dto, Usuario usuario, HttpServletRequest solicitudHttp) {
        validarNombre(dto);
        clienteRepository.findByNumeroDocumento(dto.numeroDocumento()).ifPresent(c -> {
            throw new IllegalArgumentException("Ya existe un cliente con ese número de documento");
        });
        Cliente cliente = new Cliente();
        copiar(dto, cliente);
        clienteRepository.save(cliente);
        auditoriaService.registrar(AccionAuditoria.CREACION, usuario, "Cliente", cliente.getId(),
                "Cliente creado: " + cliente.getNombreCompleto(), null, cliente.getNumeroDocumento(), solicitudHttp);
        return convertir(cliente);
    }

    @Transactional
    public ClienteDTO actualizar(Long id, ClienteDTO dto, Usuario usuario, HttpServletRequest solicitudHttp) {
        validarNombre(dto);
        Cliente cliente = obtener(id);
        clienteRepository.findByNumeroDocumento(dto.numeroDocumento()).filter(c -> !c.getId().equals(id)).ifPresent(c -> {
            throw new IllegalArgumentException("Ya existe un cliente con ese número de documento");
        });
        String anterior = cliente.getNumeroDocumento();
        copiar(dto, cliente);
        auditoriaService.registrar(AccionAuditoria.ACTUALIZACION, usuario, "Cliente", id,
                "Cliente actualizado", anterior, cliente.getNumeroDocumento(), solicitudHttp);
        return convertir(cliente);
    }

    @Transactional
    public void desactivar(Long id, Usuario usuario, HttpServletRequest solicitudHttp) {
        Cliente cliente = obtener(id);
        cliente.setActivo(false);
        auditoriaService.registrar(AccionAuditoria.CAMBIO_ESTADO, usuario, "Cliente", id,
                "Cliente desactivado", "activo", "inactivo", solicitudHttp);
    }

    public Cliente obtener(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado"));
    }

    private void validarNombre(ClienteDTO dto) {
        boolean empresaSinNombre = dto.tipoDocumento() == com.electromecanica.app.entity.TipoDocumento.RUC
                && (dto.razonSocial() == null || dto.razonSocial().isBlank());
        boolean personaSinNombre = dto.tipoDocumento() != com.electromecanica.app.entity.TipoDocumento.RUC
                && (dto.nombres() == null || dto.nombres().isBlank());
        if (empresaSinNombre || personaSinNombre) {
            throw new IllegalArgumentException("Indique la razón social o los nombres del cliente");
        }
    }

    private void copiar(ClienteDTO dto, Cliente cliente) {
        cliente.setTipoDocumento(dto.tipoDocumento());
        cliente.setNumeroDocumento(dto.numeroDocumento().trim());
        cliente.setRazonSocial(dto.razonSocial());
        cliente.setNombres(dto.nombres());
        cliente.setApellidos(dto.apellidos());
        cliente.setCorreo(dto.correo());
        cliente.setTelefono(dto.telefono());
        cliente.setDireccion(dto.direccion());
        cliente.setActivo(dto.activo() == null || dto.activo());
    }

    private ClienteDTO convertir(Cliente cliente) {
        return new ClienteDTO(cliente.getId(), cliente.getTipoDocumento(), cliente.getNumeroDocumento(),
                cliente.getRazonSocial(), cliente.getNombres(), cliente.getApellidos(), cliente.getCorreo(),
                cliente.getTelefono(), cliente.getDireccion(), cliente.getActivo(), cliente.getNombreCompleto());
    }
}
