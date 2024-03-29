package com.virex.admclient;

import com.virex.admclient.PostDialog.PublicDialogListener;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;

/**
 * VirEx-84.narod.ru
 */

public class Topics extends ListActivity {

    private static final int IDM_ADDTOPIC = 100;
    private static final int IDM_OPTIONS = 101;
    private static final int IDM_REFRESH = 102;
    private static final int IDM_BACK = 103;

    private static final int IDD_PROGRESS_DOWNLOAD = 0;
    // private static final int IDD_PROGRESS_FIND = 1;

    // Boolean NeedUpdate = false;

    public final String urlListTopics = "http://www.delphimaster.ru/cgi-bin/client.pl?getnew=%s&n=%s";
    public final String urlPostTopic = "http://www.delphimaster.ru/cgi-bin/forum.pl";
    public Handler handler;
    public TopicsListAdapter dd;
    DB db;
    HttpConnection httpconnection;
    private String ForumId;
    String url;
    ProgressDialog progressDialog_download;
    // ProgressDialog progressDialog_find;

    String SelectTopicId = "-1";

    public void reload() {
	if (db.GetTopicsCount(ForumId) == 0) {
	    dd.clear();
	    dd.notifyDataSetChanged();// fix
	}

	dd.SetAllOld();// помечаем все топики как "устаревшие"
	// dd.clear();
	dd.notifyDataSetChanged();

	// db.LoadTopics(ForumId);
	url = String.format(urlListTopics, db.GetTopicsLastMod(ForumId), ForumId);
	httpconnection.gettopics(url);

	/*
	 * // первые 10 символов - unix формат даты // url =
	 * url.replace("[i]",String.valueOf(System.currentTimeMillis() - // (100
	 * * 60 * 60 * 1000)).substring(0, 10)); int hour = 10; try { hour =
	 * PreferenceManager.getDefaultSharedPreferences(this).getInt(
	 * getString(R.string.pr_signature), 10); } catch (Exception e) { } url
	 * = url.replace("[i]",String.valueOf( System.currentTimeMillis() -
	 * (hour * 60 * 60 * 1000)).substring(0, 10));
	 */
	// получаем дату последнего топика
	Log.v("reload", url);

    }

