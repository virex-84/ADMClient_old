package com.virex.admclient;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.EditText; 
/**
 * VirEx-84.narod.ru
 */
public class PostDialog extends Dialog implements android.view.View.OnClickListener {
	PublicDialogListener listener;
	EditText etTitle;
	EditText etText;

	//интерфейс для обратной связи
	public interface PublicDialogListener {
		public void onOkClick(String title, String text);
		public void onCancelClick();
		}	
	
	public PostDialog(Context context, final String title, final String data, PublicDialogListener listener) {
		super(context);
		this.listener=listener;
		
		//setTitle("title");
		setContentView(R.layout.dlgnewtopic);

		etTitle = (EditText) findViewById(R.id.etTitle);
		etText = (EditText) findViewById(R.id.etText);
		Button btnOk = (Button) findViewById(R.id.btnOk);
		Button btnCancel = (Button) findViewById(R.id.btnCancel);

		btnOk.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
	
		
		//для поста не нужен заголовок
		if (title==""){
			etTitle.setVisibility(View.INVISIBLE);
		} else {
			etTitle.setText(title);
		}
		
		etText.setText(data);
		etText.requestFocus();
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnOk:
			//возвращаем результат
			String title="";
			String text="";
			title+=etTitle.getText().toString();
		    text+=etText.getText().toString();
			listener.onOkClick(title, text);
			break;
		case R.id.btnCancel:
			listener.onCancelClick();
			break;
		}// switch
		this.dismiss();
	}

}
