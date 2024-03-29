package com.virex.admclient;

import android.app.ListActivity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.content.Intent;
/**
 * VirEx-84.narod.ru
 */

public class Forums extends ListActivity {
	@Override
	protected void onStop() {
		//ConnectionManager.getInstance().
		super.onStop();
	}

	private static final int IDM_OPTIONS = 100;
	private static final int IDM_REFRESH = 101;
	private static final int IDM_EXIT = 102;	
	
	String urlListForums = "http://www.delphimaster.ru/cgi-bin/client.pl?getforums=1";
	Handler handler;
	HttpConnection httpconnection;
	TopicsListAdapter dd;
	DB db; 
	ReloadDialog reloaddialog;

	public void reload(){
		dd.clear();
		dd.notifyDataSetChanged();
		db.LoadForums();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Форумы");
		
		handler = new Handler() {
			Bundle values;
			public void handleMessage(Message message) {

				switch (message.what) {

				case DB.DB_ONSTART:
					break;
				case DB.DB_ONLINE:
					values = message.getData();
					dd.add(values.getString("title"), values.getString("dsc"), values.getString("n"));
					dd.notifyDataSetChanged();
					break;
				case DB.DB_ONSTOP:
					Log.v("DB_ONSTOP", "");
					httpconnection.getforums(urlListForums);
					break;					
				
				// пошло соединение
				case HttpConnection.GET_START:
					break;

				// строка скачана
				// добавляем в массив
				case HttpConnection.GET_LINEREAD: 
					values = message.getData();
					if (values.size()<1) break;
					String title=values.getString("title");
					String dsc=values.getString("dsc");
					String n=values.getString("id");
					//добавляем в базу
					if (db.AddForum(n,title,dsc)){
						dd.add("(!)"+title, dsc, n);
						dd.notifyDataSetChanged();
					};
					break;

				case HttpConnection.GET_ERROR:
					// запрашиваем "хотите обновить?"
					if (dd.getCount()<1){
						reloaddialog.show();
					};
					break;
				}// end switch
			}
		};

		dd = new TopicsListAdapter(getListView().getContext());
		getListView().setAdapter(dd);
		db = new DB(getListView().getContext(),handler,Forums.this);
		httpconnection=new HttpConnection(handler);
		reloaddialog=new ReloadDialog(this,ReloadDialog.mReGet, httpconnection);
		
		//db.Clear();
		reload();
	}

	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(this, Topics.class);
		intent.putExtra("ForumId",dd.get(position).id.trim());
		intent.putExtra("Title", dd.get(position).title.trim());//передаем заголовок форума
		startActivity(intent);

	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, IDM_OPTIONS, Menu.NONE, "Настройки");		
		menu.add(Menu.NONE, IDM_REFRESH, Menu.NONE, "Обновить");
		menu.add(Menu.NONE, IDM_EXIT, Menu.NONE, "Выход");
		return (super.onCreateOptionsMenu(menu));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case IDM_OPTIONS: 
			//окно настроек
			Intent intent = new Intent(); 
			intent.setClass(getBaseContext(), Options.class); 
			startActivity(intent); 
			break;		
		case IDM_REFRESH:
			reload();
			break;
		case IDM_EXIT:
			finish();
			break;
		}
		return true;
	}	

}
