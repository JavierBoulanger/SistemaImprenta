package pe.edu.utp.sistemaimprenta.controller;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import pe.edu.utp.sistemaimprenta.dao.*;
import pe.edu.utp.sistemaimprenta.model.*;
import pe.edu.utp.sistemaimprenta.util.Notification;
import pe.edu.utp.sistemaimprenta.util.NotificationType;

import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Element;
import com.lowagie.text.pdf.PdfWriter;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

public class ReportController implements Initializable {

    @FXML
    private VBox contenedorGraficosClientes;
    @FXML
    private VBox contenedorGraficosUsuarios;
    @FXML
    private VBox contenedorGraficosPedidos;
    @FXML
    private VBox contenedorGraficosProductos;
    @FXML
    private VBox contenedorGraficosPagos;

    @FXML
    private DatePicker fechaInicioClientes;
    @FXML
    private DatePicker fechaFinClientes;
    @FXML
    private Button btnFiltrarClientes;
    @FXML
    private Button btnPdfClientes;

    @FXML
    private DatePicker fechaInicioUsuarios;
    @FXML
    private DatePicker fechaFinUsuarios;
    @FXML
    private Button btnFiltrarUsuarios;
    @FXML
    private Button btnPdfUsuarios;

    @FXML
    private DatePicker fechaInicioPedidos;
    @FXML
    private DatePicker fechaFinPedidos;
    @FXML
    private Button btnFiltrarPedidos;
    @FXML
    private Button btnPdfPedidos;

    @FXML
    private DatePicker fechaInicioProductos;
    @FXML
    private DatePicker fechaFinProductos;
    @FXML
    private Button btnFiltrarProductos;
    @FXML
    private Button btnPdfProductos;

    @FXML
    private DatePicker fechaInicioPagos;
    @FXML
    private DatePicker fechaFinPagos;
    @FXML
    private Button btnFiltrarPagos;
    @FXML
    private Button btnPdfPagos;

    @FXML
    private Label lblClientesNuevos;
    @FXML
    private Label lblVentasMensuales;
    @FXML
    private Label lblPedidosCompletados;

    private CustomerDao customerDao = new CustomerDao();
    private UserDao userDao = new UserDao();
    private OrderDao orderDao = new OrderDao();
    private PaymentDao paymentDao = new PaymentDao();
    private ProductDao productDao = new ProductDao();

    private List<Customer> listaClientes;
    private List<Order> listaPedidos;
    private List<Payment> listaPagos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        inicializarFechas(fechaInicioClientes, fechaFinClientes);
        inicializarFechas(fechaInicioUsuarios, fechaFinUsuarios);
        inicializarFechas(fechaInicioPedidos, fechaFinPedidos);
        inicializarFechas(fechaInicioProductos, fechaFinProductos);
        inicializarFechas(fechaInicioPagos, fechaFinPagos);

        btnFiltrarClientes.setOnAction(e -> cargarReporteClientes());
        btnFiltrarUsuarios.setOnAction(e -> cargarReporteUsuarios());
        btnFiltrarPedidos.setOnAction(e -> cargarReportePedidos());
        btnFiltrarProductos.setOnAction(e -> cargarReporteProductos());
        btnFiltrarPagos.setOnAction(e -> cargarReportePagos());

        btnPdfClientes.setOnAction(e -> exportarPdf(
                contenedorGraficosClientes, "Reporte de Clientes",
                fechaInicioClientes.getValue(), fechaFinClientes.getValue()
        ));

        btnPdfUsuarios.setOnAction(e -> exportarPdf(
                contenedorGraficosUsuarios, "Reporte de Usuarios",
                fechaInicioUsuarios.getValue(), fechaFinUsuarios.getValue()
        ));

        btnPdfPedidos.setOnAction(e -> exportarPdf(
                contenedorGraficosPedidos, "Reporte de Pedidos",
                fechaInicioPedidos.getValue(), fechaFinPedidos.getValue()
        ));

        btnPdfProductos.setOnAction(e -> exportarPdf(
                contenedorGraficosProductos, "Reporte de Productos",
                fechaInicioProductos.getValue(), fechaFinProductos.getValue()
        ));

        btnPdfPagos.setOnAction(e -> exportarPdf(
                contenedorGraficosPagos, "Reporte de Pagos",
                fechaInicioPagos.getValue(), fechaFinPagos.getValue()
        ));

        cargarReporteClientes();
        cargarReporteUsuarios();
        cargarReportePedidos();
        cargarReporteProductos();
        cargarReportePagos();

