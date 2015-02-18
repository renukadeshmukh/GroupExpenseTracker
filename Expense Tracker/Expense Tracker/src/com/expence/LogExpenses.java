package com.expence;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LogExpenses extends Activity {

	SQLiteDatabase mydb;
	Members members;
	DataBaseHelper dbhelper;
	String groupId;
	String expId;
	Button payee;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logexpenses);
		members = new Members();
		groupId = (String) getIntent().getCharSequenceExtra("groupid");
		dbhelper = new DataBaseHelper("Expense_tracker", "Groups", this);
		mydb = openOrCreateDatabase("Expense_tracker", MODE_PRIVATE, null);
		mydb
				.execSQL("CREATE TABLE IF NOT EXISTS "
						+ "Expenses"
						+ " (name VARCHAR, payee VARCHAR, paidfor VARCHAR, amount VARCHAR,groupid VARCHAR,expenseid VARCHAR,status INTEGER);");
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

		payee = (Button) findViewById(R.id.payee);
		payee.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				payeeDialog.show();
			}
		});

		Button paidFor = (Button) findViewById(R.id.paidfor);
		paidFor.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				paidForDialog.show();
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
								payeeMembers.add(members.getName().get(which),
										members.getNumber().get(which), members
												.getId().get(which));
								payee.setText(members.getName().get(which));
								dialog.dismiss();
							}
						});
		payeeMembers = new Members();
		paidMembers = new Members();
		CharSequence[] items = members.getName().toArray(
				new CharSequence[members.getName().size()]);
		paidForDialog = new AlertDialog.Builder(this).setTitle("Members")
				.setMultiChoiceItems(items, null,
						new DialogInterface.OnMultiChoiceClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which, boolean isChecked) {

								if (isChecked && !paidforIds.contains(which)) {
									paidforIds.add(which);
								} else if (!isChecked
										&& paidforIds.contains(which)) {
									paidforIds
											.remove(paidforIds.indexOf(which));
								}
							}
						});
		paidForDialog.setPositiveButton("Ok",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						for (int i = 0; i < paidforIds.size(); i++) {
							paidMembers.add(members.getName().get(
									paidforIds.get(i)), members.getNumber()
									.get(paidforIds.get(i)), members.getId()
									.get(paidforIds.get(i)));
						}
					}
				});
		ename = (EditText) findViewById(R.id.expname);
		amount = (EditText) findViewById(R.id.amount);
		Button save = (Button) findViewById(R.id.saveexpense);
		save.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (ename.getText().length() > 0
						&& amount.getText().length() > 0
						&& payeeMembers.getName().size() > 0
						&& paidMembers.getName().size() > 0) {
					String pname = payeeMembers.getName().get(
							payeeMembers.getName().size() - 1);
					ArrayList<String> pfor = paidMembers.getName();
					expId = Integer.toString((int) (Math.random() * 100000));
					String amt = Integer.toString(Integer.parseInt(amount
							.getText().toString())
							/ pfor.size());
					for (String mname : pfor) {
						ContentValues newvals = new ContentValues();
						newvals.put("name", ename.getText().toString());
						newvals.put("payee", pname);
						newvals.put("paidfor", mname);
						newvals.put("groupid", groupId);
						newvals.put("expenseid", expId);
						newvals.put("amount", amt);
						newvals.put("status", 0);
						Log.d("values", "################## " + pname + mname
								+ expId + amt);
						try {
							mydb.insertOrThrow("Expenses", null, newvals);
						} catch (Exception e) {
							Log.e("insert", e.getMessage());
						}

					}
					ename.setText("");
					amount.setText("");
					paidMembers = new Members();
					payeeMembers = new Members();
					paidforIds.clear();
					Toast t = Toast.makeText(LogExpenses.this,
							"Expense Saved!", 1000);
					t.show();
				} else {
					Toast t = Toast.makeText(LogExpenses.this,
							"All Fields are compulsary", 1000);
					t.show();
				}
				payee.setText("Payee");

			}
		});
		Button done = (Button) findViewById(R.id.doneexpense);
		done.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				pd.setMessage("generating reports and sending info..");
				pd.show();
				SmsManager sender = SmsManager.getDefault();
				ArrayList<String> names = members.getName();
				int r;
				try {
					r = mydb.delete("Report", "groupid = '" + groupId + "'",
							null);
				} catch (Exception e) {
					Log.e("delete", e.getMessage());
				}
				for (int i = 0; i < names.size(); i++) {
					for (int j = 0; j < names.size(); j++) {
						if (j == i)
							continue;
						Cursor cursor = mydb.rawQuery("SELECT * FROM "
								+ "Expenses" + " WHERE groupid='" + groupId
								+ "'" + " And " + "paidfor=" + "'"
								+ names.get(i) + "'" + " And " + "payee=" + "'"
								+ names.get(j) + "'", null);
						int rows = cursor.getCount();
						if (rows == 0)
							continue;
						cursor.moveToFirst();
						int total = 0;
						while (true) {
							total += Integer.parseInt(cursor.getString(cursor
									.getColumnIndex("amount")));
							if (cursor.isLast())
								break;
							else
								cursor.moveToNext();
						}
						ContentValues newvals = new ContentValues();
						newvals.put("who", names.get(i));
						newvals.put("whom", names.get(j));
						newvals.put("groupid", groupId);
						newvals.put("amount", Integer.toString(total));
						if (!(members.getNumber().get(i)
								.equalsIgnoreCase("null"))) {
							Log.d("send message", "came in");
							try {
								sender.sendTextMessage(members.getNumber().get(
										i), null, "Please pay " + names.get(j)
										+ " " + total, null, null);
							} catch (Exception e) {
								e.printStackTrace();
							}
						} else {
							Log.d("send message", "error : "
									+ members.getNumber().get(i));
						}
						/*
						 * if(i == 0) { int id = (int)Math.random()*100000;
						 * dbhelper.insertCredit(names.get(j),
						 * Integer.toString(total), ""+id); } if(j == 0) { int
						 * id = (int)Math.random()*100000;
						 * dbhelper.insertDebit(names.get(i),
						 * Integer.toString(total), ""+id); }
						 */

						Log.d("values", "################## " + names.get(i)
								+ names.get(j) + total);
						try {
							mydb.insertOrThrow("Report", null, newvals);
						} catch (Exception e) {
							Log.e("insert", "###############" + e.getMessage());
						}
					}
				}

				for (int i = 1; i < (members.getId().size() - 1); i++) {

					int amt = 0;
					Cursor cursor = mydb.rawQuery("SELECT * FROM Expenses WHERE paidfor='"
							+ members.getName().get(i) + "' AND payee='"
							+ members.getName().get(0) + "' AND status=0", null);
					int amtIndex = cursor.getColumnIndex("amount");
					
					if (cursor.getCount() > 0) {
						cursor.moveToFirst();
						while (true) {
							amt += Integer.parseInt(cursor.getString(amtIndex));
							if (cursor.isLast())
								break;
							cursor.moveToNext();
						}
					}
					cursor = mydb.rawQuery("SELECT * FROM Expenses WHERE payee='"
							+ members.getName().get(i) + "' AND paidfor='"
							+ members.getName().get(0) + "' AND status=0", null);
					amtIndex = cursor.getColumnIndex("amount");
					
					if (cursor.getCount() > 0) {
						cursor.moveToFirst();
						while (true) {
							amt -= Integer.parseInt(cursor.getString(amtIndex));
							if (cursor.isLast())
								break;
							cursor.moveToNext();
						}
					}
					if (amt > 0) {
						int id = (int)(Math.random()*10000);
						dbhelper.insertDebit(members.getName().get(i), ""+amt, ""+id);
						/*debAmount.add("" + amt);
						debFrom.add(members.getName().get(i));*/
					} else if (amt < 0) {
						int id = (int)(Math.random()*100000);
						dbhelper.insertCredit(members.getName().get(i), ""+(-amt), ""+id);
/*						creditAmount.add("" + amt);
						creditTo.add(members.getName().get(i));
*/					}
				}
				ContentValues newvals = new ContentValues();
				newvals.put("status", 1);
				mydb.update("Expenses", newvals, "status=0", null);
				pd.dismiss();
				mydb.close();
				dbhelper.close();
				finish();
			}
		});
		pd = new ProgressDialog(this);
	}

	ProgressDialog pd;
	EditText ename;
	EditText amount;
	Builder payeeDialog;
	Builder paidForDialog;
	Members payeeMembers;
	Members paidMembers;
	ArrayList<Integer> paidforIds = new ArrayList<Integer>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
//		mydb.close();
//		dbhelper.close();
		super.onDestroy();
	}

}
