package derrapchatgpt;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class GestorRepuestos extends JPanel {
    private JTable tablaRepuestos;
    private DefaultTableModel modelo;
    private Conector conector;

    public GestorRepuestos() {
        setLayout(new BorderLayout());
        conector = new Conector();

        // Panel superior con título
        JPanel panelSuperior = new JPanel();
        panelSuperior.add(new JLabel("Gestión de Repuestos"));
        add(panelSuperior, BorderLayout.NORTH);

        // Tabla de repuestos
        modelo = new DefaultTableModel();
        modelo.addColumn("ID Repuesto");
        modelo.addColumn("Nombre");
        modelo.addColumn("Marca");
        modelo.addColumn("Precio Compra");
        modelo.addColumn("Precio Venta");
        modelo.addColumn("Cantidad");
        tablaRepuestos = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(tablaRepuestos);
        add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel();
        JButton btnActualizar = new JButton("Actualizar Lista");
        panelBotones.add(btnActualizar);
        add(panelBotones, BorderLayout.SOUTH);

        // Eventos de botones
        btnActualizar.addActionListener(e -> cargarRepuestos());

        // Cargar repuestos al abrir el panel
        cargarRepuestos();
    }

    private void cargarRepuestos() {
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
        }
    }

    private void agregarRepuesto() {
        String nombre = JOptionPane.showInputDialog("Ingrese el nombre del repuesto:");
        String cantidadStr = JOptionPane.showInputDialog("Ingrese la cantidad:");
        String proveedor = JOptionPane.showInputDialog("Ingrese el proveedor:");

        if (nombre == null || cantidadStr == null || proveedor == null ||
            nombre.isEmpty() || cantidadStr.isEmpty() || proveedor.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese una cantidad válida.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO repuestos (nombre, cantidad, proveedor) VALUES (?, ?, ?)";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nombre);
            ps.setInt(2, cantidad);
            ps.setString(3, proveedor);
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Repuesto agregado correctamente.");
                cargarRepuestos();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void modificarRepuesto() {
        int filaSeleccionada = tablaRepuestos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un repuesto para modificar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idRepuesto = (int) modelo.getValueAt(filaSeleccionada, 0);
        String nuevoNombre = JOptionPane.showInputDialog("Nuevo nombre:");
        String nuevaCantidadStr = JOptionPane.showInputDialog("Nueva cantidad:");

        if (nuevoNombre == null || nuevaCantidadStr == null || nuevoNombre.isEmpty() || nuevaCantidadStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar los nuevos valores.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int nuevaCantidad;
        try {
            nuevaCantidad = Integer.parseInt(nuevaCantidadStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese una cantidad válida.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE repuestos SET nombre = ?, cantidad = ? WHERE idrepuesto = ?";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nuevoNombre);
            ps.setInt(2, nuevaCantidad);
            ps.setInt(3, idRepuesto);
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Repuesto modificado correctamente.");
                cargarRepuestos();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void eliminarRepuesto() {
        int filaSeleccionada = tablaRepuestos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un repuesto para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idRepuesto = (int) modelo.getValueAt(filaSeleccionada, 0);
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar este repuesto?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM repuestos WHERE idrepuesto = ?";
            try (Connection conexion = conector.getConexion();
                 PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setInt(1, idRepuesto);
                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas > 0) {
                    JOptionPane.showMessageDialog(this, "Repuesto eliminado correctamente.");
                    cargarRepuestos();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}


