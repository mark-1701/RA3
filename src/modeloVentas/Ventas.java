package modeloVentas;

import config.Conexion;
import java.sql.*;
import java.util.LinkedList;

public class Ventas {
    Conexion cn = new Conexion();
    Connection con;
    PreparedStatement ps;
    ResultSet rs;
    
    public LinkedList<Factura> listaFiltroFacturas(String noFactura, String nit, String nombre, String fecha) {
        LinkedList<Factura> lista = new LinkedList<Factura>();
        String sql = "SELECT * FROM informacion_facturas WHERE no_factura LIKE ? AND nit LIKE ? AND nombre LIKE ? AND DATE_FORMAT(fecha, '%d-%m-%Y') LIKE ? ORDER BY no_factura ASC;";
        try {
            con = cn.Conectar();
            ps = con.prepareStatement(sql);
            ps.setString(1, "%" + noFactura + "%");
            ps.setString(2, "%" + nit + "%");
            ps.setString(3, "%" + nombre + "%");
            ps.setString(4, "%" + fecha + "%");
            rs = ps.executeQuery();
            while (rs.next()) {
                Factura factura = new Factura();
                factura.setNoFactura(rs.getInt(2));
                factura.setNit(rs.getString(3));
                factura.setNombre(rs.getString(4));
                factura.setFecha(rs.getString(5));
                factura.setTotal(rs.getInt(6));
                lista.add(factura);
                System.out.println(factura.toString());
            }
        } catch (Exception e) {
            System.out.println("ERROR FILTRO FACTURAS: " + e);
        }
        return lista;
    } 
}
