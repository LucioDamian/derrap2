package derrap2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class GestorStock extends JPanel {
    private JTable tablaStock;
    private DefaultTableModel modelo;
    private Conector conector;

    public GestorStock() {
        setLayout(new BorderLayout());
        conector = new Conector();

        // Panel superior con título
        JPanel panelSuperior = new JPanel();
        panelSuperior.add(new JLabel("Gestión de Stock"));
        add(panelSuperior, BorderLayout.NORTH);

        // Tabla de stock
        modelo = new DefaultTableModel();
        modelo.addColumn("ID Repuesto");
        modelo.addColumn("Nombre");
        modelo.addColumn("Marca");
        modelo.addColumn("Precio Compra");
        modelo.addColumn("Precio Venta");
        modelo.addColumn("Cantidad");
        tablaStock = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(tablaStock);
        add(scrollPane, BorderLayout.CENTER);

        // Cargar stock al abrir el panel
        cargarStock();
    }

    public void cargarStock() {
        modelo.setRowCount(0);
        String sql = "SELECT idrepuesto, nombre, marca, precio_compra, precio_venta, cantidad FROM stock";

        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getInt("idrepuesto"),
                    rs.getString("nombre"), // Cambiado de "nombre_pieza" a "nombre"
                    rs.getString("marca"),
                    rs.getDouble("precio_compra"),
                    rs.getDouble("precio_venta"),
                    rs.getInt("cantidad")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    public void actualizarStock() {
        modelo.setRowCount(0);
        String sql = "SELECT idrepuesto, nombre, marca, precio_compra, precio_venta, cantidad FROM stock";

        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("idrepuesto"),
                        rs.getString("nombre"),
                        rs.getString("marca"),
                        rs.getDouble("precio_compra"),
                        rs.getDouble("precio_venta"),
                        rs.getInt("cantidad")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al actualizar stock: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
