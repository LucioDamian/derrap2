package derrap2;
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

        // Panel de botones
        JPanel panelBotones = new JPanel();
        JButton btnAgregar = new JButton("Generar Factura");
        JButton btnEliminar = new JButton("Eliminar Factura");
        JButton btnActualizar = new JButton("Actualizar");

        panelBotones.add(btnAgregar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnActualizar);
        add(panelBotones, BorderLayout.SOUTH);

        // Eventos de botones
        btnAgregar.addActionListener(e -> generarFactura());
        btnEliminar.addActionListener(e -> eliminarFactura());
        btnActualizar.addActionListener(e -> cargarFacturas());

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
    
    private double calcularTotal(String descripcion) {
        double total = 0;
        String[] servicios = descripcion.split(", ");

        for (String servicio : servicios) {
            String sql = "SELECT precio FROM servicios WHERE nombre = ?";
            try (Connection conexion = conector.getConexion();
                 PreparedStatement ps = conexion.prepareStatement(sql)) {
                ps.setString(1, servicio);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    total += rs.getDouble("precio");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return total;
    }
    
    private String obtenerServiciosCliente(int clienteId) {
        String descripcion = "";
        String sql = "SELECT descripcion FROM ordenreparacion WHERE cliente_id = ? AND estadoreparacion = 'Finalizada'";

        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                descripcion = rs.getString("descripcion");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return descripcion;
    }
    
    private void generarFactura() {
        // Seleccionar cliente
        String clienteId = JOptionPane.showInputDialog("Ingrese el ID del cliente:");
        if (clienteId == null || clienteId.trim().isEmpty()) return;

        // Obtener la descripción de los servicios en las órdenes de reparación
        String descripcion = obtenerServiciosCliente(Integer.parseInt(clienteId));
        if (descripcion.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No hay servicios asociados a este cliente.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Calcular el total
        double total = calcularTotal(descripcion);
        if (total == 0) {
            JOptionPane.showMessageDialog(this, "No se encontraron precios para los servicios seleccionados.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Seleccionar método de pago
        String[] opcionesPago = {"Efectivo", "Tarjeta", "Fraccionado", "Mixto"};
        String metodoPago = (String) JOptionPane.showInputDialog(
                null,
                "Seleccione el método de pago:",
                "Método de Pago",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opcionesPago,
                opcionesPago[0]
        );

        if (metodoPago == null) return;

        // Insertar la factura
        String sql = "INSERT INTO facturas (cliente_id, vehiculo_matricula, total, metodo_pago) VALUES (?, (SELECT vehiculo_matricula FROM ordenreparacion WHERE cliente_id = ? LIMIT 1), ?, ?)";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, Integer.parseInt(clienteId));
            ps.setInt(2, Integer.parseInt(clienteId));
            ps.setDouble(3, total);
            ps.setString(4, metodoPago);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Factura generada correctamente.");
            cargarFacturas();
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



