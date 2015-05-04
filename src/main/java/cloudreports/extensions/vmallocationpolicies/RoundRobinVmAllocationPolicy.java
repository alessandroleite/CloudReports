package cloudreports.extensions.vmallocationpolicies;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;

import cloudreports.models.DatacenterRegistry;
import cloudreports.models.Migration;

public class RoundRobinVmAllocationPolicy extends VmAllocationPolicy implements VmAllocationPolicyExtensible 
{
	private final Map<String, Host> vmTable = newHashMap();

	private final CircularHostList hosts;
	
	public RoundRobinVmAllocationPolicy(List<? extends PowerHost> hosts, DatacenterRegistry datacenter) 
    {
		super(hosts);
		this.hosts = new CircularHostList(hosts);
	}
	
	@Override
	public boolean allocateHostForVm(Vm vm) 
	{
		if (this.vmTable.containsKey(vm.getUid())) 
		{
			return true;
		}
 
		boolean vmAllocated = false;
 
		Host host = this.hosts.next();
		
		if (host != null) 
		{
			vmAllocated = this.allocateHostForVm(vm, host);
		}
 
		return vmAllocated;
	}
 
	@Override
	public boolean allocateHostForVm(Vm vm, Host host) 
	{
		if (host != null && host.vmCreate(vm)) 
		{
			vmTable.put(vm.getUid(), host);
			Log.formatLine("%.4f: VM #" + vm.getId() + " has been allocated to the host#" + host.getId() + 
					" datacenter #" + host.getDatacenter().getId() + "(" + host.getDatacenter().getName() + ") #", CloudSim.clock());
			return true;
		}
		
		return false;
	}
 
	@Override
	public List<Map<String, Object>> optimizeAllocation(List<? extends Vm> vmList) 
	{
		return Collections.emptyList();
	}
 
	@Override
	public void deallocateHostForVm(Vm vm) 
	{
		Host host = this.vmTable.remove(vm.getUid());
 
		if (host != null) 
		{
			host.vmDestroy(vm);
		}
	}
 
	@Override
	public Host getHost(Vm vm) 
	{
		return this.vmTable.get(vm.getUid());
	}
 
	@Override
	public Host getHost(int vmId, int userId) {
		return this.vmTable.get(Vm.getUid(userId, vmId));
	}

	@Override
	public List<Migration> getListOfMigrationsToBeExecuted(List<? extends Vm> vmList) 
	{
		return Collections.emptyList();
	}
}
