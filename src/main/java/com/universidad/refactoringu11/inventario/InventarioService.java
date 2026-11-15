
package com.universidad.refactoringu11.inventario;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.universidad.refactoringu11.reportes.ReporteBajoStockService;
import org.springframework.context.annotation.Lazy;
import java.util.ArrayList;
import java.util.List;

@Service
public class InventarioService {

    private final List<Producto> productos = new ArrayList<>();
    private final List<String> movimientos = new ArrayList<>();
    private ReporteBajoStockService reporteBajoStockService; // colaborador opcional, sin final

    @Autowired(required = false)
    @Lazy
    public void setReporteBajoStockService(ReporteBajoStockService reporteBajoStockService) {
        this.reporteBajoStockService = reporteBajoStockService;
    }

    public void agregarProducto(Producto producto) {
        productos.add(producto);
    }

    // Coordina el proceso de actualización del inventario
    public String actualizarInventario(Long productoId, int cantidad,
                                        String tipoMovimiento) {

        if (!datosEntradaValidos(productoId, cantidad, tipoMovimiento)) {
            return "ERROR_DATOS_INVALIDOS";
        }

        Producto producto = buscarProducto(productoId);

        if (producto == null) {
            return "ERROR_PRODUCTO_NO_EXISTE";
        }

        int nuevoStock = calcularNuevoStock(
                producto, cantidad, tipoMovimiento);

        if (tipoMovimiento.equals("SALIDA") && nuevoStock < 0) {
            return "ERROR_STOCK_INSUFICIENTE";
        }

        producto.setStock(nuevoStock);

        boolean necesitaReorden =
                nuevoStock < producto.getStockMinimo();

        int cantidadReorden =
                calcularCantidadReorden(producto, nuevoStock);

        registrarMovimiento(
                producto, cantidad, tipoMovimiento, nuevoStock);

        return generarRespuestaInventario(
                producto, nuevoStock, necesitaReorden, cantidadReorden);
    }

    // 1. Validación de los datos
    private boolean datosEntradaValidos(Long productoId, int cantidad,
                                        String tipoMovimiento) {

        return productoId != null
                && cantidad != 0
                && (tipoMovimiento.equals("ENTRADA")
                || tipoMovimiento.equals("SALIDA"));
    }

    // 2. Búsqueda del producto
    private Producto buscarProducto(Long productoId) {

        for (Producto producto : productos) {
            if (producto.getId().equals(productoId)) {
                return producto;
            }
        }

        return null;
    }

    // 3. Cálculo del nuevo stock
    private int calcularNuevoStock(Producto producto, int cantidad,
                                   String tipoMovimiento) {

        if (tipoMovimiento.equals("ENTRADA")) {
            return producto.getStock() + cantidad;
        }

        return producto.getStock() - cantidad;
    }

    // 4. Cálculo de la cantidad de reorden
    private int calcularCantidadReorden(Producto producto, int nuevoStock) {

        if (nuevoStock < producto.getStockMinimo()) {
            return (producto.getStockMinimo() * 3) - nuevoStock;
        }

        return 0;
    }

    // 5. Registro del movimiento
    private void registrarMovimiento(Producto producto, int cantidad,
                                     String tipoMovimiento, int nuevoStock) {

        String registro = tipoMovimiento + " de " + cantidad
                + " unidades de " + producto.getNombre()
                + " -> stock final: " + nuevoStock;

        movimientos.add(registro);
        System.out.println(registro);
    }

    // 6. Generación de respuesta y alerta
    private String generarRespuestaInventario(
            Producto producto, int nuevoStock,
            boolean necesitaReorden, int cantidadReorden) {

        if (necesitaReorden) {
             if (reporteBajoStockService != null) {
            System.out.println(reporteBajoStockService.generarReporte());
            }
            System.out.println("ALERTA: " + producto.getNombre()
                    + " bajo stock minimo. Reordenar "
                    + cantidadReorden + " unidades.");

            return "OK_BAJO_STOCK_REORDEN_" + cantidadReorden;
        }

        return "OK_" + nuevoStock;
    }

   
   
}
