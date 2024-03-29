package com.virex.admclient;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
/**
 * VirEx-84.narod.ru
 */
public class ReloadDialog extends AlertDialog {
	public final static int mReGet = 0;
	public final static int mRePost = 1;

	public ReloadDialog(Context context, final int method, final HttpConnection httpconnection) {
		super(context);
		setMessage("Обновить?");
		// кнопка "Yes", при нажатии на которую диалог закроется
		setButton("Да", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				switch (method) {
				case mReGet:
					httpconnection.reget();
					break;
				case mRePost:
					httpconnection.repost();
					break;
				}
				dismiss();
			}
		});
		// кнопка "No", при нажатии на которую ничего не
		// произойдет
		setButton2("Нет", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				dismiss();
			}
		});
		//show();
	}

}
