package derrap2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Mecanico extends JFrame {
    private JTable tableOrdenes;
    private DefaultTableModel modeloOrdenes;
    private JTable tableStock;
    private DefaultTableModel modeloStock;
    private Conector con;
    private String dniMecanico;

    public Mecanico(String dniMecanico) {
        this.dniMecanico = dniMecanico;
        setTitle("Panel de Mecánico");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        con = new Conector();

        JTabbedPane tabbedPane = new JTabbedPane();

        // Panel de órdenes de reparación
        JPanel panelOrdenes = new JPanel(new BorderLayout());
        modeloOrdenes = new DefaultTableModel(new String[]{"ID Orden", "Vehículo", "Estado", "Fecha Ingreso", "Mecánico Asignado"}, 0);
        tableOrdenes = new JTable(modeloOrdenes);
        JScrollPane scrollOrdenes = new JScrollPane(tableOrdenes);
        panelOrdenes.add(scrollOrdenes, BorderLayout.CENTER);

        JButton btnAsignarOrden = new JButton("Asignar Orden");
        JButton btnModificarOrden = new JButton("Modificar Orden");
        JButton btnTerminarOrden = new JButton("Terminar Orden");
        JButton btnCancelarOrden = new JButton("Cancelar Orden");
        JPanel panelBotonesOrdenes = new JPanel();
        panelBotonesOrdenes.add(btnAsignarOrden);
        panelBotonesOrdenes.add(btnModificarOrden);
        panelBotonesOrdenes.add(btnTerminarOrden);
        panelBotonesOrdenes.add(btnCancelarOrden);
        panelOrdenes.add(panelBotonesOrdenes, BorderLayout.SOUTH);

        tabbedPane.add("Órdenes de Reparación", panelOrdenes);

        // Panel de stock
        JPanel panelStock = new JPanel(new BorderLayout());
        modeloStock = new DefaultTableModel(new String[]{"ID Repuesto", "Nombre", "Cantidad"}, 0);
        tableStock = new JTable(modeloStock);
        JScrollPane scrollStock = new JScrollPane(tableStock);
        panelStock.add(scrollStock, BorderLayout.CENTER);

        tabbedPane.add("Stock Disponible", panelStock);

        add(tabbedPane);

        // Listeners de botones
        btnAsignarOrden.addActionListener(e -> asignarOrden());
        btnModificarOrden.addActionListener(e -> modificarOrden());
        btnTerminarOrden.addActionListener(e -> terminarOrden());
        btnCancelarOrden.addActionListener(e -> cancelarOrden());

        cargarOrdenes();
        cargarStock();
    }

    private void cargarOrdenes() {
        modeloOrdenes.setRowCount(0);  // Limpia la tabla antes de cargar

        String sql = "SELECT idorden, vehiculo_matricula, estadoreparacion, fecha_ingreso, mecanico_asignado " +
                     "FROM ordenreparacion " +
                     "WHERE estadoreparacion != 'Finalizada' " +
                     "AND (mecanico_asignado = ? OR mecanico_asignado IS NULL)";

        System.out.println("DNI Mecánico: " + dniMecanico);  // Debug para verificar el DNI

        try (Connection conexion = con.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, dniMecanico);
            ResultSet rs = ps.executeQuery();

            boolean hayResultados = false;
            while (rs.next()) {
                modeloOrdenes.addRow(new Object[]{
                    rs.getInt("idorden"),
                    rs.getString("vehiculo_matricula"),
                    rs.getString("estadoreparacion"),
                    rs.getDate("fecha_ingreso"),
                    rs.getString("mecanico_asignado")
                });
                hayResultados = true;
            }

            if (!hayResultados) {
                System.out.println("No hay órdenes pendientes para este mecánico.");
            }

            modeloOrdenes.fireTableDataChanged(); // Actualiza la tabla
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void cargarStock() {
        modeloStock.setRowCount(0);
        String sql = "SELECT idrepuesto, nombre, cantidad FROM stock";
        try (Connection conexion = con.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                modeloStock.addRow(new Object[]{rs.getInt("idrepuesto"), rs.getString("nombre"), rs.getInt("cantidad")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void asignarOrden() {
        int fila = tableOrdenes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione una orden para asignar.");
            return;
        }
        int idOrden = (int) modeloOrdenes.getValueAt(fila, 0);
        String sql = "UPDATE ordenreparacion SET mecanico_asignado = ? WHERE idorden = ?";
        try (Connection conexion = con.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, dniMecanico);
            ps.setInt(2, idOrden);
            ps.executeUpdate();
            cargarOrdenes();
            JOptionPane.showMessageDialog(this, "Orden asignada correctamente.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void modificarOrden() {
        JOptionPane.showMessageDialog(this, "Funcionalidad en desarrollo.");
    }

    private void terminarOrden() {
        JOptionPane.showMessageDialog(this, "Funcionalidad en desarrollo.");
    }

    private void cancelarOrden() {
        JOptionPane.showMessageDialog(this, "Funcionalidad en desarrollo.");
    }
}
