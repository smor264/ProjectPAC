package application;

import java.util.ArrayList;
/*
 * SetArrayList is an ArrayList that:
 * 1) checks for uniqueness of elements before adding.
 * 2) can only append to the end of the list.
 */

public class SetArrayList<T> {
	ArrayList<T> array;

	public SetArrayList()  {
		array = new ArrayList<T>();
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
	public T getNFromTop(int n) {
		if (array.size() == 0) {
			return null;
		}
		else {
			return array.get(array.size() - 1 - n);
		}
	}

	public int size() {
		return array.size();
	}

	public boolean isEmpty() {
		return array.isEmpty();
	}
}
