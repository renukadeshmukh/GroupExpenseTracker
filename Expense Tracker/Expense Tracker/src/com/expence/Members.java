package com.expence;

import java.io.Serializable;
import java.util.ArrayList;



public class Members implements Serializable{
 	/**
	 * 
	 */
	private static final long serialVersionUID = -4038920927960364485L;
	private ArrayList<String> name;
	private ArrayList<String> number;
	private ArrayList<String> id;
	
	public Members()
	{
		name = new ArrayList<String>();
		number = new ArrayList<String>();
		id = new ArrayList<String>();
	}
	
	public void add(String name,String number,String id)
	{
		for(int i=0;i<this.name.size();i++)
		{
			if(this.name.get(i).equalsIgnoreCase(name))
				return;
		}
		this.name.add(name);
		this.number.add(number);
		this.id.add(id);
	}
	
	/**
	 * @return the name
	 */
	public ArrayList<String> getName() {
		return name;
	}

	/**
	 * @return the number
	 */
	public ArrayList<String> getNumber() {
		return number;
	}

	/**
	 * @return the number
	 */
	public ArrayList<String> getId() {
		return id;
	}
	
}
