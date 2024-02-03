package resarcana.utils;

public interface Distributor<T extends Object> {

	public T getObject(int index);
}
