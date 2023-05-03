package modeloVentas;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import config.Conexion;
import java.awt.Desktop;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.*;
import java.util.LinkedList;
import javax.swing.JOptionPane;
import modelo.Cliente;
import modelo.Producto;

public class NuevaVenta {

    Conexion cn = new Conexion();
    Connection con;
    PreparedStatement ps;
    ResultSet rs;
    
    public int generarCodigoFactura() {
        String sql = "SELECT MAX(no_factura) FROM facturas;";
        try {
            con = cn.Conectar();
            ps = con.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getInt(1) + 1;
            }
        } catch (Exception e) {
            System.out.println("ERROR AL GENERAR EL NUEVO CODIGO FACTURA: " + e);
        }
        return 0;
    }

    public LinkedList<Cliente> listaFiltroClientes(String nombre, String nit, String correo, String genero) {
        LinkedList<Cliente> lista = new LinkedList<Cliente>();
        String sql = "SELECT codigo, nombre FROM clientes WHERE nombre LIKE ? AND nit LIKE ? AND correo LIKE ? AND genero LIKE ?;";
        try {
            con = cn.Conectar();
            ps = con.prepareStatement(sql);
            ps.setString(1, "%" + nombre + "%");
            ps.setString(2, "%" + nit + "%");
            ps.setString(3, "%" + correo + "%");
            ps.setString(4, "%" + genero + "%");
            rs = ps.executeQuery();
            String nombreEncontrado = "";
            while (rs.next()) {
                Cliente cliente = new Cliente();
                cliente.setCodigo(rs.getInt(1));
                cliente.setNombre(rs.getString(2));
                lista.add(cliente);
            }
        } catch (Exception e) {
            System.out.println("ERROR FILTRO CLIENTES: " + e);
        }
        return lista;
    }

    public boolean comprobarCodigo(int codigo) {
        String sql = "SELECT COUNT(*) FROM productos WHERE codigo = ?;";
        try {
            con = cn.Conectar();
            ps = con.prepareStatement(sql);
            ps.setInt(1, codigo);
            rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getBoolean(1);
            }
        } catch (Exception e) {
            System.out.println("ERROR AL COMPROBAR CODIGO PRODUCTO: " + e);
        }
        return false;
    }

    public boolean comprobarCantidad(int codigo, int cantidad) {
        String sql = "SELECT fn_comprobar_cantidad(?, ?);";
        try {
            con = cn.Conectar();
            ps = con.prepareStatement(sql);
            ps.setInt(1, codigo);
            ps.setInt(2, cantidad);
            rs = ps.executeQuery();
            while (rs.next()) {
                return rs.getBoolean(1);
            }
        } catch (Exception e) {
            System.out.println("ERROR AL COMPROBAR CANTIDAD PRODUCTOS STOCK: " + e);
        }
        return false;
    }

    public Producto buscarProducto(int codigo) {
        String sql = "SELECT codigo, nombre, precio FROM productos WHERE codigo = ?";
        try {
            con = cn.Conectar();
            ps = con.prepareStatement(sql);
            ps.setInt(1, codigo);
            rs = ps.executeQuery();
            while (rs.next()) {
                Producto p = new Producto();
                p.setCodigo(rs.getInt(1));
                p.setNombre(rs.getString(2));
                p.setPrecio(rs.getFloat(3));
                return p;
            }
        } catch (Exception e) {
            System.out.println("ERROR BUSCAR PRUDUDCTO AGREGAR TABLA: " + e);
        }
        return null;
    }

    public void GenararFactura(int noFactura, int codigoCliente, int codigoVendedor, float total) {
        String sql = "CALL sp_generar_factura(?, ?, ?, ?);";
        try {
            con = cn.Conectar();
            ps = con.prepareStatement(sql);
            ps.setInt(1, noFactura);
            ps.setInt(2, codigoCliente);
            ps.setInt(3, codigoVendedor);
            ps.setFloat(4, total);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("ERROR GENERAR FACTURA:" + e);
        }
    }

    public void AgregarProductoDetalleFactura(int noFactura, int codigoProducto, int cantidad, float precio) {
        String sql = "CALL sp_agregar_producto_detalle_factura(?, ?, ?, ?);";
        try {
            con = cn.Conectar();
            ps = con.prepareStatement(sql);
            ps.setInt(1, noFactura);
            ps.setInt(2, codigoProducto);
            ps.setInt(3, cantidad);
            ps.setFloat(4, precio);
            ps.executeUpdate();
        } catch (Exception e) {
            System.out.println("ERROR GENERAR FACTURA:" + e);
        }
    }

    public void solicitarFactura(int noFactura) {
        String query1 = "SELECT * FROM informacion_facturas WHERE no_factura = ?;";
        String query2 = "SELECT * FROM informacion_detalle_facturas WHERE no_factura = ?;";
        Factura factura = new Factura();
        LinkedList<DetalleFactura> listaDetalleFactura = new LinkedList<DetalleFactura>();
        boolean encontrado = false;
        try {
            con = cn.Conectar();
            ps = con.prepareStatement(query1);
            ps.setInt(1, noFactura);
            rs = ps.executeQuery();
            while (rs.next()) {
                factura.setCaja(rs.getInt(1));
                factura.setNoFactura(rs.getInt(2));
                factura.setNit(rs.getString(3));
                factura.setNombre(rs.getString(4));
                factura.setFecha(rs.getString(5));
                factura.setTotal(rs.getFloat(6));
                encontrado = true;
            }
            if (encontrado) {
                ps = con.prepareStatement(query2);
                ps.setInt(1, noFactura);
                rs = ps.executeQuery();
                while (rs.next()) {
                    DetalleFactura detalleFactura = new DetalleFactura();
                    detalleFactura.setCodigo(rs.getInt(2));
                    detalleFactura.setNombre(rs.getString(3));
                    detalleFactura.setCantidad(rs.getInt(4));
                    detalleFactura.setPrecio(rs.getFloat(5));
                    detalleFactura.setSubTotal(rs.getFloat(6));
                    listaDetalleFactura.add(detalleFactura);
                }
                generarPDFFactura(factura, listaDetalleFactura);
            } else {
                JOptionPane.showMessageDialog(null, "El no de la factura no fue encontrado", "Alerta", JOptionPane.WARNING_MESSAGE);
            }

        } catch (Exception e) {
            System.out.println("ERROR GENERAR PDF DE LA FACTURA: " + e);
        }

    }

    public void generarPDFFactura(Factura factura, LinkedList<DetalleFactura> listaDetalleFactura) throws FileNotFoundException, FileNotFoundException, DocumentException {
        FileOutputStream gen = new FileOutputStream("factura.pdf");
        Document documento = new Document();
        PdfWriter.getInstance(documento, gen);
        documento.open();
        BaseColor color = new BaseColor(255, 255, 255); // Rojo
        Font font = new Font(Font.FontFamily.UNDEFINED, 12, Font.NORMAL, color);
        Paragraph parrafo = new Paragraph("BLUE MALL");
        parrafo.setAlignment(1);
        documento.add(parrafo);
        //INFORMACION CLIENTE
        documento.add(new Paragraph("\n"));
        documento.add(new Paragraph("Caja: " + factura.getCaja()));
        documento.add(new Paragraph("No Factura: " + factura.getNoFactura()));
        documento.add(new Paragraph("NIT: " + factura.getNit()));
        documento.add(new Paragraph("Nombre: " + factura.getNombre()));
        documento.add(new Paragraph("Fecha: " + factura.getFecha()));
        documento.add(new Paragraph("\n"));
        //TABLA
        PdfPTable tabla = new PdfPTable(5);
        tabla.setWidthPercentage(100);
        tabla.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell codigo = new PdfPCell(new Phrase("Codigo", font));
        codigo.setBackgroundColor(BaseColor.RED);
        codigo.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell nombre = new PdfPCell(new Phrase("Nombre", font));
        nombre.setBackgroundColor(BaseColor.RED);
        nombre.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell cantidad = new PdfPCell(new Phrase("Cantidad", font));
        cantidad.setBackgroundColor(BaseColor.RED);
        cantidad.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell precio = new PdfPCell(new Phrase("Precio", font));
        precio.setBackgroundColor(BaseColor.RED);
        precio.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell subTotal = new PdfPCell(new Phrase("Subtotal", font));
        subTotal.setBackgroundColor(BaseColor.RED);
        subTotal.setHorizontalAlignment(Element.ALIGN_CENTER);
        tabla.addCell(codigo);
        tabla.addCell(nombre);
        tabla.addCell(cantidad);
        tabla.addCell(precio);
        tabla.addCell(subTotal);
        for (DetalleFactura d : listaDetalleFactura) {
            tabla.addCell(String.valueOf(d.getCodigo()));
            tabla.addCell(d.getNombre());
            tabla.addCell(String.valueOf(d.getCantidad()));
            tabla.addCell(Float.toString(d.getPrecio()));
            tabla.addCell(Float.toString(d.getSubTotal()));
        }

        // Crear un objeto de tipo float array para el ancho de cada columna
        float[] columnWidths = {1f, 4f, 2f, 2f, 2f};
        tabla.setTotalWidth(columnWidths);

        documento.add(tabla);
        documento.add(new Paragraph("\n"));
        documento.add(new Paragraph("Total: " + factura.getTotal()));
        documento.close();
        JOptionPane.showMessageDialog(null, "La factura se genero exitosamente", "Alerta", JOptionPane.WARNING_MESSAGE);
        try {
            File sucursales_doc = new File("factura.pdf");
            Desktop.getDesktop().open(sucursales_doc);
        } catch (Exception e) {
        }
    }
}
