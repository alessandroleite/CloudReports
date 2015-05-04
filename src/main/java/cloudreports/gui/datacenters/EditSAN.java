/* 
 * Copyright (c) 2010-2012 Thiago T. Sá
 * 
 * This file is part of CloudReports.
 *
 * CloudReports is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * CloudReports is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * For more information about your rights as a user of CloudReports,
 * refer to the LICENSE file or see <http://www.gnu.org/licenses/>.
 */

package cloudreports.gui.datacenters;

import cloudreports.dao.DatacenterRegistryDAO;
import cloudreports.dao.SanStorageRegistryDAO;
import cloudreports.gui.MainView;
import cloudreports.models.DatacenterRegistry;
import cloudreports.models.SanStorageRegistry;

/**
 * The EditSAN form.
 * Most of its code is generated automatically by the NetBeans IDE.
 * 
 * @author      Thiago T. Sá
 * @since       1.0
 */
@SuppressWarnings("serial")
public class EditSAN extends javax.swing.JDialog {

    /** The SAN registry being edited. */
    SanStorageRegistry ssr;
    
    /** An instance of SAN registry DAO. */
    SanStorageRegistryDAO srDAO = new SanStorageRegistryDAO();
    
    /** A specific datacenter view. */
    SpecificDatacenterView sdcv;
    
    /** Creates a new EditSAN form. */
    public EditSAN(SanStorageRegistry ssr, SpecificDatacenterView sdcv) {
        this.ssr=ssr;
        this.srDAO = new SanStorageRegistryDAO();
        this.sdcv=sdcv;
        initComponents();
        capacitySpinner.setValue(ssr.getCapacity());
        bwSpinner.setValue(ssr.getBandwidth());
        latencySpinner.setValue(ssr.getNetworkLatency());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        okButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        capacityLabel = new javax.swing.JLabel();
        bwLabel = new javax.swing.JLabel();
        latencyLabel = new javax.swing.JLabel();
        bwSpinner = new javax.swing.JSpinner();
        capacitySpinner = new javax.swing.JSpinner();
        latencySpinner = new javax.swing.JSpinner();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Edit SAN");
        setModal(true);
        setResizable(false);

        okButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/cloudreports/gui/resources/ok.png"))); // NOI18N
        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Settings"));

        capacityLabel.setText("Capacity:");

        bwLabel.setText("Bandwidth:");

        latencyLabel.setText("Latency:");

        bwSpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1.0d), Double.valueOf(1.0d), null, Double.valueOf(100.0d)));
        bwSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                bwSpinnerStateChanged(evt);
            }
        });

        capacitySpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(1000.0d), Double.valueOf(1000.0d), null, Double.valueOf(100.0d)));
        capacitySpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                capacitySpinnerStateChanged(evt);
            }
        });

        latencySpinner.setModel(new javax.swing.SpinnerNumberModel(Double.valueOf(0.1d), Double.valueOf(0.1d), null, Double.valueOf(0.1d)));
        latencySpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                latencySpinnerStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(bwLabel)
                    .addComponent(capacityLabel)
                    .addComponent(latencyLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(latencySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bwSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(capacitySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(116, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {bwSpinner, capacitySpinner, latencySpinner});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(capacityLabel)
                    .addComponent(capacitySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bwLabel)
                    .addComponent(bwSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(latencyLabel)
                    .addComponent(latencySpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(274, Short.MAX_VALUE)
                .addComponent(okButton, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(okButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /** 
     * Changes the capacity of this SAN whenever the state of the capacity
     * spinner changes.
     *
     * @param   evt     a change event.
     * @since           1.0
     */     
    private void capacitySpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_capacitySpinnerStateChanged
        String n = String.valueOf(capacitySpinner.getValue());
        ssr.setCapacity(Double.valueOf(n));
    }//GEN-LAST:event_capacitySpinnerStateChanged

    /** 
     * Changes the bandwidth of this SAN whenever the state of the bandwidth
     * spinner changes.
     *
     * @param   evt     a change event.
     * @since           1.0
     */      
    private void bwSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_bwSpinnerStateChanged
        String n = String.valueOf(bwSpinner.getValue());
        ssr.setBandwidth(Double.valueOf(n));
    }//GEN-LAST:event_bwSpinnerStateChanged

    /** 
     * Changes the latency of this SAN whenever the state of the latency
     * spinner changes.
     *
     * @param   evt     a change event.
     * @since           1.0
     */      
    private void latencySpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_latencySpinnerStateChanged
        String n = String.valueOf(latencySpinner.getValue());
        ssr.setNetworkLatency(Double.valueOf(n));
    }//GEN-LAST:event_latencySpinnerStateChanged

    /** 
     * Updates the SAN registry and the specific datacenter view when
     * the OK button is clicked.
     *
     * @param   evt     an action event.
     * @since           1.0
     */     
    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        srDAO.updateSanStorageRegistry(ssr);
        
        //Get updated version of the datacenter registry
        DatacenterRegistryDAO drDAO = new DatacenterRegistryDAO();
        DatacenterRegistry dcr = drDAO.getDatacenterRegistry(sdcv.getDatacenterRegistry().getId());
       //Updates the View reference
        sdcv.setDcr(dcr);
        
        sdcv.updateSansTable();
        sdcv.updateInformationPanel();
        MainView.setDatacenterModified(true);
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bwLabel;
    private javax.swing.JSpinner bwSpinner;
    private javax.swing.JLabel capacityLabel;
    private javax.swing.JSpinner capacitySpinner;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel latencyLabel;
    private javax.swing.JSpinner latencySpinner;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables

}
