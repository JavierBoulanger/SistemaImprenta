package pe.edu.utp.sistemaimprenta.controller;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
public class SidebarItemController {
    @FXML
    private ImageView icon;
    @FXML
    private Label label;

    private final ColorAdjust lightenEffect;
    public SidebarItemController() {
        lightenEffect = new ColorAdjust();
        lightenEffect.setSaturation(-1);   
        lightenEffect.setBrightness(0.4);  
    }
    public void setLabelText(String text) {
        label.setText(text);
    }
    public void setIconImage(Image image) {
        icon.setImage(image);
    }

    public void lightenIcon() {
        icon.setEffect(lightenEffect);
    }
}