    @Override
    protected Dialog onCreateDialog(int id) {
	switch (id) {
	case IDD_PROGRESS_DOWNLOAD:
	    progressDialog_download = new ProgressDialog(Topics.this);
	    progressDialog_download.setCancelable(true);
	    progressDialog_download.setOnCancelListener(new OnCancelListener() {
		public void onCancel(DialogInterface paramDialogInterface) {
		    db.Break();
		    httpconnection.Break();
		}
	    });
	    progressDialog_download.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	    progressDialog_download.setMessage("...");
	    return progressDialog_download;
	    /*
	     * case IDD_PROGRESS_FIND: progressDialog_find = new
	     * ProgressDialog(Topics.this);
	     * progressDialog_find.setCancelable(true);
	     * progressDialog_find.setOnCancelListener(new OnCancelListener() {
	     * public void onCancel(DialogInterface paramDialogInterface) {
	     * 
	     * } });
	     * progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	     * progressDialog_find.setMessage("Поиск..."); return
	     * progressDialog_find;
	     */
	default:
	    return null;
	}
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setTitle(getIntent().getExtras().getString("Title"));
	handler = new Handler() {
	    Bundle values;
	    int pid_download = 0;
	    
	    public void handleMessage(Message message) {
		switch (message.what) {

		case DB.DB_ONSTART:
		    pid_download = 0;
		    showDialog(IDD_PROGRESS_DOWNLOAD);
		    progressDialog_download.setMessage(getString(R.string.db_download));
		    values = message.getData();
		    progressDialog_download.setMax(values.getInt("max"));
		    break;

		case DB.DB_ONLINE:
		    values = message.getData();
		    if (values.size() < 1)
			break;

		    /*
		     * //чтобы не перегружать заново из базы if
		     * (!dd.find(values.getString("title"))){//<--- большая
		     * нагрузка
		     * dd.add(values.getString("title"),values.getString("dsc"),
		     * values.getString("id")); dd.notifyDataSetChanged(); }
		     */
		    
		    dd.add(values.getString("title"), values.getString("dsc"), values.getString("id"), values.getString("name"), values.getString("count"), false, values.getString("lastmod"));
		    dd.notifyDataSetChanged();
		    progressDialog_download.setProgress(pid_download);
		    pid_download++;
		    break;
		case DB.DB_ONSTOP:
		    /*
		     * if (NeedUpdate) { try {
		     * dismissDialog(IDD_PROGRESS_DOWNLOAD); } catch (Exception
		     * e1) {
		     * 
		     * } NeedUpdate = false;
		     * getListView().setSelection(dd.findPosition
		     * (SelectTopicId)); break; }
		     */
		    // из базы загрузили, теперь грузим из инета
		    // httpconnection.gettopics(url);
		    reload();
		    break;

		// пошло соединение
		case HttpConnection.GET_START:
		    showDialog(IDD_PROGRESS_DOWNLOAD);
		    progressDialog_download.setMessage(getString(R.string.http_download));
		    values = message.getData();
		    if (values.size() < 1)
			break;
		    String max = values.getString("max");
		    if (max.length() > 0) {
			try {
			    progressDialog_download.setMax(pid_download+Integer.valueOf(max));
			    break;// выходим, дальше обрабатывать строку не
				  // нужно
			} catch (Exception e) {
			    // progressDialog_download.setMax(100);
			}

		    }
		    ;
		    break;

		// строка скачана
		// добавляем в массив
		case HttpConnection.GET_LINEREAD:
		    values = message.getData();

		    String title = values.getString("title");
		    String dsc = values.getString("dsc");
		    String id = values.getString("id");
		    String lastmod = values.getString("lastmod");

		    String name = values.getString("name");
		    String count = values.getString("count");

		    // добавляем в базу
		    if (db.AddTopic(ForumId, id, name, title, "", "", count, dsc, "", lastmod, "", "")) {
			dd.add(title, dsc, id, name, count, true,lastmod);
			//dd.notifyDataSetChanged();
		    } else {
			// если не удалось добавить значит запись уже есть и ее
			// нужно обновить
			// обновляем топик в базе
			db.UpdateTopic(ForumId, id, lastmod, count);
			// поднимаем запись вверх в списке
			// dd.upitem(id);//upitem не подходит т.к. список веток
			// грузится "перевернутым" - сначала приходит самая
			// "свежая" ветка, затем постарше и т.д.
			//dd.insert(pid_insert, title, dsc, id, name, count);
			dd.update(title, dsc, id, name, count, true, lastmod);
			//dd.notifyDataSetChanged();
			// NeedUpdate = true;
		    }
		    progressDialog_download.setProgress(pid_download);
		    pid_download++;
		    break;

		//
		case HttpConnection.GET_ERROR:
		    Exception e = (Exception) message.obj;
		    Toast.makeText(getApplicationContext(), "Ошибка подключения:\n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

		    // если вобще ничего не загружено, спрашиваем о перезагрузке
		    if (dd.getCount() < 1) {
			new ReloadDialog(getListView().getContext(), ReloadDialog.mReGet, httpconnection).show();
		    }
		    ;
		case HttpConnection.GET_SUCCEED:
		    try {
			dismissDialog(IDD_PROGRESS_DOWNLOAD);
		    } catch (Exception e1) {

		    }
		    getListView().setSelection(dd.findPosition(SelectTopicId));
		    /*
		     * if (NeedUpdate) { dd.clear(); reload(); }
		     */
		    break;
		case HttpConnection.POST_ERROR:
		    // ошибка добавления нового поста, выводим диалог
		    new ReloadDialog(getListView().getContext(), ReloadDialog.mRePost, httpconnection).show();
		    break;
		case HttpConnection.POST_SUCCEED:
		    // запостили успешно, обновляем
		    reload();
		    break;
		}// end switch
	    }
	};

	dd = new TopicsListAdapter(getListView().getContext());
	db = new DB(getListView().getContext(), handler, Topics.this);
	getListView().setAdapter(dd);
	httpconnection = new HttpConnection(handler);
	ForumId = getIntent().getExtras().getString("ForumId");

	db.LoadTopics(ForumId);
    }

    public void onListItemClick(ListView parent, View v, int position, long id) {
	Intent intent = new Intent();
	intent.setClass(this, Pages.class);
	intent.putExtra("ForumId", getIntent().getExtras().getString("ForumId").trim());
	intent.putExtra("TopicId", dd.get(position).id.trim());
	intent.putExtra("Title", dd.get(position).title.trim());// передаем
	// заголовок
	// темы

	SelectTopicId = dd.get(position).id.trim();
	startActivityForResult(intent, 0);
    }

    // Pages закрыли, перезагружаем топики
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	super.onActivityResult(requestCode, resultCode, data);
	reload();
    }

    @Override
    public void onBackPressed() {
	db.Break();// прерываем загрузку
	httpconnection.Break();// прерываем загрузку
	super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	menu.add(Menu.NONE, IDM_ADDTOPIC, Menu.NONE, "Добавить");
	menu.add(Menu.NONE, IDM_OPTIONS, Menu.NONE, "Настройки");
	menu.add(Menu.NONE, IDM_REFRESH, Menu.NONE, "Обновить");
	menu.add(Menu.NONE, IDM_BACK, Menu.NONE, "Назад");
	return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
	case IDM_ADDTOPIC:

	    PostDialog postdialogtopic = new PostDialog(this, "Тема", "", new PublicDialogListener() {

		@Override
		public void onOkClick(String title, String text) {
		    SharedPreferences options = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		    Bundle data = new Bundle();
		    data.putString("n", ForumId);
		    data.putString("name", options.getString(getString(R.string.pr_login), ""));
		    data.putString("topsw", options.getString(getString(R.string.pr_password), ""));
		    data.putString("email", options.getString(getString(R.string.pr_email), ""));
		    data.putString("signature", options.getString(getString(R.string.pr_signature), ""));
		    data.putString("title", title);
		    data.putString("text", text);

		    httpconnection.addtopic(urlPostTopic, data);
		}

		@Override
		public void onCancelClick() {
		    // TODO Auto-generated method stub

		}
	    });
	    postdialogtopic.show();
	    break;
	case IDM_OPTIONS:
	    // окно настроек
	    Intent intent = new Intent();
	    intent.setClass(getBaseContext(), Options.class);
	    startActivity(intent);
	    break;
	case IDM_REFRESH:
	    reload();
	    break;
	case IDM_BACK:
	    finish();
	    break;
	}
	return true;
    }
}