package com.proyecto.zentra.service;

import com.proyecto.zentra.model.Producto;
import com.proyecto.zentra.repository.ProductoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository repository;

    // Spring entrega el repository solo (inyección de dependencias)
    public ProductoService(ProductoRepository repository) {
        this.repository = repository;
    }

    public List<Producto> listar() {
        return repository.findAll();
    }

    public Optional<Producto> buscarPorId(Long id) {
        return repository.findById(id);
    }

    public List<Producto> buscarPorNombre(String texto) {
        return repository.findByNombreContainingIgnoreCase(texto);
    }

    public Producto crear(Producto producto) {
        // Regla de negocio: no puede haber dos productos con el mismo nombre
        if (repository.existsByNombreIgnoreCase(producto.getNombre())) {
            throw new IllegalArgumentException("Ya existe un producto con ese nombre");
        }
        producto.setId(null); // así siempre se crea uno nuevo
        return repository.save(producto);
    }

    public Optional<Producto> actualizar(Long id, Producto datos) {
        return repository.findById(id).map(existente -> {
            existente.setNombre(datos.getNombre());
            existente.setCategoria(datos.getCategoria());
            existente.setPrecio(datos.getPrecio());
            existente.setStock(datos.getStock());
            existente.setStockMinimo(datos.getStockMinimo());
            return repository.save(existente);
        });
    }

    public boolean eliminar(Long id) {
        if (!repository.existsById(id)) {
            return false;
        }
        repository.deleteById(id);
        return true;
    }
}
