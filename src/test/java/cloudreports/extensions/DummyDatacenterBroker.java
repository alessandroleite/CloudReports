package cloudreports.extensions;

import java.util.List;

import cloudreports.extensions.brokers.Broker;

@Extension(name = DummyDatacenterBroker.NAME)
public class DummyDatacenterBroker extends Broker 
{
	public static final String NAME = "Dummy broker";
	
	public DummyDatacenterBroker(String name) throws Exception 
	{
		super(name);
	}

	@Override
	public int getDatacenterId() 
	{
		return getId();
	}

	@Override
	public List<Integer> getDatacenterIdList() 
	{
		return super.getDatacenterIdsList();
	}
}
