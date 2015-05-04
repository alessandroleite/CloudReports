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

package cloudreports.simulation;

import cloudreports.business.CustomerRegistryBusiness;
import cloudreports.business.DatacenterRegistryBusiness;
import cloudreports.dao.CustomerRegistryDAO;
import cloudreports.dao.DatacenterRegistryDAO;
import cloudreports.dao.NetworkMapEntryDAO;
import cloudreports.extensions.PowerDatacenter;
import cloudreports.gui.Dialog;
import cloudreports.models.*;
import cloudreports.utils.RandomNumberGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import qrbg.ServiceDeniedException;
import static java.lang.String.*;
import static cloudreports.enums.ExtensionTypes.*;

/**
 * Provides methods that generate CloudSim entities to be used in a simulation.
 * 
 * @author      Thiago T. Sá
 * @since       1.0
 */
public class EntityFactory {
	
	private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(EntityFactory.class.getName());
    
    /**
     * Creates instances of CloudSim's PowerDatacenter class from a list of 
     * datacenter registries.
     * 
     * @param   dcrList a list of datacenter registries.
     * @return          a map containing names of datacenters as keys and
     *                  PowerDatacenter instances as values.
     * @since           1.0
     */    
    static HashMap<String, PowerDatacenter> createDatacenters() {
        List<DatacenterRegistry> dcrList = DatacenterRegistryBusiness.getListOfDatacenters();
        HashMap<String, PowerDatacenter> map = new HashMap<String, PowerDatacenter>();

        for (DatacenterRegistry dcr : dcrList) {
            List<PowerHost> hostList = createHosts(dcr.getHostList());
            if (hostList == null) {
                return null;
            }

            DatacenterCharacteristics chars = new DatacenterCharacteristics(dcr.getArchitecture(),
                    dcr.getOs(),
                    dcr.getVmm(),
                    hostList,
                    dcr.getTimeZone(),
                    dcr.getCostPerSec(),
                    dcr.getCostPerMem(),
                    dcr.getCostPerStorage(),
                    dcr.getCostPerBw());

            LinkedList<Storage> storageList = createStorageList(dcr.getSanList());


            try 
            {
                
                Optional<VmAllocationPolicy> allocationPolicy = VM_ALLOCATION_POLICY.getExtensionInstanceByName(dcr.getAllocationPolicyAlias(), hostList, dcr);
                
                if (!allocationPolicy.isPresent()) 
                {
                    Dialog.showErrorMessage(null, format("Error on loading the allocation policy [%s]", dcr.getAllocationPolicyAlias()));
                    return null;
                }

                PowerDatacenter newDC = new PowerDatacenter(dcr.getName(),
                        chars,
                        allocationPolicy.get(),
                        storageList,
                        dcr.getSchedulingInterval(),
                        dcr.getMonitoringInterval());

                newDC.setDisableMigrations(!dcr.isVmMigration());

                map.put(dcr.getName(), newDC);
            } 
            catch (Exception ex) 
            {
            	LOG.error("Error on creating data centers. Error message: [{}]", ex.getMessage(), ex);
            }
        }

        return map;
    }

    /**
     * Creates instances of CloudSim's PowerHost class from a list of host registers.
     * 
     * @param   hostList    a list of datacenter registers
     * @return              a list of PowerHost instances.
     * @since               1.0
     */        
    static List<PowerHost> createHosts(final List<HostRegistry> hostList) 
    {
        final List<PowerHost> list = new ArrayList<PowerHost>();

        int i = 0;
        
        for (HostRegistry hr : hostList) 
        {
            for (int n = 0; n < hr.getAmount(); n++) 
            {
                List<Pe> peList = createPes(hr);
                
                if (peList == null) 
                {
                    return null;
                }
                
                Optional<RamProvisioner> rp = RAM_PROVISIONER.getExtensionInstanceByName(hr.getRamProvisionerAlias(), hr.getRam());
                
                if (!rp.isPresent()) 
                {
                    Dialog.showErrorMessage(null, format("Error on loading the RAM provisioner [%s]", hr.getRamProvisionerAlias()));
                    return null;
                }

                Optional<BwProvisioner> bp = BW_PROVISIONER.getExtensionInstanceByName(hr.getBwProvisionerAlias(), (long) hr.getBw());
                
                if (!bp.isPresent()) 
                {
                    Dialog.showErrorMessage(null, format("Error on loading the bandwidth provisioner [%s]", hr.getBwProvisionerAlias()));
                    return null;
                }

                Optional<VmScheduler> vs = VM_SCHEDULER.getExtensionInstanceByName(hr.getSchedulingPolicyAlias(), peList);
                
                if (!vs.isPresent()) 
                {
                	Dialog.showErrorMessage(null, format("Error on loading the VM scheduling policy [%s]", hr.getSchedulingPolicyAlias()));
                    return null;
                }
                
                Optional<PowerModel> pm = POWER_MODEL.getExtensionInstanceByName(hr.getPowerModelAlias(), hr.getMaxPower(), hr.getStaticPowerPercent());
                
                if (!pm.isPresent()) 
                {
                    Dialog.showErrorMessage(null, format("Error on loading the power model [%s]", hr.getPowerModelAlias()));
                    return null;
                }

                list.add(new PowerHost(i, rp.get(), bp.get(), hr.getStorage(), peList, vs.get(), pm.get()));
                i++;
            }
        }

        return list;
    }

