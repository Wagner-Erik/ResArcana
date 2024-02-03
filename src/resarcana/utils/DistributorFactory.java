package resarcana.utils;

import java.util.ArrayList;

public class DistributorFactory {

	private DistributorFactory() {
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends Object> Distributor<T> getModuloDistributor(ArrayList<T> objects) {
		int size = objects.size();
		ArrayList<T> list = (ArrayList<T>) objects.clone();
		return new Distributor<T>() {

			@Override
			public T getObject(int index) {
				return list.get(index % size);
			}
			
		};
	}
}
