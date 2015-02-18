package com.expence;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class Report extends Activity {

	SQLiteDatabase mydb;
	Members members;
	DataBaseHelper dbhelper;
	String groupId;
	String expId;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report);
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
		ArrayList<String> reports = new ArrayList<String>();

		Cursor cursor = mydb.rawQuery("SELECT * FROM Expenses WHERE groupid='"
				+ groupId + "'", null);
		int rows = cursor.getCount();
		from = new String[rows];
		to = new String[rows];
		expense = new String[rows];
		amount = new String[rows];
		cursor.moveToFirst();
		count = rows;
		int i = 0;
		if (rows > 0) {
			while (true) {
				from[i] = cursor.getString(cursor.getColumnIndex("payee"));
				to[i] = cursor.getString(cursor.getColumnIndex("paidfor"));
				amount[i] = cursor.getString(cursor.getColumnIndex("amount"));
				expense[i++] = cursor.getString(cursor.getColumnIndex("name"));
				reports.add(from + " should pay " + amount + " to " + to);
				if (cursor.isLast())
					break;
				else
					cursor.moveToNext();
			}
		}
		ListView lv = (ListView) findViewById(R.id.reportlist);
		/*lv
				.setAdapter(new ArrayAdapter<String>(this, R.layout.textlist,
						reports));*/
		lv.setAdapter(new ListAdapter());
	}
	int count;
	String from[];
	String to[];
	String expense[];
	String amount[];
	
	class ListAdapter extends BaseAdapter {
		LayoutInflater inflate;
		int width;
		public ListAdapter() {
			inflate = LayoutInflater.from(Report.this);
			width = getWindowManager().getDefaultDisplay().getWidth();
		}
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return count;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = inflate.inflate(R.layout.reportitem, null);			
			TextView expenseName =(TextView) convertView.findViewById(R.id.expensename);
			TextView amount = (TextView)convertView.findViewById(R.id.amountvalue);
			TextView paidfor = (TextView)convertView.findViewById(R.id.paidforname);
			TextView payee = (TextView)convertView.findViewById(R.id.payeename);
			expenseName.setLayoutParams(new LinearLayout.LayoutParams(width/4,LayoutParams.WRAP_CONTENT));
			amount.setLayoutParams(new LinearLayout.LayoutParams(width/4,LayoutParams.WRAP_CONTENT));
			paidfor.setLayoutParams(new LinearLayout.LayoutParams(width/4,LayoutParams.WRAP_CONTENT));
			payee.setLayoutParams(new LinearLayout.LayoutParams(width/4,LayoutParams.WRAP_CONTENT));
			expenseName.setText(expense[position]);
			amount.setText(Report.this.amount[position]);
			paidfor.setText(to[position]);
			payee.setText(from[position]);
			return convertView;
		}

	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		dbhelper.close();
		mydb.close();
		
		super.onDestroy();
	}
	
	
}
