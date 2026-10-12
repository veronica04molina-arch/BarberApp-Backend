package com.barberapp.barberapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.barberapp.barberapp.dto.CitaClienteDTO;
import com.barberapp.barberapp.dto.CitaRequest;
import com.barberapp.barberapp.model.Barbero;
import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.model.CitaServicio;
import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.CitaServicioRepository;
import com.barberapp.barberapp.repository.ServicioRepository;
import com.barberapp.barberapp.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class CitaServicioHistoricoTest {

    @Mock private CitaRepository citaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private BarberoRepository barberoRepository;
    @Mock private ServicioRepository servicioRepository;
    @Mock private CitaServicioRepository citaServicioRepository;
    @Mock private DisponibilidadService disponibilidadService;
    @Mock private NotificacionService notificacionService;
    @InjectMocks private CitaService citaService;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void unaCitaHistoricaConservaElNombreDeUnServicioInactivo() {
        Usuario cliente = usuario(4, "Cliente", "cliente");
        Cita cita = new Cita();
        cita.setId(31);
        cita.setIdUsuario(4);
        cita.setIdBarbero(7);
        cita.setFecha(LocalDate.of(2026, 10, 5));
        cita.setHora(LocalTime.of(17, 30));
        cita.setEstado("confirmada");

        CitaServicio relacion = new CitaServicio();
        relacion.setIdCita(31);
        relacion.setIdServicio(11);

        Servicio servicioInactivo = new Servicio();
        servicioInactivo.setId(11);
        servicioInactivo.setNombre("Arreglo de barba");
        servicioInactivo.setEstado("inactivo");
        servicioInactivo.setPrecio(BigDecimal.valueOf(25000));
        servicioInactivo.setDuracion(30);

        Barbero barbero = new Barbero();
        barbero.setId(7);
        barbero.setUsuario(usuario(8, "Ana", "barbero"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cliente, null));
        when(citaRepository.findByIdUsuario(4)).thenReturn(List.of(cita));
        when(citaServicioRepository.findByIdCita(31)).thenReturn(List.of(relacion));
        when(servicioRepository.findById(11)).thenReturn(Optional.of(servicioInactivo));
        when(barberoRepository.findById(7)).thenReturn(Optional.of(barbero));

        List<CitaClienteDTO> resultado = citaService.listarCitasCliente(4);

        assertEquals(List.of("Arreglo de barba"), resultado.getFirst().getServicios());
    }

    @Test
    void nuevaCitaGuardaUnaInstantaneaDeLosValoresDelServicio() {
        Usuario cliente = usuario(4, "Cliente", "cliente");
        Servicio servicio = new Servicio();
        servicio.setId(11);
        servicio.setIdBarbero(7);
        servicio.setNombre("Corte clasico");
        servicio.setPrecio(BigDecimal.valueOf(25000));
        servicio.setDuracion(30);
        servicio.setDescripcion("Corte clasico");
        servicio.setEstado("activo");
        Barbero barbero = new Barbero();
        barbero.setId(7);
        barbero.setEstado("activo");
        barbero.setUsuario(usuario(8, "Ana", "barbero"));
        Cita citaGuardada = new Cita();
        citaGuardada.setId(31);
        citaGuardada.setIdUsuario(4);
        citaGuardada.setIdBarbero(7);
        citaGuardada.setFecha(LocalDate.now().plusDays(2));
        citaGuardada.setHora(LocalTime.of(10, 0));

        CitaRequest request = new CitaRequest();
        request.setIdUsuario(4);
        request.setIdBarbero(7);
        request.setIdServicios(List.of(11));
        request.setFecha(LocalDate.now().plusDays(2));
        request.setHora(LocalTime.of(10, 0));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(cliente, null));
        when(usuarioRepository.findById(4)).thenReturn(Optional.of(cliente));
        when(barberoRepository.findByIdForUpdate(7)).thenReturn(Optional.of(barbero));
        when(barberoRepository.findById(7)).thenReturn(Optional.of(barbero));
        when(servicioRepository.findById(11)).thenReturn(Optional.of(servicio));
        when(disponibilidadService.estaDisponible(7, request.getFecha(), request.getHora(), 30)).thenReturn(true);
        when(citaRepository.save(any(Cita.class))).thenReturn(citaGuardada);

        citaService.guardarCita(request);

        ArgumentCaptor<CitaServicio> relacion = ArgumentCaptor.forClass(CitaServicio.class);
        verify(citaServicioRepository).save(relacion.capture());
        assertEquals(31, relacion.getValue().getIdCita());
        assertEquals(11, relacion.getValue().getIdServicio());
        assertEquals(BigDecimal.valueOf(25000), relacion.getValue().getPrecioAplicado());
        assertEquals("Corte clasico", relacion.getValue().getDescripcionAplicada());
    }

    private Usuario usuario(int id, String nombre, String rol) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre(nombre);
        usuario.setRol(rol);
        return usuario;
    }
}
