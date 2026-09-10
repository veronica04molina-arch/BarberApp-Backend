package com.barberapp.barberapp.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.barberapp.barberapp.dto.AgendaCitaDTO;
import com.barberapp.barberapp.model.Cita;
import com.barberapp.barberapp.model.CitaServicio;
import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.model.Usuario;
import com.barberapp.barberapp.repository.BarberoRepository;
import com.barberapp.barberapp.repository.CitaRepository;
import com.barberapp.barberapp.repository.CitaServicioRepository;
import com.barberapp.barberapp.repository.ServicioRepository;
import com.barberapp.barberapp.repository.UsuarioRepository;

@Service
public class AgendaService {

        private final CitaRepository citaRepository;
        private final BarberoRepository barberoRepository;
        private final UsuarioRepository usuarioRepository;
        private final CitaServicioRepository citaServicioRepository;
        private final ServicioRepository servicioRepository;

        public AgendaService(
                        CitaRepository citaRepository,
                        BarberoRepository barberoRepository,
                        UsuarioRepository usuarioRepository,
                        CitaServicioRepository citaServicioRepository,
                        ServicioRepository servicioRepository) {

                this.citaRepository = citaRepository;
                this.barberoRepository = barberoRepository;
                this.usuarioRepository = usuarioRepository;
                this.citaServicioRepository = citaServicioRepository;
                this.servicioRepository = servicioRepository;
        }

        // Obtener las citas del barbero desde hoy en adelante
        public List<AgendaCitaDTO> obtenerAgendaBarbero(Integer idBarbero) {

                // Verificar que el barbero exista
                if (!barberoRepository.existsById(idBarbero)) {
                        throw new RuntimeException(
                                        "El barbero con ID " + idBarbero + " no existe");
                }

                // Obtener la fecha actual
                LocalDate hoy = LocalDate.now();

                // Buscar citas desde hoy en adelante
                List<Cita> citas = citaRepository
                                .findByIdBarberoAndFechaGreaterThanEqualOrderByFechaAscHoraAsc(
                                                idBarbero,
                                                hoy);

                List<AgendaCitaDTO> agenda = new ArrayList<>();

                for (Cita cita : citas) {

                        // Buscar el cliente
                        Usuario usuario = usuarioRepository
                                        .findById(cita.getIdUsuario())
                                        .orElse(null);

                        String nombreCliente = usuario != null
                                        ? usuario.getNombre()
                                        : "Cliente no encontrado";

                        // Buscar el servicio asociado a la cita
                        List<CitaServicio> relaciones = citaServicioRepository
                                        .findByIdCita(cita.getId());

                        String nombreServicio = "Servicio no encontrado";

                        if (!relaciones.isEmpty()) {

                                CitaServicio relacion = relaciones.get(0);

                                Servicio servicio = servicioRepository
                                                .findById(relacion.getIdServicio())
                                                .orElse(null);

                                if (servicio != null) {
                                        nombreServicio = servicio.getNombre();
                                }
                        }

                        // Crear el DTO
                        AgendaCitaDTO dto = new AgendaCitaDTO(
                                        cita.getId(),
                                        cita.getFecha(),
                                        cita.getHora(),
                                        nombreCliente,
                                        nombreServicio,
                                        cita.getEstado(),
                                        cita.getNotas());

                        agenda.add(dto);
                }

                return agenda;
        }
}