    /**
     * Creates instances of CloudSim's PowerPe class from a host registry.
     * 
     * @param   hr  a host registry.
     * @return      a list of PowerPe instances.
     * @since       1.0
     */     
    static List<Pe> createPes(HostRegistry hr) 
    {
        List<Pe> list = new ArrayList<Pe>();

        for (int i = 0; i < hr.getNumOfPes(); i++) 
        {
        	
            Optional<PeProvisioner> pp = PE_PROVISIONER.getExtensionInstanceByName(hr.getPeProvisionerAlias(), hr.getMipsPerPe()); 
            
            if (!pp.isPresent()) 
            {
                Dialog.showErrorMessage(null, format("Error on loading the PE provisioning policy [%s]", hr.getPeProvisionerAlias()));
                return null;
            }

            list.add(new Pe(i, pp.get()));
        }

        return list;
    }

    /**
     * Creates instances of CloudSim's SanStorage class from a list of SAN registers
     * 
     * @param   sanList a list of SAN registers.
     * @return          a list of SanStorage instances.
     * @since           1.0
     */         
    static LinkedList<Storage> createStorageList(List<SanStorageRegistry> sanList) 
    {
        LinkedList<Storage> list = new LinkedList<Storage>();

        for (SanStorageRegistry sr : sanList) 
        {
            try 
            {
                list.add(new SanStorage(sr.getName(),
                        sr.getCapacity(),
                        sr.getBandwidth(),
                        sr.getNetworkLatency()));
            } 
            catch (ParameterException e) 
            {
            	LOG.error("Error on crearing the SanStorage; Error message [{}]", e.getMessage(), e);
            }
        }

        return list;
    }

    /**
     * Creates instances of CloudSim's DatacenterBroker class from a list of customer registers.
     * 
     * @param   customerList    a list of customer instances.
     * @return                  a map containing names of customers as keys and
     *                          DatacenterBroker instances as values.
     * @since                   1.0
     */     
    static HashMap<String, DatacenterBroker> createBrokers() 
    {
        List<CustomerRegistry> customerList = CustomerRegistryBusiness.getListOfCustomers();
        HashMap<String, DatacenterBroker> map = new HashMap<String, DatacenterBroker>();

        try 
        {
            for (CustomerRegistry cr : customerList) 
            {
                UtilizationProfile up = cr.getUtilizationProfile();
                String name = cr.getName();

                Optional<DatacenterBroker> broker = DATACENTER_BROKER.getExtensionInstanceByName(up.getBrokerPolicyAlias(), name); 
                
                if (!broker.isPresent()) 
                {
                    Dialog.showErrorMessage(null, format("Error on loading datacenter broker [%s]", up.getBrokerPolicyAlias()));
                    return null;
                }

                int brokerId = broker.get().getId();
                
                List<Vm> vmList = createVms(cr.getVmList(), brokerId);
                
                if (vmList == null) 
                {
                    return null;
                }

                broker.get().submitVmList(vmList);
                
                List<Cloudlet> cloudletList = createCloudlets(up, brokerId, new CustomerRegistryDAO().getNumOfVms(cr.getId()));
                
                if (cloudletList == null) 
                {
                    return null;
                }

                broker.get().submitCloudletList(cloudletList);
                map.put(cr.getName(), broker.get());
            }
        } 
        catch (IOException | ServiceDeniedException ex ) 
        {
        	LOG.error(ex.getMessage(), ex);
        } 

        return map;
    }

    /**
     * Creates instances of CloudSim's VM class from a list of virtual machine registers.
     * 
     * @param   vmList      a list of virtual machine registers.
     * @param   brokerId    the id of the broker that owns the virtual machines. 
     * @return              a list of VM instances.
     * @since               1.0
     */         
    static List<Vm> createVms(List<VirtualMachineRegistry> vmList, int brokerId) 
    {
        List<Vm> list = new ArrayList<Vm>();

        int vmId = 0;
        for (VirtualMachineRegistry vmr : vmList) 
        {
            for (int n = 0; n < vmr.getAmount(); n++) 
            {
                Optional<CloudletScheduler> cs = CLOUDLET_SCHEDULER.getExtensionInstanceByName(vmr.getSchedulingPolicyAlias(), vmr.getMips(), vmr.getPesNumber()); 
                
                if (!cs.isPresent()) 
                {
                    Dialog.showErrorMessage(null, format("Error on loading the cloudlet scheduler [%s]", vmr.getSchedulingPolicyAlias()));
                    return null;
                }

                list.add(new Vm(vmId,
                        brokerId,
                        vmr.getMips(),
                        vmr.getPesNumber(),
                        vmr.getRam(),
                        vmr.getBw(),
                        vmr.getSize(),
                        vmr.getVmm(),
                        cs.get()));
                vmId++;
            }
        }
        return list;
    }

