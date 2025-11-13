package pe.edu.utp.sistemaimprenta.controller;

import java.net.URL;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
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

    @FXML
    private HBox topBar;

    private User user;
    private HBox selectedItem = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setImage(imgLogo, ConfigUtil.get("img.logo"));
        setImage(imgUser, "/images/DefaultProfileUser.png");
        itemLogOut.setOnAction(this::logOut);
    }

    private void crearItemsVendedor() {
        createSidebarItem("Clientes", "/images/EyePassword", "/views/UsersPane.fxml");
        createSidebarItem("Pedidos", "", "/views/Peliculas.fxml");
        createSidebarItem("Cotizaciones", "", "/views/Peliculas.fxml");
        createSidebarItem("Pagos", "", "/views/Peliculas.fxml");
        createSidebarItem("Reportes", "", "/views/Peliculas.fxml");
    }

    private void crearItemsOperario() {
        createSidebarItem("Produccion", "", "/views/Clientes.fxml");
        createSidebarItem("Detalles Tecnicos", "", "/views/Peliculas.fxml");
        createSidebarItem("Incidencias", "", "/views/Peliculas.fxml");
    }

    private void crearItemsAdministrador() {
        createSidebarItem("Clientes", ConfigUtil.get("img.customers"), FxmlPath.CUSTOMER_PANE.getPath());
        createSidebarItem("Personal", ConfigUtil.get("img.users"), FxmlPath.USER_PANE.getPath());
        createSidebarItem("Auditoria", ConfigUtil.get("img.audit"), FxmlPath.AUDIT_PANE.getPath());
        createSidebarItem("Productos", ConfigUtil.get("img.products"), FxmlPath.PRODUCT_PANE.getPath());
        createSidebarItem("Pedidos", ConfigUtil.get("img.orders"), FxmlPath.ORDER_PANE.getPath());
        createSidebarItem("Configuracion", ConfigUtil.get("img.configuration"), FxmlPath.CONFIG_PANE.getPath());
        createSidebarItem("Pagos", ConfigUtil.get("img.configuration"), FxmlPath.PAYMENT_PANE.getPath());
    }

    private void setImage(ImageView imageView, String resourcePath) {
        Image image = new Image(getClass().getResourceAsStream(resourcePath));
        imageView.setImage(image);
    }

    public void setUser(User user) {
        if (user != null) {
            this.user = user;
            labelUsername.setText(user.getUsername());
            labelTypeUser.setText(getUserTypeCentralized());

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
        AuditUtil.registrar(getUser(), "Se desconectó del sistema", AuditType.LOGOUT);
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
