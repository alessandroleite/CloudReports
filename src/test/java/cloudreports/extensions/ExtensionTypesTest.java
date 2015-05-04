package cloudreports.extensions;

import java.util.LinkedList;
import java.util.Set;

import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.power.PowerHost;
import org.junit.Test;

import cloudreports.enums.ExtensionTypes;
import cloudreports.extensions.vmallocationpolicies.RoundRobinVmAllocationPolicy;
import cloudreports.extensions.vmallocationpolicies.VmAllocationPolicyExtensible;
import cloudreports.models.DatacenterRegistry;

import com.google.common.base.Optional;

import static cloudreports.enums.ExtensionTypes.*;
import static org.assertj.core.api.Assertions.*;

public class ExtensionTypesTest 
{
	@Test
	public void checkIfAnnotatedCustomExtensionInstancesAreDisponible()
	{
		Optional<VmAllocationPolicy> vmAllocationPolicy = VM_ALLOCATION_POLICY.getExtensionInstanceByName(DummyVmAllocationPolicy.NAME, new LinkedList<PowerHost>(), new DatacenterRegistry());
		
		assertThat(vmAllocationPolicy.isPresent()).isTrue();
		assertThat(vmAllocationPolicy.get().getClass().isAssignableFrom(DummyVmAllocationPolicy.class)).isTrue();
	}
	
	
	@Test
	public void checkIfOrdinaryExtensionsAreDisponible()
	{
		Optional<VmAllocationPolicy> vmAllocationPolicy = VM_ALLOCATION_POLICY.getExtensionInstanceByName(RoundRobinVmAllocationPolicy.class.getSimpleName(), new LinkedList<PowerHost>(), new DatacenterRegistry());
		
		assertThat(vmAllocationPolicy.isPresent()).isTrue();
		assertThat(vmAllocationPolicy.get().getClass().isAssignableFrom(RoundRobinVmAllocationPolicy.class)).isTrue();
	}
	
	@Test
	public void checkIfExtensionOfATypeIsAvailable()
	{
		Set<Class<? extends VmAllocationPolicyExtensible>> vmAllocationPolicies = ExtensionTypes.getByTypeOf(VmAllocationPolicyExtensible.class);
		
		assertThat(vmAllocationPolicies).isNotNull();
		assertThat(vmAllocationPolicies.isEmpty()).isFalse();
		
		assertThat(vmAllocationPolicies.contains(RoundRobinVmAllocationPolicy.class)).isTrue();
	}
	
	@Test
	public void createExtensionFromNameAndRootBase()
	{
		Optional<VmAllocationPolicyExtensible> vmAllocationPolicy =  
				ExtensionTypes.getExtensionInstanceOf(VmAllocationPolicyExtensible.class, RoundRobinVmAllocationPolicy.class.getSimpleName(), new LinkedList<PowerHost>(), new DatacenterRegistry());
		
		assertThat(vmAllocationPolicy.isPresent()).isTrue();
		assertThat(vmAllocationPolicy.get().getClass().isAssignableFrom(RoundRobinVmAllocationPolicy.class)).isTrue();
		
		Optional<VmAllocationPolicyExtensible> policy = ExtensionTypes.getExtensionInstanceOf(VmAllocationPolicyExtensible.class, DummyVmAllocationPolicy.NAME, new LinkedList<PowerHost>(), new DatacenterRegistry());
		
		assertThat(policy.isPresent()).isTrue();
		assertThat(policy.get().getClass().isAssignableFrom(DummyVmAllocationPolicy.class)).isTrue();
	}
}
