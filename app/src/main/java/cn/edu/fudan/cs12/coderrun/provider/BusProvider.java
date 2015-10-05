package cn.edu.fudan.cs12.coderrun.provider;

import de.halfbit.tinybus.TinyBus;
/**
 * Created by Li on 2015/10/2.
 */
public class BusProvider {
	private final static TinyBus ourInstance = new TinyBus();

	private BusProvider() {
	}

	public static TinyBus getInstance() {
		return ourInstance;
	}
}
