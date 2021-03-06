/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package smma.juegosTablero.gui;

import jade.core.AID;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import juegosTablero.Vocabulario.ModoJuego;
import juegosTablero.Vocabulario.TipoJuego;
import smma.juegosTablero.Constantes.Enfrentamiento;
import static smma.juegosTablero.Constantes.VACIO;
import smma.juegosTablero.agentes.AgenteCentralJuego;
import smma.juegosTablero.util.RegistroJuego;
import smma.juegosTablero.util.RegistroJuegoFinalizado;

/**
 *
 * @author pedroj
 */
public class JuegosTableroJFrame extends javax.swing.JFrame {
    private final AgenteCentralJuego agente;
    private OkCancelDialog finalizacion;

    /**
     * Creates new form Consola
     * @param agente
     */
    public JuegosTableroJFrame(AgenteCentralJuego agente) {
        initComponents();
        
        this.agente = agente;
        
        this.setTitle("Juegos de Tablero - Agente Central: " + agente.getLocalName());
        setVisible(true);
        
        // Completamos los modos de juego
        jComboBoxModo.removeAllItems();
        for( ModoJuego modo : ModoJuego.values() ) {
            jComboBoxModo.addItem(modo.name());
        }
        
        // Completamos los posibles enfrentamientos
        jComboBoxVictorias.removeAllItems();
        for( Enfrentamiento partidas : Enfrentamiento.values() ) {
            jComboBoxVictorias.addItem(partidas.name());
        }
    }

    public void activaProponerJuego(List<TipoJuego> listaJuegos) {
        jComboBoxTipoJuego.removeAllItems();
        for ( TipoJuego juego : listaJuegos )
            jComboBoxTipoJuego.addItem(juego.toString());
        proponerJuego.setEnabled(true);
    }
    
    public void activaCompletarJuego(Set<String> idJuegos) {
        jComboBoxJuegos.removeAllItems();
        
        if( idJuegos.isEmpty() ) {
            completarJuego.setEnabled(false);
        } else {
            Iterator it = idJuegos.iterator();
            while( it.hasNext() ) {
                jComboBoxJuegos.addItem((String) it.next());
            }
            completarJuego.setEnabled(true);
        }
    }
    
    public void activaPendiente(List<RegistroJuego> listaPendientes) {
        jComboBoxJuegosInc.removeAllItems();
        
        if( listaPendientes.isEmpty() ) {
            anulaPendiente();
        } else {
            
            for( RegistroJuego pendiente : listaPendientes ) {
                jComboBoxJuegosInc.addItem(pendiente.getJuegoPropuesto().getJuego().getIdJuego());
            }
        
            juegoPendiente.setEnabled(true);
            eliminarJuego.setEnabled(true);
        }
    }
    
    public void activaReproduccion(List<RegistroJuegoFinalizado> juegosFinalizados) {
        jComboBoxJuegosFin.removeAllItems();
        
        if( juegosFinalizados.isEmpty() ) {
            anulaReproduccion();
        } else {
            for( RegistroJuegoFinalizado juego : juegosFinalizados ) {
                jComboBoxJuegosFin.addItem(juego.getIdJuego());
            }
            
            reproducirJuego.setEnabled(true);
        }
    }
    
    public void anulaPendiente() {
        juegoPendiente.setEnabled(false);
        eliminarJuego.setEnabled(false);
    }
    
    public void anulaReproduccion() {
        reproducirJuego.setEnabled(false);
    }
    
    public void anulaProponerJuego() {
        jComboBoxTipoJuego.removeAllItems();
        proponerJuego.setEnabled(false);
    }
    
    public void anulaCompletarJuego(Set<String> idJuegos) {
        completarJuego.setEnabled(false);
        
        jComboBoxJuegos.removeAllItems();
        Iterator it = idJuegos.iterator();
        while( it.hasNext() ) {
            jComboBoxJuegos.addItem((String) it.next());
        }
    }
    
