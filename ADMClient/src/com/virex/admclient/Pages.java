package com.virex.admclient;

import java.util.ArrayList;

import com.virex.admclient.PostDialog.PublicDialogListener;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * VirEx-84.narod.ru
 */
public class Pages extends Activity {

    private static final int IDM_OPTIONS = 100;
    private static final int IDM_REFRESH = 101;
    private static final int IDM_BACK = 102;

    private static final int IDD_PROGRESS_DOWNLOAD = 0;
    // private static final int IDD_PROGRESS_FIND = 1;

    public final String urlListTopic = "http://www.delphimaster.ru/cgi-bin/client.pl?getconf=%s&n=%s&from=%s&to=%s";
    public final String urlPostTopic = "http://www.delphimaster.ru/cgi-bin/forum.pl";

    public ArrayList<String> data = new ArrayList<String>();
    HttpConnection httpconnection;
    DB db;
    WebView topic;
    public String ForumId;
    public String TopicId;
    String urllist;
    public SharedPreferences options;
    ProgressDialog progressDialog_download;
    private Handler handler;

    // ProgressDialog progressDialog_find;

    // класс для обработки java вызовов
    // из java скрипта страницы
    class Poster {
	Context context;

	Poster(Context context) {
	    this.context = context;
	}

	// пользователь нажал на кнопку "цитата" или "ответ"
	// формируем ответ и выводим в окне диалога
	public void reply(final int method, final String html) {
	    String data = "<i>&gt;";
	    switch (method) {
	    // ответ - только первая строка
	    case 0:
		// оставляем до первого тега абзаца
		data += html.substring(0, html.indexOf("\n"));
		break;
	    // цитата - весь пост
	    case 1:
		data += html.trim().replaceAll("\n", "\n&gt; ");
		break;
	    }
	    data += "</i>\n\n";

	    PostDialog postdialogtopic = new PostDialog(context, "", data, new PublicDialogListener() {

		@Override
		public void onOkClick(String title, String text) {

		    Bundle data = new Bundle();
		    data.putString("n", ForumId);
		    data.putString("id", TopicId);
		    data.putString("name", options.getString(getString(R.string.pr_login), ""));
		    data.putString("topsw", options.getString(getString(R.string.pr_password), ""));
		    data.putString("email", options.getString(getString(R.string.pr_email), ""));
		    data.putString("signature", options.getString(getString(R.string.pr_signature), ""));
		    data.putString("text", text);

		    httpconnection.addpost(urlPostTopic, data);
		    finishActivity(0);
		}

		@Override
		public void onCancelClick() {
		    Log.v("cancelpost!!!!", "-");
		}

	    });
	    postdialogtopic.show();
	}
    }

    public void reload() {
	data.clear();
	db.LoadPages(ForumId, TopicId);
	urllist = String.format(urlListTopic, TopicId, ForumId, db.GetPagesCount(ForumId, TopicId), "-1");
    }

    @Override
    protected Dialog onCreateDialog(int id) {
	switch (id) {
	case IDD_PROGRESS_DOWNLOAD:
	    progressDialog_download = new ProgressDialog(Pages.this);
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
	     * ProgressDialog(Pages.this);
	     * progressDialog_find.setCancelable(true);
	     * progressDialog_find.setOnCancelListener(new OnCancelListener() {
	     * public void onCancel(DialogInterface paramDialogInterface) {
	     * 
	     * } }); //
	     * progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	     * progressDialog_find.setMessage("Поиск..."); return
	     * progressDialog_find;
	     */
	default:
	    return null;
	}
    }

    // добавляем свою разметку для каждого поста
    public String formatPost(String content, int PostId, Boolean isNewPost) {
	final String btnotvet = "<input type='submit' value='%s' onClick='var id=document.getElementById(\"postid%d\"); window.Poster.reply(0,id.innerText);' />";
	final String btncitate = "<input type='submit' value='%s' onClick='var id=document.getElementById(\"postid%d\"); window.Poster.reply(1,id.innerText);' />";

	String res = ""; 
	if (isNewPost) { 
	    // выделяем новые посты белым фоном
	    res = "<div class='new' id='postid" + String.valueOf(PostId) + "'>";
	} else {
	    // пост загруженный из базы
	    res = "<div class='old' id='postid" + String.valueOf(PostId) + "'>";
	}
	;
	if (content != null)// fix при прерывании загрузки возможен пустой
			    // content
	    res += content.replaceFirst("<p>", " [" + String.valueOf(PostId) + "]<p>");// вставляем
	// номер
	// сообщения
	res += String.format(btnotvet, "ответить", PostId);
	res += String.format(btncitate, "цитировать", PostId);
	res += "</div>";
	res += "<hr>";
	return res;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);

