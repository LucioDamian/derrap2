package derrapchatgpt;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FormularioVehiculo extends JDialog {
    private JTextField txtMatricula, txtMarca, txtModelo, txtAño, txtClienteID;
    private JButton btnGuardar, btnCancelar;
    private GestorVehiculos gestor;
    private String matriculaExistente;
    private Conector conector;

    public FormularioVehiculo(GestorVehiculos gestor, String matricula) {
        this.gestor = gestor;
        this.matriculaExistente = matricula;
        this.conector = new Conector();

        setTitle(matricula == null ? "Agregar Vehículo" : "Modificar Vehículo");
        setSize(400, 300);
        setLocationRelativeTo(gestor);
        setModal(true);
        setLayout(new GridLayout(6, 2, 10, 10));

        // Crear etiquetas y campos de texto
        add(new JLabel("Matrícula:"));
        txtMatricula = new JTextField();
        add(txtMatricula);

        add(new JLabel("Marca:"));
        txtMarca = new JTextField();
        add(txtMarca);

        add(new JLabel("Modelo:"));
        txtModelo = new JTextField();
        add(txtModelo);

        add(new JLabel("Año:"));
        txtAño = new JTextField();
        add(txtAño);

        add(new JLabel("ID Cliente:"));
        txtClienteID = new JTextField();
        add(txtClienteID);

        // Botones
        btnGuardar = new JButton("Guardar");
        btnCancelar = new JButton("Cancelar");

        add(btnGuardar);
        add(btnCancelar);

        // Eventos de botones
        btnGuardar.addActionListener(e -> guardarVehiculo());
        btnCancelar.addActionListener(e -> dispose());

        // Si estamos modificando, cargamos los datos actuales
        if (matricula != null) {
            cargarDatosVehiculo(matricula);
            txtMatricula.setEnabled(false);
        }

        setVisible(true);
    }

    private void cargarDatosVehiculo(String matricula) {
        String sql = "SELECT * FROM vehiculos WHERE matricula = ?";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, matricula);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                txtMatricula.setText(rs.getString("matricula"));
                txtMarca.setText(rs.getString("marca"));
                txtModelo.setText(rs.getString("modelo"));
                txtAño.setText(String.valueOf(rs.getInt("año")));
                txtClienteID.setText(String.valueOf(rs.getInt("cliente_id")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void guardarVehiculo() {
        String matricula = txtMatricula.getText().trim();
        String marca = txtMarca.getText().trim();
        String modelo = txtModelo.getText().trim();
        String añoTexto = txtAño.getText().trim();
        String clienteIDTexto = txtClienteID.getText().trim();

        // Validaciones
        if (matricula.isEmpty() || marca.isEmpty() || modelo.isEmpty() || añoTexto.isEmpty() || clienteIDTexto.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int año, clienteId;
        try {
            año = Integer.parseInt(añoTexto);
            clienteId = Integer.parseInt(clienteIDTexto);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "El año y el ID del cliente deben ser números válidos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Verificar si el cliente existe antes de continuar
        if (!clienteExiste(clienteId)) {
            JOptionPane.showMessageDialog(this, "El ID del cliente no existe en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql;
        if (matriculaExistente == null) {
            sql = "INSERT INTO vehiculos (matricula, marca, modelo, año, cliente_id) VALUES (?, ?, ?, ?, ?)";
        } else {
            sql = "UPDATE vehiculos SET marca = ?, modelo = ?, año = ?, cliente_id = ? WHERE matricula = ?";
        }

        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            if (matriculaExistente == null) {
                ps.setString(1, matricula);
                ps.setString(2, marca);
                ps.setString(3, modelo);
                ps.setInt(4, año);
                ps.setInt(5, clienteId);
            } else {
                ps.setString(1, marca);
                ps.setString(2, modelo);
                ps.setInt(3, año);
                ps.setInt(4, clienteId);
                ps.setString(5, matriculaExistente);
            }
            ps.executeUpdate();
            gestor.cargarVehiculos();
            dispose();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    private boolean clienteExiste(int clienteId) {
        String sql = "SELECT idcliente FROM clientes WHERE idcliente = ?";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, clienteId);
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

}
