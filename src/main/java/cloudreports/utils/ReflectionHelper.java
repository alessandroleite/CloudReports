package cloudreports.utils;

import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.scanners.TypeElementsScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;


public final class ReflectionHelper 
{
	private static final Reflections REFLECTIONS;

	static 
	{
		REFLECTIONS = new Reflections(new ConfigurationBuilder()
		        .addUrls(ClasspathHelper.forJavaClassPath())
				.addScanners(new TypeElementsScanner(), 
						     new TypeAnnotationsScanner(),
						     new SubTypesScanner(), 
						     new TypeElementsScanner()));
	}

	private ReflectionHelper() 
	{
		throw new UnsupportedOperationException();
	}
	
	
	public static Reflections getReflections()
	{
		return REFLECTIONS;
	}
	
	/**
     * Returns all sub types in hierarchy of a given type.
     */
    public static <T> Set<Class<? extends T>> getSubTypesOf(final Class<T> type) 
    {
        return REFLECTIONS.getSubTypesOf(type);
    }
}
