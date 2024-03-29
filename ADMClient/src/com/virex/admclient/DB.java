package com.virex.admclient;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.app.Activity;

/**
 * VirEx-84.narod.ru
 */
public class DB {
    public static final int DB_ONSTART = 111;
    public static final int DB_ONLINE = 222;
    public static final int DB_ONSTOP = 333;

    private int LOAD_FORUMS = 1;
    private int LOAD_TOPICS = 2;
    private int LOAD_PAGES = 3;

    // private static SQLiteDatabase rdb;
    private static SQLiteDatabase db;
    private static final String DB_FILE = "delphimaster.db";
    private Context mcontext;
    private Handler mhandler;
    private Activity mactivity;
    private boolean BreakLoading = false;

    public DB(Context context, Handler handler, Activity activity) {
	super();
	mcontext = context;
	mhandler = handler;
	mactivity = activity;
	db = (new DBOpenHelper(context, activity)).getWritableDatabase();

	// rdb = (new DBOpenHelper(context)).getReadableDatabase();
    }

    private static class DBOpenHelper extends SQLiteOpenHelper {
	// private static final String SQL_TABLE_EXISTS =
	// "SELECT name FROM sqlite_master WHERE type='table' AND name='%s';";
	private static final String SQL_CREATE_FORUMS = "CREATE TABLE forums (_id INTEGER PRIMARY KEY AUTOINCREMENT, n TEXT UNIQUE, title TEXT, dsc TEXT);";
	private static final String SQL_CREATE_TOPICS = "CREATE TABLE topics (_id INTEGER PRIMARY KEY AUTOINCREMENT, n TEXT, id TEXT UNIQUE, name TEXT, title TEXT, answers TEXT, email TEXT, count TEXT, dsc TEXT, date TEXT, lastmod INTEGER, vd TEXT, loginid TEXT);";
	private static final String SQL_CREATE_PAGES = "CREATE TABLE pages (_id INTEGER PRIMARY KEY AUTOINCREMENT, n TEXT, id TEXT, content TEXT UNIQUE);";

	// база анкет, чтобы в отсутствие инета глянуть
	private static final String SQL_CREATE_ANKETA = "CREATE TABLE anketa (_id INTEGER PRIMARY KEY AUTOINCREMENT, sex TEXT, name TEXT, hobby TEXT, homepage TEXT, city TEXT, login TEXT, about TEXT, education TEXT, id TEXT UNIQUE, date TEXT, email TEXT, day TEXT, icq TEXT);";

	private static String GetPath(Context context) {
	    if (context.getExternalFilesDir(null).canWrite()) {
		return context.getExternalFilesDir(null).getAbsolutePath() + "/";
	    } else {
		return "";
	    }
	}

	public DBOpenHelper(Context context, Activity activity) {
	    super(context,GetPath(context) + DB_FILE, null, 1);
	}

	// при первом создании базы
	@Override
	public void onCreate(SQLiteDatabase db) {
	    try {
		db.execSQL(SQL_CREATE_FORUMS);
	    } catch (Exception e) {
	    }
	    ;
	    try {
		db.execSQL(SQL_CREATE_TOPICS);
	    } catch (Exception e) {
	    }
	    ;
	    try {
		db.execSQL(SQL_CREATE_PAGES);
	    } catch (Exception e) {
	    }
	    ;
	    try {
		db.execSQL(SQL_CREATE_ANKETA);
	    } catch (Exception e) {
	    }
	    ;
	}

	@Override
	public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2) {
	}

