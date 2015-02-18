package com.expence;

import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class MyDues extends Activity {

	DataBaseHelper dbhelper;
	ArrayList<String> debFrom = new ArrayList<String>();
	ArrayList<String> debAmount = new ArrayList<String>();
	ArrayList<String> debId = new ArrayList<String>();
	ArrayList<String> creditTo = new ArrayList<String>();
	ArrayList<String> creditAmount = new ArrayList<String>();
	ArrayList<String> creditId = new ArrayList<String>();
	SQLiteDatabase mydb;
	Members members;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mydues);
		dbhelper = new DataBaseHelper("Expense_tracker", "Groups", this);
		mydb = openOrCreateDatabase("Expense_tracker", MODE_PRIVATE, null);
		mydb
				.execSQL("CREATE TABLE IF NOT EXISTS "
						+ "Expenses"
						+ " (name VARCHAR, payee VARCHAR, paidfor VARCHAR, amount VARCHAR,groupid VARCHAR,expenseid VARCHAR,status INTEGER);");
		Cursor cursor = mydb.rawQuery("SELECT * FROM Members", null);
		members = new Members();
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
		debAmount = new ArrayList<String>();
		debFrom = new ArrayList<String>();
		creditAmount = new ArrayList<String>();
		creditTo = new ArrayList<String>();
		/*
		 * for (int i = 1; i < (members.getId().size() - 1); i++) {
		 * cursor.moveToFirst();
		 * 
		 * int amt = 0; cursor =
		 * mydb.rawQuery("SELECT * Expenses WHERE paidfor='" +
		 * members.getName().get(i) + "' AND payee='" + members.getName().get(i)
		 * + "'", null); int amtIndex = cursor.getColumnIndex("amount"); if
		 * (cursor.getCount() > 0) { while (true) { amt +=
		 * Integer.parseInt(cursor.getString(amtIndex)); if(cursor.isLast())
		 * break; cursor.moveToNext(); } } cursor =
		 * mydb.rawQuery("SELECT * Expenses WHERE payee='" +
		 * members.getName().get(i) + "' AND paidfor='" +
		 * members.getName().get(i) + "'", null);
		 * 
		 * if (cursor.getCount() > 0) { while (true) { amt -=
		 * Integer.parseInt(cursor.getString(amtIndex)); if(cursor.isLast())
		 * break; cursor.moveToNext(); } } if(amt > 0) { debAmount.add(""+amt);
		 * debFrom.add(members.getName().get(i)); } else if(amt < 0) {
		 * creditAmount.add(""+amt); creditTo.add(members.getName().get(i)); } }
		 */

		cursor = dbhelper.getDebits();
		 rows = cursor.getCount();
		if (rows > 0) {
			int nameIndex = cursor.getColumnIndex("who");
			int numberIndex = cursor.getColumnIndex("amount");
			int idIndex = cursor.getColumnIndex("id");
			int i = 0;
			cursor.moveToFirst();
			while (i < rows) {
				debFrom.add(cursor.getString(nameIndex));
				debAmount.add(cursor.getString(numberIndex));
				debId.add(cursor.getString(idIndex));
				i++;
				if (cursor.isLast())
					break;
				else
					cursor.moveToNext();
			}
		}
		cursor = dbhelper.getCredits();
		rows = cursor.getCount();
		if (rows > 0) {
			int nameIndex = cursor.getColumnIndex("whom");
			int numberIndex = cursor.getColumnIndex("amount");
			int idIndex = cursor.getColumnIndex("id");
			int i = 0;
			cursor.moveToFirst();
			while (i < rows) {
				creditTo.add(cursor.getString(nameIndex));
				creditAmount.add(cursor.getString(numberIndex));
				creditId.add(cursor.getString(idIndex));
				i++;
				if (cursor.isLast())
					break;
				else
					cursor.moveToNext();
			}
		}
		ListView lv = (ListView) findViewById(R.id.duelist);
		lv.setAdapter(new ListAdapter(true));
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int pos,
					long arg3) {
				showClearDues();
				selectedIndex = pos;
			}
		});
		Button debButton = (Button) findViewById(R.id.debits);
		Button creditButton = (Button) findViewById(R.id.credits);
		debButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ListView lv = (ListView) findViewById(R.id.duelist);
				lv.setAdapter(new ListAdapter(true));
				debstatus = true;
			}
		});
		creditButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ListView lv = (ListView) findViewById(R.id.duelist);
				lv.setAdapter(new ListAdapter(false));
				debstatus = false;
			}
		});
		mydb.close();
		dbhelper.close();
	}

	boolean debstatus = true;
	int selectedIndex = 0;

	class ListAdapter extends BaseAdapter {
		LayoutInflater inflate;
		int width;
		int count;
		boolean deb;

		public ListAdapter(boolean deb) {
			inflate = LayoutInflater.from(MyDues.this);
			width = getWindowManager().getDefaultDisplay().getWidth();
			this.deb = deb;
			if (deb)
				count = debFrom.size();
			else
				count = creditTo.size();
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
			convertView = inflate.inflate(R.layout.dueitem, null);
			TextView name = (TextView) convertView.findViewById(R.id.duename);
			TextView amount = (TextView) convertView
					.findViewById(R.id.dueamount);
			if (deb) {
				name.setText(debFrom.get(position));
				amount.setText(debAmount.get(position));
			} else {
				name.setText(creditTo.get(position));
				amount.setText(creditAmount.get(position));
			}
			return convertView;
		}

	}

	void showClearDues() {
		final AlertDialog.Builder alert = new AlertDialog.Builder(this);
		final EditText input = new EditText(this);
		alert.setView(input);
		alert.setPositiveButton("Clear", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				int amount = Integer.parseInt(value);
				dbhelper = new DataBaseHelper("Expense_tracker", "Groups", MyDues.this);
				if (debstatus) {
					String amt = debAmount.get(selectedIndex);
					int debAmt = Integer.parseInt(amt);
					if (amount > debAmt) {
						Toast t = Toast.makeText(MyDues.this,
								"Amount can't greater than dues", 2000);
						t.show();
					} else {
						debAmount.set(selectedIndex, "" + (debAmt - amount));

						int amt1 = (debAmt - amount);
						/*
						 * Cursor cursor =
						 * mydb.rawQuery("SELECT * Expenses WHERE paidfor='" +
						 * members.getName().get(0) + "' AND payee='" +
						 * members.getName().get(selectedIndex) + "'", null);
						 */
							  dbhelper.updateDebits(debFrom.get(selectedIndex),
							  debAmount.get(selectedIndex), debId
							  .get(selectedIndex));
							 
					}
				} else {
					String amt = creditAmount.get(selectedIndex);
					int debAmt = Integer.parseInt(amt);
					if (amount > debAmt) {
						Toast t = Toast.makeText(MyDues.this,
								"Amount can't greater than dues", 2000);
						t.show();
					} else {
						creditAmount.set(selectedIndex, "" + (debAmt - amount));
						
						  dbhelper.updateCredits(creditTo.get(selectedIndex),
						  creditAmount.get(selectedIndex), creditId
						  .get(selectedIndex));
						 
					}
				}
				ListView lv = (ListView) findViewById(R.id.duelist);
				lv.setAdapter(new ListAdapter(debstatus));
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				});
		alert.show();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		dbhelper.close();
		super.onDestroy();
	}

}
