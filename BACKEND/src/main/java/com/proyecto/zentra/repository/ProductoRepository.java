package com.proyecto.zentra.repository;

import com.proyecto.zentra.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // Spring genera la consulta solo por el nombre del método
    List<Producto> findByNombreContainingIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);
}