	// опции
	options = PreferenceManager.getDefaultSharedPreferences(this);

	topic = new WebView(this);
	setContentView(topic);
	topic.setWebViewClient(new WebViewClient() {
	    @Override
	    public boolean shouldOverrideUrlLoading(WebView view, String url) {
		Log.v("shouldOverrideUrlLoading", url);
		if (url.contains("forum.pl")) {
		    // пример: same://nope/forum.pl?n=2&id=1213998361
		    String fid = url.substring(url.indexOf("n=") + "n=".length(), url.indexOf("&id"));
		    String tid = url.substring(url.indexOf("id=") + "id=".length());

		    // открываем в новом эктивити т.к. в текущем будет
		    // дозагрузка страницы, т.е. ненужная каша
		    Intent intent = new Intent();
		    intent.setClass(getBaseContext(), Pages.class);
		    intent.putExtra("ForumId", fid);
		    intent.putExtra("TopicId", tid);
		    // intent.putExtra("Title",
		    // "Перемещенное: "+db.GetTopicTitle(fid,tid));
		    intent.putExtra("Title", "Перемещенное: "+getTitle());
		    Log.v("get", intent.getExtras().toString());
		    startActivityForResult(intent, 0);
		    return true;
		}
		;
		if (url.contains("anketa.pl?id=")) {

		    String aid = url.substring(url.indexOf("id=") + "id=".length());
		    Intent intent = new Intent();
		    intent.setClass(getBaseContext(), Anketa.class);
		    intent.putExtra("AnketaId", aid);
		    startActivity(intent);
		    return true;

		}
		// для внешних ссылок
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(intent);
		return true;
	    }
	});

	handler = new Handler() {
	    Bundle values;
	    int pid_download = 0;

	    public void handleMessage(Message message) {

		switch (message.what) {

		case DB.DB_ONSTART:
		    pid_download = 0;// fix
		    showDialog(IDD_PROGRESS_DOWNLOAD);
		    progressDialog_download.setMessage(getString(R.string.db_download));
		    values = message.getData();
		    progressDialog_download.setMax(values.getInt("max"));
		    break;
		case DB.DB_ONLINE:
		    values = message.getData();
		    data.add(formatPost(values.getString("content"), pid_download, false));
		    progressDialog_download.setProgress(pid_download);
		    pid_download++;
		    break;
		case DB.DB_ONSTOP:
		    httpconnection.getpages(urllist);
		    break;

		// пошло соединение
		case HttpConnection.GET_START:
		    data.add("<a name='start_read' />"); // ставим якорь

		    progressDialog_download.setMessage(getString(R.string.http_download));
		    values = message.getData();
		    if (values.size() < 1)
			break;

		    String max = values.getString("max");
		    if (max.length() > 0) {
			try {
			    progressDialog_download.setMax(pid_download + Integer.valueOf(max));
			    break;// выходим, дальше обрабатывать строку не
				  // нужно
			} catch (Exception e) {
			    // progressDialog.setMax(100);
			}

		    }
		    ;

		    break;

		// строка скачана
		// добавляем в массив
		case HttpConnection.GET_LINEREAD:
		    values = message.getData();

		    // добавляем в базу
		    db.AddPage(ForumId, TopicId, values.getString("content"));

		    // переводим в utf-8 т.к. в строках видимо хранится в такой
		    // кодировке
		    // в topic.loadData(data.toString()) в кодировке
		    // windows-1251 загрузить не удалось
		    data.add(formatPost(values.getString("content"), pid_download, true).replace("windows-1251", "utf-8"));
		    progressDialog_download.setProgress(pid_download);
		    pid_download++;
		    break;

		// соединение успешно завершено
		// обновляем список
		// p/s/ даже если ошибка сети, всеравно грузим в webview
		case HttpConnection.GET_ERROR:
		    Exception e = (Exception) message.obj;
		    Toast.makeText(getApplicationContext(), "Ошибка подключения:\n" + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
		case HttpConnection.GET_SUCCEED:

		    // Toast.makeText(getApplicationContext(),
		    // R.string.http_end,Toast.LENGTH_SHORT).show();
		    progressDialog_download.setMessage(getString(R.string.format_download));
		    Thread t = new Thread(new Runnable() {
			public void run() {

			    // пока такой вот финт ушами
			    // как сделать ArrayList<String> data -> string?
			    String c = "";
			    String ln = "";
			    int i = 0;
			    while (i < data.size()) {
				ln = data.get(i);
				// заменяем некоторые символы для корректного
				// отображения
				ln = ln.replaceAll("%", "&permil;");
				ln = ln.replaceAll("\"", "&quot;");
				c += ln;
				i += 1;
			    }
			    //выбор темы (обычная и "ночная") см. ресурсы в папке assets
			    String maincss="";
			    if (options.getBoolean("black_theme", false)) {
				maincss="main_black.css";
			    } else {
				maincss="main.css";
			    }
			    c = String.format("<html><meta http-equiv='Content-Type' content='text/html'; charset='utf-8' /><link rel='stylesheet' type='text/css' href='%s' /><body onLoad='javascript:location=\"#start_read\"'> %s </body></html>", maincss,c);
			    // topic.loadData(c, "text/html", "utf-8");
			    // topic.loadData(String.format("<html><meta http-equiv='Content-Type' content='text/html'; charset='utf-8' /><body bgcolor=#f2f0f0> %s </body></html>",c),
			    // "text/html", "utf-8");

			    // loadData грузит только один раз, поэтому
			    // приходится применять loadDataWithBaseURL

			    topic.loadDataWithBaseURL(/* "same://nope" */"file:///android_asset/", c, "text/html", "utf-8", null);

			    try {// прячем в try на случай если загрузка
				 // прервана (диалог уже закрыт)
				dismissDialog(IDD_PROGRESS_DOWNLOAD);
			    } catch (Exception e) {
			    }
			    ;
			}
		    });
		    t.run();
		    break;
		case HttpConnection.POST_ERROR:
		    // ошибка добавления поста, переспрашиваем
		    new ReloadDialog(getBaseContext(), ReloadDialog.mRePost, httpconnection).show();
		    break;
		case HttpConnection.POST_SUCCEED:
		    // запостили успешно, обновляем
		    reload();
		    break;
		}// end switch

	    }
	};

	ForumId = getIntent().getExtras().getString("ForumId");
	TopicId = getIntent().getExtras().getString("TopicId");
	setTitle(getIntent().getExtras().getString("Title"));// заголовок

	topic.getSettings().setSaveFormData(false);// сохранение кэша/кукисов не
	// нужно
	topic.getSettings().setJavaScriptEnabled(true);
	topic.getSettings().setSupportZoom(true);
	topic.addJavascriptInterface(new Poster(this), "Poster");

	httpconnection = new HttpConnection(handler);
	db = new DB(getBaseContext(), handler, Pages.this);

	reload();
    }

