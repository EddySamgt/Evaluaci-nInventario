package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.math.BigDecimal;

/**
 * Entidad que representa un producto en el inventario.
 * <p>
 * Esta clase es un POYO simple que se serializa/deserializa con Jackson.
 * No contiene lógica de negocio.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Producto {

    private Long id;
    private String nombre;
    private String categoria;
    private Integer cantidad;
    private BigDecimal precio;
    private Estado estado;

    // Constructor vacío requerido por Jackson
    public Producto() {
    }

    // Constructor con todos los campos para conveniencia
    public Producto(Long id, String nombre, String categoria, Integer cantidad, BigDecimal precio, Estado estado) {
        this.id = id;
        this.nombre = nombre;
        this.categoria = categoria;
        this.cantidad = cantidad;
        this.precio = precio;
        this.estado = estado;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Producto{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", categoria='" + categoria + '\'' +
                ", cantidad=" + cantidad +
                ", precio=" + precio +
                ", estado=" + estado +
                '}';
    }

    /**
     * Estados posibles de un producto.
     */
    public enum Estado {
        DISPONIBLE,
        AGOTADO,
        DESCONTINUADO
    }
}