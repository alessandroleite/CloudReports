package cloudreports.extensions;

import java.util.Collections;
import java.util.List;

import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.power.PowerHost;

import cloudreports.extensions.vmallocationpolicies.VmAllocationPolicyExtensible;
import cloudreports.models.DatacenterRegistry;
import cloudreports.models.Migration;

@Extension(name = DummyVmAllocationPolicy.NAME)
public class DummyVmAllocationPolicy extends VmAllocationPolicySimple implements VmAllocationPolicyExtensible 
{
	public static final String NAME = "Dummy VM Allocation";
	
	public DummyVmAllocationPolicy(List<? extends PowerHost> hosts, DatacenterRegistry dr) 
	{
		super(hosts);
	}

	@Override
	public List<Migration> getListOfMigrationsToBeExecuted(List<? extends Vm> vmList) 
	{
		return Collections.emptyList();
	}
}
