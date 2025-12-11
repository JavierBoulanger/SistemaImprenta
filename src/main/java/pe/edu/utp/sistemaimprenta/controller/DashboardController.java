package pe.edu.utp.sistemaimprenta.controller;

import java.net.URL;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import pe.edu.utp.sistemaimprenta.model.AuditType;
import pe.edu.utp.sistemaimprenta.model.User;
import pe.edu.utp.sistemaimprenta.model.UserType;
import pe.edu.utp.sistemaimprenta.util.AuditUtil;
import pe.edu.utp.sistemaimprenta.util.ConfigUtil;
import pe.edu.utp.sistemaimprenta.util.FxmlPath;
import pe.edu.utp.sistemaimprenta.util.UserAware;
import pe.edu.utp.sistemaimprenta.util.ViewLoader;
import pe.edu.utp.sistemaimprenta.util.ViewLoader.SidebarItemResult;

public class DashboardController implements Initializable {

    @FXML
    private ImageView imgLogo;

    @FXML
    private ImageView imgUser;

    @FXML
    private Label labelTypeUser;

    @FXML
    private Label labelUsername;

    @FXML
    private Pane mainPanel;

    @FXML
    private MenuItem itemLogOut;

    @FXML
    private VBox sideBar;

    private User user;
    private HBox selectedItem = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setImage(imgLogo, ConfigUtil.get("img.logo"));
        setImage(imgUser, "/images/DefaultProfileUser.png");
        
        itemLogOut.setOnAction(this::logOut);
    }

 
    private void setImage(ImageView imageView, String resourcePath) {
        if (imageView == null || resourcePath == null || resourcePath.isEmpty()) {
            return;
        }

        try {
            Image image = null;

            if (resourcePath.startsWith("file:")) {
                image = new Image(resourcePath);
            } 

            else {
                String path = resourcePath.startsWith("/") ? resourcePath : "/" + resourcePath;
                var stream = getClass().getResourceAsStream(path);
                if (stream != null) {
                    image = new Image(stream);
                } else {
                    System.err.println("Recurso no encontrado (interno): " + path);
                }
            }
            if (image != null && !image.isError()) {
                imageView.setImage(image);
            }

        } catch (Exception e) {
            System.err.println("Error al cargar imagen en Dashboard: " + resourcePath);
            e.printStackTrace();
        }
    }

    private void crearItemsVendedor() {
        createSidebarItem("Clientes", ConfigUtil.get("img.customers"), FxmlPath.CUSTOMER_PANE.getPath());
        createSidebarItem("Productos", ConfigUtil.get("img.products"), FxmlPath.PRODUCT_PANE.getPath());
        createSidebarItem("Pedidos", ConfigUtil.get("img.orders"), FxmlPath.ORDER_PANE.getPath());
        createSidebarItem("Pagos", ConfigUtil.get("img.payments"), FxmlPath.PAYMENT_PANE.getPath());
        createSidebarItem("Reportes", ConfigUtil.get("img.reports"), FxmlPath.REPORT_PANE.getPath());
    }

    private void crearItemsOperario() {
        //sin implementar
    }

    private void crearItemsAdministrador() {
        createSidebarItem("Clientes", ConfigUtil.get("img.customers"), FxmlPath.CUSTOMER_PANE.getPath());
        createSidebarItem("Personal", ConfigUtil.get("img.users"), FxmlPath.USER_PANE.getPath());
        createSidebarItem("Auditoria", ConfigUtil.get("img.audit"), FxmlPath.AUDIT_PANE.getPath());
        createSidebarItem("Productos", ConfigUtil.get("img.products"), FxmlPath.PRODUCT_PANE.getPath());
        createSidebarItem("Pedidos", ConfigUtil.get("img.orders"), FxmlPath.ORDER_PANE.getPath());
        createSidebarItem("Configuracion", ConfigUtil.get("img.configuration"), FxmlPath.CONFIG_PANE.getPath());
        createSidebarItem("Pagos", ConfigUtil.get("img.payments"), FxmlPath.PAYMENT_PANE.getPath());
        createSidebarItem("Reportes", ConfigUtil.get("img.reports"), FxmlPath.REPORT_PANE.getPath());
        createSidebarItem("Monitoreo", ConfigUtil.get("img.health"), FxmlPath.HEALTH_PANE.getPath());
    }

    public void setUser(User user) {
        if (user != null) {
            this.user = user;
            labelUsername.setText(user.getUsername());
            labelTypeUser.setText(getUserTypeCentralized());
            
            sideBar.getChildren().clear();

            switch (user.getType()) {
                case UserType.VENDEDOR ->
                    crearItemsVendedor();
                case UserType.ADMINISTRADOR ->
                    crearItemsAdministrador();
                case UserType.OPERARIO_PRODUCCION ->
                    crearItemsOperario();
            }
        }
    }

    private String getUserTypeCentralized() {
        return user.getType().name().toLowerCase().toUpperCase();
    }

    private void createSidebarItem(String i18nKey, String iconPath, String fxmlToLoad) {  
        SidebarItemResult result = ViewLoader.loadSidebarItem(i18nKey, iconPath);
        if (result == null) {
            return;
        }

        HBox sidebarItem = result.node();
        sidebarItem.setOnMouseClicked(e -> {
            if (selectedItem != null) {
                selectedItem.getStyleClass().remove("selected");
            }
            sidebarItem.getStyleClass().add("selected");
            selectedItem = sidebarItem;
            Object controller = ViewLoader.changeMainPanelGetController(mainPanel, fxmlToLoad);

            if (controller instanceof UserAware userAwareController) {
                userAwareController.setUsuarioActual(user);
            }
        });

        sidebarItem.getStyleClass().add("menu-item");
        sideBar.getChildren().add(sidebarItem);
    }

    private void logOut(ActionEvent event) {
        if (getUser() != null) {
             AuditUtil.registrar(getUser(), "Se desconectó del sistema", AuditType.LOGOUT);
        }
        closeCurrentStage();
        ViewLoader.openWindow(FxmlPath.AUTH.getPath(), "Login", false);
    }

    private void closeCurrentStage() {
        Stage currentStage = (Stage) mainPanel.getScene().getWindow();
        currentStage.close();
    }

    public User getUser() {
        return user;
    }
}