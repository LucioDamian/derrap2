package derrapchatgpt;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class GestorClientes extends JPanel {
    private JTable tablaClientes;
    private DefaultTableModel modelo;
    private Conector conector;

    public GestorClientes() {
        setLayout(new BorderLayout());
        conector = new Conector();

        // Panel superior con título
        JPanel panelSuperior = new JPanel();
        panelSuperior.add(new JLabel("Gestión de Clientes"));
        add(panelSuperior, BorderLayout.NORTH);

        // Tabla de clientes
        modelo = new DefaultTableModel();
        modelo.addColumn("ID Cliente");
        modelo.addColumn("DNI");
        modelo.addColumn("Nombre");
        modelo.addColumn("Teléfono");
        tablaClientes = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(tablaClientes);
        add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel();
        JButton btnAgregar = new JButton("Agregar Cliente");
        JButton btnModificar = new JButton("Modificar Cliente");
        JButton btnEliminar = new JButton("Eliminar Cliente");
        JButton btnActualizar = new JButton("Actualizar Lista");
        JButton btnAsignarVehiculo = new JButton("Asignar Vehículo");

        panelBotones.add(btnAgregar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnAsignarVehiculo);
        add(panelBotones, BorderLayout.SOUTH);

        // Eventos de botones
        btnAgregar.addActionListener(e -> agregarCliente());
        btnModificar.addActionListener(e -> modificarCliente());
        btnEliminar.addActionListener(e -> eliminarCliente());
        btnActualizar.addActionListener(e -> cargarClientes());
        btnAsignarVehiculo.addActionListener(e -> asignarVehiculoACliente());

        // Cargar clientes al abrir el panel
        cargarClientes();
    }

    private void cargarClientes() {
        modelo.setRowCount(0);
        String sql = "SELECT idcliente, dni, nombre, telefono FROM clientes";

        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("idcliente"),
                        rs.getString("dni"),
                        rs.getString("nombre"),
                        rs.getString("telefono")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void agregarCliente() {
        String dni = JOptionPane.showInputDialog("Ingrese el DNI del cliente:");
        String nombre = JOptionPane.showInputDialog("Ingrese el nombre del cliente:");
        String telefono = JOptionPane.showInputDialog("Ingrese el teléfono del cliente:");

        if (dni == null || nombre == null || telefono == null || dni.isEmpty() || nombre.isEmpty() || telefono.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO clientes (dni, nombre, telefono) VALUES (?, ?, ?)";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, dni);
            ps.setString(2, nombre);
            ps.setString(3, telefono);
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Cliente agregado correctamente.");
                cargarClientes();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void modificarCliente() {
        int filaSeleccionada = tablaClientes.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente para modificar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idCliente = (int) modelo.getValueAt(filaSeleccionada, 0);
        String nuevoNombre = JOptionPane.showInputDialog("Nuevo nombre del cliente:");
        String nuevoTelefono = JOptionPane.showInputDialog("Nuevo teléfono del cliente:");

        if (nuevoNombre == null || nuevoTelefono == null || nuevoNombre.isEmpty() || nuevoTelefono.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar los nuevos valores.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE clientes SET nombre = ?, telefono = ? WHERE idcliente = ?";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nuevoNombre);
            ps.setString(2, nuevoTelefono);
            ps.setInt(3, idCliente);
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Cliente modificado correctamente.");
                cargarClientes();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void eliminarCliente() {
        int filaSeleccionada = tablaClientes.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idCliente = (int) modelo.getValueAt(filaSeleccionada, 0);
        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Está seguro de eliminar este cliente?", "Confirmar eliminación", JOptionPane.YES_NO_OPTION);

        if (confirmacion == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM clientes WHERE idcliente = ?";
            try (Connection conexion = conector.getConexion();
                 PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setInt(1, idCliente);
                int filasAfectadas = ps.executeUpdate();
                if (filasAfectadas > 0) {
                    JOptionPane.showMessageDialog(this, "Cliente eliminado correctamente.");
                    cargarClientes();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void asignarVehiculoACliente() {
        int filaSeleccionada = tablaClientes.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente para asignarle un vehículo.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idCliente = (int) modelo.getValueAt(filaSeleccionada, 0);
        String matricula = JOptionPane.showInputDialog("Ingrese la matrícula del vehículo:");

        if (matricula == null || matricula.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar la matrícula del vehículo.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Actualiza el cliente asociado al vehículo
        String sql = "UPDATE vehiculos SET cliente_id = ? WHERE matricula = ?";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idCliente);
            ps.setString(2, matricula);
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Vehículo asignado correctamente al cliente.");
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró el vehículo con la matrícula proporcionada.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}




