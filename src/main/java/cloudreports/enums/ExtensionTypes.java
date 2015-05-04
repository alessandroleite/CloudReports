package cloudreports.enums;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.vidageek.mirror.dsl.Mirror;
import net.vidageek.mirror.invoke.dsl.ConstructorHandler;

import org.cloudbus.cloudsim.CloudletScheduler;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.VmScheduler;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.BwProvisioner;
import org.cloudbus.cloudsim.provisioners.PeProvisioner;
import org.cloudbus.cloudsim.provisioners.RamProvisioner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import cloudreports.extensions.Extension;
import cloudreports.extensions.brokers.Broker;
import cloudreports.extensions.vmallocationpolicies.VmAllocationPolicyExtensible;
import static com.google.common.collect.Maps.*;
import static com.google.common.collect.Sets.*;
import static com.google.common.collect.Collections2.*;
import static com.google.common.base.Preconditions.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.base.Strings.*;
import static cloudreports.utils.ReflectionHelper.*;

@SuppressWarnings("unchecked")
public enum ExtensionTypes 
{
	/**
	 * Bandwidth provisioner.
	 * 
	 * <strong>Base class</strong>: org.cloudbus.cloudsim.provisioners.BwProvisioner
	 * <strong>Constructor signature:</strong> public ClassName(long);
	 */
	BW_PROVISIONER(BwProvisioner.class),
	
	/**
	 * 
	 */
	CLOUDLET_SCHEDULER(CloudletScheduler.class),
	
	/**
	 * 
	 */
	DATACENTER_BROKER(Broker.class),
	
	
	/**
	 * 
	 */
	PE_PROVISIONER(PeProvisioner.class),

	/**
	 * 
	 */
	POWER_MODEL(PowerModel.class),
	
	/**
	 * 
	 */
	RAM_PROVISIONER(RamProvisioner.class),
	
	/**
	 * 
	 */
	UTILIZATION_MODEL(UtilizationModel.class),

	/**
	 * 
	 */
	VM_ALLOCATION_POLICY(VmAllocationPolicyExtensible.class), 
	
	/**
	 * 
	 */
	VM_SCHEDULER (VmScheduler.class);

	private final Class<?> type;

	private final Map<Class<?>, Set<ExtensionDef>> extensions = newHashMap();
	
	private static final Logger LOG = LoggerFactory.getLogger(ExtensionTypes.class.getName());
	
	public static <T> Set<Class<? extends T>> getByTypeOf(final Class<T> superType) 
	{
		checkNotNull(superType);
		final Set<Class<? extends T>> result = newHashSet();
		
		for (Class<?> clazz : transform(getExtensionDefOf(superType), EXTENSIONDEF_TO_CLASS_FUNCTION)) 
		{
			result.add((Class<? extends T>) clazz);
		}

		return result;
	}
	
	
	public static <T> Optional<T> getExtensionInstanceOf(final Class<T> clazz, final String name, final Object ... args)
	{
		checkNotNull(clazz);
		checkState(!isNullOrEmpty(name));
		
		Optional<T> result = Optional.absent();
		
		for (ExtensionTypes ext: values())
		{
			if (ext.type.isAssignableFrom(clazz))
			{
				result = ext.getExtensionInstanceByName(name, args);
				break;
			}
		}
		
		return result;
	}
	
	private static Set<ExtensionDef> getExtensionDefOf(Class<?> baseType)
	{
		Set<ExtensionDef> result = newHashSet();
		
		for (ExtensionTypes ext: values())
		{
			if (ext.type.isAssignableFrom(baseType))
			{
				result = ext.extensions.get(baseType);
			}
		}
		
		return result;
	}

	private <T> ExtensionTypes(Class<T> type) 
	{
		this.type = type;

		extensions.put(type, new HashSet<ExtensionDef>());

		for (Class<?> clazz : getSubTypesOf(type)) 
		{
			final Extension annotation = clazz.getAnnotation(Extension.class);
			ExtensionDef def = new ExtensionDef(annotation != null ? annotation.name() : clazz.getSimpleName(), clazz);
			extensions.get(type).add(def);
		}
	}

	protected <T> Class<T> getExtensionType() 
	{
		return (Class<T>) type;
	}

	public Set<Class<?>> getExtensionTypes() 
	{
		final HashSet<Class<?>> result = newHashSet();

		for (ExtensionDef def : getExtensionsSet()) 
		{
			result.add(def.clazz);
		}

		return result;
	}
	
	public String[] getNames()
	{
		List<String> names = newArrayList(transform(getExtensionsSet(), new Function<ExtensionDef, String>() 
		{
			@Override
			public String apply(ExtensionDef input) 
			{
				return input.name;
			}
		}));
		
		return names.toArray(new String[names.size()]);
	}
	
	public <T> Optional<Class<T>> getExtensionByName(final String name) 
	{
		Collection<ExtensionDef> filter = filter(getExtensionsSet(), new Predicate<ExtensionDef>() 
	    {
			@Override
			public boolean apply(ExtensionDef input) 
			{
				return input.name.equalsIgnoreCase(name);
			}
		});
		
		return filter.isEmpty() ? Optional.<Class<T>>absent() : Optional.of((Class<T>) Iterables.get(filter, 0).clazz);
	}
	
	public <T> Optional<T> getExtensionInstanceByName(final String name, final Object ... args) 
	{
		Optional<T> result = Optional.absent();
		
		if (!isNullOrEmpty(name))
		{
			final ConstructorHandler<Object> handler = new Mirror().on(getExtensionByName(name).get()).invoke().constructor();
			final boolean hasArguments = args != null && args.length > 0;
			
			try
			{
				result = Optional.fromNullable((T) (hasArguments ? handler.withArgs(args) : handler.withoutArgs()));
			}
			catch(RuntimeException exception)
			{
				LOG.error("Error on creating the extension [{}] with arguments [{}]. Error message [{}]", name, Arrays.toString(args), exception.getMessage(), exception);
				
				try
				{
					LOG.info("Trying to create the extension [{}] using the default constructor", name);
					
					result = hasArguments ? Optional.fromNullable((T) handler.withoutArgs()) : Optional.<T> absent();
					
					LOG.info("Extension [{}] created throught the default constructor", name);
				}
				catch(RuntimeException exception2)
				{
					LOG.error("Error on trying to create the extension [{}], using the default constructor", name);
				}
			}
		}
		
		return result;
	}
	
	private Set<ExtensionDef> getExtensionsSet() 
	{
		return this.extensions.get(type);
	}

	private class ExtensionDef implements Comparable<ExtensionDef> 
	{
		private final String name;
		private final Class<?> clazz;

		public ExtensionDef(String name, Class<?> clazz) 
		{
			this.name = name == null || name.trim().isEmpty() ? clazz.getSimpleName() : name;
			this.clazz = clazz;
		}

		@Override
		public int compareTo(ExtensionDef other) 
		{
			return this.name.compareTo(other.name);
		}
	}

	static final Function<ExtensionDef, Class<?>> EXTENSIONDEF_TO_CLASS_FUNCTION = new Function<ExtensionDef, Class<?>>() 
	{
		@Override
		public Class<?> apply(ExtensionDef input) 
		{
			return input.clazz;
		}
	};
}