    public void agentesGrupoJuegos(List<AID> listaAgentes) {
        jComboBoxAgentesGrupo.removeAllItems();
        for( AID agente : listaAgentes ) {
            jComboBoxAgentesGrupo.addItem(agente.getLocalName());
        }
        
        if ( jComboBoxAgentesGrupo.getItemCount() > VACIO ) {
            if ( jComboBoxJuegos.getItemCount() > VACIO ) {
                completarJuego.setEnabled(true);
            } else if ( jComboBoxJuegosFin.getItemCount() > VACIO ) {
                reproducirJuego.setEnabled(true);
            }
            
        } else {
            completarJuego.setEnabled(false);
            reproducirJuego.setEnabled(false);
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

        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jComboBoxAgentesGrupo = new javax.swing.JComboBox<>();
        jComboBoxJuegos = new javax.swing.JComboBox<>();
        completarJuego = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jComboBoxJuegosFin = new javax.swing.JComboBox<>();
        reproducirJuego = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxTipoJuego = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jComboBoxVictorias = new javax.swing.JComboBox<>();
        jComboBoxJuegosInc = new javax.swing.JComboBox<>();
        jLabel5 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jComboBoxModo = new javax.swing.JComboBox<>();
        proponerJuego = new javax.swing.JButton();
        eliminarJuego = new javax.swing.JButton();
        juegoPendiente = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setAlwaysOnTop(true);
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel4.setText("Agentes Grupo Juegos");

        jComboBoxAgentesGrupo.setMaximumSize(new java.awt.Dimension(40, 40));

        completarJuego.setText("Completar Juego");
        completarJuego.setEnabled(false);
        completarJuego.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                completarJuegoActionPerformed(evt);
            }
        });

        jLabel6.setText("Juegos Disponibles");

        jLabel7.setText("Juegos Completados");

        reproducirJuego.setText("Reproducir Juego");
        reproducirJuego.setEnabled(false);
        reproducirJuego.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reproducirJuegoActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jComboBoxAgentesGrupo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBoxJuegos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel6))
                        .addGap(46, 46, 46)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel7)
                            .addComponent(jComboBoxJuegosFin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 126, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(completarJuego)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(reproducirJuego)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jComboBoxAgentesGrupo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 34, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxJuegos, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxJuegosFin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(completarJuego)
                    .addComponent(reproducirJuego))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel1.setText("Tipo Juego");

        jLabel3.setText("Enfrentamiento");

        jLabel5.setText("Juegos Pendientes");

        jLabel2.setText("Modo Juego");

        proponerJuego.setText("Proponer Juego");
        proponerJuego.setEnabled(false);
        proponerJuego.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                proponerJuegoActionPerformed(evt);
            }
        });

        eliminarJuego.setText("Eliminar Juego");
        eliminarJuego.setEnabled(false);
        eliminarJuego.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eliminarJuegoActionPerformed(evt);
            }
        });

        juegoPendiente.setText("Juego Pendiente");
        juegoPendiente.setEnabled(false);
        juegoPendiente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                juegoPendienteActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(jComboBoxTipoJuego, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)
                    .addComponent(jComboBoxModo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(103, 103, 103)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jComboBoxVictorias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel5)
                            .addComponent(jLabel3)
                            .addComponent(jComboBoxJuegosInc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(50, Short.MAX_VALUE)
                .addComponent(proponerJuego)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(juegoPendiente)
                .addGap(5, 5, 5)
                .addComponent(eliminarJuego)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxTipoJuego, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxVictorias, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(37, 37, 37)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxJuegosInc, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBoxModo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(eliminarJuego)
                    .addComponent(juegoPendiente)
                    .addComponent(proponerJuego))
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        finalizacion = new OkCancelDialog(this, true, agente);
        finalizacion.setVisible(true);
    }//GEN-LAST:event_formWindowClosing

    private void completarJuegoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_completarJuegoActionPerformed
        // TODO add your handling code here:
        int indiceAgente = jComboBoxAgentesGrupo.getSelectedIndex();
        String idJuego = (String) jComboBoxJuegos.getSelectedItem();
        agente.completarJuego(indiceAgente, idJuego);
    }//GEN-LAST:event_completarJuegoActionPerformed

    private void proponerJuegoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_proponerJuegoActionPerformed
        // TODO add your handling code here:
        // Proponemos un juego con los parámetros seleccionados
        ModoJuego modoJuego;
        TipoJuego tipoJuego = null;
        int victorias;
        
        for ( TipoJuego tipo : TipoJuego.values() )
            if ( jComboBoxTipoJuego.getSelectedItem().toString().equals(tipo.name()) ) {
                tipoJuego = tipo;
                break;
            }
        
        modoJuego = ModoJuego.values()[jComboBoxModo.getSelectedIndex()];
        victorias = Enfrentamiento.values()[jComboBoxVictorias.getSelectedIndex()].victorias();
        
        agente.proponerJuego(tipoJuego, modoJuego, victorias);
    }//GEN-LAST:event_proponerJuegoActionPerformed

    private void eliminarJuegoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminarJuegoActionPerformed
        // TODO add your handling code here:
        int index = jComboBoxJuegosInc.getSelectedIndex();
        jComboBoxJuegosInc.removeItemAt(index);
        if ( jComboBoxJuegosInc.getItemCount() == VACIO ) {
            juegoPendiente.setEnabled(false);
            eliminarJuego.setEnabled(false);  
        }
        
        agente.eliminarJuego(index);
    }//GEN-LAST:event_eliminarJuegoActionPerformed

    private void juegoPendienteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_juegoPendienteActionPerformed
        // TODO add your handling code here:
        int index = jComboBoxJuegosInc.getSelectedIndex();
        jComboBoxJuegosInc.removeItemAt(index);
        if ( jComboBoxJuegosInc.getItemCount() == VACIO ) {
            juegoPendiente.setEnabled(false);
            eliminarJuego.setEnabled(false);  
        }
        
        agente.juegoPendiente(index);
    }//GEN-LAST:event_juegoPendienteActionPerformed

    private void reproducirJuegoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reproducirJuegoActionPerformed
        // TODO add your handling code here:
        int index = jComboBoxJuegosFin.getSelectedIndex();
        
        agente.reproducirJuego(index);
    }//GEN-LAST:event_reproducirJuegoActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton completarJuego;
    private javax.swing.JButton eliminarJuego;
    private javax.swing.JComboBox<String> jComboBoxAgentesGrupo;
    private javax.swing.JComboBox<String> jComboBoxJuegos;
    private javax.swing.JComboBox<String> jComboBoxJuegosFin;
    private javax.swing.JComboBox<String> jComboBoxJuegosInc;
    private javax.swing.JComboBox<String> jComboBoxModo;
    private javax.swing.JComboBox<String> jComboBoxTipoJuego;
    private javax.swing.JComboBox<String> jComboBoxVictorias;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton juegoPendiente;
    private javax.swing.JButton proponerJuego;
    private javax.swing.JButton reproducirJuego;
    // End of variables declaration//GEN-END:variables

}
