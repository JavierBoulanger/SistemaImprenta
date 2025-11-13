package pe.edu.utp.sistemaimprenta.model;

public enum PaymentMethod {
   EFECTIVO(1, "Efectivo"),
    TARJETA(2, "Tarjeta"),
    TRANSFERENCIA(3, "Transferencia");

    private final int id;
    private final String nombre;

    PaymentMethod(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() { return id; }
    public String getNombre() { return nombre; }

    public static PaymentMethod fromId(int id) {
        for (PaymentMethod m : values()) {
            if (m.id == id) return m;
        }
        return null;
    }
}