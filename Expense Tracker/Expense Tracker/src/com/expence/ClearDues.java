package com.expence;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;

public class ClearDues extends Activity {

	SQLiteDatabase mydb;
	Members members;
	DataBaseHelper dbhelper;
	String groupId;
	String expId;
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dues);
		members = new Members();
		groupId = (String) getIntent().getCharSequenceExtra("groupid");
		dbhelper = new DataBaseHelper("Expense_tracker", "Groups", this);
		mydb = openOrCreateDatabase("Expense_tracker", MODE_PRIVATE, null);
		mydb
				.execSQL("CREATE TABLE IF NOT EXISTS "
						+ "Expenses"
						+ " (name VARCHAR, payee VARCHAR, paidfor VARCHAR, amount VARCHAR,groupid VARCHAR,expenseid VARCHAR);");
		mydb
				.execSQL("CREATE TABLE IF NOT EXISTS "
						+ "Report"
						+ " (who VARCHAR, whom VARCHAR, amount VARCHAR, groupid VARCHAR);");
		Cursor cursor = dbhelper.getMembers(groupId);
		int rows = cursor.getCount();
		if (rows > 0) {
			int nameIndex = cursor.getColumnIndex("name");
			int numberIndex = cursor.getColumnIndex("number");
			int idIndex = cursor.getColumnIndex("id");
			int i = 0;
			cursor.moveToFirst();
			while (i < rows) {
				members.add(cursor.getString(nameIndex), cursor
						.getString(numberIndex), cursor.getString(idIndex));
				i++;
				if (cursor.isLast())
					break;
				else
					cursor.moveToNext();
			}
		}

		Button payee = (Button) findViewById(R.id.paidby);
		payee.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				payeeDialog.show();
			}
		});
		Button paidTo = (Button)findViewById(R.id.paidto);
		paidTo.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {			
				paidToDialog.show();
			}
		});
		payeeDialog = new AlertDialog.Builder(this).setTitle("Members")
		.setAdapter(
				new ArrayAdapter<String>(this, R.layout.textlist,
						members.getName()),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						paidByName = members.getName().get(which);
						dialog.dismiss();
					}
				});
		paidToDialog = new AlertDialog.Builder(this).setTitle("Members")
		.setAdapter(
				new ArrayAdapter<String>(this, R.layout.textlist,
						members.getName()),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog,
							int which) {
						paidToName = members.getName().get(which);
						dialog.dismiss();
					}
				});
		amount = (EditText)findViewById(R.id.paidamount);
		Button clear = (Button)findViewById(R.id.cleardues);
		clear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int r;
				try {
					r = mydb.delete("Report", "groupid = '" + groupId + "'" +" AND "+"who="
							+"'"+paidByName+"'"+" AND "+"whom="+"'"+paidToName+"'",
							null);
				} catch (Exception e) {
					Log.e("delete", e.getMessage());
				}
				amount.setText("");
			}
		});
		Button done = (Button)findViewById(R.id.donedues);
		done.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {			
				mydb.close();
				dbhelper.close();
				finish();
			}
		});

	}
	Builder payeeDialog;
	Builder paidToDialog;
	String paidByName;
	String paidToName;
	EditText amount;
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		mydb.close();
		dbhelper.close();
		super.onDestroy();
	}
	
}
