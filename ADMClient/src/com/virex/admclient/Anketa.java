package com.virex.admclient;

import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;

/**
 * VirEx-84.narod.ru
 */

public class Anketa extends Activity {
    private static final int NONE = 0;
    private static final int LINK = 1;
    private static final int MAIL = 2;
    private static final int ICQ  = 3;
    private static final int LOGIN  = 4;

    WebView anketa;
    HttpConnection httpconnection;
    ReloadDialog reloaddialog;
    public ArrayList<String> data = new ArrayList<String>();
    //DB db;

    //
    private String formatinfo(String name, String param, int what) {
	switch (what) {
	case NONE:
	    return String.format("<b>%s:</b> %s<br>", name, param);
	case LINK:
	    return String.format("<b>%s:</b> <a href='%s'>%s</a><br>", name, param,param);
	case MAIL:
	    return String.format("<b>%s:</b> <a href='mailto:%s'>%s</a><br>", name, param,param);
	case ICQ:
	    return String.format("<b>%s:</b> <a href='http://www.icq.com/%s'>%s</a><br>", name, param,param);
	case LOGIN:
	    return String.format("<b>%s:</b> <font color='red'><b>%s</b></font><br>", name, param);	    
	}// end switch
	return "";
    }

    private String formatinfo(String name, String param) {
	return formatinfo(name, param, NONE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setTitle("Анкета");
	anketa = new WebView(this);
	setContentView(anketa);
	final Handler handler = new Handler() {
	    Bundle values;

	    public void handleMessage(Message message) {
		switch (message.what) {
		case HttpConnection.GET_START:
		    data.clear();
		    break;
		case HttpConnection.GET_LINEREAD:
		    values = message.getData();
		    data.add(formatinfo("Дата обновления анкеты:", values.getString("date")));
		    data.add(formatinfo("Логин", values.getString("login"),LOGIN));
		    data.add(formatinfo("E-mail", values.getString("email"),MAIL));
		    data.add(formatinfo("ICQ", values.getString("icq"),ICQ));
		    data.add(formatinfo("Сайт", values.getString("homepage"),LINK));
		    data.add(formatinfo("Реальное имя", values.getString("name")));
		    data.add(formatinfo("Дата рождения", values.getString("day")));
		    data.add(formatinfo("Город", values.getString("city")));
		    data.add(formatinfo("Пол", values.getString("sex")));
		    data.add(formatinfo("Образование", values.getString("education")));
		    data.add(formatinfo("Увлечения / хобби", values.getString("hobby")));
		    data.add(formatinfo("Интересное о себе", values.getString("about")));
		    break;
		case HttpConnection.GET_ERROR:
		    values = message.getData();
		    
		    if (values == null) {// если ошибка подключения
			reloaddialog.show();
			break;
		    }
		    ;
		    data.add(formatinfo("Ошибка", values.getString("error")));
		case HttpConnection.GET_SUCCEED:
		    // финт ушами
		    String c = "";
		    String ln = "";
		    int i = 0;
		    while (i < data.size()) {
			ln = data.get(i);
			// заменяем некоторые символы для корректного
			// отображения
			// ln = ln.replaceAll("%", "&permil;");
			// ln = ln.replaceAll("\"", "&quot;");
			c += ln;
			i += 1;
		    }
		    c = String.format("<html><meta http-equiv='Content-Type' content='text/html'; charset='utf-8' /><body bgcolor='#f2f0f0'> %s </body></html>", c);
		    anketa.loadData(c, "text/html", "utf-8");
		    break;
		}// end switch
	    }
	};
	httpconnection = new HttpConnection(handler);
	reloaddialog = new ReloadDialog(this, ReloadDialog.mReGet, httpconnection);
	//db = new DB(getBaseContext(), handler, Anketa.this);
	//db.LoadAnketa(getIntent().getExtras().getString("AnketaId"));
	httpconnection.getan