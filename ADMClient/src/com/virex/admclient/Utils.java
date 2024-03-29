package com.virex.admclient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * VirEx-84.narod.ru
 */
class Item {
    String title;
    String dsc;
    String id;
    String name;
    String count;
    // флаг для полной прорисовки (для списка веток)
    Boolean ext = false;
    // новая ли ветка
    Boolean newer = false;
    //для сортировки
    String lastmod;
}

/**
 * FastList класс для быстрого поиска нужного итема через indexOf: каждая
 * позиция элемента привязывается к хэшу строки по которой нужно быстро найти
 * элемент (id) --- если же организовать поиск нужного элемента в цикле,
 * построчно сравнивая id - нагрузка на телефон будет очень большой
 */
class FastList extends ArrayList<Item> {

    HashMap<String, Integer> items = new HashMap<String, Integer>();

    private void rehash() {
	items.clear();
	int i = 0;
	while (i < size()) {
	    items.put(get(i).id, i);
	    i++;
	}
    }

    @Override
    public boolean add(Item object) {
	Boolean result = super.add(object);
	if (result) {
	    items.put(object.id, size() - 1);// запоминаем хэш-код
							// строки (id) и
							// порядковый номер
							// элемента
	    //Log.v("add id=" + object.id, String.valueOf(size() - 1));
	}
	return result;
    }

    public void update(Item object) {
	object.newer=true;
	try{
	this.set(indexOf(object.id), object);
	}catch (ArrayIndexOutOfBoundsException e){
	    return;
	}
	sort();
    }
    
    public void sort(){
	Comparator<Item> comparator = new Comparator<Item>() {

	    @Override
	    public int compare(Item arg0, Item arg1) {
		return arg1.lastmod.compareToIgnoreCase(arg0.lastmod);
	    }
	};
	Collections.sort(this, comparator);
	rehash();
    }
 

    @Override
    public void clear() {
	items.clear();
	super.clear();
    }

    public int indexOf(String id) {
	try {
	    return items.get(id);// особая стрит мэджик
	} catch (Exception e) {
	    return -1;
	}
    }

    @Override
    public Item remove(int index) {
	Item result = super.remove(index);
	if (result != null)
	    items.remove(result.id);
	return result;
    }

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

}

class TopicsListAdapter extends BaseAdapter {
    private Context mContext;
    FastList items = new FastList();

    public void SetAllOld() {
	int i = 0;
	while (i < items.size()) {
	    items.get(i).newer = false;
	    i++;
	}
    }

    // добавление топика
    public void add(String title, String dsc, String id, String name, String count, Boolean isNewer, String lastmod) {
	Item t = new Item();
	t.dsc = dsc;
	t.title = title;
	t.id = id;
	t.name = name;
	t.count = count;
	t.ext = true;
	t.newer = isNewer;
	t.lastmod=lastmod;
	items.add(t);
	notifyDataSetChanged();
    }

    // добавление форума
    public void add(String title, String dsc, String id) {
	Item t = new Item();
	t.dsc = dsc;
	t.title = title;
	t.id = id;
	t.ext = false;// на всякий случай
	items.add(t);
	notifyDataSetChanged();
    }
    
    public void update(String title, String dsc, String id, String name, String count, Boolean isNewer, String lastmod) {
	Item t = new Item();
	t.dsc = dsc;
	t.title = title;
	t.id = id;
	t.name = name;
	t.count = count;
	t.ext = true;
	t.newer = isNewer;
	t.lastmod=lastmod;
	items.update(t);
	notifyDataSetChanged();
    }

    public TopicsListAdapter(Context context) {
	mContext = context;
    }

    @Override
    public int getCount() {
	return items.size();
    }

    public void clear() {
	items.clear();
    }

    public Item get(int position) {
	return items.get(position);
    }

    public int findPosition(String id) {
	return items.indexOf(id);
    }

    // отрисовка элементов в списке
    // рисуем заголовок и описание
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
	final int tvTitle = 1;
	final int tvDsc = 2;
	final int tvName = 3;
	final int tvCount = 4;
	LinearLayout lv;
	LinearLayout infolv;
	TextView tv;
	if (convertView == null) {
	    // создаем объекты
	    lv = new LinearLayout(mContext);
	    lv.setOrientation(LinearLayout.VERTICAL);

	    tv = new TextView(mContext);
	    tv.setId(tvTitle);// присваиваем идентификатор
	    tv.setTextSize(23);
	    tv.setTextColor(Color.WHITE);
	    if (get(position).newer) {
		tv.setText("(!)" + get(position).title);
	    } else {
		tv.setText(get(position).title);
	    }
	    lv.addView(tv, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

	    tv = new TextView(mContext);
	    tv.setId(tvDsc);
	    tv.setTextSize(15);
	    tv.setText(get(position).dsc);
	    lv.addView(tv, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

	    if (get(position).ext) {
		infolv = new LinearLayout(mContext);
		infolv.setOrientation(LinearLayout.HORIZONTAL);

		tv = new TextView(mContext);
		tv.setId(tvName); 
		tv.setTextSize(15);
		tv.setTypeface(null, Typeface.BOLD);
		tv.setText("Автор: " + get(position).name);
		tv.setGravity(Gravity.LEFT);
		infolv.addView(tv, new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

		tv = new TextView(mContext);
		tv.setId(tvCount);
		tv.setTextSize(15);
		tv.setTypeface(null, Typeface.BOLD);
		tv.setText(get(position).count);
		tv.setGravity(Gravity.RIGHT);
		infolv.addView(tv, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		lv.addView(infolv, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
	    }
	} else {
	    // получаем уже созданные объекты
	    // и обязательно сэтим
	    lv = (LinearLayout) convertView;
	    tv = (TextView) lv.findViewById(tvTitle);// получаем по
						     // идентификатору
	    tv.setTextSize(23);
	    tv.setTextColor(Color.WHITE);
	    if (get(position).newer) {
		tv.setText("(!)" + get(position).title);
	    } else {
		tv.setText(get(position).title);
	    }

	    tv = (TextView) lv.findViewById(tvDsc);
	    tv.setTextSize(15);
	    tv.setText(get(position).dsc);

	    if (get(position).ext) {
		tv = (TextView) lv.findViewById(tvName);
		tv.setTextSize(15);
		tv.setTypeface(null, Typeface.BOLD);
		tv.setText("Автор: " + get(position).name);

		tv = (TextView) lv.findViewById(tvCount);
		tv.setTextSize(15);
		tv.setTypeface(null, Typeface.BOLD);
		tv.setText(get(position).count);
	    }

	}
	return lv;

    }

    public Object getItem(int position) {
	return null;
    }

    public long getItemId(int position) {
	return position;
    }

}