package modeloVentas;

public class Factura {

    int caja;
    int noFactura;
    String nit;
    String nombre;
    String fecha;
    float total;

    public Factura() {
    }

    public Factura(int caja, int noFactura, String nit, String nombre, String fecha, float total) {
        this.caja = caja;
        this.noFactura = noFactura;
        this.nit = nit;
        this.nombre = nombre;
        this.fecha = fecha;
        this.total = total;
    }

    public int getCaja() {
        return caja;
    }

    public void setCaja(int caja) {
        this.caja = caja;
    }

    public int getNoFactura() {
        return noFactura;
    }

    public void setNoFactura(int noFactura) {
        this.noFactura = noFactura;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public float getTotal() {
        return total;
    }

    public void setTotal(float total) {
        this.total = total;
    }

    @Override
    public String toString() {
        return "Factura{" + "caja=" + caja + ", noFactura=" + noFactura + ", nit=" + nit + ", nombre=" + nombre + ", fecha=" + fecha + ", total=" + total + '}';
    }
}
