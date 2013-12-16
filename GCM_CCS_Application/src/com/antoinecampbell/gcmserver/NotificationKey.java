package com.antoinecampbell.gcmserver;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "notification_key")
public class NotificationKey
{
    @DatabaseField(id = true)
    private String name;
    @DatabaseField
    private String notificaciton_key;
    
    public NotificationKey()
    {
    }

    public NotificationKey(String name, String notificaciton_key)
    {
	this.notificaciton_key = notificaciton_key;
	this.name = name;
    }

    public String getNotificaciton_key()
    {
        return notificaciton_key;
    }

    public void setNotificaciton_key(String notificaciton_key)
    {
        this.notificaciton_key = notificaciton_key;
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
	return name + " : " + notificaciton_key;
    }
    
    
}
