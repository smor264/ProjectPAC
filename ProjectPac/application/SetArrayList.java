package application;

import java.util.ArrayList;
/*
 * SetArrayList is an ArrayList that:
 * 1) checks for uniqueness of elements.
 * 2) can only append to the end of the list.
 */
public class SetArrayList<T> {
	ArrayList<T> array = new ArrayList<T>();
	
	public SetArrayList()  {
		// TODO Auto-generated constructor stub
	}
	public boolean append(T element) {
		if (array.contains(element)) {
			return false;
		}
		else {
			return array.add(element);
		}
	}
	public void remove(T element) {
		int index = array.indexOf(element);
		if (index >= 0) {
			array.remove(index);
		}
	}
	public T getTop() {
		if (array.size() == 0) {
			return null;
		}
		else {
			return array.get(array.size() - 1);
		}
	}
	public int size() {
		return array.size();
	}
}
