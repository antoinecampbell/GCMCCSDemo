package com.antoinecampbell.gcmdemo;

public class Globals
{
    public static final String TAG = "GCM DEMO";

    public static final String GCM_SENDER_ID = "Your ProjectID";
    
    public static final String PREFS_NAME = "GCM_DEMO";
    public static final String PREFS_PROPERTY_REG_ID = "registration_id";
    
    public static final long GCM_TIME_TO_LIVE = 60L * 60L * 24L * 7L * 4L; // 4 Weeks
}