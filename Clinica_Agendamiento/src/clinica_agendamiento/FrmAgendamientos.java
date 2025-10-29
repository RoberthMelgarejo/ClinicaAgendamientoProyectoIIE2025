/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JDialog.java to edit this template
 */
package clinica_agendamiento;

import java.text.SimpleDateFormat; //IMPORTACION DEL FORMATO SIMPLE
import java.util.Calendar; //IMPORTACION DEL CALENDARIO
import java.util.Date; //IMPORTACION DE LA FECHA (DATE)
import java.util.Locale; //IMPORTACION DE LOCALE
import javax.swing.JOptionPane; 
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author rober
 */
public class FrmAgendamientos extends javax.swing.JDialog {
    private BaseDatos bd = new BaseDatos();
    private char opc='z';
    private Grilla grd = new Grilla();

    /**
     * Creates new form FrmAgendamientos
     */
    public FrmAgendamientos(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
        this.optPendiente.setEnabled(false);
        this.optCancelado.setEnabled(false);
        if (!bd.hayConexion()) {
            JOptionPane.showMessageDialog(null, "Error al conectar con la Base de Datos.");
        }

        this.habilitarCampos(false);
        this.habilitarBotones(true);
        this.setLocationRelativeTo(null);

        cargarDoctores();
        actualizarGrillaHoy();
        actualizarGrillaAgendamientos();
        
    }
    // ======== CARGA DE DOCTORES EN EL COMBO ========
    private void cargarDoctores() {
        cboDoctores.removeAllItems();
        bd.cargarCombo(cboDoctores, "Ci_Doctor, CONCAT(Nombre,' ',Apellido) as Doctor", "doctores");
    }

    // ======== ACTUALIZACIÓN DE GRILLA SEGÚN EL DÍA ========
    private void actualizarGrilla(String dia) {
        try {
            String sql = "SELECT DISTINCT d.Ci_Doctor, CONCAT(d.Nombre,' ',d.Apellido) AS Doctor, " +
             "t.Dia, t.Hora_Inicio, t.Hora_Fin, d.Estado AS Estado " +
             "FROM doctores d " +
             "INNER JOIN turnos t ON d.Ci_Doctor = t.Ci_Doctor " +
             "WHERE t.Dia = '" + dia + "'";

            ResultSet rs = bd.consultarRegistros(sql);

            DefaultTableModel modelo = new DefaultTableModel();
            modelo.addColumn("CI Doctor");
            modelo.addColumn("Doctor");
            modelo.addColumn("Día");
            modelo.addColumn("Hora Inicio");
            modelo.addColumn("Hora Fin");
            modelo.addColumn("Estado");

            // Usamos Set<String> y normalizamos el id a String (seguro ante INT o VARCHAR)
            java.util.Set<String> doctoresAgregados = new java.util.HashSet<>();

            while (rs.next()) {
                // Obtener el valor sin asumir tipo: puede venir como INT o VARCHAR
                Object objCi = null;
                try {
                    objCi = rs.getObject("Ci_Doctor");
                } catch (Exception ex) {
                    // En caso de que el alias tenga otro nombre, intentamos con índice 1
                    try { objCi = rs.getObject(1); } catch (Exception ex2) { objCi = null; }
                }

                if (objCi == null) {
                    // Si por alguna razón no pudimos leer el CI, saltamos esa fila
                    continue;
                }

                String ciDoctor = objCi.toString().trim();
                if (ciDoctor.isEmpty()) {
                    continue;
                }

                // Evita duplicados: si ya está, seguimos
                if (doctoresAgregados.contains(ciDoctor)) {
                    continue;
                }

                doctoresAgregados.add(ciDoctor);

                // Leemos el resto de columnas de forma segura
                String nombreDoctor = "";
                try { nombreDoctor = rs.getString("Doctor"); } catch (Exception ex) { nombreDoctor = ""; }
                String diaRow = "";
                try { diaRow = rs.getString("Dia"); } catch (Exception ex) { diaRow = ""; }
                String horaInicio = "";
                try { horaInicio = rs.getString("Hora_Inicio"); } catch (Exception ex) { horaInicio = ""; }
                String horaFin = "";
                try { horaFin = rs.getString("Hora_Fin"); } catch (Exception ex) { horaFin = ""; }
                String estado = "";
                try { estado = rs.getString("Estado"); } catch (Exception ex) { estado = ""; }

                modelo.addRow(new Object[]{
                    ciDoctor,
                    nombreDoctor,
                    diaRow,
                    horaInicio,
                    horaFin,
                    estado
                });
            }

            grdDatos.setModel(modelo);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar la grilla: " + e.getMessage());
        }
    }

