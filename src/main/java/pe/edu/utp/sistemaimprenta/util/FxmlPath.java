package pe.edu.utp.sistemaimprenta.util;

public enum FxmlPath {
    AUTH("/views/AuthView.fxml"),
    LOGIN_PANE("/views/LoginPane.fxml"),
    REGISTER_PANE("/views/RegisterPane.fxml"),
    DASHBOARD("/views/DashboardView.fxml"),
    SIDEBAR_ITEM("/views/SidebarItem.fxml"),
    CUSTOMER_PANE("/views/CustomersPane.fxml"),
    USER_PANE("/views/UsersPane.fxml"),
    AUDIT_PANE("/views/AuditPane.fxml"),
    AUDIT_ITEM("/views/AuditItem.fxml"),
    PRODUCT_PANE("/views/ProductPane.fxml"),
    ORDER_PANE("/views/OrderPane.fxml"),
    CONFIG_PANE("/views/ConfigPane.fxml"),
    PAYMENT_PANE("/views/PaymentPaneDashboard.fxml"),
    PAYMENT_ITEM("/views/PaymentItem.fxml"),
    TICKET("/views/TicketView.fxml");
    
    private final String path;

    private FxmlPath(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
