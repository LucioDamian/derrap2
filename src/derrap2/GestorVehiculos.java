package derrap2;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.*;

public class GestorVehiculos extends JPanel {
    private JTable tabla;
    private DefaultTableModel modelo;
    private Conector conector;

    public GestorVehiculos() {
        conector = new Conector();
        setLayout(new BorderLayout());

        modelo = new DefaultTableModel();
        modelo.addColumn("Matrícula");
        modelo.addColumn("Marca");
        modelo.addColumn("Modelo");
        modelo.addColumn("Año");
        modelo.addColumn("Cliente ID");

        tabla = new JTable(modelo);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JPanel panelBotones = new JPanel();
        JButton btnAgregar = new JButton("Agregar Vehículo");
        JButton btnModificar = new JButton("Modificar Vehículo");
        JButton btnEliminar = new JButton("Eliminar Vehículo");

        panelBotones.add(btnAgregar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        add(panelBotones, BorderLayout.SOUTH);

        btnAgregar.addActionListener(e -> new FormularioVehiculo(this, null));
        btnModificar.addActionListener(e -> modificarVehiculo());
        btnEliminar.addActionListener(e -> eliminarVehiculo());

        cargarVehiculos();
    }


    void cargarVehiculos() {
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
                        rs.getInt("año"), // Se asegura que "año" se lea correctamente
                        rs.getInt("cliente_id")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void abrirFormularioVehiculo(String matricula) {
        new FormularioVehiculo(this, matricula);
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
        int filaSeleccionada = tabla.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un vehículo para modificar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String matricula = (String) modelo.getValueAt(filaSeleccionada, 0);
        new FormularioVehiculo(this, matricula);
    }

    private void eliminarVehiculo() {
        int filaSeleccionada = tabla.getSelectedRow();
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


