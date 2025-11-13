package pe.edu.utp.sistemaimprenta.model;

public enum PaymentStatus {
    PENDIENTE(1, "Pendiente"),
    PAGADO(2, "Pagado"),
    PARCIAL(3, "Parcial"),
    ANULADO(4, "Anulado");

    private final int id;
    private final String nombre;

    PaymentStatus(int id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public int getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public static PaymentStatus fromId(int id) {
        for (PaymentStatus e : values()) {
            if (e.id == id) {
                return e;
            }
        }
        return null;
    }
}
