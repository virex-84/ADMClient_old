package com.virex.admclient;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
/**
 * VirEx-84.narod.ru
 * 
 * добавил класс на будущее, пока не реализовано
 */
public class Find extends ListActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	Intent intent = getIntent();
	
	if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	    //Берем строку запроса из экстры
	    //String query = intent.getStringExtra(SearchManager.QUERY);
	    //Выполняем поиск
	}
    }
}
