package com.virex.admclient;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.conn.params.ConnManagerParams;

import android.os.*;

/**
 * Asynchronous HTTP connections
 * 
 * @author Greg Zavitz & Joseph Roth
 */
public class HttpConnection implements Runnable {

    public static final int GET_START = 0;
    public static final int GET_ERROR = 1;
    public static final int GET_SUCCEED = 2;
    public static final int GET_LINEREAD = 3;

    public static final int POST_START = 4;
    public static final int POST_ERROR = 5;
    public static final int POST_SUCCEED = 6;
    public static final int POST_LINEREAD = 7;

    private static final int GET_FORUMS = 0;
    private static final int GET_TOPICS = 1;
    private static final int GET_PAGES = 2;
    private static final int GET_ANKETA = 3;
    private static final int ADDTOPIC = 4;
    private static final int ADDPOST = 5;

    private String url;
    private int method;
    private Handler handler;
    private Bundle data;

    public int timeout;

    private DefaultHttpClient httpClient;

    public void Break() {
	try {
	    // закрываем подключение
	    Thread.currentThread().interrupt();
	    httpClient.getConnectionManager().shutdown();
	    ConnectionManager.getInstance().didComplete(this);
	} catch (Exception e) {
	}
    }

    public HttpConnection() {
	this(new Handler());
    }

    public HttpConnection(Handler _handler) {
	handler = _handler;
    }

    public void create(int method, String url, Bundle data) {
	this.method = method;
	this.url = url;
	if (data != null) {
	    this.data = (Bundle) data.clone();
	}
	this.timeout = 15000;
	ConnectionManager.getInstance().push(this);
    }

    public void getforums(String url) {
	create(GET_FORUMS, url, null);
    }

    public void gettopics(String url) {
	create(GET_TOPICS, url, null);
    }

    public void getpages(String url) {
	create(GET_PAGES, url, null);
    }

    public void getanketa(String url) {
	create(GET_ANKETA, url, null);
    }

    // повторный запрос
    public void reget() {
	create(this.method, this.url, null);
    }

    public void addtopic(String url, Bundle data) {
	create(ADDTOPIC, url, data);
    }

    public void addpost(String url, Bundle data) {
	create(ADDPOST, url, data);
    }

    // повторный запрос
    public void repost() {
	create(this.method, this.url, this.data);
    }

    public void run() {
	httpClient = new DefaultHttpClient();
	HttpParams params = httpClient.getParams();

	HttpPost httpPost;
	UrlEncodedFormEntity ent;
	List<NameValuePair> params1;

	HttpConnectionParams.setSoTimeout(params, timeout);
	HttpConnectionParams.setConnectionTimeout(params, timeout);
	ConnManagerParams.setTimeout(params, timeout);
	try {
	    HttpResponse response = null;
	    switch (method) {
	    case GET_FORUMS:
	    case GET_TOPICS:
	    case GET_PAGES:
	    case GET_ANKETA:
		// посылаем сообщение о начале процесса
		handler.sendMessage(Message.obtain(handler, GET_START));

		HttpGet httpGet = new HttpGet(url);
		httpGet.addHeader("User-Agent", "ADMClient");
		httpGet.addHeader("Accept", "text/html");
		response = httpClient.execute(httpGet);
		break;
	    case ADDPOST:
		// посылаем сообщение о начале процесса
		handler.sendMessage(Message.obtain(handler, POST_START));

		httpPost = new HttpPost(url);
		httpPost.addHeader("User-Agent", "ADMClient");
		httpPost.addHeader("Accept", "text/html");

		// формируем ответ
		params1 = new ArrayList<NameValuePair>();
		params1.add(new BasicNameValuePair("n", data.getString("n")));
		params1.add(new BasicNameValuePair("id", data.getString("id")));
		params1.add(new BasicNameValuePair("name", data.getString("name")));
		params1.add(new BasicNameValuePair("topsw", data.getString("topsw")));
		params1.add(new BasicNameValuePair("email", data.getString("email")));
		params1.add(new BasicNameValuePair("signature", data.getString("signature")));
		params1.add(new BasicNameValuePair("text", data.getString("text")));
		params1.add(new BasicNameValuePair("add2", "Добавить"));

		ent = new UrlEncodedFormEntity(params1, "windows-1251");
		httpPost.setEntity(ent);

		response = httpClient.execute(httpPost);
		break;
	    case ADDTOPIC:
		// посылаем сообщение о начале процесса
		handler.sendMessage(Message.obtain(handler, POST_START));

		httpPost = new HttpPost(url);
		httpPost.addHeader("User-Agent", "ADMClient");
		httpPost.addHeader("Accept", "text/html");

		// формируем тему
		params1 = new ArrayList<NameValuePair>();
		params1.add(new BasicNameValuePair("n", data.getString("n")));
		params1.add(new BasicNameValuePair("name", data.getString("name")));
		params1.add(new BasicNameValuePair("topsw", data.getString("topsw")));
		params1.add(new BasicNameValuePair("email", data.getString("email")));
		params1.add(new BasicNameValuePair("signature", data.getString("signature")));
		params1.add(new BasicNameValuePair("title", data.getString("title")));
		params1.add(new BasicNameValuePair("text", data.getString("text")));
		params1.add(new BasicNameValuePair("add", "Добавить"));
		ent = new UrlEncodedFormEntity(params1, "windows-1251");
		httpPost.setEntity(ent);

		response = httpClient.execute(httpPost);
		break;
	    }
	    //загружаем результат
	    processEntity(response.getEntity());
	    
	    //ошибок нет, пишем успешное завершение
	    switch (method) {
	    case GET_FORUMS:
	    case GET_TOPICS:
	    case GET_PAGES:
	    case GET_ANKETA:
		handler.sendMessage(Message.obtain(handler,GET_SUCCEED));
		break;
	    case ADDPOST:
	    case ADDTOPIC:
		handler.sendMessage(Message.obtain(handler, POST_SUCCEED));
		break;
	    }//end switch
	    
	} catch (Exception e) {
	    e.printStackTrace();
	    switch (method) {
	    case GET_FORUMS:
	    case GET_TOPICS:
	    case GET_PAGES:
	    case GET_ANKETA:
		handler.sendMessage(Message.obtain(handler, GET_ERROR, e));
		break;
	    case ADDPOST:
	    case ADDTOPIC:
		handler.sendMessage(Message.obtain(handler, POST_ERROR, e));
		break;
	    }//end switch

	}
	ConnectionManager.getInstance().didComplete(this);
    }

