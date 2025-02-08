package derrapchatgpt;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class GestorVehiculos extends JPanel {
    private JTable tablaVehiculos;
    private DefaultTableModel modelo;
    private Conector conector;

    public GestorVehiculos() {
        setLayout(new BorderLayout());
        conector = new Conector();

        // Panel superior con título
        JPanel panelSuperior = new JPanel();
        panelSuperior.add(new JLabel("Gestión de Vehículos"));
        add(panelSuperior, BorderLayout.NORTH);

        // Tabla de vehículos
        modelo = new DefaultTableModel();
        modelo.addColumn("Matrícula");
        modelo.addColumn("Marca");
        modelo.addColumn("Modelo");
        modelo.addColumn("Año");
        modelo.addColumn("Cliente ID");
        tablaVehiculos = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(tablaVehiculos);
        add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel();
        JButton btnActualizar = new JButton("Actualizar Lista");
        panelBotones.add(btnActualizar);
        add(panelBotones, BorderLayout.SOUTH);

        // Eventos de botones
        btnActualizar.addActionListener(e -> cargarVehiculos());

        // Cargar vehículos al abrir el panel
        cargarVehiculos();
    }

    private void cargarVehiculos() {
        modelo.setRowCount(0);
        String sql = "SELECT matricula, marca, modelo, año, cliente_id FROM vehiculos";

        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getString("matricula"),
                        rs.getString("marca"),
                        rs.getString("modelo"),
                        rs.getInt("año"),
                        rs.getInt("cliente_id")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void agregarVehiculo() {
        String matricula = JOptionPane.showInputDialog("Ingrese la matrícula del vehículo:");
        String marca = JOptionPane.showInputDialog("Ingrese la marca:");
        String modeloVehiculo = JOptionPane.showInputDialog("Ingrese el modelo:");
        String anioStr = JOptionPane.showInputDialog("Ingrese el año:");

        if (matricula == null || marca == null || modeloVehiculo == null || anioStr == null ||
            matricula.isEmpty() || marca.isEmpty() || modeloVehiculo.isEmpty() || anioStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int anio;
        try {
            anio = Integer.parseInt(anioStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese un año válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO vehiculos (matricula, marca, modelo, año) VALUES (?, ?, ?, ?)";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, matricula);
            ps.setString(2, marca);
            ps.setString(3, modeloVehiculo);
            ps.setInt(4, anio);
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Vehículo agregado correctamente.");
                cargarVehiculos();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void modificarVehiculo() {
        int filaSeleccionada = tablaVehiculos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un vehículo para modificar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String matricula = (String) modelo.getValueAt(filaSeleccionada, 0);
        String nuevaMarca = JOptionPane.showInputDialog("Nueva marca:");
        String nuevoModelo = JOptionPane.showInputDialog("Nuevo modelo:");
        String nuevoAnioStr = JOptionPane.showInputDialog("Nuevo año:");

        if (nuevaMarca == null || nuevoModelo == null || nuevoAnioStr == null ||
            nuevaMarca.isEmpty() || nuevoModelo.isEmpty() || nuevoAnioStr.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar los nuevos valores.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int nuevoAnio;
        try {
            nuevoAnio = Integer.parseInt(nuevoAnioStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese un año válido.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE vehiculos SET marca = ?, modelo = ?, anio = ? WHERE matricula = ?";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nuevaMarca);
            ps.setString(2, nuevoModelo);
            ps.setInt(3, nuevoAnio);
            ps.setString(4, matricula);
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Vehículo modificado correctamente.");
                cargarVehiculos();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void eliminarVehiculo() {
        int filaSeleccionada = tablaVehiculos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un vehículo para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String matricula = (String) modelo.getValueAt(filaSeleccionada, 0);
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar este vehículo?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM vehiculos WHERE matricula = ?";
            try (Connection conexion = conector.getConexion();
                 PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, matricula);
                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas > 0) {
                    JOptionPane.showMessageDialog(this, "Vehículo eliminado correctamente.");
                    cargarVehiculos();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}


