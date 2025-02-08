package derrapchatgpt;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Administrador extends JFrame {
	private CardLayout cardLayout;
    private JPanel panelContenido;
	
    public Administrador() {
    	 setTitle("Panel de Administrador");
         setSize(900, 600);
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setLocationRelativeTo(null);

         // Crear barra de herramientas superior
         JToolBar toolBar = new JToolBar();
         toolBar.setFloatable(false); // Evita que se mueva la barra

         // Botones de navegación
         JButton btnClientes = new JButton("Clientes");
         JButton btnVehiculos = new JButton("Vehículos");
         JButton btnOrdenes = new JButton("Órdenes");
         JButton btnFacturacion = new JButton("Facturación");
         JButton btnPedidos = new JButton("Pedidos");
         JButton btnRepuestos = new JButton("Repuestos");
         JButton btnCerrarSesion = new JButton("Cerrar Sesión");

         // Agregar botones a la barra de herramientas
         toolBar.add(btnClientes);
         toolBar.add(btnVehiculos);
         toolBar.add(btnOrdenes);
         toolBar.add(btnFacturacion);
         toolBar.add(btnPedidos);
         toolBar.add(btnRepuestos);
         toolBar.add(Box.createHorizontalGlue()); // Empuja los botones a la izquierda
         toolBar.add(btnCerrarSesion);

         add(toolBar, BorderLayout.NORTH); // Agregar la barra en la parte superior

         // Panel de contenido con CardLayout
         panelContenido = new JPanel();
         cardLayout = new CardLayout();
         panelContenido.setLayout(cardLayout);

         // Agregar paneles de cada sección
         panelContenido.add(new GestorClientes(), "Clientes");
         panelContenido.add(new GestorVehiculos(), "Vehículos");
         panelContenido.add(new GestorOrdenes(), "Órdenes");
         panelContenido.add(new GestorFacturas(), "Facturación");
         panelContenido.add(new GestorPedidos(), "Pedidos");
         panelContenido.add(new GestorRepuestos(), "Repuestos");

         add(panelContenido, BorderLayout.CENTER);

         // Eventos de los botones
         btnClientes.addActionListener(e -> cardLayout.show(panelContenido, "Clientes"));
         btnVehiculos.addActionListener(e -> cardLayout.show(panelContenido, "Vehículos"));
         btnOrdenes.addActionListener(e -> cardLayout.show(panelContenido, "Órdenes"));
         btnFacturacion.addActionListener(e -> cardLayout.show(panelContenido, "Facturación"));
         btnPedidos.addActionListener(e -> cardLayout.show(panelContenido, "Pedidos"));
         btnRepuestos.addActionListener(e -> cardLayout.show(panelContenido, "Repuestos"));

         btnCerrarSesion.addActionListener(e -> {
             int opcion = JOptionPane.showConfirmDialog(this, "¿Desea cerrar sesión?", "Cerrar Sesión", JOptionPane.YES_NO_OPTION);
             if (opcion == JOptionPane.YES_OPTION) {
                 dispose(); // Cierra la ventana
                 new Login().setVisible(true); // Vuelve a la pantalla de inicio de sesión
             }
         });
    }
}
