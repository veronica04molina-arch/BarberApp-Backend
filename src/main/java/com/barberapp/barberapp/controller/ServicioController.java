package com.barberapp.barberapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.barberapp.barberapp.model.Servicio;
import com.barberapp.barberapp.service.ServicioService;

@RestController
@RequestMapping("/servicios")
@CrossOrigin(origins = "*")
public class ServicioController {

    @Autowired
    private ServicioService servicioService;

    // Obtener todos los servicios
    @GetMapping
    public List<Servicio> listarServicios() {
        return servicioService.listarServicios();
    }

    // Obtener solo los servicios activos
    @GetMapping("/activos")
    public List<Servicio> listarServiciosActivos() {
        return servicioService.listarServiciosActivos();
    }

    // Registrar un servicio
    @PostMapping
    public Servicio guardarServicio(@RequestBody Servicio servicio) {
        return servicioService.guardarServicio(servicio);
    }

    // Actualizar un servicio
    @PutMapping("/{id}")
    public Servicio actualizarServicio(@PathVariable Integer id,
                                    @RequestBody Servicio servicio) {

        servicio.setId(id);

        return servicioService.guardarServicio(servicio);
    }

    // Eliminar un servicio
    @DeleteMapping("/{id}")
    public void eliminarServicio(@PathVariable Integer id) {
        servicioService.eliminarServicio(id);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Servicio> obtenerServicio(@PathVariable Integer id) {

    Servicio servicio = servicioService.obtenerServicio(id);

    if (servicio == null) {
        return ResponseEntity.notFound().build();
    }

    return ResponseEntity.ok(servicio);
    }
}