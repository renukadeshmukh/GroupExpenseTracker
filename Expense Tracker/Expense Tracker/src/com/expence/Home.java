package com.expence;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.FacebookError;
import com.facebook.android.Facebook.DialogListener;
public class Home extends Activity {
    /** Called when the activity is first created. */
	static final String appId  = "143541762383781";
	public static Facebook facebook  = new Facebook(appId);
	String name = "";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Button create = (Button)findViewById(R.id.creategroup);
        create.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Home.this,CreateGroup.class);
				startActivity(i);
				
			}
		});
        facebook.authorize(this, new String[] {"read_stream","publish_stream","email","user_events", "friends_events"  },
        		new DialogListener() {
					
					@Override
					public void onFacebookError(FacebookError e) {
						// TODO Auto-generated method stub					
//						finish();
					}
					
					@Override
					public void onError(DialogError e) {
						// TODO Auto-generated method stub
						
	//					finish();
					}
					
					@Override
					public void onComplete(Bundle values) {
			//			showReport();
						Toast t;
						if(facebook.isSessionValid())
							 t = Toast.makeText(Home.this, "Authorized", 2000);
						else
							t = Toast.makeText(Home.this, "Not Authorized", 2000);
						t.show();
					}
					
					@Override
					public void onCancel() {
						// TODO Auto-generated method stub
		//				finish();
					}
				});     
        Button mydues = (Button)findViewById(R.id.mydues);
        mydues.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Home.this, MyDues.class);
				startActivity(i);				
			}
		});
        Button group = (Button)findViewById(R.id.group);
        group.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showGroups();				
			}
		});
        String[] options = {"Log Expenses","Delete Group","Group report"};
        optionDialog = new AlertDialog.Builder(this).setTitle("Options").setAdapter(new ArrayAdapter<String>(this, R.layout.textlist,
				options), new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(which == 0 && selectedGroup > -1)
						{
							Intent i = new Intent(Home.this,LogExpenses.class);
							i.putExtra("groupid", groupIds[selectedGroup]);
							startActivityForResult(i, 1);
						}
						else if(which == 3 && selectedGroup > -1)
						{
							Intent i = new Intent(Home.this,ClearDues.class);
							i.putExtra("groupid", groupIds[selectedGroup]);							
							startActivityForResult(i, 2);
						}
						else if(which == 1 && selectedGroup > -1)
						{
							dbhelper.delete(groupIds[selectedGroup]);
							showGroups();							
						}
						else if(which == 2 && selectedGroup > -1)
						{
							Intent i = new Intent(Home.this,Report.class);
							i.putExtra("groupid", groupIds[selectedGroup]);							
							startActivityForResult(i, 3);
						}
						dialog.dismiss();						
					}
				});
        Button home = (Button)findViewById(R.id.home);
        home.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {			
		        homeLayout.setVisibility(View.VISIBLE);
		    	groupLayout.setVisibility(View.INVISIBLE);				
			}
		});
        groupLayout = (LinearLayout)findViewById(R.id.groupcontainer);
        homeLayout = (LinearLayout)findViewById(R.id.homecontainer);
        homeLayout.setVisibility(View.VISIBLE);
    	groupLayout.setVisibility(View.INVISIBLE);
    	dbhelper = new DataBaseHelper("Expense_tracker", "Groups", this);
    }
    Builder optionDialog;
    LinearLayout groupLayout;
    LinearLayout homeLayout;
    DataBaseHelper dbhelper;
    String [] groupIds;
    int selectedGroup = -1;
    void showGroups()
    {
    	homeLayout.setVisibility(View.INVISIBLE);
    	groupLayout.setVisibility(View.VISIBLE);
    	Cursor cursor = dbhelper.getData(0);
    	if(cursor == null)
    		return;
    	if(!cursor.isFirst())
    		cursor.moveToFirst();
    	int rows = cursor.getCount();
    	if(rows == 0)
    		return;
    	int nameIndex = cursor.getColumnIndex("name");
    	int idIndex = cursor.getColumnIndex("groupid");
    	String []  groupNames = new String[rows];
    	groupIds = new String[rows];
    	int i = 0;
    	while(true)
    	{
    		groupNames[i] = cursor.getString(nameIndex);
    		groupIds[i++] = cursor.getString(idIndex);
    		Log.d("gid", "####################"+ groupIds[i-1]);
    		if(cursor.isLast())
    			break;
    		else
    			cursor.moveToNext();
    	}
    	ListView lv = (ListView) findViewById(R.id.grouplist);
		lv.setAdapter(new ArrayAdapter<String>(this, R.layout.textlist,
						groupNames));
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int id,
					long arg3) {
				selectedGroup = id;
				optionDialog.show();			
			}
		});
		
    }
    
    void showReport()
    {
    	if(!facebook.isSessionValid())
    		finish();
		try {
			String response  = facebook.request("me");
			JSONObject jo = new JSONObject(response);
			name = jo.getString("name");
			SQLiteDatabase mydb = openOrCreateDatabase("Expense_tracker", MODE_PRIVATE, null);
			mydb
					.execSQL("CREATE TABLE IF NOT EXISTS "
							+ "Expenses"
							+ " (name VARCHAR, payee VARCHAR, paidfor VARCHAR, amount VARCHAR,groupid VARCHAR,expenseid VARCHAR);");
			mydb
					.execSQL("CREATE TABLE IF NOT EXISTS "
							+ "Report"
							+ " (who VARCHAR, whom VARCHAR, amount VARCHAR, groupid VARCHAR);");
			ArrayList<String> reports = new ArrayList<String>();

			Cursor cursor = mydb.rawQuery("SELECT * FROM Report WHERE who='"+name+"'", null);
			int rows = cursor.getCount();
			cursor.moveToFirst();
			
			if (rows > 0) {
				while (true) {
					String from = cursor.getString(cursor.getColumnIndex("who"));
					String to = cursor.getString(cursor.getColumnIndex("whom"));
					String amount = cursor.getString(cursor.getColumnIndex("amount"));
					reports.add("Pay "+amount+" to "+to);
					if(cursor.isLast())
						break;
					else
						cursor.moveToNext();
				}
			}
			ListView lv = (ListView)findViewById(R.id.homereport);
			lv.setAdapter(new ArrayAdapter<String>(Home.this, R.layout.textlist,
					reports));
			mydb.close();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	//	showReport();
	}
    
}