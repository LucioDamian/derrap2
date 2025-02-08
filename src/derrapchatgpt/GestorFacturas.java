package derrapchatgpt;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class GestorFacturas extends JPanel {
    private JTable tablaFacturas;
    private DefaultTableModel modelo;
    private Conector conector;

    public GestorFacturas() {
        setLayout(new BorderLayout());
        conector = new Conector();

        // Panel superior con título
        JPanel panelSuperior = new JPanel();
        panelSuperior.add(new JLabel("Gestión de Facturas"));
        add(panelSuperior, BorderLayout.NORTH);

        // Tabla de facturas
        modelo = new DefaultTableModel();
        modelo.addColumn("ID Factura");
        modelo.addColumn("Fecha");
        modelo.addColumn("Cliente ID");
        modelo.addColumn("Vehículo Matrícula");
        modelo.addColumn("Total");
        modelo.addColumn("Método de Pago");
        tablaFacturas = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(tablaFacturas);
        add(scrollPane, BorderLayout.CENTER);

        // Cargar facturas al abrir el panel
        cargarFacturas();
    }

    private void cargarFacturas() {
        modelo.setRowCount(0);
        String sql = "SELECT idfactura, fecha, cliente_id, vehiculo_matricula, total, metodo_pago FROM facturas";

        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("idfactura"),
                        rs.getTimestamp("fecha"),
                        rs.getInt("cliente_id"),
                        rs.getString("vehiculo_matricula"),
                        rs.getDouble("total"),
                        rs.getString("metodo_pago")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void agregarFactura() {
        String dniCliente = JOptionPane.showInputDialog("Ingrese el DNI del cliente:");
        String montoStr = JOptionPane.showInputDialog("Ingrese el monto total:");

        if (dniCliente == null || montoStr == null || dniCliente.isEmpty() || montoStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        double monto;
        try {
            monto = Double.parseDouble(montoStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese un monto válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO facturas (fecha, cliente_dni, monto) VALUES (NOW(), ?, ?)";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, dniCliente);
            ps.setDouble(2, monto);
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Factura agregada correctamente.");
                cargarFacturas();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void eliminarFactura() {
        int filaSeleccionada = tablaFacturas.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una factura para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idFactura = (int) modelo.getValueAt(filaSeleccionada, 0);
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar esta factura?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM facturas WHERE idfactura = ?";
            try (Connection conexion = conector.getConexion();
                 PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setInt(1, idFactura);
                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas > 0) {
                    JOptionPane.showMessageDialog(this, "Factura eliminada correctamente.");
                    cargarFacturas();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}



