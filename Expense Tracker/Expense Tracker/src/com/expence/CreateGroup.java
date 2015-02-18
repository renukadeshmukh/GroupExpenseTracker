package com.expence;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.facebook.android.Facebook;

public class CreateGroup extends Activity {

	DatePicker dpciker;
	EditText groupName;
	Spinner currency;
	Button fb;
	Button event;
	Button phonebook;
	Button done;
	Facebook facebook;
	Members members;
	DataBaseHelper dbHelper;
	String selectedCurrency = "INR";
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.creategroup);
		members = new Members();
		dbHelper = new DataBaseHelper("Expense_tracker", "Groups", this);
		dpciker = (DatePicker)findViewById(R.id.datepicker);
		groupName =  (EditText)findViewById(R.id.groupname);
		currency = (Spinner)findViewById(R.id.currency);
		fb = (Button)findViewById(R.id.fbadd);
		phonebook = (Button)findViewById(R.id.contactadd);
		event = (Button)findViewById(R.id.eventadd);
		done = (Button)findViewById(R.id.done);
		facebook = Home.facebook;
		try {
			String response  = facebook.request("me");
			JSONObject jo = new JSONObject(response);
			members.add(jo.getString("first_name")+" "+jo.getString("last_name"), "null", jo.getString("id"));
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
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
	            this, R.array.currency, android.R.layout.simple_spinner_item);
	    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    currency.setAdapter(adapter);	    
	    currency.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View arg1,
					int arg2, long arg3) {
					
				selectedCurrency = parent.getItemAtPosition(arg2).toString();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		fb.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				getFbFriends();
			}
		});
		phonebook.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intentContact = new Intent(Intent.ACTION_PICK,
						ContactsContract.Contacts.CONTENT_URI);
				startActivityForResult(intentContact, 1);
				
			}
		});
		event.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getEventFriends();
			}
		});
		
		done.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				createGroup();
				
			}
		});
		main = (ScrollView)findViewById(R.id.main);
		listcontainer = (RelativeLayout)findViewById(R.id.listcontainer);
		add = (Button)findViewById(R.id.admembers);
		add.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				listcontainer.setVisibility(View.INVISIBLE);
				main.setVisibility(View.VISIBLE);
				for(int i=0;i<checked.size();i++)
				{
					Log.d("members", "###########################"+i +friendName.get(checked.get(i)));
					members.add(friendName.get(checked.get(i)), "null", friendId.get(checked.get(i)));
				}
			}
		});
		cancel = (Button)findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				listcontainer.setVisibility(View.INVISIBLE);
				main.setVisibility(View.VISIBLE);
			}
		});
		Button back = (Button)findViewById(R.id.backtomain);
		back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dbHelper.close();
				finish();				
			}
		});
	}
	Button cancel;
	
	void createGroup()
	{
		if(groupName.getText().length() > 0 )
		{
			/*ByteArrayOutputStream babuff = new ByteArrayOutputStream();
			ObjectOutputStream objOut;
			try {
				objOut = new ObjectOutputStream(babuff);
				objOut.writeObject(members);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.e("serious","##########################"+ e.toString());
				e.printStackTrace();
			}*/
			String gid = Integer.toString(((int)(Math.random()*100000)));
			dbHelper.Insert(groupName.getText().toString(), dpciker.toString(), selectedCurrency, gid);
			dbHelper.insertMembers(members, gid);
			Toast t  = Toast.makeText(this, "Group Created", 1000);
			t.show();
			dbHelper.close();
			SmsManager sender = SmsManager.getDefault();
			int i = 0;
			while(i<members.getName().size())
			{
				if(!members.getNumber().get(i).equalsIgnoreCase("null"))
				{
					Log.d("send message", " "+ members.getNumber().get(i));
					sender.sendTextMessage(members.getNumber().get(i), null, "You have been added to group "+groupName.getText().toString(),null,null);
				}
				else
				{
					Log.d("send message", "error : "+ members.getNumber().get(i));
				}
				i++;
			}
			
			
			finish();
		}
		else
		{
			Toast t  = Toast.makeText(this, "Please Enter Group Name", 1000);
			t.show();
		}
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == 1 && data != null)
		{			
			getContactInfo(data);
		}
	}



	void getFbFriends()
	{
		try {
			listcontainer.setVisibility(View.VISIBLE);
			main.setVisibility(View.INVISIBLE);
		
			String response = facebook.request("me/friends");
			showAttending(response);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void getContactInfo(Intent intent) {

		Cursor cursor = managedQuery(intent.getData(), null, null, null, null);
		while (cursor.moveToNext()) {
			String contactId = cursor.getString(cursor
					.getColumnIndex(ContactsContract.Contacts._ID));
			String name = cursor
					.getString(cursor
							.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));

			String hasPhone = cursor
					.getString(cursor
							.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

			if (hasPhone.equalsIgnoreCase("1"))
				hasPhone = "true";
			else
				hasPhone = "false";
			Log.d("name", name);
			String phoneNumber = "null";
			if (Boolean.parseBoolean(hasPhone)) {
				Cursor phones = getContentResolver().query(
						ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						null,
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID
								+ " = " + contactId, null, null);
				while (phones.moveToNext()) {
					phoneNumber = phones
							.getString(phones
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					Log.d("number", phoneNumber);
				}
				
				phones.close();
			}
			members.add(name, phoneNumber, "null");
		} // while (cursor.moveToNext())
		cursor.close();
	}// getContactInfo
	Button add;
	ScrollView main;
	RelativeLayout listcontainer;
	void getEventFriends()
	{
		
		main.setVisibility(View.INVISIBLE);
		
		listcontainer.setVisibility(View.VISIBLE);
		
		add.setVisibility(View.INVISIBLE);
		getData();
	}
	
	/*
	 * event thingy
	 */
	
	void showAttending(String response) {
		JSONObject outer;
		friendId.clear();
		friendName.clear();
		add.setVisibility(View.VISIBLE);
		try {
			outer = new JSONObject(response);
			final JSONArray data = outer.getJSONArray("data");
			String[] events = new String[data.length()];
			for (int i = 0; i < data.length(); i++) {
				events[i] = ((JSONObject) data.get(i)).getString("name");
				friendName.add(((JSONObject) data.get(i)).getString("name"));
				friendId.add(((JSONObject) data.get(i)).getString("id"));
			}
			ListView lv = (ListView) findViewById(R.id.eventlist);
/*			lv.setAdapter(new ArrayAdapter<String>(this, R.layout.list,
							events));*/
			lv.setAdapter(new EventsAdapter(this));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		first = false;
	}
	
	boolean first = true;

	void showEvents(String response) {
		JSONObject outer;
		try {
			outer = new JSONObject(response);
			final JSONArray data = outer.getJSONArray("data");
			String[] events = new String[data.length()];
			for (int i = 0; i < data.length(); i++) {
				events[i] = ((JSONObject) data.get(i)).getString("name");
			}
			ListView lv = (ListView) findViewById(R.id.eventlist);
			lv.setAdapter(new ArrayAdapter<String>(this, R.layout.textlist,
							events));
			lv.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int pos, long arg3) {
					try {
						String id = ((JSONObject) data.get(pos))
								.getString("id");
						String response = facebook.request(id + "/attending");
						Log.d("attending", response);
						showAttending(response);
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (MalformedURLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			});

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		first = false;
	}

	String stringResponse;

	void getData() {
		if (facebook.isSessionValid()) {
			try {
				String response = facebook.request("me/events");
				stringResponse = response;
				showEvents(response);
				Log.d("events", response);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (KeyEvent.KEYCODE_BACK == keyCode && !first) {
			main.setVisibility(View.VISIBLE);			
			listcontainer.setVisibility(View.INVISIBLE);			
			add.setVisibility(View.VISIBLE);
			return true;
		} else
			return super.onKeyDown(keyCode, event);

	}

	class EventsAdapter extends BaseAdapter {

		Context context;
		LayoutInflater inflater;
		JSONArray data;
		String[] events;
		String [] ids;
		public EventsAdapter(Context ctx)
		{
			context = ctx;
	    	inflater = LayoutInflater.from(ctx);
	    	checked = new ArrayList<Integer>();
		
		}
		public EventsAdapter(Context ctx, String response) {
			context = ctx;
	    	inflater = LayoutInflater.from(ctx);
			try {
				JSONObject outer = new JSONObject(response);
				data = outer.getJSONArray("data");
				
				events = new String[data.length()];
				ids = new String[data.length()];
				for (int i = 0; i < data.length(); i++) {

					events[i] = ((JSONObject) data.get(i)).getString("name");
					ids[i] = ((JSONObject) data.get(i)).getString("id");					
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return friendName.size();
		}

		@Override
		public Object getItem(int arg0) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// TODO Auto-generated method stub
			return arg0;
		}

		@Override
		public View getView(int pos, View view, ViewGroup arg2) {
			// TODO Auto-generated method stub
			view = inflater.inflate(R.layout.list, null);
		//	Log.d("inside", "came here"+arg0);
			TextView name = (TextView) view.findViewById(R.id.textview);
			name.setText(friendName.get(pos));
			CheckBox cbox = (CheckBox)view.findViewById(R.id.checkbox);
			cbox.setTag(pos);
			cbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					int pos = (Integer)buttonView.getTag();
					if(checked.contains(pos)&& !isChecked)
					{
						checked.remove(checked.indexOf(pos));
					}
					else if(!checked.contains(pos)&& isChecked)
					{
						checked.add(pos);
					}
				}
			});
			return view;
		}
	}	
	
	ArrayList<String> friendName = new ArrayList<String>();	
	ArrayList<String> friendId = new ArrayList<String>();
	ArrayList<Integer> checked = new ArrayList<Integer>();
	/* (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		dbHelper.close();
		super.onDestroy();
	}
	
}