    private void actualizarGrillaHoy() {
        SimpleDateFormat formatoDia = new SimpleDateFormat("EEEE", new Locale("es", "ES"));
        String dia = formatoDia.format(new Date());
        actualizarGrilla(dia);
    }
    // ======= ACTUALIZAR GRILLA DE AGENDAMIENTOS =======
    private void actualizarGrillaAgendamientos() {
        try {
            String sql = "SELECT a.Id_Agendamiento, a.Fecha_Cita, a.Estado_Cita, " +
                         "a.Ci_Paciente, CONCAT(p.Nombre, ' ', p.Apellido) AS Paciente, " +
                         "a.Ci_Doctor, CONCAT(d.Nombre, ' ', d.Apellido) AS Doctor, " +
                         "CONCAT(t.Hora_Inicio, ' - ', t.Hora_Fin) AS Turno " +
                         "FROM agendamientos a " +
                         "INNER JOIN pacientes p ON a.Ci_Paciente = p.Ci_Paciente " +
                         "INNER JOIN doctores d ON a.Ci_Doctor = d.Ci_Doctor " +
                         "LEFT JOIN turnos t ON d.Ci_Doctor = t.Ci_Doctor";

            java.sql.ResultSet rs = bd.consultarRegistros(sql);
            DefaultTableModel modelo = new DefaultTableModel();
            modelo.addColumn("ID AGENDAMIENTO");
            modelo.addColumn("FECHA");
            modelo.addColumn("ESTADO");
            modelo.addColumn("CI PACIENTE");
            modelo.addColumn("NOMBRE PACIENTE");
            modelo.addColumn("CI DOCTOR");
            modelo.addColumn("NOMBRE DOCTOR");
            modelo.addColumn("TURNO");

            while (rs.next()) {
                modelo.addRow(new Object[]{
                    rs.getString("Id_Agendamiento"),
                    rs.getString("Fecha_Cita"),
                    rs.getString("Estado_Cita"),
                    rs.getString("Ci_Paciente"),
                    rs.getString("Paciente"),
                    rs.getString("Ci_Doctor"),
                    rs.getString("Doctor"),
                    rs.getString("Turno")
                });
            }
            grdAgendamiento.setModel(modelo);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error al cargar agendamientos: " + e.getMessage());
        }
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        grdAgendamiento = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        grdDatos = new javax.swing.JTable();
        txtBuscar = new javax.swing.JFormattedTextField();
        jLabel4 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        btnGuardar = new javax.swing.JButton();
        btnBusqueda = new javax.swing.JButton();
        btnCancelarAgendamiento = new javax.swing.JButton();
        btnAgregar = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        cboDoctores = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        txtCIPaciente = new javax.swing.JFormattedTextField();
        jLabel2 = new javax.swing.JLabel();
        optCancelado = new javax.swing.JRadioButton();
        optPendiente = new javax.swing.JRadioButton();
        jLabel1 = new javax.swing.JLabel();
        txtIDAgendamiento = new javax.swing.JFormattedTextField();
        lblAgendamiento = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        cboFecha = new org.freixas.jcalendar.JCalendarCombo();
        jPanel4 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(0, 51, 102));

        grdAgendamiento.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID AGENDAMIENTO", "FECHA", "ESTADO", "CI PACIENTE", "NOMBRE PACIENTE", "CI DOCTOR", "NOMBRE DOCTOR", "TURNO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(grdAgendamiento);

