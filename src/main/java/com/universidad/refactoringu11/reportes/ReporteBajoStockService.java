
package com.universidad.refactoringu11.reportes;

import com.universidad.refactoringu11.inventario.NotificadorBajoStock;
import com.universidad.refactoringu11.inventario.Producto;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReporteBajoStockService implements NotificadorBajoStock {

    // Almacena las alertas pendientes para el próximo reporte
    private final List<String> alertas = new ArrayList<>();

    @Override
    public void notificar(Producto producto, int cantidadReorden) {

        String alerta = "Producto: " + producto.getNombre()
                + " | ID: " + producto.getId()
                + " | Stock actual: " + producto.getStock()
                + " | Stock minimo: " + producto.getStockMinimo()
                + " | Cantidad a reordenar: " + cantidadReorden;

        alertas.add(alerta);

        System.out.println("Alerta registrada para el reporte: "
                + producto.getNombre());
    }

    public String generarReporte() {

        if (alertas.isEmpty()) {
            return "===== REPORTE DE BAJO STOCK =====\n"
                    + "No hay productos con alertas pendientes.\n"
                    + "=================================";
        }

        StringBuilder reporte = new StringBuilder();

        reporte.append("===== REPORTE DE BAJO STOCK =====\n");
        reporte.append("Total de alertas: ")
                .append(alertas.size())
                .append("\n\n");

        for (int i = 0; i < alertas.size(); i++) {
            reporte.append(i + 1)
                    .append(". ")
                    .append(alertas.get(i))
                    .append("\n");
        }

        reporte.append("=================================");

        // Se limpian las alertas después de generar el reporte
        alertas.clear();

        return reporte.toString();
    }
}
