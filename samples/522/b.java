import javax.management.AttributeNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServerDelegate;

class MBeanServerDelegateImpl extends MBeanServerDelegate implements DynamicMBean, MBeanRegistration {
    /**
     * Obtains the value of a specific attribute of the MBeanServerDelegate.
     *
     * @param attribute The name of the attribute to be retrieved
     *
     * @return  The value of the attribute retrieved.
     *
     * @exception AttributeNotFoundException
     * @exception MBeanException
     *            Wraps a &lt;CODE&gt;java.lang.Exception&lt;/CODE&gt; thrown by the
     *            MBean's getter.
     */
    public Object getAttribute(String attribute)
	    throws AttributeNotFoundException, MBeanException, ReflectionException {
	try {
	    // attribute must not be null
	    //
	    if (attribute == null)
		throw new AttributeNotFoundException("null");

	    // Extract the requested attribute from file
	    //
	    if (attribute.equals("MBeanServerId"))
		return getMBeanServerId();
	    else if (attribute.equals("SpecificationName"))
		return getSpecificationName();
	    else if (attribute.equals("SpecificationVersion"))
		return getSpecificationVersion();
	    else if (attribute.equals("SpecificationVendor"))
		return getSpecificationVendor();
	    else if (attribute.equals("ImplementationName"))
		return getImplementationName();
	    else if (attribute.equals("ImplementationVersion"))
		return getImplementationVersion();
	    else if (attribute.equals("ImplementationVendor"))
		return getImplementationVendor();

	    // Unknown attribute
	    //
	    else
		throw new AttributeNotFoundException("null");

	} catch (AttributeNotFoundException x) {
	    throw x;
	} catch (JMRuntimeException j) {
	    throw j;
	} catch (SecurityException s) {
	    throw s;
	} catch (Exception x) {
	    throw new MBeanException(x, "Failed to get " + attribute);
	}
    }

}

