package derrap2;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FormularioCliente extends JFrame {
    private JTextField txtDNI, txtNombre, txtApellido, txtTelefono, txtEmail;
    private JButton btnGuardar;
    private Conector conector;
    private GestorClientes gestorClientes;
    private boolean esModificacion;
    private int idCliente;

    public FormularioCliente(GestorClientes gestor, boolean modificar, int idCliente, String dni, String nombre, String apellido, String telefono, String email) {
        this.gestorClientes = gestor;
        this.conector = new Conector();
        this.esModificacion = modificar;
        this.idCliente = idCliente;

        setTitle(modificar ? "Modificar Cliente" : "Agregar Cliente");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(6, 2));

        add(new JLabel("DNI:"));
        txtDNI = new JTextField(dni);
        add(txtDNI);

        add(new JLabel("Nombre:"));
        txtNombre = new JTextField(nombre);
        add(txtNombre);

        add(new JLabel("Apellido:"));
        txtApellido = new JTextField(apellido);
        add(txtApellido);

        add(new JLabel("Teléfono:"));
        txtTelefono = new JTextField(telefono);
        add(txtTelefono);

        add(new JLabel("Email:"));
        txtEmail = new JTextField(email);
        add(txtEmail);

        btnGuardar = new JButton(modificar ? "Actualizar" : "Guardar");
        btnGuardar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (esModificacion) {
                    modificarCliente();
                } else {
                    guardarCliente();
                }
            }
        });

        add(btnGuardar);
        add(new JLabel()); // Espacio vacío

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void guardarCliente() {
        String dni = txtDNI.getText();
        String nombre = txtNombre.getText();
        String apellido = txtApellido.getText();
        String telefono = txtTelefono.getText();
        String email = txtEmail.getText();

        if (dni.isEmpty() || nombre.isEmpty() || apellido.isEmpty()) {
            JOptionPane.showMessageDialog(this, "DNI, Nombre y Apellido son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = "INSERT INTO clientes (dni, nombre, apellido, telefono, email) VALUES (?, ?, ?, ?, ?)";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, dni);
            ps.setString(2, nombre);
            ps.setString(3, apellido);
            ps.setString(4, telefono);
            ps.setString(5, email);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cliente agregado correctamente.");
            gestorClientes.cargarClientes();
            dispose();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void modificarCliente() {
        String dni = txtDNI.getText();
        String nombre = txtNombre.getText();
        String apellido = txtApellido.getText();
        String telefono = txtTelefono.getText();
        String email = txtEmail.getText();

        if (dni.isEmpty() || nombre.isEmpty() || apellido.isEmpty() || telefono.isEmpty() || email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }


        String sql = "UPDATE clientes SET dni = ?, nombre = ?, apellido = ?, telefono = ?, email = ? WHERE idcliente = ?";
        try (Connection conexion = conector.getConexion();
             PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, dni);
            ps.setString(2, nombre);
            ps.setString(3, apellido);
            ps.setString(4, telefono);
            ps.setString(5, email);
            ps.setInt(6, idCliente);
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cliente modificado correctamente.");
            gestorClientes.cargarClientes();
            dispose();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
