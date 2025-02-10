package derrap2;

import java.sql.*;

public class Conector {
    private static final String URL = "jdbc:mysql://localhost:3306/derrapdb2?useSSL=false";
    private static final String USUARIO = "root";
    private static final String CLAVE = "root";

    // Método para obtener una nueva conexión
    public Connection getConexion() {
        try {
            return DriverManager.getConnection(URL, USUARIO, CLAVE);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Método para ejecutar consultas SELECT y devolver un ResultSet
    public ResultSet ejecutarConsulta(String consulta, Object... parametros) {
        try {
            Connection conexion = getConexion();
            PreparedStatement stmt = conexion.prepareStatement(consulta);
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            return stmt.executeQuery();  // Se debe cerrar en la clase que lo usa
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // Método para ejecutar INSERT, UPDATE, DELETE
    public int ejecutarActualizacion(String consulta, Object... parametros) {
        try (Connection conexion = getConexion();
             PreparedStatement stmt = conexion.prepareStatement(consulta)) {
            for (int i = 0; i < parametros.length; i++) {
                stmt.setObject(i + 1, parametros[i]);
            }
            return stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }

    // Método para cerrar una conexión específica
    public void cerrarConexion(Connection cn) {
        try {
            if (cn != null && !cn.isClosed()) {
                cn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}




