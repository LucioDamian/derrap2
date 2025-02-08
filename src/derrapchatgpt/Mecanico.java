package derrapchatgpt;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Mecanico extends JFrame {
    private JTable table;
    private DefaultTableModel modelo;
    private Conector con;

    public Mecanico() {
        setTitle("Panel de Mecánico");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        con = new Conector();

        JPanel contentPane = new JPanel(new BorderLayout());
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);

        // Modelo de la tabla
        modelo = new DefaultTableModel();
        modelo.addColumn("ID Orden");
        modelo.addColumn("Estado");
        modelo.addColumn("DNI Mecánico");
        modelo.addColumn("Matrícula Vehículo");
        table = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(table);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new GridLayout(2, 3, 10, 10));
        JButton btnAgregar = new JButton("Agregar Orden");
        JButton btnModificar = new JButton("Modificar Orden");
        JButton btnEliminar = new JButton("Eliminar Orden");
        JButton btnActualizarEstado = new JButton("Actualizar Estado");
        JButton btnConsultarStock = new JButton("Consultar Stock");
        JButton btnSolicitarPieza = new JButton("Solicitar Piezas");
        JButton btnActualizar = new JButton("Actualizar Órdenes");
        JButton btnSalir = new JButton("Salir");

        // Agregar botones al panel
        panelBotones.add(btnAgregar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnActualizarEstado);
        panelBotones.add(btnConsultarStock);
        panelBotones.add(btnSolicitarPieza);
        panelBotones.add(btnActualizar);
        panelBotones.add(btnSalir);
        contentPane.add(panelBotones, BorderLayout.SOUTH);

        // Acciones de botones
        btnAgregar.addActionListener(e -> agregarOrden());
        btnModificar.addActionListener(e -> modificarOrden());
        btnEliminar.addActionListener(e -> eliminarOrden());
        btnActualizarEstado.addActionListener(e -> actualizarEstadoOrden());
        btnConsultarStock.addActionListener(e -> consultarStock());
        btnSolicitarPieza.addActionListener(e -> solicitarPieza());
        btnActualizar.addActionListener(e -> cargarOrdenes());
        btnSalir.addActionListener(e -> dispose());

        // Cargar órdenes al iniciar
        cargarOrdenes();
    }
    
    private void actualizarEstadoOrden() {
        int filaSeleccionada = table.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una orden de la lista.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Obtener el ID de la orden seleccionada
        int idOrden = (int) modelo.getValueAt(filaSeleccionada, 0);

        // Opciones de estado
        String[] opciones = {"Sin comenzar", "En reparación", "Finalizada"};
        String nuevoEstado = (String) JOptionPane.showInputDialog(
                this,
                "Seleccione el nuevo estado de la orden:",
                "Actualizar Estado",
                JOptionPane.QUESTION_MESSAGE,
                null,
                opciones,
                opciones[0]);

        // Validar si el usuario seleccionó un estado
        if (nuevoEstado == null) {
            return;
        }

        String sql = "UPDATE ordenreparacion SET estadoreparacion = ? WHERE idorden = ?";

        try (Connection conexion = con.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idOrden);

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Estado actualizado correctamente.");
                cargarOrdenes();
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró la orden.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al actualizar el estado: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void cargarOrdenes() {
        modelo.setRowCount(0);
        String sql = "SELECT idorden, estadoreparacion, usuario_dni, vehiculo_matricula FROM ordenreparacion";
        try (Connection conexion = con.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("idorden"),
                        rs.getString("estadoreparacion"),
                        rs.getString("usuario_dni"),
                        rs.getString("vehiculo_matricula")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void agregarOrden() {
        String dniMecanico = JOptionPane.showInputDialog("Ingrese el DNI del mecánico:");
        String matricula = JOptionPane.showInputDialog("Ingrese la matrícula del vehículo:");
        String descripcion = JOptionPane.showInputDialog("Ingrese la descripción del problema:");

        if (dniMecanico == null || dniMecanico.trim().isEmpty() ||
            matricula == null || matricula.trim().isEmpty() ||
            descripcion == null || descripcion.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO ordenreparacion (usuario_dni, vehiculo_matricula, estadoreparacion, descripcion) VALUES (?, ?, 'Sin comenzar', ?)";
        
        try (Connection conexion = con.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, dniMecanico);
            ps.setString(2, matricula);
            ps.setString(3, descripcion);

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Orden de reparación agregada correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarOrdenes();
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo agregar la orden.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void modificarOrden() {
        String idOrden = JOptionPane.showInputDialog("Ingrese el ID de la orden a modificar:");
        String nuevoEstado = JOptionPane.showInputDialog("Ingrese el nuevo estado de la orden:");

        String sql = "UPDATE ordenreparacion SET estadoreparacion = ? WHERE idorden = ?";

        try (Connection conexion = con.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, Integer.parseInt(idOrden));

            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Orden modificada correctamente.");
                cargarOrdenes();
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró la orden.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void eliminarOrden() {
        String idOrden = JOptionPane.showInputDialog("Ingrese el ID de la orden a eliminar:");
        String sql = "DELETE FROM ordenreparacion WHERE idorden = ?";

        try (Connection conexion = con.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setInt(1, Integer.parseInt(idOrden));
            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Orden eliminada correctamente.");
                cargarOrdenes();
            } else {
                JOptionPane.showMessageDialog(this, "No se encontró la orden.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void consultarStock() {
        String sql = "SELECT nombre, cantidad FROM stock ORDER BY nombre";

        StringBuilder stockLista = new StringBuilder("Stock de piezas disponibles:\n");

        try (Connection conexion = con.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                stockLista.append(rs.getString("nombre"))
                          .append(": ")
                          .append(rs.getInt("cantidad"))
                          .append(" unidades\n");
            }

            JOptionPane.showMessageDialog(this, stockLista.toString(), "Stock Disponible", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al consultar el stock: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void solicitarPieza() {
        String pieza = JOptionPane.showInputDialog("Ingrese el nombre de la pieza a solicitar:");
        String cantidadStr = JOptionPane.showInputDialog("Ingrese la cantidad a solicitar:");

        if (pieza == null || pieza.trim().isEmpty() || cantidadStr == null || cantidadStr.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar el nombre de la pieza y la cantidad.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int cantidad;
        try {
            cantidad = Integer.parseInt(cantidadStr);
            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese una cantidad válida.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO pedidos (pieza, cantidad, estado) VALUES (?, ?, 'Pendiente')";

        try (Connection conexion = con.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, pieza);
            ps.setInt(2, cantidad);

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Pedido de pieza registrado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Error al registrar el pedido.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al solicitar la pieza: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


}


