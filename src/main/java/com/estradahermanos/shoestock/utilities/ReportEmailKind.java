package com.estradahermanos.shoestock.utilities;

import com.estradahermanos.shoestock.error.BusinessException;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum ReportEmailKind
{
    SUMMARY("summary", "", "Resumen", "reporte-resumen.pdf"),
    SUMMARY_NEW_STYLES("summary", "new-styles", "Resumen · Estilos nuevos", "reporte-estilos-nuevos.pdf"),
    SALES_TOP_PRODUCTS("sales", "top-products", "Ventas · Más vendidos", "reporte-ventas-mas-vendidos.pdf"),
    SALES_BOTTOM_PRODUCTS("sales", "bottom-products", "Ventas · Menos vendidos", "reporte-ventas-menos-vendidos.pdf"),
    SALES_BY_PERIOD("sales", "by-period", "Ventas · Detalle del período", "reporte-ventas-periodo.pdf"),
    SALES_BY_CATEGORY("sales", "by-category", "Ventas · Por categoría", "reporte-ventas-categoria.pdf"),
    SALES_BY_SIZE("sales", "by-size", "Ventas · Por talla", "reporte-ventas-talla.pdf"),
    SALES_BY_SUPPLIER("sales", "by-supplier", "Ventas · Por proveedor", "reporte-ventas-proveedor.pdf"),
    SALES_VOLUME("sales", "volume", "Ventas · Volumen", "reporte-ventas-volumen.pdf"),
    INVENTORY_STATUS("inventory", "status", "Inventario · Estado actual", "reporte-inventario-estado.pdf"),
    INVENTORY_BY_GROUP("inventory", "by-group", "Inventario · Por grupo", "reporte-inventario-grupo.pdf"),
    INVENTORY_LOW_STOCK("inventory", "low-stock", "Inventario · Stock bajo", "reporte-inventario-stock-bajo.pdf"),
    INVENTORY_OUT_OF_STOCK("inventory", "out-of-stock", "Inventario · Agotados", "reporte-inventario-agotados.pdf"),
    INVENTORY_SLOW_MOVERS("inventory", "slow-movers", "Inventario · Sin rotación", "reporte-inventario-sin-rotacion.pdf"),
    INVENTORY_RESTOCK("inventory", "restock", "Inventario · Qué pedir", "reporte-inventario-reponer.pdf"),
    MERCHANDISE_PENDING("merchandise", "pending", "Mercadería · Pedidos pendientes", "reporte-mercaderia-pendientes.pdf"),
    MERCHANDISE_RECEIVED("merchandise", "received", "Mercadería · Pedidos recibidos", "reporte-mercaderia-recibidos.pdf"),
    MERCHANDISE_BY_SUPPLIER("merchandise", "by-supplier", "Mercadería · Por proveedor", "reporte-mercaderia-proveedor.pdf"),
    MERCHANDISE_VS_SALES("merchandise", "vs-sales", "Mercadería · Pedido vs venta", "reporte-mercaderia-vs-venta.pdf"),
    MERCHANDISE_NOT_SELLING("merchandise", "not-selling", "Mercadería · Pedido que no se vende", "reporte-mercaderia-sin-venta.pdf");

    private static final Set<String> KNOWN_REPORTS = Set.of("summary", "sales", "inventory", "merchandise");
    private static final Map<String, ReportEmailKind> BY_KEY = Stream.of(values())
            .collect(Collectors.toUnmodifiableMap(ReportEmailKind::lookupKey, Function.identity()));

    private final String report;
    private final String subReport;
    private final String title;
    private final String fileName;

    ReportEmailKind(String report, String subReport, String title, String fileName)
    {
        this.report = report;
        this.subReport = subReport;
        this.title = title;
        this.fileName = fileName;
    }

    public static ReportEmailKind of(String report, String subReport)
    {
        String normalizedReport = normalize(report);
        if (!KNOWN_REPORTS.contains(normalizedReport))
        {
            throw badRequest("Unknown report");
        }
        ReportEmailKind kind = BY_KEY.get(normalizedReport + "|" + normalize(subReport));
        if (Objects.isNull(kind))
        {
            throw badRequest("Unknown sub-report");
        }
        return kind;
    }

    private String lookupKey()
    {
        return report + "|" + subReport;
    }

    private static String normalize(String value)
    {
        if (!StringUtils.hasText(value))
        {
            return "";
        }
        return value.trim().toLowerCase(Locale.ROOT);
    }

    private static BusinessException badRequest(String message)
    {
        return BusinessException.builder()
                .code(HttpStatus.BAD_REQUEST)
                .message(message)
                .build();
    }
}