        actualizarKpis();
    }

    private void actualizarKpis() {

        lblClientesNuevos.setText(String.valueOf(customerDao.findAll().size()));

        listaPedidos = orderDao.findAll();

        long pedidosCompletados = listaPedidos.size();

        double ventasTotales = listaPedidos.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        lblPedidosCompletados.setText(String.valueOf(pedidosCompletados));
        lblVentasMensuales.setText(String.format("S/ %.2f", ventasTotales));
    }

    private void inicializarFechas(DatePicker inicio, DatePicker fin) {
        inicio.setValue(LocalDate.now().minusMonths(3));
        fin.setValue(LocalDate.now());
    }

    private void cargarReporteClientes() {

        LocalDate ini = fechaInicioClientes.getValue();
        LocalDate fin = fechaFinClientes.getValue();

        listaClientes = customerDao.findAll().stream()
                .filter(c -> !c.getCreatedAt().toLocalDate().isBefore(ini)
                && !c.getCreatedAt().toLocalDate().isAfter(fin))
                .collect(Collectors.toList());

        contenedorGraficosClientes.getChildren().clear();

        contenedorGraficosClientes.getChildren().add(graficoClientesPorMes());
        contenedorGraficosClientes.getChildren().add(graficoClientesPorDia());
        contenedorGraficosClientes.getChildren().add(graficoCrecimientoAcumulado());
    }

    private Chart graficoClientesPorMes() {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();

        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle("Clientes Registrados por Mes");

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Registros");

        listaClientes.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCreatedAt().getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")),
                        Collectors.counting()
                ))
                .forEach((mes, cnt) -> serie.getData().add(new XYChart.Data<>(mes.toUpperCase(), cnt)));

        chart.getData().add(serie);
        return chart;
    }

    private Chart graficoClientesPorDia() {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle("Actividad de Registro Diario");

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Clientes");

        listaClientes.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()
                ))
                .forEach((dia, cnt) -> serie.getData().add(new XYChart.Data<>(dia, cnt)));

        chart.getData().add(serie);
        return chart;
    }

    private Chart graficoCrecimientoAcumulado() {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(x, y);
        chart.setTitle("Crecimiento Acumulado");

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Acumulado");

        Map<String, Long> porDia = listaClientes.stream()
                .collect(Collectors.groupingBy(
                        c -> c.getCreatedAt().toLocalDate().toString(),
                        Collectors.counting()
                ));

        List<String> fechas = new ArrayList<>(porDia.keySet());
        Collections.sort(fechas);

        long acumulado = 0;
        for (String f : fechas) {
            acumulado += porDia.get(f);
            serie.getData().add(new XYChart.Data<>(f, acumulado));
        }

        chart.getData().add(serie);
        return chart;
    }

    private void cargarReporteUsuarios() {

        LocalDate ini = fechaInicioUsuarios.getValue();
        LocalDate fin = fechaFinUsuarios.getValue();

        List<User> usuariosFiltrados = userDao.findAll().stream()
                .filter(u -> u.getCreatedAt() != null
                && !u.getCreatedAt().toLocalDate().isBefore(ini)
                && !u.getCreatedAt().toLocalDate().isAfter(fin))
                .collect(Collectors.toList());

        contenedorGraficosUsuarios.getChildren().clear();

        PieChart pie = new PieChart();
        pie.setTitle("Usuarios por Rol");

        usuariosFiltrados.stream()
                .collect(Collectors.groupingBy(User::getType, Collectors.counting()))
                .forEach((rol, count)
                        -> pie.getData().add(new PieChart.Data(rol + " (" + count + ")", count)));

        contenedorGraficosUsuarios.getChildren().add(pie);
    }

    private void cargarReportePedidos() {

        LocalDate ini = fechaInicioPedidos.getValue();
        LocalDate fin = fechaFinPedidos.getValue();

        listaPedidos = orderDao.findAll().stream()
                .filter(o -> !o.getCreatedAt().toLocalDate().isBefore(ini)
                && !o.getCreatedAt().toLocalDate().isAfter(fin))
                .collect(Collectors.toList());

        contenedorGraficosPedidos.getChildren().clear();

        if (listaPedidos.isEmpty()) {
            contenedorGraficosPedidos.getChildren().add(new Label("No hay pedidos en este rango."));
            return;
        }

        Map<String, Double> ventas = listaPedidos.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().getMonth().getDisplayName(TextStyle.FULL, new Locale("es")),
                        Collectors.summingDouble(Order::getTotalAmount)
                ));

        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();

        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle("Ventas Mensuales");

        XYChart.Series<String, Number> serie = new XYChart.Series<>();

        ventas.forEach((mes, total)
                -> serie.getData().add(new XYChart.Data<>(mes.toUpperCase(), total)));

        chart.getData().add(serie);
        contenedorGraficosPedidos.getChildren().add(chart);

        PieChart estados = new PieChart();
        estados.setTitle("Estados de Pedido");

        listaPedidos.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getState() != null ? o.getState().toString() : "DESCONOCIDO",
                        Collectors.counting()
                ))
                .forEach((est, cantidad)
                        -> estados.getData().add(new PieChart.Data(est, cantidad)));

        contenedorGraficosPedidos.getChildren().add(estados);
    }

    private void cargarReporteProductos() {

        LocalDate ini = fechaInicioProductos.getValue();
        LocalDate fin = fechaFinProductos.getValue();
        List<Object[]> productosVendidos = orderDao.findTopSellingProducts(ini, fin, 10);
        contenedorGraficosProductos.getChildren().clear();

        if (productosVendidos.isEmpty()) {
            contenedorGraficosProductos.getChildren().add(new Label("No hay ventas en este rango."));
            return;
        }

        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();

        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle("Top 5 Productos Vendidos");

        XYChart.Series<String, Number> serie = new XYChart.Series<>();

        for (Object[] row : productosVendidos) {
            String producto = (String) row[0];
            long cantidad = ((Number) row[1]).longValue();
            serie.getData().add(new XYChart.Data<>(producto, cantidad));
        }

        chart.getData().add(serie);
        contenedorGraficosProductos.getChildren().add(chart);
    }

    private void cargarReportePagos() {

        LocalDate ini = fechaInicioPagos.getValue();
        LocalDate fin = fechaFinPagos.getValue();

        listaPagos = paymentDao.listarPagos().stream()
                .filter(p -> !p.getFechaPago().toLocalDate().isBefore(ini)
                && !p.getFechaPago().toLocalDate().isAfter(fin))
                .collect(Collectors.toList());

        contenedorGraficosPagos.getChildren().clear();

        if (listaPagos.isEmpty()) {
            contenedorGraficosPagos.getChildren().add(new Label("No hay pagos en este rango."));
            return;
        }

        // Métodos de pago
        PieChart metodos = new PieChart();
        metodos.setTitle("Métodos de Pago");

        listaPagos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getMetodoPago() != null ? p.getMetodoPago().getNombre() : "DESCONOCIDO",
                        Collectors.counting()
                ))
                .forEach((m, cnt)
                        -> metodos.getData().add(new PieChart.Data(m, cnt)));

        contenedorGraficosPagos.getChildren().add(metodos);

        PieChart estados = new PieChart();
        estados.setTitle("Estado del Pago");

        listaPagos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getEstadoPago() != null ? p.getEstadoPago().getNombre() : "DESCONOCIDO",
                        Collectors.summingDouble(Payment::getMonto)
                ))
                .forEach((e, total)
                        -> estados.getData().add(new PieChart.Data(e + " (S/ " + total + ")", total)));

        contenedorGraficosPagos.getChildren().add(estados);
    }

    private void exportarPdf(VBox contenedor, String titulo, LocalDate ini, LocalDate fin) {
        try {
            String home = System.getProperty("user.home");
            String path = home + File.separator + "Downloads";
            String fileName = path + File.separator
                    + titulo.replace(" ", "_") + "_" + System.currentTimeMillis() + ".pdf";

            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, new FileOutputStream(fileName));
            doc.open();

            Paragraph title = new Paragraph(titulo, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20));
            title.setAlignment(Element.ALIGN_CENTER);
            doc.add(title);

            Paragraph range = new Paragraph(
                    "Rango de Fechas: " + ini + "  -  " + fin,
                    FontFactory.getFont(FontFactory.HELVETICA, 12)
            );
            range.setAlignment(Element.ALIGN_CENTER);
            doc.add(range);

            doc.add(new Paragraph(" "));

            for (Node nodo : contenedor.getChildren()) {

                if (nodo instanceof Chart) {
                    Chart ch = (Chart) nodo;

                    String titleChart = ch.getTitle() != null ? ch.getTitle() : "Gráfico";

                    Paragraph subt = new Paragraph(titleChart, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14));
                    subt.setAlignment(Element.ALIGN_LEFT);
                    doc.add(subt);

                    WritableImage img = ch.snapshot(new SnapshotParameters(), null);
                    BufferedImage buff = SwingFXUtils.fromFXImage(img, null);

                    File tmp = File.createTempFile("chart", ".png");
                    ImageIO.write(buff, "png", tmp);

                    Image chartImg = Image.getInstance(tmp.getAbsolutePath());
                    chartImg.scaleToFit(750, 450);
                    chartImg.setAlignment(Element.ALIGN_CENTER);

                    doc.add(chartImg);
                    doc.add(new Paragraph(" "));
                }
            }

            doc.close();

            Notification.showNotification("PDF Exportado",
                    "Guardado en: " + fileName,
                    4, NotificationType.SUCCESS);

        } catch (Exception ex) {
            ex.printStackTrace();
            Notification.showNotification("Error", "No se pudo exportar PDF", 4, NotificationType.ERROR);
        }
    }
}