    /**
     * Creates instances of CloudSim's Cloudlet class from a customer's
     * utilization profile.
     * 
     * @param   ugr         the utilization profile.
     * @param   brokerId    the id of the broker that owns the cloudlets.
     * @param   numOfVms    the number of virtual machines.
     * @return              a list of Cloudlet instances.
     * @since               1.0
     */          
    static List<Cloudlet> createCloudlets(UtilizationProfile ugr, int brokerId, long numOfVms) throws IOException, ServiceDeniedException {
        List<Cloudlet> list = new ArrayList<Cloudlet>();

        for (int i = 0; i < numOfVms; i++) 
        {
        	
            Optional<UtilizationModel> cpu = UTILIZATION_MODEL.getExtensionInstanceByName(ugr.getUtilizationModelCpuAlias());
            
            if (!cpu.isPresent()) 
            {
                Dialog.showErrorMessage(null, format("Error on loading the CPU utilization model [%s]", ugr.getUtilizationModelCpuAlias()));
                return null;
            }

            Optional<UtilizationModel> ram = UTILIZATION_MODEL.getExtensionInstanceByName(ugr.getUtilizationModelRamAlias()); 
            
            if (!ram.isPresent()) 
            {
                Dialog.showErrorMessage(null, format("Error on loading the RAM utilization model [%s]", ugr.getUtilizationModelRamAlias()));
                return null;
            }

            Optional<UtilizationModel> bw = UTILIZATION_MODEL.getExtensionInstanceByName(ugr.getUtilizationModelBwAlias()); 
            
            if (!bw.isPresent()) 
            {
                Dialog.showErrorMessage(null, format("Error on loading the bandwidth utilization model [%s]", ugr.getUtilizationModelBwAlias()));
                return null;
            }

            Cloudlet cloudlet = new Cloudlet(i,
                    (long) ((long) ugr.getLength() * RandomNumberGenerator.getRandomNumbers(1).get(0)),
                    ugr.getCloudletsPesNumber(),
                    ugr.getFileSize(),
                    ugr.getOutputSize(),
                    cpu.get(),
                    ram.get(),
                    bw.get());

            cloudlet.setUserId(brokerId);
            cloudlet.setVmId(i);
            list.add(cloudlet);
        }

        return list;
    }

    /**
     * Sets up all the network links to be simulated,
     * 
     * @param   datacenters the datacenters being simulated.
     * @param   brokers     the brokers being simulated.
     * @since               1.0
     */      
    static void setUpNetworkLinks(HashMap<String, PowerDatacenter> datacenters, HashMap<String, DatacenterBroker> brokers) 
    {
        NetworkMapEntryDAO neDAO = new NetworkMapEntryDAO();

        /*
         * Establish all links whose source is a datacenter
         */
        DatacenterRegistryDAO drDAO = new DatacenterRegistryDAO();
        String[] datacenterNames = drDAO.getAllDatacentersNames();

        for (String source : datacenterNames) 
        {
            PowerDatacenter src = datacenters.get(source);

            List<NetworkMapEntry> destinationList = neDAO.getListOfDestinations(source);

            for (NetworkMapEntry entry : destinationList) 
            {
                String destinationName = entry.getDestination();

                if (drDAO.getDatacenterRegistry(destinationName) != null) 
                { //destination is a datacenter
                    PowerDatacenter dest = datacenters.get(destinationName);
                    NetworkTopology.addLink(src.getId(), dest.getId(), entry.getBandwidth(), entry.getLatency());
                } 
                else 
                { //destination is a customer
                    DatacenterBroker dest = brokers.get(destinationName);
                    NetworkTopology.addLink(src.getId(), dest.getId(), entry.getBandwidth(), entry.getLatency());
                }
            }
        }


        /*
         * Establish all links whose source is a customer
         */
        CustomerRegistryDAO crDAO = new CustomerRegistryDAO();
        String[] customerNames = crDAO.getCustomersNames();

        for (String source : customerNames) 
        {
            DatacenterBroker src = brokers.get(source);

            List<NetworkMapEntry> destinationList = neDAO.getListOfDestinations(source);

            for (NetworkMapEntry entry : destinationList) 
            {
                String destinationName = entry.getDestination();

                if (drDAO.getDatacenterRegistry(destinationName) != null) 
                { //destination is a datacenter
                    PowerDatacenter dest = datacenters.get(destinationName);
                    NetworkTopology.addLink(src.getId(), dest.getId(), entry.getBandwidth(), entry.getLatency());
                } 
                else 
                { //destination is a customer
                    DatacenterBroker dest = brokers.get(destinationName);
                    NetworkTopology.addLink(src.getId(), dest.getId(), entry.getBandwidth(), entry.getLatency());
                }
            }
        }
    }
}
