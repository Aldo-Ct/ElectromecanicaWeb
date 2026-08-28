package com.electromecanica.app.service;

import com.electromecanica.app.entity.DetalleVenta;
import com.electromecanica.app.entity.MetodoPago;
import com.electromecanica.app.entity.Venta;
import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.itextpdf.layout.properties.VerticalAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

@Service
public class ComprobanteVentaService {
    private static final Color AZUL_OSCURO = new DeviceRgb(5, 48, 94);
    private static final Color AZUL = new DeviceRgb(12, 76, 137);
    private static final Color AZUL_CLARO = new DeviceRgb(238, 246, 253);
    private static final Color BORDE_CLARO = new DeviceRgb(188, 210, 231);
    private static final Color GRIS_TEXTO = new DeviceRgb(65, 79, 94);
    private static final Color ROJO = new DeviceRgb(190, 32, 32);
    private static final Color VERDE = new DeviceRgb(20, 126, 61);
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");

    public byte[] generarPdf(Venta venta) {
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        try (PdfWriter escritor = new PdfWriter(salida);
             PdfDocument documentoPdf = new PdfDocument(escritor);
             Document documento = new Document(documentoPdf, PageSize.A4)) {
            documento.setMargins(28, 30, 26, 30);

            documento.add(crearEncabezado(venta));
            documento.add(crearInformacionVenta(venta));
            documento.add(new Paragraph(" ").setHeight(2).setMargin(0).setPadding(0)
                    .setBackgroundColor(AZUL_OSCURO));
            documento.add(new Paragraph(" ").setFontSize(5).setMargin(0));
            documento.add(crearTablaProductos(venta));
            documento.add(crearCierreVenta(venta));
            documento.add(crearAgradecimiento(venta, documentoPdf));
            documento.add(crearPiePagina());
        } catch (java.io.IOException excepcion) {
            throw new IllegalStateException("No se pudo generar el comprobante PDF", excepcion);
        }
        return salida.toByteArray();
    }

