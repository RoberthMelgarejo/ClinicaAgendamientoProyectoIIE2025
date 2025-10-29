/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package clinica_agendamiento;

import java.sql.*;
import java.util.ArrayList;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
/**
 *
 * @author Guillermo Villalba
 */
public class BaseDatos {
    private String host = "localhost";
    private String baseDatos="CLINICA_AGENDAMIENTO_VER4";
    private String usuBD = "root";
    private String clave = "9029";
    private Connection conexion = null;
    private static Statement sentencia;
    
    public BaseDatos(){
        this.hayConexion();
    }
    public boolean hayConexion(){

        if (conexion != null)
            return true;

        try {
            // Se registra el Driver de MySQL
//            DriverManager.registerDriver(new com.mysql.jdbc.Driver());
////            conexion = DriverManager.getConnection("jdbc:mysql://"+this.host+"/"+this.baseDatos,this.usuBD,this.clave);
            conexion = DriverManager.getConnection("jdbc:mysql://"+this.host+"/"+this.baseDatos+"?characterEncoding=utf8",this.usuBD,this.clave);
            



        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error de conexión"+e.getMessage());
            return false;
        }
        return true;
    }
    public boolean borrarRegistro(String tabla, String condicion){
        try {
            // Se crea un Statement, para realizar la consulta
            Statement s = conexion.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            // Se realiza la consulta.
            JOptionPane optionPane=new JOptionPane();
            Object[] opciones={"Si","No"};

            int ret=optionPane.showOptionDialog(null,"Esta seguro de ELIMINAR el REGISTRO? ","Pregunta",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE,null,opciones,opciones[0]);
            //Si la opcion escogida es Si
            if(ret==JOptionPane.YES_OPTION)
                s.executeUpdate("delete from "+tabla+" where "+condicion);
        } catch (SQLException e) {
            if (e.getErrorCode()==1451){
                JOptionPane.showMessageDialog(null, "El registro esta relacionado con otros registros\nno podrá borrarlo." ,
                        "Atencion", JOptionPane.INFORMATION_MESSAGE);
            }else{
                JOptionPane.showMessageDialog(null, "Ocurrio Un error" + e.getMessage(), "Atencion",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            return false;
        }
        return true;
    }

    public boolean borrarRegistroSinPreguntar(String tabla, String condicion){
        try {
            // Se crea un Statement, para realizar la consulta
            Statement s = conexion.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            s.executeUpdate("delete from "+tabla+" where "+condicion);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, "No se ha eliminado el registro seleccionado\nPuede estar usándose en otra tabla", "Atencion",
                        JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean insertarRegistro(String tabla, String campos, String valores){
        try {
            // Se crea un Statement, para realizar la consulta
            Statement s = conexion.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            // Se realiza la consulta.
            s.executeUpdate("insert into "+tabla+" ("+campos+") values ("+valores+")");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrio Un error al insertar \n"+ e.getMessage() , "Atencion",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return true;
    }

    public boolean insertarRegistro(String tabla,  String valores){
        try {
            // Se crea un Statement, para realizar la consulta
            Statement s = conexion.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            // Se realiza la consulta.
            s.executeUpdate("insert into "+tabla+" values ("+valores+")");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrio Un error al insertar \n"+ e.getMessage() , "Atencion",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return true;
    }
    public boolean actualizarRegistro(String tabla, String campos, String criterio){
        try {
            // Se crea un Statement, para realizar la consulta
            Statement s = conexion.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);
            // Se realiza la consulta.
            s.executeUpdate("update "+tabla+" set "+campos+" where " +criterio);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrio Un error\n"+e.getMessage() , "Atencion",
                    JOptionPane.INFORMATION_MESSAGE);
            return false;
        }
        return true;
    }
    public ResultSet consultarRegistros(String sql) {
        ResultSet rs = null;
        try {
            // Se crea un Statement, para realizar la consulta
            Statement s = conexion.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_UPDATABLE);

            // Se realiza la consulta. Los resultados se guardan en el ResultSet rs
            rs = s.executeQuery(sql);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Ocurrio Un error"+ e.getMessage() , "Atencion",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        return rs;
    }
    public void cargarCombo(JComboBox combo, String campos, String tabla){
         ResultSet rsC;
         try{
             //Crear la sentencia para la consulta
             sentencia = (Statement)conexion.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             //Se ejecuta la consulta
             rsC = sentencia.executeQuery("select " + campos + " from " + tabla);
           //Se inicializa el arraylist
             ArrayList<DatosCombo> camposCombo;
             camposCombo = new ArrayList();
             //Recorrer los registros
             while (rsC.next()) {
                 //Agregar al arraylist datos del rsC
                 camposCombo.add(new DatosCombo(rsC.getInt(1), rsC.getString(2)));
             }
             for (DatosCombo nombre: camposCombo){
                 //Agregar items al combo
                 combo.addItem(nombre);
             }
         }catch(Exception e) {
             //Si ocurrio un error mostrar mensaje
             JOptionPane.showMessageDialog(null, "Error al llenar combo\n" + e.getMessage()  , "Llenar Combo - "  + combo.getName(), JOptionPane.ERROR_MESSAGE);
         }

     }
        /** Cierra la conexion con la base de datos */
    public void cierraConexion() {
        try {
            conexion.close();
        } catch (Exception e) {

        }
    }
    public Connection miConexion(){
        return this.conexion;
    }
}
