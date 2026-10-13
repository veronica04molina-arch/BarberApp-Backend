package com.barberapp.barberapp.integration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.barberapp.barberapp.auth.JwtService;
import com.barberapp.barberapp.model.Barbero;
import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.model.ExcepcionHorario;
import com.barberapp.barberapp.model.HorarioSemanal;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.ExcepcionHorarioRepository;
import com.barberapp.barberapp.repository.HorarioSemanalRepository;
import com.barberapp.barberapp.repository.SesionRepository;
import com.barberapp.barberapp.repository.UsuarioRepository;
import com.barberapp.barberapp.service.SesionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
abstract class AuthorizationIntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private BarberoRepository barberoRepository;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private HorarioSemanalRepository horarioSemanalRepository;

    @Autowired
    private ExcepcionHorarioRepository excepcionHorarioRepository;

    @Autowired
    private SesionRepository sesionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private SesionService sesionService;

    protected Usuario clienteA;
    protected Usuario clienteB;
    protected Usuario clienteConCita;
    protected Usuario clienteSinCita;
    protected Usuario usuarioBarberoA;
    protected Usuario usuarioBarberoB;
    protected Barbero barberoA;
    protected Barbero barberoB;

    @BeforeEach
    void prepararDatos() {
        limpiarDatos();

        clienteA = crearUsuario("cliente-a", "cliente");
        clienteB = crearUsuario("cliente-b", "cliente");
        clienteConCita = crearUsuario("cliente-con-cita", "cliente");
        clienteSinCita = crearUsuario("cliente-sin-cita", "cliente");
        usuarioBarberoA = crearUsuario("barbero-a", "barbero");
        usuarioBarberoB = crearUsuario("barbero-b", "barbero");
        barberoA = crearBarbero(usuarioBarberoA, "Cortes", 5);
        barberoB = crearBarbero(usuarioBarberoB, "Barba", 7);
    }

    @AfterEach
    void limpiarDespuesDeCadaPrueba() {
        limpiarDatos();
    }

    protected String autorizacion(Usuario usuario) {
        String token = jwtService.generarToken(usuario);
        sesionService.registrarSesion(usuario.getId(), token);
        return "Bearer " + token;
    }

    protected String json(Object cuerpo) throws JsonProcessingException {
        return objectMapper.writeValueAsString(cuerpo);
    }

    protected MediaType jsonMediaType() {
        return MediaType.APPLICATION_JSON;
    }

    protected Cita crearCita(Usuario cliente, Barbero barbero) {
        Cita cita = new Cita();
        cita.setIdUsuario(cliente.getId());
        cita.setIdBarbero(barbero.getId());
        cita.setFecha(LocalDate.now().plusDays(2));
        cita.setHora(LocalTime.of(10, 0));
        cita.setEstado("pendiente");
        cita.setFechaCreacion(LocalDateTime.now());
        return citaRepository.save(cita);
    }

    protected HorarioSemanal crearHorario(Barbero barbero) {
        HorarioSemanal horario = new HorarioSemanal();
        horario.setIdBarbero(barbero.getId());
        horario.setDiaSemana("LUNES");
        horario.setHoraInicio(LocalTime.of(9, 0));
        horario.setHoraFin(LocalTime.of(12, 0));
        horario.setEstado("disponible");
        return horarioSemanalRepository.save(horario);
    }

    protected ExcepcionHorario crearExcepcion(Barbero barbero) {
        ExcepcionHorario excepcion = new ExcepcionHorario();
        excepcion.setIdBarbero(barbero.getId());
        excepcion.setFecha(LocalDate.now().plusDays(10));
        excepcion.setTipo("NO_DISPONIBLE");
        excepcion.setMotivo("Prueba");
        return excepcionHorarioRepository.save(excepcion);
    }

    protected Map<String, Object> cuerpoHorario(Integer idBarbero) {
        return Map.of(
                "idBarbero", idBarbero,
                "diaSemana", "MARTES",
                "horaInicio", "09:00",
                "horaFin", "12:00",
                "estado", "disponible");
    }

    protected Map<String, Object> cuerpoExcepcion(Integer idBarbero) {
        return Map.of(
                "idBarbero", idBarbero,
                "fecha", LocalDate.now().plusDays(12).toString(),
                "tipo", "NO_DISPONIBLE",
                "motivo", "Prueba");
    }

    private Usuario crearUsuario(String sufijo, String rol) {
        Usuario usuario = new Usuario();
        usuario.setNombre("Usuario " + sufijo);
        usuario.setEmail(sufijo + "@test.local");
        usuario.setTelefono("300000" + String.format("%04d", Math.abs(sufijo.hashCode()) % 10000));
        usuario.setPassword(passwordEncoder.encode("password-seguro"));
        usuario.setRol(rol);
        usuario.setFechaRegistro(LocalDate.now());
        return usuarioRepository.save(usuario);
    }

    private Barbero crearBarbero(Usuario usuario, String especialidad, int experiencia) {
        Barbero barbero = new Barbero();
        barbero.setUsuario(usuario);
        barbero.setEspecialidad(especialidad);
        barbero.setExperiencia(experiencia);
        barbero.setEstado("activo");
        return barberoRepository.save(barbero);
    }

    private void limpiarDatos() {
        sesionRepository.deleteAll();
        citaRepository.deleteAll();
        horarioSemanalRepository.deleteAll();
        excepcionHorarioRepository.deleteAll();
        barberoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }
}
