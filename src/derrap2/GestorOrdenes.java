package derrap2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;

public class GestorOrdenes extends JPanel {
    private JTable tablaOrdenes;
    private DefaultTableModel modelo;
    private Conector conector;
    private JComboBox<String> filtroEstado;

    public GestorOrdenes() {
        setLayout(new BorderLayout());
        conector = new Conector();

        // Panel superior con título
        JPanel panelSuperior = new JPanel(new FlowLayout());
        panelSuperior.add(new JLabel("Gestión de Órdenes de Reparación"));

        // Filtro por estado de reparación
        filtroEstado = new JComboBox<>(new String[]{"Todas", "Sin comenzar", "En diagnóstico", "En reparación", "Finalizada"});
        filtroEstado.addActionListener(e -> cargarOrdenes());
        panelSuperior.add(new JLabel("Filtrar por estado:"));
        panelSuperior.add(filtroEstado);
        add(panelSuperior, BorderLayout.NORTH);

        // Tabla de órdenes
        modelo = new DefaultTableModel();
        modelo.addColumn("ID Orden");
        modelo.addColumn("Estado");
        modelo.addColumn("DNI Mecánico");
        modelo.addColumn("Matrícula Vehículo");
        modelo.addColumn("Cliente ID");
        modelo.addColumn("Descripción");
        modelo.addColumn("Fecha Visita");
        tablaOrdenes = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(tablaOrdenes);
        add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel();
        JButton btnAgregar = new JButton("Agregar Orden");
        JButton btnModificar = new JButton("Modificar Orden");
        JButton btnEliminar = new JButton("Eliminar Orden");
        JButton btnActualizar = new JButton("Actualizar Lista");

        panelBotones.add(btnAgregar);
        panelBotones.add(btnModificar);
        panelBotones.add(btnEliminar);
        panelBotones.add(btnActualizar);
        add(panelBotones, BorderLayout.SOUTH);

        // Eventos de botones
        btnAgregar.addActionListener(e -> agregarOrden());
        btnModificar.addActionListener(e -> mostrarVentanaSeleccionModificar());
        btnEliminar.addActionListener(e -> eliminarOrden());
        btnActualizar.addActionListener(e -> cargarOrdenes());

        // Cargar órdenes al abrir el panel
        cargarOrdenes();
    }
    