    private Table crearEncabezado(Venta venta) {
        Table encabezado = new Table(UnitValue.createPercentArray(new float[]{66, 34})).useAllAvailableWidth();
        encabezado.setMarginBottom(20);

        Table marca = new Table(UnitValue.createPercentArray(new float[]{18, 82})).useAllAvailableWidth();
        Cell simbolo = new Cell().setBorder(Border.NO_BORDER).setBackgroundColor(AZUL_OSCURO)
                .setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(TextAlignment.CENTER)
                .setPadding(9).add(new Paragraph("EM").setMargin(0).setBold().setFontSize(18)
                        .setFontColor(new DeviceRgb(255, 255, 255)));
        Cell identidad = new Cell().setBorder(Border.NO_BORDER).setPaddingLeft(10).setPaddingTop(0);
        identidad.add(new Paragraph("ELECTROMECÁNICA").setMargin(0).setBold().setFontSize(22)
                .setFontColor(AZUL_OSCURO));
        identidad.add(new Paragraph("Sistema de ventas e inventario de equipos\neléctricos y mecánicos")
                .setMarginTop(3).setMarginBottom(0).setFontSize(9.5f).setFontColor(GRIS_TEXTO));
        marca.addCell(simbolo);
        marca.addCell(identidad);
        encabezado.addCell(celdaSinBorde().setPaddingRight(16).add(marca));

        Table comprobante = new Table(1).useAllAvailableWidth();
        comprobante.addCell(new Cell().setBorder(new SolidBorder(AZUL_OSCURO, 1.2f))
                .setBackgroundColor(AZUL_OSCURO).setPadding(6).setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(venta.getComprobante().getTipo().name()).setMargin(0).setBold()
                        .setFontSize(13).setFontColor(new DeviceRgb(255, 255, 255))));
        comprobante.addCell(new Cell().setBorder(new SolidBorder(AZUL_OSCURO, 1.2f))
                .setPadding(10).setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(venta.getComprobante().getNumero()).setMargin(0).setBold()
                        .setFontSize(15).setFontColor(AZUL_OSCURO)));
        encabezado.addCell(celdaSinBorde().setVerticalAlignment(VerticalAlignment.MIDDLE).add(comprobante));
        return encabezado;
    }

    private Table crearInformacionVenta(Venta venta) {
        Table informacion = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
        informacion.setMarginBottom(4);
        informacion.addCell(celdaInformacion("VENTA", venta.getNumeroVenta()));
        informacion.addCell(celdaInformacion("CLIENTE", venta.getCliente().getNombreCompleto()
                + "\nDocumento: " + venta.getCliente().getNumeroDocumento()));
        informacion.addCell(celdaInformacion("FECHA", venta.getFecha().format(FORMATO_FECHA)));
        informacion.addCell(celdaInformacion("VENDEDOR", venta.getVendedor().getNombre()
                + " " + venta.getVendedor().getApellido()));
        return informacion;
    }

    private Cell celdaInformacion(String titulo, String valor) {
        Cell celda = celdaSinBorde().setPadding(6).setPaddingLeft(12);
        celda.setBorderLeft(new SolidBorder(AZUL, 2.2f));
        celda.add(new Paragraph(titulo).setMargin(0).setBold().setFontSize(10).setFontColor(AZUL_OSCURO));
        celda.add(new Paragraph(valor).setMarginTop(3).setMarginBottom(0).setFontSize(9.5f));
        return celda;
    }

    private Table crearTablaProductos(Venta venta) {
        Table tabla = new Table(UnitValue.createPercentArray(new float[]{16, 30, 8, 15, 16, 15}))
                .useAllAvailableWidth();
        tabla.setMarginBottom(18);
        agregarCabecera(tabla, "SKU");
        agregarCabecera(tabla, "PRODUCTO");
        agregarCabecera(tabla, "CANT.");
        agregarCabecera(tabla, "P. LISTA\n(INCL. IGV)");
        agregarCabecera(tabla, "P. ACORDADO\n(INCL. IGV)");
        agregarCabecera(tabla, "IMPORTE\n(INCL. IGV)");
        boolean alternar = false;
        for (DetalleVenta detalle : venta.getDetalles()) {
            Color fondo = alternar ? AZUL_CLARO : new DeviceRgb(255, 255, 255);
            agregarDato(tabla, detalle.getProducto().getSku(), TextAlignment.LEFT, fondo);
            agregarDato(tabla, detalle.getProducto().getNombre(), TextAlignment.LEFT, fondo);
            agregarDato(tabla, detalle.getCantidad().toString(), TextAlignment.CENTER, fondo);
            agregarDato(tabla, monto(detalle.getPrecioLista()), TextAlignment.RIGHT, fondo);
            agregarDato(tabla, monto(detalle.getPrecioUnitario()), TextAlignment.RIGHT, fondo);
            agregarDato(tabla, monto(detalle.getSubtotal()), TextAlignment.RIGHT, fondo);
            alternar = !alternar;
        }
        return tabla;
    }

    private Table crearCierreVenta(Venta venta) {
        BigDecimal importeLista = venta.getDetalles().stream()
                .map(detalle -> detalle.getPrecioLista().multiply(BigDecimal.valueOf(detalle.getCantidad())))
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal importeAcordado = venta.getDetalles().stream().map(DetalleVenta::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
        BigDecimal ahorroNegociacion = importeLista.subtract(importeAcordado).max(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_UP);

        Table cierre = new Table(UnitValue.createPercentArray(new float[]{34, 66})).useAllAvailableWidth();
        cierre.setKeepTogether(true).setMarginBottom(21);
        cierre.addCell(celdaSinBorde().setPaddingRight(12).setVerticalAlignment(VerticalAlignment.BOTTOM)
                .add(crearCajaPago(venta)));
        cierre.addCell(celdaSinBorde().setPaddingLeft(12).add(crearResumen(venta, importeLista, ahorroNegociacion)));
        return cierre;
    }

    private Table crearCajaPago(Venta venta) {
        Table pago = new Table(1).useAllAvailableWidth();
        Border bordeCaja = new SolidBorder(AZUL, 1);
        pago.addCell(new Cell().setBorder(Border.NO_BORDER).setBorderTop(bordeCaja)
                .setBorderLeft(bordeCaja).setBorderRight(bordeCaja)
                .setBackgroundColor(AZUL_CLARO).setPadding(8)
                .add(new Paragraph("PAGO").setMargin(0).setBold().setFontSize(11).setFontColor(AZUL_OSCURO)));
        Cell cuerpo = new Cell().setBorder(Border.NO_BORDER).setBorderBottom(bordeCaja)
                .setBorderLeft(bordeCaja).setBorderRight(bordeCaja).setPadding(10);
        cuerpo.add(etiquetaValor("Método de pago", venta.getMetodoPago().name(), AZUL_OSCURO));
        if (venta.getMetodoPago() == MetodoPago.EFECTIVO) {
            cuerpo.add(etiquetaValor("Monto recibido", monto(venta.getMontoRecibido()), GRIS_TEXTO));
            cuerpo.add(etiquetaValor("Vuelto", monto(venta.getVuelto()), VERDE));
        } else {
            cuerpo.add(etiquetaValor("Importe pagado", monto(venta.getTotal()), GRIS_TEXTO));
        }
        pago.addCell(cuerpo);
        return pago;
    }

    private Paragraph etiquetaValor(String etiqueta, String valor, Color colorValor) {
        return new Paragraph().setMarginTop(2).setMarginBottom(8).setPaddingBottom(6)
                .setBorderBottom(new SolidBorder(BORDE_CLARO, .6f))
                .add(etiqueta + ":\n").add(new Text(valor)
                        .setBold().setFontSize(11).setFontColor(colorValor));
    }

    private Table crearResumen(Venta venta, BigDecimal importeLista, BigDecimal ahorroNegociacion) {
        Table resumen = new Table(UnitValue.createPercentArray(new float[]{68, 32})).useAllAvailableWidth();
        agregarFilaResumen(resumen, "Precios de lista (incluyen IGV)", monto(importeLista), GRIS_TEXTO);
        if (ahorroNegociacion.signum() > 0) {
            agregarFilaResumen(resumen, "Ahorro por precio acordado", "- " + monto(ahorroNegociacion), ROJO);
        }
        if (venta.getDescuento().signum() > 0) {
            agregarFilaResumen(resumen, "Descuento adicional", "- " + monto(venta.getDescuento()), ROJO);
        }
        agregarFilaResumen(resumen, "Valor de venta (sin IGV)", monto(venta.getSubtotal()), GRIS_TEXTO);
        agregarFilaResumen(resumen, "IGV incluido (18%)", monto(venta.getImpuesto()), GRIS_TEXTO);
        resumen.addCell(new Cell().setBorder(Border.NO_BORDER).setBorderLeft(new SolidBorder(AZUL, 1))
                .setBorderBottom(new SolidBorder(AZUL, 1)).setBackgroundColor(AZUL_OSCURO).setPadding(10)
                .add(new Paragraph("PRECIO FINAL").setMargin(0).setBold().setFontSize(12)
                        .setFontColor(new DeviceRgb(255, 255, 255))));
        resumen.addCell(new Cell().setBorder(Border.NO_BORDER).setBorderRight(new SolidBorder(AZUL, 1))
                .setBorderBottom(new SolidBorder(AZUL, 1)).setBackgroundColor(AZUL_OSCURO).setPadding(10)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(monto(venta.getTotal())).setMargin(0).setBold().setFontSize(15)
                        .setFontColor(new DeviceRgb(255, 255, 255))));
        return resumen;
    }

    private void agregarFilaResumen(Table tabla, String etiqueta, String valor, Color colorValor) {
        Border separador = new SolidBorder(BORDE_CLARO, .5f);
        tabla.addCell(new Cell().setBorder(Border.NO_BORDER).setBorderLeft(new SolidBorder(AZUL, 1))
                .setBorderTop(separador).setBorderBottom(separador).setPadding(8)
                .add(new Paragraph(etiqueta + ":").setMargin(0).setFontSize(9.5f)));
        tabla.addCell(new Cell().setBorder(Border.NO_BORDER).setBorderRight(new SolidBorder(AZUL, 1))
                .setBorderTop(separador).setBorderBottom(separador).setPadding(8)
                .setTextAlignment(TextAlignment.RIGHT)
                .add(new Paragraph(valor).setMargin(0).setBold().setFontSize(10).setFontColor(colorValor)));
    }

    private Table crearAgradecimiento(Venta venta, PdfDocument documentoPdf) {
        Table agradecimiento = new Table(UnitValue.createPercentArray(new float[]{76, 24})).useAllAvailableWidth();
        agradecimiento.setKeepTogether(true).setMarginTop(4).setMarginBottom(18);
        Cell mensaje = new Cell().setBorder(new SolidBorder(AZUL, 1)).setBorderRight(Border.NO_BORDER)
                .setBackgroundColor(AZUL_CLARO).setPadding(12).setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.LEFT);
        mensaje.add(new Paragraph("¡Gracias por su compra!").setMargin(0).setBold().setFontSize(15)
                .setFontColor(AZUL_OSCURO));
        mensaje.add(new Paragraph("Lo esperamos pronto.").setMarginTop(3).setMarginBottom(5)
                .setFontSize(9.5f).setFontColor(GRIS_TEXTO));
        mensaje.add(new Paragraph("Para ubicar esta venta en una devolución, escanee el QR o use el comprobante "
                + venta.getComprobante().getNumero() + ".").setMargin(0).setFontSize(8.5f).setFontColor(GRIS_TEXTO));

        String contenidoQr = "VENTA:" + venta.getId() + "|COMPROBANTE:" + venta.getComprobante().getNumero();
        BarcodeQRCode codigoQr = new BarcodeQRCode(contenidoQr);
        Image imagenQr = new Image(codigoQr.createFormXObject(documentoPdf)).setWidth(70).setHeight(70);
        Cell qr = new Cell().setBorder(new SolidBorder(AZUL, 1)).setBorderLeft(Border.NO_BORDER)
                .setBackgroundColor(AZUL_CLARO).setPadding(7).setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER);
        qr.add(imagenQr.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER));
        qr.add(new Paragraph("QR DE LA VENTA").setMarginTop(2).setMarginBottom(0).setBold().setFontSize(6.5f)
                .setFontColor(AZUL_OSCURO));
        agradecimiento.addCell(mensaje);
        agradecimiento.addCell(qr);
        return agradecimiento;
    }

    private Table crearPiePagina() {
        Table pie = new Table(UnitValue.createPercentArray(new float[]{50, 50})).useAllAvailableWidth();
        pie.addCell(celdaSinBorde().setBorderTop(new SolidBorder(AZUL_OSCURO, 1.2f))
                .setPaddingTop(8).setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph("Comprometidos con la calidad y el servicio.").setMargin(0)
                        .setFontSize(8.5f).setFontColor(AZUL_OSCURO)));
        pie.addCell(celdaSinBorde().setBorderTop(new SolidBorder(AZUL_OSCURO, 1.2f))
                .setPaddingTop(8).setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph("Gracias por su preferencia.").setMargin(0)
                        .setFontSize(8.5f).setFontColor(AZUL_OSCURO)));
        return pie;
    }

    private void agregarCabecera(Table tabla, String texto) {
        tabla.addHeaderCell(new Cell().setBorder(new SolidBorder(new DeviceRgb(255, 255, 255), .4f))
                .setBackgroundColor(AZUL_OSCURO).setPadding(8).setVerticalAlignment(VerticalAlignment.MIDDLE)
                .setTextAlignment(TextAlignment.CENTER)
                .add(new Paragraph(texto).setMargin(0).setBold().setFontSize(8)
                        .setFontColor(new DeviceRgb(255, 255, 255))));
    }

    private void agregarDato(Table tabla, String texto, TextAlignment alineacion, Color fondo) {
        tabla.addCell(new Cell().setBorder(new SolidBorder(AZUL, .55f)).setBackgroundColor(fondo)
                .setPadding(7).setVerticalAlignment(VerticalAlignment.MIDDLE).setTextAlignment(alineacion)
                .add(new Paragraph(texto).setMargin(0).setFontSize(8.5f)));
    }

    private Cell celdaSinBorde() {
        return new Cell().setBorder(Border.NO_BORDER);
    }

    private String monto(BigDecimal valor) {
        return "S/ " + valor.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
