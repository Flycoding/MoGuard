package com.flyingh.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class GroupAdapter<K, V extends Collection<E>, E extends Comparable<? super E>> extends BaseAdapter {
	private static final String TAG = "GroupAdapter";
	private Context context;
	private Transformer<E, K> transformer;
	private boolean sortKey;
	private Comparator<K> keyComparator;
	private boolean sortValues;
	private Comparator<E> valuesComparator;

	private final List<Object> list = new ArrayList<>();
	private final Set<Integer> keyPositions = new HashSet<>();

	public GroupAdapter(Context context, Transformer<E, K> transformer, V v) {
		this(context, transformer, true, null, true, null, v);
	}

	public GroupAdapter(Context context, Transformer<E, K> transformer, boolean sortKey, Comparator<K> keyComparator, boolean sortValues,
			Comparator<E> valuesComparator, V v) {
		super();
		this.context = context;
		this.transformer = transformer;
		this.sortKey = sortKey;
		this.keyComparator = keyComparator;
		this.sortValues = sortValues;
		this.valuesComparator = valuesComparator;
		initData(v);
	}

	private void initData(V v) {
		if (v == null) {
			return;
		}
		Map<K, V> map = group(v);
		for (Map.Entry<K, V> me : map.entrySet()) {
			keyPositions.add(list.size());
			list.add(me.getKey());
			list.addAll(sortValues ? sortValues(me.getValue()) : me.getValue());
		}
	}

	private List<E> sortValues(V value) {
		List<E> list = new ArrayList<>(value);
		Collections.sort(list, valuesComparator);
		return list;
	}

	@SuppressWarnings("unchecked")
	private Map<K, V> group(V v) {
		Map<K, V> map = sortKey ? new TreeMap<K, V>(keyComparator) : new LinkedHashMap<K, V>();
		for (E e : v) {
			K k = transformer.transform(e);
			if (map.get(k) == null) {
				try {
					map.put(k, (V) v.getClass().newInstance());
				} catch (Exception e1) {
					Log.i(TAG, e1.getMessage());
					throw new RuntimeException(e1);
				}
			}
			map.get(k).add(e);
		}
		return map;
	}

	public interface Transformer<E, K> {
		K transform(E e);
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		return isKeyPosition(position) ? 0 : 1;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressWarnings("unchecked")
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {
			view = isKeyPosition(position) ? newKeyView(position, context, (K) getItem(position), parent) : newValueView(position, context,
					(E) getItem(position), parent);
		} else {
			view = convertView;
		}
		if (isKeyPosition(position)) {
			bindKeyView(view, position, context, (K) getItem(position));
		} else {
			bindValueView(view, position, context, (E) getItem(position));
		}
		return view;
	}

	public void changeData(V v) {
		reset();
		initData(v);
		notifyDataSetChanged();
	}

	public void clear() {
		reset();
		notifyDataSetChanged();
	}

	private void reset() {
		list.clear();
		keyPositions.clear();
	}

	public boolean isKeyPosition(int position) {
		return keyPositions.contains(position);
	}

	public abstract View newKeyView(int position, Context context, K item, ViewGroup parent);

	public abstract void bindKeyView(View view, int position, Context context, K k);

	public abstract View newValueView(int position, Context context, E item, ViewGroup parent);

	public abstract void bindValueView(View view, int position, Context context, E e);

}