    private void eliminarOrden() {
        int filaSeleccionada = tablaOrdenes.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una orden para eliminar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idOrden = (int) modelo.getValueAt(filaSeleccionada, 0);

        // Confirmación antes de eliminar
        int confirmacion = JOptionPane.showConfirmDialog(this, 
                "¿Está seguro de que desea eliminar la orden seleccionada?", 
                "Confirmar eliminación", 
                JOptionPane.YES_NO_OPTION);

        if (confirmacion != JOptionPane.YES_OPTION) {
            return; // Si el usuario cancela, no se hace nada
        }

        String sql = "DELETE FROM ordenreparacion WHERE idorden = ?";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Orden eliminada correctamente.");
                cargarOrdenes(); // Recargar la tabla después de la eliminación
            } else {
                JOptionPane.showMessageDialog(this, "No se pudo eliminar la orden.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al eliminar la orden.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }


    private void agregarOrden() {
        String matricula = JOptionPane.showInputDialog("Ingrese la matrícula del vehículo:");
        if (matricula == null || matricula.trim().isEmpty()) return;

        // Obtener cliente_id antes de insertar la orden
        String sqlObtenerCliente = "SELECT cliente_id FROM vehiculos WHERE matricula = ?";
        int clienteId = -1;
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sqlObtenerCliente)) {
            ps.setString(1, matricula);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                clienteId = rs.getInt("cliente_id");
            } else {
                JOptionPane.showMessageDialog(this, "El vehículo no está registrado o no tiene un cliente asignado.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return;
        }

        String dniMecanico = JOptionPane.showInputDialog("Ingrese el DNI del mecánico:");
        if (dniMecanico == null || dniMecanico.trim().isEmpty()) return;

        String sqlInsert = "INSERT INTO ordenreparacion (vehiculo_matricula, usuario_dni, cliente_id, estadoreparacion, descripcion) VALUES (?, ?, ?, 'Sin comenzar', ?)";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sqlInsert)) {
            ps.setString(1, matricula);
            ps.setString(2, dniMecanico);
            ps.setInt(3, clienteId);
            ps.setString(4, "Descripción de la reparación");
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Orden agregada correctamente.");
            cargarOrdenes();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al agregar la orden.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    
    private String seleccionarServicios() {
        String[] opciones = {"Diagnóstico", "PreITV", "Frenos y ABS", "Aceite y filtros",
                             "Neumáticos", "Revisión oficial", "Matrículas",
                             "Chapa y pintura", "Equilibrado y alineación", "Climatización", "Electrónica"};
        
        JPanel panel = new JPanel(new GridLayout(opciones.length, 1));
        JCheckBox[] checkboxes = new JCheckBox[opciones.length];
        
        for (int i = 0; i < opciones.length; i++) {
            checkboxes[i] = new JCheckBox(opciones[i]);
            panel.add(checkboxes[i]);
        }
        
        int resultado = JOptionPane.showConfirmDialog(null, panel, "Seleccione los servicios", JOptionPane.OK_CANCEL_OPTION);
        
        if (resultado == JOptionPane.OK_OPTION) {
            List<String> seleccionados = new ArrayList<>();
            for (JCheckBox cb : checkboxes) {
                if (cb.isSelected()) {
                    seleccionados.add(cb.getText());
                }
            }
            return String.join(", ", seleccionados);
        }
        return "";
    }


    private void mostrarVentanaSeleccionModificar() {
        int filaSeleccionada = tablaOrdenes.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una orden para modificar.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int idOrden = (int) modelo.getValueAt(filaSeleccionada, 0);

        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Seleccionar campo a modificar", true);
        dialog.setSize(300, 200);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        JPanel panelOpciones = new JPanel(new GridLayout(4, 1));
        JCheckBox cbEstado = new JCheckBox("Estado");
        JCheckBox cbDniMecanico = new JCheckBox("DNI Mecánico");
        JCheckBox cbDescripcion = new JCheckBox("Descripción");
        JCheckBox cbFechaVisita = new JCheckBox("Fecha Visita");

        panelOpciones.add(cbEstado);
        panelOpciones.add(cbDniMecanico);
        panelOpciones.add(cbDescripcion);
        panelOpciones.add(cbFechaVisita);

        JButton btnConfirmar = new JButton("Confirmar");
        btnConfirmar.addActionListener(e -> {
            if (cbEstado.isSelected()) {
                mostrarVentanaModificarEstado(idOrden);
            } else if (cbDniMecanico.isSelected()) {
                modificarOrden(idOrden, "usuario_dni", JOptionPane.showInputDialog("Ingrese el nuevo DNI del mecánico:"));
            } else if (cbDescripcion.isSelected()) {
                modificarOrden(idOrden, "descripcion", seleccionarServicios());
            } else if (cbFechaVisita.isSelected()) {
                modificarOrden(idOrden, "fecha_ingreso", JOptionPane.showInputDialog("Ingrese la nueva fecha (YYYY-MM-DD HH:MM:SS):"));
            }
            dialog.dispose();
        });

        dialog.add(panelOpciones, BorderLayout.CENTER);
        dialog.add(btnConfirmar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private void cargarOrdenes() {
        modelo.setRowCount(0); // Limpia la tabla antes de cargar datos
        String filtro = filtroEstado.getSelectedItem().toString();
        
        String sql = "SELECT idorden, estadoreparacion, usuario_dni, vehiculo_matricula, cliente_id, descripcion, fecha_ingreso FROM ordenreparacion";
        
        if (!filtro.equals("Todas")) {
            sql += " WHERE estadoreparacion = ?";
        }

        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            if (!filtro.equals("Todas")) {
                ps.setString(1, filtro);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    modelo.addRow(new Object[]{
                        rs.getInt("idorden"),                  // ID Orden
                        rs.getString("estadoreparacion"),      // Estado
                        rs.getString("usuario_dni"),           // DNI Mecánico
                        rs.getString("vehiculo_matricula"),    // Matrícula Vehículo
                        rs.getInt("cliente_id"),               // Cliente ID (antes no estaba en la consulta)
                        rs.getString("descripcion"),           // Descripción
                        rs.getTimestamp("fecha_ingreso")       // Fecha Visita (Timestamp en MySQL)
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al cargar las órdenes.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostrarVentanaModificarEstado(int idOrden) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Modificar Estado de Orden", true);
        dialog.setSize(300, 250);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        // Crear botones de radio para los estados
        JPanel panelEstados = new JPanel(new GridLayout(4, 1));
        ButtonGroup grupoEstados = new ButtonGroup();

        JRadioButton rbSinComenzar = new JRadioButton("Sin comenzar");
        JRadioButton rbDiagnostico = new JRadioButton("En diagnóstico");
        JRadioButton rbEnReparacion = new JRadioButton("En reparación");
        JRadioButton rbFinalizada = new JRadioButton("Finalizada");

        grupoEstados.add(rbSinComenzar);
        grupoEstados.add(rbDiagnostico);
        grupoEstados.add(rbEnReparacion);
        grupoEstados.add(rbFinalizada);

        panelEstados.add(rbSinComenzar);
        panelEstados.add(rbDiagnostico);
        panelEstados.add(rbEnReparacion);
        panelEstados.add(rbFinalizada);

        // Obtener el estado actual para preseleccionar el botón correspondiente
        String estadoActual = obtenerEstadoOrden(idOrden);
        if (estadoActual != null) {
            if (estadoActual.equals("Sin comenzar")) rbSinComenzar.setSelected(true);
            else if (estadoActual.equals("En diagnóstico")) rbDiagnostico.setSelected(true);
            else if (estadoActual.equals("En reparación")) rbEnReparacion.setSelected(true);
            else if (estadoActual.equals("Finalizada")) rbFinalizada.setSelected(true);
        }

        // Botón de confirmar cambio
        JButton btnConfirmar = new JButton("Confirmar");
        btnConfirmar.addActionListener(e -> {
            String nuevoEstado = null;
            if (rbSinComenzar.isSelected()) nuevoEstado = "Sin comenzar";
            if (rbDiagnostico.isSelected()) nuevoEstado = "En diagnóstico";
            if (rbEnReparacion.isSelected()) nuevoEstado = "En reparación";
            if (rbFinalizada.isSelected()) nuevoEstado = "Finalizada";

            if (nuevoEstado != null) {
                modificarOrden(idOrden, "estadoreparacion", nuevoEstado);
            }
            dialog.dispose();
        });

        dialog.add(panelEstados, BorderLayout.CENTER);
        dialog.add(btnConfirmar, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
    
    private String obtenerEstadoOrden(int idOrden) {
        String estado = null;
        String sql = "SELECT estadoreparacion FROM ordenreparacion WHERE idorden = ?";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idOrden);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    estado = rs.getString("estadoreparacion");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return estado;
    }



    private void modificarOrden(int idOrden, String campo, String nuevoValor) {
        if (nuevoValor == null || nuevoValor.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El campo no puede estar vacío.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "UPDATE ordenreparacion SET " + campo + " = ? WHERE idorden = ?";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, nuevoValor);
            ps.setInt(2, idOrden);
            int filasAfectadas = ps.executeUpdate();
            if (filasAfectadas > 0) {
                JOptionPane.showMessageDialog(this, "Orden modificada correctamente.");
                cargarOrdenes();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
