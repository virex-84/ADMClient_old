package com.virex.admclient;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;

/**
 * VirEx-84.narod.ru
 */
public class Options extends PreferenceActivity {

    /* (non-Javadoc)
     * @see android.preference.PreferenceActivity#onPreferenceTreeClick(android.preference.PreferenceScreen, android.preference.Preference)
     */
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
	//редактирование анкеты
	if (preference.getKey().equals("edit_anketa")){
		/* по идее нужно послать post запрос http://www.delphimaster.ru/cgi-bin/anketa.pl, login, psw
		 * и сделать это лучше в webview, но пока откроем во внешнем браузере
		 * */
		// переходим на внешнюю ссылку, редактирование анкеты
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.delphimaster.ru/anketa/index.html"));
		startActivity(intent);
		return false;
	};
	//очистка базы
	if (preference.getKey().equals("dbclear")){
		AlertDialog.Builder b = new AlertDialog.Builder(preference.getContext());
		b.setTitle("Внимание").setMessage("Вы действительно хотите очистить базу?").setPositiveButton("Отмена", null).setNegativeButton("OK", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
			DB db = new DB(getBaseContext(), null, null);
			db.Clear();
		    }
		});
		b.show();	    
	}
	return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	// загружаем предпочтения из ресурсов
	addPreferencesFromResource(R.xml.options);
	/*
	 * если в ресурсах (файл strings.xml) не указать значения строк по
	 * умолчанию, то настройки никак не сохранятся, не знаю почему но вот
	 * токой фичебаг
	 */

    }
}