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
import javafx.scene.control.TitledPane;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import pe.edu.utp.sistemaimprenta.dao.CustomerDao;
import pe.edu.utp.sistemaimprenta.dao.OrderDao;
import pe.edu.utp.sistemaimprenta.dao.UserDao;
import pe.edu.utp.sistemaimprenta.dao.ProductDao;
import pe.edu.utp.sistemaimprenta.dao.PaymentDao;
import pe.edu.utp.sistemaimprenta.model.Customer;
import pe.edu.utp.sistemaimprenta.model.Order;
import pe.edu.utp.sistemaimprenta.model.User;
import pe.edu.utp.sistemaimprenta.model.Payment;
import pe.edu.utp.sistemaimprenta.util.Notification;
import pe.edu.utp.sistemaimprenta.util.NotificationType;
import com.lowagie.text.Document;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.Element;
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
    private TitledPane paneClientes;
    @FXML
    private TitledPane paneUsuarios;
    @FXML
    private TitledPane panePedidos;
    @FXML
    private TitledPane paneProductos;
    @FXML
    private TitledPane panePagos;

    @FXML
    private DatePicker fechaInicio;
    @FXML
    private DatePicker fechaFin;
    @FXML
    private Button btnFiltrar;

    @FXML
    private Button btnPdfClientes;
    @FXML
    private Button btnPdfUsuarios;
    @FXML
    private Button btnPdfPedidos;
    @FXML
    private Button btnPdfProductos;
    @FXML
    private Button btnPdfPagos;

    @FXML
    private Label lblClientesNuevos;
    @FXML
    private Label lblVentasMensuales;
    @FXML
    private Label lblPedidosCompletados;

    private final CustomerDao customerDao = new CustomerDao();
    private final UserDao userDao = new UserDao();
    private final OrderDao orderDao = new OrderDao();
    private final PaymentDao paymentDao = new PaymentDao();

    private List<Customer> clientesFiltrados;
    private List<Order> listaPedidos;
    private List<Payment> listaPagos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        fechaInicio.setValue(LocalDate.now().minusMonths(6));
        fechaFin.setValue(LocalDate.now());

        btnFiltrar.setOnAction(e -> cargarReporteClientes());
        btnPdfClientes.setOnAction(e -> exportarPdf(contenedorGraficosClientes, "Reporte de Clientes"));

        btnPdfUsuarios.setOnAction(e -> exportarPdf(contenedorGraficosUsuarios, "Reporte de Usuarios"));

        btnPdfPedidos.setOnAction(e -> exportarPdf(contenedorGraficosPedidos, "Reporte de Pedidos"));

        btnPdfProductos.setOnAction(e -> exportarPdf(contenedorGraficosProductos, "Reporte de Productos"));

        btnPdfPagos.setOnAction(e -> exportarPdf(contenedorGraficosPagos, "Reporte de Pagos"));

        cargarReporteClientes();
        cargarReporteUsuarios();
        cargarReportePedidos();
        cargarReporteProductos();
        cargarReportePagos();

        actualizarKpis();
    }

    private void actualizarKpis() {
        lblClientesNuevos.setText(String.valueOf(customerDao.findAll().size()));

        if (listaPedidos == null) {
            listaPedidos = orderDao.findAll();
        }

        long pedidosCompletados = listaPedidos.stream()
                .count();

        double ventasTotales = listaPedidos.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        lblPedidosCompletados.setText(String.valueOf(pedidosCompletados));
        lblVentasMensuales.setText(String.format("S/ %.2f", ventasTotales));
    }

    //s1
    private void cargarReporteClientes() {
        clientesFiltrados = filtrarClientes();
        contenedorGraficosClientes.getChildren().clear();

        contenedorGraficosClientes.getChildren().add(crearGraficoClientesPorMes());
        contenedorGraficosClientes.getChildren().add(crearGraficoClientesPorDia());
        contenedorGraficosClientes.getChildren().add(crearGraficoCrecimientoAcumulado());
    }

    private List<Customer> filtrarClientes() {
        LocalDate ini = fechaInicio.getValue();
        LocalDate fin = fechaFin.getValue();
        List<Customer> todos = customerDao.findAll();

        if (ini == null || fin == null) {
            return todos;
        }

        return todos.stream()
                .filter(c -> !c.getCreatedAt().toLocalDate().isBefore(ini)
                && !c.getCreatedAt().toLocalDate().isAfter(fin))
                .collect(Collectors.toList());
    }

    private Chart crearGraficoClientesPorMes() {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle("Clientes Registrados por Mes");
        x.setLabel("Mes");
        y.setLabel("Cantidad");

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Registros");

        clientesFiltrados.stream()
                .collect(Collectors.groupingBy(c -> c.getCreatedAt().getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")).toUpperCase(), Collectors.counting()))
                .forEach((mes, cnt) -> serie.getData().add(new XYChart.Data<>(mes, cnt)));

        chart.getData().add(serie);
        return chart;
    }

    private Chart crearGraficoClientesPorDia() {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle("Actividad de Registro Diario");
        x.setLabel("Fecha");
        y.setLabel("Nuevos Clientes");

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Clientes");

        clientesFiltrados.stream()
                .collect(Collectors.groupingBy(c -> c.getCreatedAt().toLocalDate().toString(), Collectors.counting()))
                .forEach((dia, cnt) -> serie.getData().add(new XYChart.Data<>(dia, cnt)));

        chart.getData().add(serie);
        return chart;
    }

    private Chart crearGraficoCrecimientoAcumulado() {
        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(x, y);
        chart.setTitle("Crecimiento Acumulado de Cartera");
        x.setLabel("Fecha");
        y.setLabel("Total Clientes");

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Tendencia");

        Map<String, Long> porDia = clientesFiltrados.stream()
                .collect(Collectors.groupingBy(c -> c.getCreatedAt().toLocalDate().toString(), Collectors.counting()));

        List<String> fechasOrd = new ArrayList<>(porDia.keySet());
        Collections.sort(fechasOrd);

        long acumulado = 0;
        for (String f : fechasOrd) {
            acumulado += porDia.get(f);
            serie.getData().add(new XYChart.Data<>(f, acumulado));
        }
        chart.getData().add(serie);
        return chart;
    }

    //s2
    private void cargarReporteUsuarios() {
        List<User> usuarios = userDao.findAll();
        contenedorGraficosUsuarios.getChildren().clear();

        PieChart pieChart = new PieChart();
        pieChart.setTitle("Distribución de Usuarios por Rol");

        usuarios.stream()
                .collect(Collectors.groupingBy(User::getType, Collectors.counting()))
                .forEach((tipo, count) -> {
                    PieChart.Data data = new PieChart.Data(tipo.toString() + " (" + count + ")", count);
                    pieChart.getData().add(data);
                });

        contenedorGraficosUsuarios.getChildren().add(pieChart);
    }

    //s3
    private void cargarReportePedidos() {
        contenedorGraficosPedidos.getChildren().clear();

        listaPedidos = orderDao.findAll();

        if (listaPedidos.isEmpty()) {
            contenedorGraficosPedidos.getChildren().add(new Label("No hay pedidos registrados para generar reportes."));
            return;
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> ventasChart = new BarChart<>(xAxis, yAxis);
        ventasChart.setTitle("Ingresos por Ventas (Mensual)");
        xAxis.setLabel("Mes");
        yAxis.setLabel("Monto (S/)");

        XYChart.Series<String, Number> serieVentas = new XYChart.Series<>();
        serieVentas.setName("Total Facturado");

        Map<String, Double> ventasPorMes = listaPedidos.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getCreatedAt().getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES")).toUpperCase(),
                        Collectors.summingDouble(Order::getTotalAmount)
                ));

        ventasPorMes.forEach((mes, total)
                -> serieVentas.getData().add(new XYChart.Data<>(mes, total))
        );

        ventasChart.getData().add(serieVentas);
        contenedorGraficosPedidos.getChildren().add(ventasChart);

        PieChart estadoChart = new PieChart();
        estadoChart.setTitle("Distribución de Estados de Pedidos");

        Map<String, Long> conteoEstados = listaPedidos.stream()
                .collect(Collectors.groupingBy(
                        o -> o.getState() != null ? o.getState().toString() : "DESCONOCIDO",
                        Collectors.counting()
                ));

        conteoEstados.forEach((estado, cantidad) -> {
            PieChart.Data data = new PieChart.Data(estado + " (" + cantidad + ")", cantidad);
            estadoChart.getData().add(data);
        });

        contenedorGraficosPedidos.getChildren().add(estadoChart);
    }

    //s4
    private void cargarReporteProductos() {
        contenedorGraficosProductos.getChildren().clear();

        List<Object[]> productosVendidos = orderDao.findTopSellingProducts(5);

        if (productosVendidos.isEmpty()) {
            contenedorGraficosProductos.getChildren().add(new Label("No hay datos de ventas para generar reportes de productos."));
            return;
        }

        CategoryAxis x = new CategoryAxis();
        NumberAxis y = new NumberAxis();
        BarChart<String, Number> chart = new BarChart<>(x, y);
        chart.setTitle("Top 5 Productos Más Vendidos (Por Unidades)");
        x.setLabel("Producto");
        y.setLabel("Unidades Vendidas");

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        serie.setName("Unidades");

        for (Object[] data : productosVendidos) {
            String nombreProducto = (String) data[0];
            Long unidadesVendidas = (Long) data[1];
            serie.getData().add(new XYChart.Data<>(nombreProducto, unidadesVendidas));
        }

        chart.getData().add(serie);
        contenedorGraficosProductos.getChildren().add(chart);

        // Agregando gráfico de ingresos por categoría de producto
        List<Object[]> ingresosPorTipo = orderDao.findIncomeByProductType();

        if (!ingresosPorTipo.isEmpty()) {
            PieChart pieChart = new PieChart();
            pieChart.setTitle("Ingresos por Tipo de Producto");

            for (Object[] data : ingresosPorTipo) {
                String tipo = (String) data[0];
                Double ingresos = (Double) data[1];
                PieChart.Data pieData = new PieChart.Data(tipo + " (S/" + String.format("%.2f", ingresos) + ")", ingresos);
                pieChart.getData().add(pieData);
            }
            contenedorGraficosProductos.getChildren().add(pieChart);
        }
    }

    //s5
    private void cargarReportePagos() {
        contenedorGraficosPagos.getChildren().clear();

        listaPagos = paymentDao.listarPagos();

        if (listaPagos.isEmpty()) {
            contenedorGraficosPagos.getChildren().add(new Label("No hay pagos registrados para generar reportes."));
            return;
        }

        PieChart metodoPagoChart = new PieChart();
        metodoPagoChart.setTitle("Métodos de Pago Utilizados");

        Map<String, Long> conteoMetodos = listaPagos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getMetodoPago() != null ? p.getMetodoPago().getNombre() : "DESCONOCIDO",
                        Collectors.counting()
                ));

        conteoMetodos.forEach((metodo, cantidad) -> {
            PieChart.Data data = new PieChart.Data(metodo + " (" + cantidad + ")", cantidad);
            metodoPagoChart.getData().add(data);
        });

        contenedorGraficosPagos.getChildren().add(metodoPagoChart);

        PieChart estadoPagoChart = new PieChart();
        estadoPagoChart.setTitle("Distribución de Pagos por Estado");

        Map<String, Double> montosPorEstado = listaPagos.stream()
                .collect(Collectors.groupingBy(
                        p -> p.getEstadoPago() != null ? p.getEstadoPago().getNombre() : "DESCONOCIDO",
                        Collectors.summingDouble(Payment::getMonto)
                ));

        montosPorEstado.forEach((estado, monto) -> {
            PieChart.Data data = new PieChart.Data(estado + " (S/" + String.format("%.2f", monto) + ")", monto);
            estadoPagoChart.getData().add(data);
        });

        contenedorGraficosPagos.getChildren().add(estadoPagoChart);
    }

    private void exportarPdf(VBox contenedor, String tituloReporte) {
        try {
            String fileName = tituloReporte.replace(" ", "_") + "_" + System.currentTimeMillis() + ".pdf";
            Document documento = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(documento, new FileOutputStream(fileName));
            documento.open();

            com.lowagie.text.Font fontTitulo = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph mainTitle = new Paragraph(tituloReporte, fontTitulo);
            mainTitle.setAlignment(Element.ALIGN_CENTER);
            documento.add(mainTitle);
            documento.add(new Paragraph(" "));

            for (Node nodo : contenedor.getChildren()) {
                if (nodo instanceof Chart) {
                    Chart chart = (Chart) nodo;

                    String chartTitle = chart.getTitle() != null ? chart.getTitle() : "Gráfico Sin Título";
                    com.lowagie.text.Font fontSub = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
                    Paragraph pTitle = new Paragraph(chartTitle, fontSub);
                    pTitle.setSpacingBefore(10);
                    documento.add(pTitle);

                    WritableImage image = chart.snapshot(new SnapshotParameters(), null);
                    BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);

                    File temp = File.createTempFile("chart_pdf", ".png");
                    ImageIO.write(bufferedImage, "png", temp);

                    Image img = Image.getInstance(temp.getAbsolutePath());
                    img.scaleToFit(750, 450);
                    img.setAlignment(Element.ALIGN_CENTER);

                    documento.add(img);
                    documento.add(new Paragraph(" "));
                }
            }

            documento.close();
            Notification.showNotification("Exportar PDF", "Archivo generado: " + fileName, 4, NotificationType.SUCCESS);

        } catch (Exception ex) {
            ex.printStackTrace();
            Notification.showNotification("Error", "No se pudo exportar el PDF", 4, NotificationType.ERROR);
        }
    }
}