    // fix получение параметра до первого символа таба
    public final String parseString(String source, String param) {
	String res = "";

	res = source.substring(source.indexOf(param) + param.length() + 1);// +1
									   // символ
									   // "="
	// последний параметр в конце строки
	if (source.indexOf("\t") == -1)
	    return res;
	try {
	    res = res.substring(0, res.indexOf("\t"));
	} catch (Exception e) {
	}

	return res.trim();
    }

    private void sendmessage(int msg, final Bundle values) {
	Message message = new Message();
	message.what = msg;
	message.setData((Bundle) values.clone());
	handler.sendMessage(message);
    }

    private void processEntity(HttpEntity entity) throws IllegalStateException, IOException {

	BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent(), "windows-1251"));
	String line = "";

	Bundle values = new Bundle();
	while ((line = br.readLine()) != null) {

	    values.clear();
	    
	    // количество постов,страниц
	    if (line.contains("Allcount")) {
		String max = line.substring("Allcount=".length());
		values.putString("max", max.trim());
		sendmessage(GET_START, values);
	    } else

	    switch (this.method) {
	    case GET_FORUMS:
		values.putString("id", parseString(line, "n"));
		values.putString("title", parseString(line, "title"));
		values.putString("dsc", parseString(line, "dsc"));
		if (values.getString("title").length() != 0) {
		    sendmessage(GET_LINEREAD, values);
		}
		break;
	    case GET_TOPICS:
		//Log.v("GET_TOPICS", line);
		values.putString("title", parseString(line, "title"));
		values.putString("dsc", parseString(line, "dsc"));
		values.putString("id", parseString(line, "id"));
		values.putString("lastmod", parseString(line, "lastmod"));
		
		//для отрисовки автора ветки и количества сообщений
		values.putString("name", parseString(line, "name"));
		values.putString("count", parseString(line, "count"));

		if (values.getString("title").length() != 0) {
		    sendmessage(GET_LINEREAD, values);
		}
		break;
	    case GET_PAGES:
		if (!line.contains("ERROR")) {// пропускаем
		    values.putString("content", line);
		    if (values.getString("content").length() != 0) {
			sendmessage(GET_LINEREAD, values);
		    }
		}
		break;
	    case GET_ANKETA:
		if (line.contains("ERROR")) {
		    // посылаем ошибку
		    values.putString("error", parseString(line, "ERROR"));
		    handler.sendMessage(Message.obtain(handler, GET_ERROR, ""));
		} else {
		    // пример: sex=Мужской name=Евгений hobby=летающий йох
		    // homepage= city=Москва login=TUser about=htajhvf
		    // fhafuhfabb ytbp,t;yf<br><br>rnj ghfxbnfk? njn ufnja
		    // education=высшее id=1191932862 date=18.07.10 21:27
		    // email=evaksianov@gmail.com 0day=14.04.1981
		    // icq=201-137-028
		    values.putString("sex", parseString(line, "sex"));
		    values.putString("name", parseString(line, "name"));
		    values.putString("hobby", parseString(line, "hobby"));
		    values.putString("homepage", parseString(line, "homepage"));
		    values.putString("city", parseString(line, "city"));
		    values.putString("login", parseString(line, "login"));
		    values.putString("about", parseString(line, "about"));
		    values.putString("education", parseString(line, "education"));
		    values.putString("date", parseString(line, "date"));
		    values.putString("email", parseString(line, "email"));
		    values.putString("day", parseString(line, "0day"));// fix:
								       // изза
								       // 0day
								       // не
								       // создается
								       // таблица,
								       // хз
								       // почему
		    values.putString("icq", parseString(line, "icq"));
		    sendmessage(GET_LINEREAD, values);
		}
		break;
	    case ADDTOPIC:
		break;
	    case ADDPOST:
		break;
	    }
	};// end while
	br.close();
	// закрываем подключение
	httpClient.getConnectionManager().shutdown();
    }

}