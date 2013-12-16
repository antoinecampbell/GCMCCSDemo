package com.antoinecampbell.gcmserver;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "users")
public class User
{
    @DatabaseField(id=true)
    private String gcm_id;
    @DatabaseField(index = true)
    private String name;
    
    public User()
    {
    }
    
    public User(String name, String gcm_id)
    {
	this.name = name;
	this.gcm_id = gcm_id;
    }

    public String getGcm_id()
    {
        return gcm_id;
    }

    public void setGcm_id(String gcm_id)
    {
        this.gcm_id = gcm_id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    @Override
    public String toString()
    {
	return name + " : " + gcm_id;
    }
    
    
}
