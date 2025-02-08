package derrapchatgpt;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class GestorPedidos extends JPanel {
    private JTable tablaPedidos;
    private DefaultTableModel modelo;
    private Conector conector;

    public GestorPedidos() {
        setLayout(new BorderLayout());
        conector = new Conector();

        // Panel superior con título
        JPanel panelSuperior = new JPanel();
        panelSuperior.add(new JLabel("Gestión de Pedidos"));
        add(panelSuperior, BorderLayout.NORTH);

        // Tabla de pedidos
        modelo = new DefaultTableModel();
        modelo.addColumn("ID Pedido");
        modelo.addColumn("Pieza");
        modelo.addColumn("Cantidad");
        modelo.addColumn("Estado");
        modelo.addColumn("Fecha Pedido");
        tablaPedidos = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(tablaPedidos);
        add(scrollPane, BorderLayout.CENTER);

        // Cargar pedidos al abrir el panel
        cargarPedidos();
    }

    private void cargarPedidos() {
        modelo.setRowCount(0);
        String sql = "SELECT idpedido, pieza, cantidad, estado, fecha_pedido FROM pedidos";

        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("idpedido"),
                        rs.getString("pieza"),
                        rs.getInt("cantidad"),
                        rs.getString("estado"),
                        rs.getTimestamp("fecha_pedido")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void agregarPedido() {
        String proveedor = JOptionPane.showInputDialog("Ingrese el proveedor:");
        String estado = JOptionPane.showInputDialog("Ingrese el estado del pedido:");

        if (proveedor == null || estado == null || proveedor.isEmpty() || estado.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO pedidos (fecha, proveedor, estado) VALUES (NOW(), ?, ?)";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, proveedor);
            ps.setString(2, estado);
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Pedido agregado correctamente.");
                cargarPedidos();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void modificarPedido() {
        int filaSeleccionada = tablaPedidos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido para modificar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idPedido = (int) modelo.getValueAt(filaSeleccionada, 0);
        String nuevoEstado = JOptionPane.showInputDialog("Ingrese el nuevo estado:");

        if (nuevoEstado == null || nuevoEstado.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar un estado válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE pedidos SET estado = ? WHERE idpedido = ?";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nuevoEstado);
            ps.setInt(2, idPedido);
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Pedido modificado correctamente.");
                cargarPedidos();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void eliminarPedido() {
        int filaSeleccionada = tablaPedidos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idPedido = (int) modelo.getValueAt(filaSeleccionada, 0);
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar este pedido?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM pedidos WHERE idpedido = ?";
            try (Connection conexion = conector.getConexion();
                 PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setInt(1, idPedido);
                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas > 0) {
                    JOptionPane.showMessageDialog(this, "Pedido eliminado correctamente.");
                    cargarPedidos();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}


