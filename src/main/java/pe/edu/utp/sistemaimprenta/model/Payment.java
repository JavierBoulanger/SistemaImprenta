package pe.edu.utp.sistemaimprenta.model;

import java.time.LocalDateTime;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    private int id;
    private Order order;
    private PaymentMethod metodoPago;
    private PaymentStatus estadoPago;
    private double monto;
    private LocalDateTime fechaPago;

    public Payment(Order order, PaymentMethod metodoPago, PaymentStatus estadoPago, double monto, LocalDateTime fechaPago) {
        this.order = order;
        this.metodoPago = metodoPago;
        this.estadoPago = estadoPago;
        this.monto = monto;
        this.fechaPago = fechaPago;
    }

}