    /*
     * @Override public void onBackPressed() { db.Break();// прерываем загрузку
     * httpconnection.Break();// прерываем загрузку finishActivity(0); //
     * finishActivity(getIntent().getExtras().getInt("TopicId"));
     * super.onBackPressed(); }
     */

    public boolean onKeyDown(int keyCode, KeyEvent event) {
	// поиск по WebView
	// к сожалению функция WebView.showFindDialog доступна начиная с API11
	// (андроид 3.0) поэтому приходится делать поиск вручную
	if ((keyCode == KeyEvent.KEYCODE_SEARCH)) {
	    final Dialog findDialog = new Dialog(topic.getContext());
	    findDialog.setTitle("Поиск");
	    // грузим готовый интерфейс диалога из ресурсов
	    findDialog.setContentView(R.layout.finddialog);

	    Button btnFind = (Button) findDialog.findViewById(R.id.btnFind);
	    Button btnCancel = (Button) findDialog.findViewById(R.id.btnCancel);
	    btnFind.setOnClickListener(new OnClickListener() {
		Boolean find_first = true;

		@Override
		public void onClick(View arg0) {
		    if (find_first) {
			EditText etFindText = (EditText) findDialog.findViewById(R.id.etFindText);
			topic.findAll(etFindText.getText().toString());
			find_first = false;
		    } else {
			topic.findNext(true);
		    }
		}
	    });
	    btnCancel.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View arg0) {
		    findDialog.dismiss();// закрываем поиск
		}
	    });
	    findDialog.show();
	}
	if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    // if (topic.canGoBack()) {// если можно вернутся - возвращаемся
	    // topic.goBack();
	    // return true;
	    // } else {

	    db.Break();// прерываем загрузку
	    httpconnection.Break();// прерываем загрузку
	    if (progressDialog_download.isShowing()) {
		// если идет загрузка - прерываем прогрессбар и показываем,
		// иначе - закрываем эктивити
		handler.sendMessage(Message.obtain(handler, HttpConnection.GET_ERROR, ""));
	    } else {
		finishActivity(0);
	    }
	}
	return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	menu.add(Menu.NONE, IDM_OPTIONS, Menu.NONE, "Настройки");
	menu.add(Menu.NONE, IDM_REFRESH, Menu.NONE, "Обновить");
	menu.add(Menu.NONE, IDM_BACK, Menu.NONE, "Назад");
	return (super.onCreateOptionsMenu(menu));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	switch (item.getItemId()) {
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
