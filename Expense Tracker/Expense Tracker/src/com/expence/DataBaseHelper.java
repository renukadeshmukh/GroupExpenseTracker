package com.expence;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.CursorJoiner.Result;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/*
 *  Database helper to interact with database 
 */

public class DataBaseHelper {
	private SQLiteDatabase mydb = null;
	private Context context;
	private String table;
	private String dbname;
	final String memberTb = "Members";

	/*
	 * constructor
	 */
	public DataBaseHelper(String DBname, String TBname, Context ctx) {
		this.context = ctx;
		table = TBname;
		dbname = DBname;
		try {
			mydb = context.openOrCreateDatabase(dbname, ctx.MODE_PRIVATE, null);
			mydb
					.execSQL("CREATE TABLE IF NOT EXISTS "
							+ TBname
							+ " (name VARCHAR, date VARCHAR, currency VARCHAR, groupid VARCHAR);");
			mydb.execSQL("CREATE TABLE IF NOT EXISTS " + "CREDIT"
					+ " (whom VARCHAR, amount VARCHAR, id VARCHAR);");
			mydb.execSQL("CREATE TABLE IF NOT EXISTS " + "DEBIT"
					+ " (who VARCHAR, amount VARCHAR, id VARCHAR);");

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * insert data into table
	 */

	boolean Insert(String name, String date, String currency, String gid) {
		ContentValues newvals = new ContentValues();
		newvals.put("name", name);
		newvals.put("date", date);
		newvals.put("currency", currency);

		/*
		 * String gid = Integer.toString(((int)(Math.random()*100000)));
		 * Log.d("gid", "###########################"+gid);
		 */
		newvals.put("groupid", gid);
		try {
			mydb.insertOrThrow(table, null, newvals);
		} catch (Exception e) {
			Log.e("insert", e.getMessage());
		}

		return true;
	}

	/*
	 * deletes given record
	 */
	int delete(String gid) {
		int r;
		try {
			r = mydb.delete(table, "groupid='" + gid + "'", null);

		} catch (Exception e) {
			Log.e("delete", e.getMessage());
			return 2;
		}

		return r;
	}

	/*
	 * queries data
	 */
	Cursor getData(int sel) {
		return mydb.rawQuery("SELECT * FROM " + table, null);
	}

	/*
	 * close db
	 */
	void close() {
		try {
			mydb.close();
		} catch (Exception e) {
			Log.e("Database", "Error while closing");
		}
	}

	Cursor getMembers(String gid) {
		return mydb.rawQuery("SELECT * FROM " + memberTb + " WHERE groupid='"
				+ gid + "'", null);

	}

	void insertMembers(Members members, String gid) {
		mydb
				.execSQL("CREATE TABLE IF NOT EXISTS "
						+ memberTb
						+ " (name VARCHAR, number VARCHAR, id VARCHAR, groupid VARCHAR);");
		ArrayList<String> names = members.getName();
		ArrayList<String> numbers = members.getNumber();
		ArrayList<String> ids = members.getId();
		int i = 0;
		while (i < names.size()) {
			ContentValues newvals = new ContentValues();
			newvals.put("name", names.get(i));
			newvals.put("number", numbers.get(i));
			newvals.put("id", ids.get(i++));

			newvals.put("groupid", gid);
			try {
				mydb.insertOrThrow(memberTb, null, newvals);
			} catch (Exception e) {
				Log.e("insert", e.getMessage());
			}

		}
	}

	void insertDebit(String from, String amount, String id) {

		ContentValues newvals = new ContentValues();
		
		Cursor cursor = mydb.rawQuery("SELECT * FROM DEBIT WHERE who='" + from + "'", null);
		if (cursor.getCount() == 1) {
			cursor.moveToFirst();
			int amt = Integer.parseInt(cursor.getString(cursor
					.getColumnIndex("amount")));
			int amt1 =  Integer.parseInt(amount);
			amount = ""+(amt+amt1);
			newvals.put("amount", amount);
			mydb.update("DEBIT", newvals, "who='" + from + "'", null);
		} else {
			try {
				newvals.put("who", from);
				newvals.put("amount", amount);
				newvals.put("id", id);
				mydb.insertOrThrow("DEBIT", null, newvals);
			} catch (Exception e) {
				Log.e("insert", e.getMessage());
			}
		}

	}

	void insertCredit(String to, String amount, String id) {
		ContentValues newvals = new ContentValues();
		Cursor cursor = mydb.rawQuery("SELECT * FROM CREDIT WHERE whom='" + to + "'", null);
		if (cursor.getCount() == 1) {
			cursor.moveToFirst();
			int amt = Integer.parseInt(cursor.getString(cursor
					.getColumnIndex("amount")));
			int amt1 =  Integer.parseInt(amount);
			amount = ""+(amt+amt1);
			newvals.put("amount", amount);
			mydb.update("CREDIT", newvals, "whom='" + to + "'", null);
		} else {
			newvals.put("whom", to);
			newvals.put("amount", amount);
			newvals.put("id", id);
			try {
				mydb.insertOrThrow("CREDIT", null, newvals);
			} catch (Exception e) {
				Log.e("insert", e.getMessage());
			}
		}
		
	}

	Cursor getDebits() {

		return mydb.rawQuery("SELECT * FROM " + "DEBIT", null);
	}

	Cursor getCredits() {
		return mydb.rawQuery("SELECT * FROM " + "CREDIT", null);
	}

	void updateDebits(String from, String amount, String id) {
		ContentValues newvals = new ContentValues();
		newvals.put("who", from);
		newvals.put("amount", amount);
		try {
			if (amount.equalsIgnoreCase("0")) {
				// mydb.rawQuery("DELETE FROM DEBIT WHERE id='"+id+"'", null);
				mydb.delete("DEBIT", "id='" + id + "'", null);
			} else {
				mydb.update("DEBIT", newvals, "id='" + id + "'", null);
			}
		} catch (Exception e) {
			Log.e("dberror", e.getMessage());
		}

		// mydb.rawQuery("UPDATE DEBIT SET who='"+from+"', "+"amount='"+amount+"' WHERE id='"+id+"'",
		// null);

	}

	void updateCredits(String from, String amount, String id) {
		// mydb.rawQuery("UPDATE CREDIT SET whom='"+from+"', "+"amount='"+amount+"' WHERE id='"+id+"'",
		// null);

		ContentValues newvals = new ContentValues();
		newvals.put("whom", from);
		newvals.put("amount", amount);
		try {
			if (amount.equalsIgnoreCase("0"))
				mydb.delete("CREDIT", "id='" + id + "'", null);
			else
				mydb.update("CREDIT", newvals, "id='" + id + "'", null);
		} catch (Exception e) {
			Log.e("dberror", e.getMessage());
		}

	}

}