	// при открытии базы пересоздаем таблицы если их нет
	@Override
	public void onOpen(SQLiteDatabase db) {
	    onCreate(db);
	}
    }

    public void Clear() {
	db.execSQL("DROP TABLE IF EXISTS forums");
	db.execSQL("DROP TABLE IF EXISTS topics");
	db.execSQL("DROP TABLE IF EXISTS pages");
	// заново создаем таблицы, переоткрывая базу
	db.close();
	db = (new DBOpenHelper(mcontext, mactivity)).getWritableDatabase();
    }

    /*
     * private Boolean InsertDB(final String table, final ContentValues values)
     * { Thread t = new Thread(new Runnable() {
     * 
     * @Override public void run() { try { db.insertOrThrow(table, "", values);
     * values.clear(); values.put("result", true); } catch (SQLException E) {
     * values.clear(); values.put("result", false); } } }); t.start(); //final
     * Boolean int не работает, эклипс ругается //пришлось извратится вот таким
     * способом return values.getAsBoolean("result"); }
     */

    public Boolean AddForum(String n, String title, String dsc) {
	ContentValues values = new ContentValues();
	values.put("n", n);
	values.put("title", title);
	values.put("dsc", dsc);
	// return InsertDB("forums",values);

	try {
	    // return String.valueOf(wdb.insert("forums", title, values));
	    db.insertOrThrow("forums", title, values);
	    // Log.v("AddForum", n + title + dsc);
	    return true;
	} catch (SQLException E) {
	    return false;
	}

    }

    public Boolean AddTopic(String n, String id, String name, String title, String answers, String email, String count, String dsc, String date, String lastmod, String vd, String loginid) {
	ContentValues values = new ContentValues();
	values.put("n", n);
	values.put("id", id);
	values.put("name", name);
	values.put("title", title);
	values.put("answers", answers);
	values.put("email", email);
	values.put("count", count);
	values.put("dsc", dsc);
	values.put("date", date);
	values.put("lastmod", lastmod);
	values.put("vd", vd);
	values.put("loginid", loginid);
	// Log.v("AddTopic", values.toString());
	// return InsertDB("topics",values);

	try {
	    db.insertOrThrow("topics", title, values);
	    
	    return true;
	} catch (SQLException E) {
	    return false;
	}

    }

    public Boolean UpdateTopic(String n, String id, String lastmod, String count) {
	ContentValues values = new ContentValues();
	values.put("n", n);
	values.put("id", id);
	values.put("lastmod", Integer.valueOf(lastmod));
	values.put("count", count);
	Log.v("UpdateTopic", values.toString());
	try {

	    // не рабочий вариант, изза которого потрачено куча времени
	    // Cursor cursor
	    // =db.rawQuery(String.format("UPDATE topics SET n='%s',id='%s',lastmod='%s' WHERE id='%s';",
	    // n, id, lastmod, id), null);
	    // mactivity.startManagingCursor(cursor);
	    // db.delete("topics", "id=" + id, null);

	    db.update("topics", values, "id=" + id, null);// <--- рабочий
							  // вариант

	    // db.updateWithOnConflict("topics", values, "id=" + id, null,
	    // SQLiteDatabase.CONFLICT_REPLACE);

	    return true;
	} catch (SQLException E) {
	    return false;
	}
    }

    public Boolean AddPage(String n, String id, String content) {
	ContentValues values = new ContentValues();
	values.put("n", n);
	values.put("id", id);
	values.put("content", content);
	// return InsertDB("pages",values);

	try {
	    db.insertOrThrow("pages", id, values);
	    // Log.v("AddPage", n + id + content);
	    return true;
	} catch (SQLException E) {
	    return false;
	}

    }

    private void sendmessage(int msg, Bundle values) {
	//Log.v("sendmessage", values.toString());
	Message message = new Message();
	message.what = msg;
	message.setData((Bundle) values.clone());
	mhandler.sendMessage(message);
	values.clear();
    }

    public void Break() {
	BreakLoading = true;
    }

    private void LoadFromDB(final int what, final String forumid, final String topicid) {
	final Bundle values = new Bundle();
	Thread t = new Thread(new Runnable() {
	    @Override
	    public void run() {

		Cursor cursor = null;
		if (what == LOAD_FORUMS) {
		    values.putInt("max", GetForumsCount());
		    sendmessage(DB.DB_ONSTART, values);
		    try {
			//cursor = db.query("forums", null, null, null, null, null, null);
			cursor = db.rawQuery("SELECT n,title,dsc FROM forums;", null);
		    } catch (Exception e) {
		    }
		}
		;
		if (what == LOAD_TOPICS) {
		    values.putInt("max", GetTopicsCount(forumid));
		    sendmessage(DB.DB_ONSTART, values);
		    try {
			cursor = db.rawQuery(String.format("SELECT id,title,dsc,lastmod,name,count FROM topics WHERE n='%s' ORDER BY lastmod DESC;", forumid), null);
		    } catch (Exception e) {
		    }
		}
		;
		if (what == LOAD_PAGES) {
		    values.putInt("max", GetPagesCount(forumid, topicid));
		    sendmessage(DB.DB_ONSTART, values);
		    try {
			cursor = db.rawQuery(String.format("SELECT content FROM pages WHERE n='%s' AND id='%s';", forumid, topicid), null);
		    } catch (Exception e) {
		    }
		}
		;
		// таблица пустая - выходим
		if (cursor == null) {
		    Log.v("cursor == null", "");
		    values.clear();
		    sendmessage(DB.DB_ONSTOP, values);
		    return;
		}
		;

		// добавляем курсор в менеджер (для автоматического закрытия
		// после запроса)
		mactivity.startManagingCursor(cursor);

		int i = 0;
		cursor.moveToFirst();
		while (i < cursor.getCount() || BreakLoading) {
		    //values.clear();
		    /*
		     * try { Thread.sleep(10); } catch (Exception e) {
		     * Thread.currentThread().interrupt(); }
		     */
		    
		    
		    try {
			if (what == LOAD_FORUMS) {
			    values.putString("n", cursor.getString(0));
			    values.putString("title", cursor.getString(1));
			    values.putString("dsc", cursor.getString(2));
			}
			;
			if (what == LOAD_TOPICS) {
			    values.putString("id", cursor.getString(0));
			    values.putString("title", cursor.getString(1));
			    values.putString("dsc", cursor.getString(2));
			    values.putString("lastmod", cursor.getString(3));
			    //try{//если база старой версии значит нужно перестраховатся
			    values.putString("name", cursor.getString(4));
			    values.putString("count", cursor.getString(5));
			    //}catch (Exception e){}
			}
			;
			if (what == LOAD_PAGES) {
			    values.putString("content", cursor.getString(0));
			}
			;
		    } catch (Exception e) {

		    }
		    
		    sendmessage(DB.DB_ONLINE, values);
		    /*
		    // жаль cursor.getExtras() не возвращает результат
		    // можно конечно сделать как в этом задокументированном финте но будет сильная нагрузка
		    int y=0;
		    String columnname="";
		    while (y<cursor.getColumnCount()){
			columnname=cursor.getColumnName(y);
			int z=0;
			while (z<cursor.getCount()){
			    values.putString(columnname, cursor.getString(y));
			    z ++;
			}
			y ++;
		    }
		    sendmessage(DB.DB_ONLINE, values);
		    */
		    cursor.moveToNext();
		    i++;
		}
		values.clear();
		sendmessage(DB.DB_ONSTOP, values);
		BreakLoading = false;
		// ConnectionManager.getInstance().didComplete(this);
	    }
	});
	t.start();
	// ConnectionManager.getInstance().push(t);
    }

    public void LoadForums() {

	LoadFromDB(LOAD_FORUMS, "", "");
    }

    public void LoadTopics(String forumid) {
	LoadFromDB(LOAD_TOPICS, forumid, "");
    }

    public void LoadPages(String forumid, String topicid) {
	LoadFromDB(LOAD_PAGES, forumid, topicid);
    }

    public void LoadAnketa(String id) {
	Cursor cursor;
	try {
	    cursor = db.rawQuery(String.format("SELECT * FROM naketa WHERE id='%s';", id), null);
	} catch (Exception e) {
	    return;
	}
	/*
	Bundle values = new Bundle();
	values.putString("data", cursor.getString(cursor.getColumnIndex("data")));
	values.putString("login", cursor.getString(cursor.getColumnIndex("login")));
	values.putString("email", cursor.getString(cursor.getColumnIndex("email")));
	values.putString("homepage", cursor.getString(cursor.getColumnIndex("homepage")));
	values.putString("name", cursor.getString(cursor.getColumnIndex("name")));
	values.putString("0day", cursor.getString(cursor.getColumnIndex("0day")));
	values.putString("city", cursor.getString(cursor.getColumnIndex("city")));
	values.putString("sex", cursor.getString(cursor.getColumnIndex("sex")));
	values.putString("education", cursor.getString(cursor.getColumnIndex("education")));
	values.putString("hobby", cursor.getString(cursor.getColumnIndex("hobby")));
	values.putString("about", cursor.getString(cursor.getColumnIndex("about")));
	sendmessage(HttpConnection.DID_LINEREAD, values);
	*/
	sendmessage(HttpConnection.GET_LINEREAD, cursor.getExtras());
	
    }

   
    public void AddOrUpdate(String table, ContentValues values, String update_index) {
	Log.v("AddOrUpdate", values.toString());
	try {//пробуем добавить
	    db.insertOrThrow(table, "null", values);
	} catch (SQLException E) {
	    try {//пробуем обновить
		db.update(table, values, update_index+"=" + values.getAsString(update_index), null);
	    } catch (Exception e) {
	    }
	}

    }

    // не робит
    public String GetTopicTitle(String forumid, String topicid) {
	String res = "";
	Cursor cursor = db.rawQuery(String.format("SELECT title FROM topics WHERE n='%s' AND id='%s';", forumid, topicid), null);
	mactivity.startManagingCursor(cursor);
	if (cursor == null) {
	    return res;
	} else {
	    cursor.moveToFirst();
	    try {
		res = cursor.getString(0);
	    } catch (Exception E) {
	    }
	    // cursor.close();
	    return res;
	}

    }

    // получаем самую последнюю дату модификации веток в данном форуме
    public String GetTopicsLastMod(String forumid) {
	String res = "-1";

	Cursor cursor = db.rawQuery(String.format("SELECT lastmod FROM topics WHERE n='%s' ORDER BY lastmod DESC;", forumid), null);
	mactivity.startManagingCursor(cursor);
	if (cursor == null) {
	    return res;
	} else {

	    // самый "верхний" топик - самый последний
	    cursor.moveToFirst();
	    try {
		res = cursor.getString(0);
	    } catch (Exception E) {
	    }
	    // cursor.close();
	    Log.v("GetTopicsLastMod", res);
	    return res;
	}

    }

    public String GetPagesLastMod(String forumid) {
	String res = "-1";
	Cursor cursor = db.rawQuery("SELECT id FROM pages WHERE n=" + forumid, null);
	mactivity.startManagingCursor(cursor);
	if (cursor == null) {
	    return res;
	} else {

	    cursor.moveToLast();
	    try {
		res = cursor.getString(0);
	    } catch (Exception E) {
	    }
	    // cursor.close();
	    return res;
	}

    }

    public int GetForumsCount() {
	int res = -1;
	try {
	    Cursor cursor = db.rawQuery("SELECT n FROM forums;", null);
	    mactivity.startManagingCursor(cursor);
	    res = cursor.getCount();
	} catch (SQLiteException e) {
	}
	;
	return res;
    }

    public int GetTopicsCount(String forumid) {
	int res = -1;
	try {
	    Cursor cursor = db.rawQuery(String.format("SELECT id FROM topics WHERE n='%s';", forumid), null);
	    mactivity.startManagingCursor(cursor);
	    res = cursor.getCount();
	} catch (SQLiteException e) {
	}
	;
	return res;
    }

    public int GetPagesCount(String forumid, String topicid) {
	int res = -1;
	try {
	    Cursor cursor = db.rawQuery(String.format("SELECT n FROM pages WHERE n='%s' AND id='%s';", forumid, topicid), null);
	    mactivity.startManagingCursor(cursor);
	    res = cursor.getCount();
	} catch (SQLiteException e) {
	}
	;
	return res;
    }
}
