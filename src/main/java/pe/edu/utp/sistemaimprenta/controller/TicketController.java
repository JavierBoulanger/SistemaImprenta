package pe.edu.utp.sistemaimprenta.controller;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import pe.edu.utp.sistemaimprenta.model.Order;
import pe.edu.utp.sistemaimprenta.model.OrderDetail;
import pe.edu.utp.sistemaimprenta.util.ConfigUtil;

public class TicketController {

    @FXML
    private Text lblNombreEmpresa;
    @FXML
    private Text lblRuc;
    @FXML
    private Text lblDireccion;

    @FXML
    private Label lblCliente;
    @FXML
    private Label lblFecha;
    @FXML
    private Label lblVendedor;
    @FXML
    private Label lblIdPedido;
    @FXML
    private Label lblTotal;

    @FXML
    private TableView<OrderDetail> tablaDetalles;
    @FXML
    private TableColumn<OrderDetail, String> colProducto;
    @FXML
    private TableColumn<OrderDetail, Integer> colCantidad;
    @FXML
    private TableColumn<OrderDetail, Double> colPrecio;
    @FXML
    private TableColumn<OrderDetail, Double> colSubtotal;

    @FXML
    private Button btnDescargar;

    private Order pedido;

    public void cargarDatos(Order order) {
        this.pedido = order;

        lblNombreEmpresa.setText(ConfigUtil.get("empresa.nombre"));
        lblRuc.setText("RUC: " + ConfigUtil.get("empresa.ruc"));
        lblDireccion.setText(ConfigUtil.get("empresa.direccion"));

        lblCliente.setText(order.getCustomer().getName());
        lblFecha.setText(order.getCreatedAt().toLocalDate().toString());
        lblVendedor.setText(order.getUser().getUsername());
        lblIdPedido.setText(String.valueOf(order.getId()));

        lblTotal.setText(String.format("S/ %.2f", order.getTotalAmount()));

        colProducto.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().getProduct().getName()));
        colCantidad.setCellValueFactory(d -> new javafx.beans.property.SimpleIntegerProperty(d.getValue().getQuantity()).asObject());
        colPrecio.setCellValueFactory(d -> new javafx.beans.property.SimpleDoubleProperty(d.getValue().getUnitPrice()).asObject());
        colSubtotal.setCellValueFactory(d -> new javafx.beans.property.SimpleDoubleProperty(d.getValue().getSubtotal()).asObject());

        tablaDetalles.setItems(FXCollections.observableArrayList(
                pedido.getDetails() != null ? pedido.getDetails() : List.of()
        ));

        btnDescargar.setOnAction(e -> descargarPDF());
    }

    private void descargarPDF() {
        if (pedido == null) {
            return;
        }

        try {
            String userHome = System.getProperty("user.home");
            String downloadDir = userHome + File.separator + "Downloads";

            File dir = new File(downloadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileName = "Boleta_Pedido_" + pedido.getId() + ".pdf";
            File pdfFile = new File(dir, fileName);

            com.lowagie.text.Document document = new com.lowagie.text.Document();
            com.lowagie.text.pdf.PdfWriter.getInstance(document, new java.io.FileOutputStream(pdfFile));
            document.open();

            var fontTitle = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 16, com.lowagie.text.Font.BOLD);
            var fontNormal = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 11);
            var fontBold = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 11, com.lowagie.text.Font.BOLD);

            String logoPath = ConfigUtil.get("img.logo");
            if (logoPath != null && !logoPath.isBlank()) {
                try {
                    InputStream logoStream = getClass().getResourceAsStream(logoPath);
                    if (logoStream != null) {
                        byte[] bytes = logoStream.readAllBytes();
                        com.lowagie.text.Image logo = com.lowagie.text.Image.getInstance(bytes);
                        logo.scaleToFit(120, 80);
                        logo.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                        document.add(logo);
                        document.add(new com.lowagie.text.Paragraph(" "));
                        logoStream.close();
                    } else {
                        System.out.println("⚠️ No se encontró el logo en el classpath: " + logoPath);
                    }
                } catch (Exception ex) {
                    System.out.println("❌ Error al cargar logo: " + ex.getMessage());
                }
            }

            com.lowagie.text.Paragraph nombre = new com.lowagie.text.Paragraph(
                    ConfigUtil.get("empresa.nombre"), fontTitle);
            nombre.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            document.add(nombre);

            com.lowagie.text.Paragraph ruc = new com.lowagie.text.Paragraph(
                    "RUC: " + ConfigUtil.get("empresa.ruc"), fontNormal);
            ruc.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            document.add(ruc);

            com.lowagie.text.Paragraph direccion = new com.lowagie.text.Paragraph(
                    ConfigUtil.get("empresa.direccion"), fontNormal);
            direccion.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            document.add(direccion);

            document.add(new com.lowagie.text.Paragraph(" "));
            com.lowagie.text.Paragraph titulo = new com.lowagie.text.Paragraph("BOLETA DE VENTA", fontBold);
            titulo.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            document.add(titulo);
            document.add(new com.lowagie.text.Paragraph(" "));

            com.lowagie.text.pdf.PdfPTable tableInfo = new com.lowagie.text.pdf.PdfPTable(2);
            tableInfo.setWidthPercentage(100);
            tableInfo.addCell(celda("Cliente:", fontBold));
            tableInfo.addCell(celda(pedido.getCustomer().getName(), fontNormal));
            tableInfo.addCell(celda("Vendedor:", fontBold));
            tableInfo.addCell(celda(pedido.getUser().getUsername(), fontNormal));
            tableInfo.addCell(celda("Fecha:", fontBold));
            tableInfo.addCell(celda(pedido.getCreatedAt().toLocalDate().toString(), fontNormal));
            tableInfo.addCell(celda("N° Pedido:", fontBold));
            tableInfo.addCell(celda(String.valueOf(pedido.getId()), fontNormal));
            document.add(tableInfo);
            document.add(new com.lowagie.text.Paragraph(" "));

            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{5, 2, 2, 2});
            table.addCell(headerCell("Producto"));
            table.addCell(headerCell("Cantidad"));
            table.addCell(headerCell("Precio"));
            table.addCell(headerCell("Subtotal"));

            for (var d : pedido.getDetails()) {
                table.addCell(celda(d.getProduct().getName(), fontNormal));
                table.addCell(celda(String.valueOf(d.getQuantity()), fontNormal));
                table.addCell(celda(String.format("S/ %.2f", d.getUnitPrice()), fontNormal));
                table.addCell(celda(String.format("S/ %.2f", d.getSubtotal()), fontNormal));
            }

            document.add(table);

            document.add(new com.lowagie.text.Paragraph(" "));
            com.lowagie.text.Paragraph total = new com.lowagie.text.Paragraph(
                    "TOTAL: S/ " + String.format("%.2f", pedido.getTotalAmount()), fontBold);
            total.setAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
            document.add(total);

            document.add(new com.lowagie.text.Paragraph(" "));
            com.lowagie.text.Paragraph gracias = new com.lowagie.text.Paragraph(
                    "Gracias por su compra.", fontNormal);
            gracias.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            document.add(gracias);

            document.close();

            java.awt.Desktop.getDesktop().open(pdfFile);

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Boleta generada");
            alert.setHeaderText(null);
            alert.setContentText("La boleta se guardó en tu carpeta de Descargas:\n" + pdfFile.getAbsolutePath());
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error al generar PDF");
            alert.setHeaderText(null);
            alert.setContentText("Ocurrió un error: " + e.getMessage());
            alert.showAndWait();
        }
    }

// --- Métodos auxiliares para celdas ---
    private com.lowagie.text.pdf.PdfPCell celda(String texto, com.lowagie.text.Font font) {
        com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(texto, font));
        cell.setBorderColor(new java.awt.Color(200, 200, 200));
        cell.setPadding(5);
        return cell;
    }

    private com.lowagie.text.pdf.PdfPCell headerCell(String texto) {
        com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(
                new com.lowagie.text.Phrase(texto, new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 11, com.lowagie.text.Font.BOLD, new java.awt.Color(255, 255, 255))));
        cell.setBackgroundColor(new java.awt.Color(52, 152, 219));
        cell.setPadding(6);
        return cell;
    }

}
