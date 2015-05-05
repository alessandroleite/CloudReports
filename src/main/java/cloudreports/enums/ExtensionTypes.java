package cloudreports.enums;

import java.lang.reflect.Constructor;
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
import cloudreports.extensions.InvalidExtensionException;
import cloudreports.extensions.brokers.Broker;
import cloudreports.extensions.vmallocationpolicies.VmAllocationPolicyExtensible;
import cloudreports.models.DatacenterRegistry;


import static java.lang.String.*;
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
	 * <p>Bandwidth provisioning policies
	 * 
	 * <strong>Base class</strong>: {@link org.cloudbus.cloudsim.provisioners.BwProvisioner}
	 * <strong>Constructor parameter types</strong>: {@link Long}
	 */
	BW_PROVISIONER(BwProvisioner.class, long.class),
	
	/**
	 * <p>Cloudlets scheduling policies
	 * 
	 * <strong>Base type</strong>: {@link org.cloudbus.cloudsim.CloudletScheduler}
	 * <strong>Constructor parameter types</strong>: {@link Double} and {@link Integer}
	 */
	CLOUDLET_SCHEDULER(CloudletScheduler.class, double.class, int.class),
	
	/**
	 * Broker policies:
	 * <strong>Base type</strong>: {@link cloudreports.extensions.brokers.Broker}
	 * <strong>Constructor parameter types<strong>: {@link String}
	 */
	DATACENTER_BROKER(Broker.class, String.class),
	
	
	/**
	 * <p>Processing elements provisioning policies
	 * 
	 * <strong>Base class</strong>: {@link org.cloudbus.cloudsim.provisioners.PeProvisioner}
	 * <strong>Constructor parameter types</strong>: {@link Double}
	 */
	PE_PROVISIONER(PeProvisioner.class, double.class),

	/**
	 * <p>Power consumption models
	 * 
	 * <strong>Base type</strong>: {@link org.cloudbus.cloudsim.power.models.PowerModel}
	 * <strong>Constructor parameter types</strong>: {@link Double} and {@link Double}
	 */
	POWER_MODEL(PowerModel.class, double.class, double.class),
	
	/**
	 * <p>RAM provisioning policies
	 * 
	 * <strong>Base class</strong>: {@link org.cloudbus.cloudsim.provisioners.RamProvisioner}
	 * <strong>Constructor parameter types</strong>: {@link Integer}
	 */
	RAM_PROVISIONER(RamProvisioner.class, Integer.class),
	
	/**
	 * <p> Resource utilization models
	 * 
	 * <strong>Base type</strong>: org.cloudbus.cloudsim.UtilizationModel
	 * <strong>Constructor parameter types</strong>: none (default constructor)
	 */
	UTILIZATION_MODEL(UtilizationModel.class),

	/**
	 * <p>Virtual machines allocation policies
	 * 
	 * <strong>Base class</strong>: {@link cloudreports.extensions.vmallocationpolicies.VmAllocationPolicyExtensible}
	 * <strong>Constructor parameter types</strong>: {@link java.util.List} and {@link DatacenterRegistry}
	 */
	VM_ALLOCATION_POLICY(VmAllocationPolicyExtensible.class, List.class, DatacenterRegistry.class), 
	
	/**
	 * <p> Virtual machines schedulers
	 * 
	 * <strong>Base type </strong>: {@link org.cloudbus.cloudsim.VmScheduler}
	 * <strong>Constructor parameter types</strong>: {@link List}
	 */
	VM_SCHEDULER (VmScheduler.class, List.class);

	private final Class<?> type;

	private final Map<Class<?>, Set<ExtensionDef>> extensions = newHashMap();
	
	private static final Logger LOG = LoggerFactory.getLogger(ExtensionTypes.class.getName());
	
	/**
	 * Returns all the sub-types of a given {@code type}.
	 *   
	 * @param type the base type. It might not be <code>null</code> 
	 * @return A non-null and unmodifiable {@link Set} with the sub-types.
	 */
	public static <T> Set<Class<? extends T>> getByTypeOf(final Class<T> type) 
	{
		checkNotNull(type);
		
		final Set<Class<? extends T>> result = newHashSet();
		
		for (Class<?> clazz : transform(getExtensionDefOf(type), EXTENSIONDEF_TO_CLASS_FUNCTION)) 
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

	/**
	 * Creates a new extension type for the given {@code baseType}.
	 * 
	 * @param baseType represents the base type (i.e., root type) of an extension. It might not be <code>null</code> 
	 * @param constructorParameterTypes the required 
	 * @throws InvalidExtensionException if the types do not have the required constructor neither a default constructor
	 * @throws IllegalStateException if there more than one extension type with the same base type.
	 */
	private <T> ExtensionTypes(final Class<T> baseType, Class<?> ... constructorParameterTypes) 
	{
		this.type = baseType;
		final Set<ExtensionDef> existing = extensions.put(checkNotNull(baseType), new HashSet<ExtensionDef>());
		checkState(existing == null);

		for (Class<?> clazz : getSubTypesOf(baseType)) 
		{
			if (constructorParameterTypes != null && constructorParameterTypes.length > 0)
			{
				Constructor<?> constructor = new Mirror().on(clazz).reflect().constructor().withArgs(constructorParameterTypes);
				
				if (constructor == null && new Mirror().on(clazz).reflect().constructor().withoutArgs() == null)
				{
					throw new InvalidExtensionException(format("The class %s does not have a constructor with the expected arguments neither a default constructor. " + 
				                                               "The required arguments are: %s", clazz.getName(), Arrays.toString(constructorParameterTypes)));
				}
			}
			
			final Extension annotation = clazz.getAnnotation(Extension.class);
			final ExtensionDef def = new ExtensionDef(annotation != null ? annotation.name() : clazz.getSimpleName(), clazz);
			
			extensions.get(baseType).add(def);
		}
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
	
	/**
	 * Returns the names of the extensions of a type.
	 * @return a non-null array with the name of the available extensions.
	 */
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

	/**
	 * Returns an extension which has the given {@code name}. 
	 * 
	 * @param name the extension name to find. There is not a direct relationship between an extension's name and its class. 
	 * @return an {@link Optional} with the extension found or {@link Optional#absent()} if there isn't an extension with the given name.
	 */
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
				LOG.error("Error on creating the extension [{}] with arguments [{}]. Error message [{}]", name, Arrays.toString(args), exception.getMessage());
				
				try
				{
					LOG.info("Trying to create the extension [{}] using the default constructor", name);
					
					result = hasArguments ? Optional.fromNullable((T) handler.withoutArgs()) : Optional.<T> absent();
					
					LOG.info("Extension [{}] created using the default constructor", name);
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
			this.name = isNullOrEmpty(name) ? clazz.getSimpleName() : name.trim();
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