        grdDatos.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null}
            },
            new String [] {
                "Doctor", "Estado", "Turno"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(grdDatos);

        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBuscarKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtBuscarKeyTyped(evt);
            }
        });

        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("BUSCAR:");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel4)
                        .addGap(18, 18, 18)
                        .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 827, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jScrollPane1)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 907, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(252, 252, 252))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 192, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(30, 30, 30)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 121, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(0, 51, 102));

        btnGuardar.setBackground(new java.awt.Color(0, 51, 102));
        btnGuardar.setForeground(new java.awt.Color(0, 51, 102));
        btnGuardar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/disquete.png"))); // NOI18N
        btnGuardar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnGuardarActionPerformed(evt);
            }
        });

        btnBusqueda.setBackground(new java.awt.Color(0, 51, 102));
        btnBusqueda.setForeground(new java.awt.Color(0, 51, 102));
        btnBusqueda.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Iconos/BUSQUEDA-PACIENTE.png"))); // NOI18N
        btnBusqueda.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnBusquedaActionPerformed(evt);
            }
        });

        btnCancelarAgendamiento.setBackground(new java.awt.Color(0, 51, 102));
        btnCancelarAgendamiento.setForeground(new java.awt.Color(0, 51, 102));
        btnCancelarAgendamiento.setIcon(new javax.swing.ImageIcon(getClass().getResource("/Iconos/cancelado.png"))); // NOI18N
        btnCancelarAgendamiento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCancelarAgendamientoActionPerformed(evt);
            }
        });

        btnAgregar.setBackground(new java.awt.Color(0, 51, 102));
        btnAgregar.setForeground(new java.awt.Color(0, 51, 102));
        btnAgregar.setIcon(new javax.swing.ImageIcon(getClass().getResource("/iconos/agregar.png"))); // NOI18N
        btnAgregar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAgregarActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(73, 73, 73)
                .addComponent(btnAgregar)
                .addGap(250, 250, 250)
                .addComponent(btnCancelarAgendamiento)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnBusqueda)
                .addGap(251, 251, 251)
                .addComponent(btnGuardar)
                .addGap(137, 137, 137))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnCancelarAgendamiento)
                    .addComponent(btnGuardar)
                    .addComponent(btnBusqueda)
                    .addComponent(btnAgregar))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        jPanel3.setBackground(new java.awt.Color(0, 51, 102));

        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("DOCTORES");

        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("CI PACIENTE");

        buttonGroup1.add(optCancelado);
        optCancelado.setForeground(new java.awt.Color(255, 255, 255));
        optCancelado.setText("Cancelado");
        optCancelado.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optCanceladoActionPerformed(evt);
            }
        });

        buttonGroup1.add(optPendiente);
        optPendiente.setForeground(new java.awt.Color(255, 255, 255));
        optPendiente.setText("Pendiente");
        optPendiente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optPendienteActionPerformed(evt);
            }
        });

        jLabel1.setForeground(new java.awt.Color(255, 255, 255));
        jLabel1.setText("ESTADO DE LA CITA");

        txtIDAgendamiento.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtIDAgendamientoActionPerformed(evt);
            }
        });

        lblAgendamiento.setForeground(new java.awt.Color(255, 255, 255));
        lblAgendamiento.setText("ID AGENDAMIENTO");

        jLabel6.setFont(new java.awt.Font("Felix Titling", 0, 12)); // NOI18N
        jLabel6.setText("ESPECIALIDADES");

        cboFecha.addDateListener(new org.freixas.jcalendar.DateListener() {
            public void dateChanged(org.freixas.jcalendar.DateEvent evt) {
                cboFechaDateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(txtIDAgendamiento, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                            .addComponent(optPendiente)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(optCancelado)
                            .addGap(10, 10, 10))
                        .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 133, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(cboDoctores, javax.swing.GroupLayout.PREFERRED_SIZE, 222, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(lblAgendamiento, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtCIPaciente, javax.swing.GroupLayout.PREFERRED_SIZE, 216, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(cboFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(24, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(lblAgendamiento)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(txtIDAgendamiento, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cboFecha, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(11, 11, 11)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(optPendiente)
                    .addComponent(optCancelado))
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addGap(18, 18, 18)
                .addComponent(txtCIPaciente, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(cboDoctores, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel6)
                .addGap(56, 56, 56))
        );

        jPanel4.setBackground(new java.awt.Color(0, 51, 102));

        jLabel5.setFont(new java.awt.Font("Felix Titling", 0, 36)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("PANEL DE AGENDAMIENTO");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(331, 331, 331)
                .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 486, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(29, 29, 29)
                .addComponent(jLabel5)
                .addContainerGap(40, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, 923, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(11, 11, 11))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cboFechaDateChanged(org.freixas.jcalendar.DateEvent evt) {//GEN-FIRST:event_cboFechaDateChanged
        Date fecha = cboFecha.getDate();
        if (fechaAtrasada(fecha)) {
            JOptionPane.showMessageDialog(null, "Fecha inválida. No puede ser pasada.");
            return;
        }

        SimpleDateFormat formatoDia = new SimpleDateFormat("EEEE", new Locale("es", "ES"));
        String dia = formatoDia.format(fecha);
        actualizarGrilla(dia);
    }//GEN-LAST:event_cboFechaDateChanged

    private void optPendienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optPendienteActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_optPendienteActionPerformed

    private void txtIDAgendamientoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtIDAgendamientoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtIDAgendamientoActionPerformed

    private void txtBuscarKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarKeyTyped
        
    }//GEN-LAST:event_txtBuscarKeyTyped

    private void txtBuscarKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_txtBuscarKeyReleased
       //SE BUSCA POR EL NOMBRE DEL PACIENTE
         grd.filtrarGrilla(grdAgendamiento, txtBuscar.getText(), 4);
    }//GEN-LAST:event_txtBuscarKeyReleased

    
    
    
     //ACCION PARA GUARDAR LOS DATOS
    private void btnGuardarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGuardarActionPerformed
        this.optPendiente.setEnabled(false);
        this.optCancelado.setEnabled(false);
        this.optPendiente.setSelected(true);
        String estado = "Pendiente";
        String ciPaciente = txtCIPaciente.getText().trim();

        if (ciPaciente.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Cargue el CI del paciente.");
            return;
        }

        Date fechaDate = cboFecha.getDate();
        if (fechaAtrasada(fechaDate)) {
            JOptionPane.showMessageDialog(null, "Fecha inválida. No puede ser pasada.");
            return;
        }

        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd");
        String fecha = formatoFecha.format(fechaDate);

        // Obtener el doctor correctamente desde el combo
        DatosCombo doctor = (DatosCombo) cboDoctores.getSelectedItem();
        if (doctor == null) {
            JOptionPane.showMessageDialog(null, "Seleccione un doctor válido.");
            return;
        }
        int ciDoctor = doctor.getCodigo();

        //VALIDACIÓN 1: Verificar si el doctor está disponible
        String sqlEstadoDoctor = "SELECT Estado FROM doctores WHERE Ci_Doctor = " + ciDoctor;
        ResultSet rsEstado = bd.consultarRegistros(sqlEstadoDoctor);
        try {
            if (rsEstado.next()) {
                String estadoDoctor = rsEstado.getString("Estado");
                if (estadoDoctor.equalsIgnoreCase("No disponible")) {
                    JOptionPane.showMessageDialog(null,
                            "El doctor seleccionado NO está disponible. No se puede agendar.",
                            "Doctor No Disponible", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(FrmAgendamientos.class.getName()).log(Level.SEVERE, null, ex);
        }

        //VALIDACIÓN 2: Verificar si ya existe un agendamiento mismo día + doctor + paciente
        String sqlExiste = "SELECT * FROM agendamientos WHERE Ci_Doctor = " + ciDoctor +
                           " AND Ci_Paciente = " + ciPaciente +
                           " AND Fecha_Cita = '" + fecha + "'";
        ResultSet rsExiste = bd.consultarRegistros(sqlExiste);

        try {
            if (rsExiste.next()) {
                JOptionPane.showMessageDialog(null,
                        "Este agendamiento ya existe.\nEl paciente ya tiene cita con este doctor en esta fecha.",
                        "Agendamiento Duplicado", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (SQLException ex) {
            Logger.getLogger(FrmAgendamientos.class.getName()).log(Level.SEVERE, null, ex);
        }

        boolean exito = false;

        if (opc == 'N') {
            // Agregar nuevo registro
            String valores = txtIDAgendamiento.getText() + ","
                    + "'" + fecha + "',"
                    + "'" + estado + "',"
                    + "'" + ciPaciente + "',"
                    + "'" + ciDoctor + "'";
            exito = bd.insertarRegistro("agendamientos", valores);
        }

        if (exito) {
            JOptionPane.showMessageDialog(null, "Agendamiento guardado correctamente.");
            actualizarGrillaAgendamientos(); 
            actualizarGrillaHoy(); 
            limpiarCampos();
            habilitarCampos(false);
            habilitarBotones(true);
            opc = 'z';
        } else {
            JOptionPane.showMessageDialog(null, "Error al guardar el agendamiento.");
        }

    }//GEN-LAST:event_btnGuardarActionPerformed

    
    
    //AGREGAMOS LOS DATOS
    private void btnAgregarActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAgregarActionPerformed
        this.txtCIPaciente.setEnabled(false);
        this.optPendiente.setEnabled(false);
        this.optCancelado.setEnabled(false);
        this.optPendiente.setSelected(true);
        opc = 'N';
        habilitarCampos(true);
        limpiarCampos();
        ResultSet rs = bd.consultarRegistros("SELECT MAX(Id_Agendamiento) +1 FROM agendamientos");
        try{
            rs.next();
            this.txtIDAgendamiento.setText(rs.getString(1));    
        }catch(Exception ex){
            JOptionPane.showMessageDialog(null, "Error");
        }
        
        habilitarBotones(false);
        
        txtIDAgendamiento.requestFocus();
    }//GEN-LAST:event_btnAgregarActionPerformed

    private void btnCancelarAgendamientoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCancelarAgendamientoActionPerformed
int fila = grdAgendamiento.getSelectedRow();
            if (fila >= 0) {
        // Obtiene el ID del agendamiento seleccionado
        String id = grdAgendamiento.getValueAt(fila, 0).toString();

        // Confirma la cancelación
        int opcion = JOptionPane.showConfirmDialog(
                null,
                "¿Está seguro de cancelar el agendamiento seleccionado?",
                "Confirmar cancelación",
                JOptionPane.YES_NO_OPTION
        );

        if (opcion == JOptionPane.YES_OPTION) {
            // Cambia el estado a "Cancelado"
            String Estado_Cita = "Cancelado";

            // Actualiza la BD
            boolean exito = bd.actualizarRegistro(
                    "agendamientos",
                    "Estado_Cita='" + Estado_Cita + "'",
                    "Id_Agendamiento=" + id // Si el ID fuera VARCHAR, usar: "Id_Agendamiento='" + id + "'"
            );

            if (exito) {
                // Marca el radio button correspondiente
                optCancelado.setSelected(true);

                // Actualiza la grilla para reflejar el cambio
                actualizarGrillaAgendamientos();

                JOptionPane.showMessageDialog(null, "Agendamiento cancelado correctamente.");
            } else {
                JOptionPane.showMessageDialog(null, "No se pudo cancelar el agendamiento.");
            }
        }
    } else {
        JOptionPane.showMessageDialog(null, "Seleccione un registro para cancelar.");
    }
        
    }//GEN-LAST:event_btnCancelarAgendamientoActionPerformed

    private void btnBusquedaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnBusquedaActionPerformed

            FrmBusquedaPaciente base = new FrmBusquedaPaciente(null, true);
            base.setVisible(true);
            int ciPaciente = base.obtenerCodBusqueda(); // obtenés el valor que el usuario seleccionó
            if (ciPaciente != 0) { // si eligió algo
                txtCIPaciente.setText(String.valueOf(ciPaciente)); // lo cargas en tu campo
            }
    }//GEN-LAST:event_btnBusquedaActionPerformed

    private void optCanceladoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optCanceladoActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_optCanceladoActionPerformed
    
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(FrmAgendamientos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(FrmAgendamientos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(FrmAgendamientos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(FrmAgendamientos.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                FrmAgendamientos dialog = new FrmAgendamientos(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAgregar;
    private javax.swing.JButton btnBusqueda;
    private javax.swing.JButton btnCancelarAgendamiento;
    private javax.swing.JButton btnGuardar;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox<String> cboDoctores;
    private org.freixas.jcalendar.JCalendarCombo cboFecha;
    private javax.swing.JTable grdAgendamiento;
    private javax.swing.JTable grdDatos;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel lblAgendamiento;
    private javax.swing.JRadioButton optCancelado;
    private javax.swing.JRadioButton optPendiente;
    private javax.swing.JFormattedTextField txtBuscar;
    private javax.swing.JFormattedTextField txtCIPaciente;
    private javax.swing.JFormattedTextField txtIDAgendamiento;
    // End of variables declaration//GEN-END:variables
 
 //***************FUNCIONES PROPIAS***************
 private boolean fechaAtrasada(Date fechaSeleccionada) {
        Calendar hoy = Calendar.getInstance();
        hoy.set(Calendar.HOUR_OF_DAY, 0);
        hoy.set(Calendar.MINUTE, 0);
        hoy.set(Calendar.SECOND, 0);
        hoy.set(Calendar.MILLISECOND, 0);
        return fechaSeleccionada.before(hoy.getTime());
    }

    private void habilitarBotones(boolean estado) {
        btnAgregar.setEnabled(estado);
        btnCancelarAgendamiento.setEnabled(estado);
        btnBusqueda.setEnabled(!estado);
        btnGuardar.setEnabled(!estado);
    }

    private void habilitarCampos(boolean estado) {
        txtIDAgendamiento.setEnabled(estado);
        txtCIPaciente.setEnabled(!estado);
        cboDoctores.setEnabled(estado);
    }

    private void limpiarCampos() {
        txtIDAgendamiento.setText("");
        txtCIPaciente.setText("");
        cboDoctores.setSelectedIndex(-1);
    }  
}







