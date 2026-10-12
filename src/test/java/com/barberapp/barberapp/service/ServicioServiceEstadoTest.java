package com.barberapp.barberapp.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.barberapp.barberapp.model.Barbero;
import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.ServicioRepository;
import com.barberapp.barberapp.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class ServicioServiceEstadoTest {

    private static final int ID_BARBERO = 7;
    private static final int ID_SERVICIO = 11;

    @Mock private ServicioRepository servicioRepository;
    @Mock private BarberoRepository barberoRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @InjectMocks private ServicioService servicioService;

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void desactivaSinEliminarNiModificarRelacionesHistoricas() {
        Servicio servicio = servicio("activo");
        autenticarBarbero();
        when(servicioRepository.findById(ID_SERVICIO)).thenReturn(Optional.of(servicio));
        when(servicioRepository.save(servicio)).thenReturn(servicio);

        servicioService.desactivarServicio(ID_SERVICIO);

        assertEquals("inactivo", servicio.getEstado());
        verify(servicioRepository).save(servicio);
        verify(servicioRepository, never()).delete(any(Servicio.class));
        verify(servicioRepository, never()).deleteById(ID_SERVICIO);
    }

    @Test
    void reactivaElMismoServicio() {
        Servicio servicio = servicio("inactivo");
        autenticarBarbero();
        when(servicioRepository.findById(ID_SERVICIO)).thenReturn(Optional.of(servicio));
        when(servicioRepository.save(servicio)).thenReturn(servicio);

        servicioService.activarServicio(ID_SERVICIO);

        assertEquals("activo", servicio.getEstado());
        verify(servicioRepository).save(servicio);
    }

    @Test
    void elListadoActivoSoloConsultaServiciosActivos() {
        Servicio activo = servicio("activo");
        when(servicioRepository.findByEstado("activo")).thenReturn(List.of(activo));

        List<Servicio> resultado = servicioService.listarServiciosActivos();

        assertEquals(List.of(activo), resultado);
        verify(servicioRepository).findByEstado("activo");
    }

    @Test
    void actualizaPrecioYDescripcionSinCambiarNombreNiDuracion() {
        Servicio actual = servicio("activo");
        Servicio cambios = servicio("activo");
        cambios.setNombre("Nombre manipulado");
        cambios.setPrecio(BigDecimal.valueOf(30000));
        cambios.setDuracion(15);
        cambios.setDescripcion("Descripcion premium");
        autenticarBarbero();
        when(servicioRepository.findById(ID_SERVICIO)).thenReturn(Optional.of(actual));
        when(servicioRepository.save(actual)).thenReturn(actual);

        Servicio resultado = servicioService.actualizarServicio(ID_SERVICIO, cambios);

        assertEquals("Arreglo de barba", resultado.getNombre());
        assertEquals(BigDecimal.valueOf(30000), resultado.getPrecio());
        assertEquals(30, resultado.getDuracion());
        assertEquals("Descripcion premium", resultado.getDescripcion());
    }

    private void autenticarBarbero() {
        Usuario usuario = new Usuario();
        usuario.setId(3);
        usuario.setRol("barbero");
        Barbero barbero = new Barbero();
        barbero.setId(ID_BARBERO);
        barbero.setUsuario(usuario);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(usuario, null, List.of()));
        when(barberoRepository.findByUsuarioId(3)).thenReturn(Optional.of(barbero));
    }

    private Servicio servicio(String estado) {
        Servicio servicio = new Servicio();
        servicio.setId(ID_SERVICIO);
        servicio.setIdBarbero(ID_BARBERO);
        servicio.setNombre("Arreglo de barba");
        servicio.setPrecio(BigDecimal.valueOf(25000));
        servicio.setDuracion(30);
        servicio.setDescripcion("Perfilado y arreglo de barba");
        servicio.setEstado(estado);
        return servicio;
    }
}
