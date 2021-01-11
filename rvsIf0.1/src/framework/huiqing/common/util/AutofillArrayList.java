package framework.huiqing.common.util;

import java.util.ArrayList;

public class AutofillArrayList<E> extends ArrayList<E> {

	private static final long serialVersionUID = 4089554511841286575L;

	private Class<E> itemClass;

	public AutofillArrayList(Class<E> itemClass) {
		this.itemClass = itemClass;
	}

	public E get(int index) {
		autoEnlarge(index);

		return super.get(index);
	}

	public E set(int index, E element) {
		autoEnlarge(index);

		return super.set(index, element);
	}

	private void autoEnlarge(int index){
		try {
			while (index >= size()) {
				add(itemClass.newInstance());
			}
		} catch (Exception e) {
		}
	}
}