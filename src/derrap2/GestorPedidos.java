package derrap2;
import javax.swing.*;

import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;


public class GestorPedidos extends JPanel {
    private JTable tablaPedidos;
    private DefaultTableModel modelo;
    private Conector conector;
    private GestorStock gestorStock; // Se agrega instancia para actualizar el stock

    public GestorPedidos(GestorStock gestorStock) {
        setLayout(new BorderLayout());
        this.gestorStock = gestorStock;
        conector = new Conector();

        // Panel superior con título
        JPanel panelSuperior = new JPanel();
        panelSuperior.add(new JLabel("Gestión de Pedidos"));
        add(panelSuperior, BorderLayout.NORTH);

        // Tabla de pedidos
        modelo = new DefaultTableModel();
        modelo.addColumn("ID Pedido");
        modelo.addColumn("Repuesto");
        modelo.addColumn("Proveedor");
        modelo.addColumn("Cantidad");
        modelo.addColumn("Estado");
        modelo.addColumn("Fecha Pedido");
        tablaPedidos = new JTable(modelo);
        JScrollPane scrollPane = new JScrollPane(tablaPedidos);
        add(scrollPane, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel();
        JButton btnAgregar = new JButton("Agregar Pedido");
        JButton btnActualizar = new JButton("Actualizar Estado");
        panelBotones.add(btnAgregar);
        panelBotones.add(btnActualizar);
        add(panelBotones, BorderLayout.SOUTH);

        // Listeners de los botones
        btnAgregar.addActionListener(e -> agregarPedido());
        btnActualizar.addActionListener(e -> actualizarEstadoPedido());

        cargarPedidos();
    }
    
    private void cargarPedidos() {
        modelo.setRowCount(0);
        String sql = "SELECT p.idpedido, s.nombre AS repuesto, prov.nombre_empresa AS proveedor, p.cantidad, " +
                     "p.estado, p.fecha_pedido FROM pedidos p " +
                     "JOIN stock s ON p.idrepuesto = s.idrepuesto " +
                     "JOIN proveedores prov ON p.idproveedor = prov.idproveedor";

        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                modelo.addRow(new Object[]{
                        rs.getInt("idpedido"),
                        rs.getString("repuesto"),
                        rs.getString("proveedor"),
                        rs.getInt("cantidad"),
                        rs.getString("estado"),
                        rs.getTimestamp("fecha_pedido")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private String[] obtenerListaProveedores() {
        return obtenerLista("SELECT nombre_empresa FROM proveedores");
    }

    private void agregarPedido() {
        String[] repuestos = obtenerListaRepuestos();
        String[] proveedores = obtenerListaProveedores();

        if (repuestos.length == 0 || proveedores.length == 0) {
            JOptionPane.showMessageDialog(this, "No hay repuestos o proveedores disponibles.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JComboBox<String> cmbRepuestos = new JComboBox<>(repuestos);
        JComboBox<String> cmbProveedores = new JComboBox<>(proveedores);
        JTextField txtCantidad = new JTextField();

        JPanel panel = new JPanel(new GridLayout(3, 2));
        panel.add(new JLabel("Repuesto:"));
        panel.add(cmbRepuestos);
        panel.add(new JLabel("Proveedor:"));
        panel.add(cmbProveedores);
        panel.add(new JLabel("Cantidad:"));
        panel.add(txtCantidad);

        int resultado = JOptionPane.showConfirmDialog(this, panel, "Nuevo Pedido", JOptionPane.OK_CANCEL_OPTION);
        if (resultado != JOptionPane.OK_OPTION) return;

        String cantidadTexto = txtCantidad.getText().trim();

        // Verificar si el campo está vacío o contiene caracteres no numéricos
        if (cantidadTexto.isEmpty() || !cantidadTexto.matches("^\\d+$")) {
            JOptionPane.showMessageDialog(this, "Ingrese un número entero válido en el campo de cantidad.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int cantidad = Integer.parseInt(cantidadTexto);

            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, "Ingrese una cantidad mayor a 0.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String repuestoSeleccionado = (String) cmbRepuestos.getSelectedItem();
            int idRepuesto = obtenerIdRepuesto(repuestoSeleccionado);

            String proveedorSeleccionado = (String) cmbProveedores.getSelectedItem();
            int idProveedor = obtenerIdProveedor(proveedorSeleccionado);

            String sql = "INSERT INTO pedidos (idrepuesto, idproveedor, cantidad, estado) VALUES (?, ?, ?, 'Pendiente')";
            try (Connection conexion = conector.getConexion();
                 PreparedStatement ps = conexion.prepareStatement(sql)) {

                ps.setInt(1, idRepuesto);
                ps.setInt(2, idProveedor);
                ps.setInt(3, cantidad);
                ps.executeUpdate();
                cargarPedidos();
                JOptionPane.showMessageDialog(this, "Pedido agregado correctamente.");
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Ingrese un número válido en el campo de cantidad.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    
    private void actualizarEstadoPedido() {
        int filaSeleccionada = tablaPedidos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un pedido para actualizar.");
            return;
        }

        int idPedido = (int) modelo.getValueAt(filaSeleccionada, 0);
        String nuevoEstado = "Recibido";

        String sql = "UPDATE pedidos SET estado = ? WHERE idpedido = ?";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idPedido);
            ps.executeUpdate();
            cargarPedidos();
            actualizarStock(idPedido);
            JOptionPane.showMessageDialog(this, "Estado actualizado correctamente.");

            // Actualizar stock en UI
            gestorStock.cargarStock();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private String[] obtenerListaRepuestos() {
        return obtenerLista("SELECT nombre FROM stock");
    }
    
    private String obtenerNombrePieza(int idRepuesto) {
        String sql = "SELECT nombre FROM stock WHERE idrepuesto = ?";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idRepuesto);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("nombre");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Desconocido";
    }
    
    

    private String[] obtenerLista(String sql) {
        List<String> lista = new ArrayList<>();
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                lista.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista.toArray(new String[0]);
    }



    private void actualizarStock(int idPedido) {
        String sqlPedido = "SELECT idrepuesto, cantidad FROM pedidos WHERE idpedido = ?";
        String sqlStock = "UPDATE stock SET cantidad = cantidad + ? WHERE idrepuesto = ?";

        try (Connection conexion = conector.getConexion();
             PreparedStatement psPedido = conexion.prepareStatement(sqlPedido);
             PreparedStatement psStock = conexion.prepareStatement(sqlStock)) {

            psPedido.setInt(1, idPedido);
            ResultSet rs = psPedido.executeQuery();

            if (rs.next()) {
                int idRepuesto = rs.getInt("idrepuesto");
                int cantidad = rs.getInt("cantidad");

                psStock.setInt(1, cantidad);
                psStock.setInt(2, idRepuesto);
                psStock.executeUpdate();

                JOptionPane.showMessageDialog(this, "Stock actualizado correctamente.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int obtenerIdRepuesto(String seleccion) {
        return Integer.parseInt(seleccion.split(" - ")[0]);
    }

    private int obtenerIdProveedor(String seleccion) {
        return Integer.parseInt(seleccion.split(" - ")[0]);
    }
    
    
}


