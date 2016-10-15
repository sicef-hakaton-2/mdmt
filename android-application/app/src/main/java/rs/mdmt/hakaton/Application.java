package rs.mdmt.hakaton;

import android.content.Context;

public class Application extends android.app.Application
{
    public static final String SHARED_PREF_KEY = "PREF";

    public static final int HELPER = 0;
    public static final int NEED_HELP = 1;

    private static Application instance;

    public Application()
    {
        instance = this;
        ServerConnection.open();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    public static Context getContext()
    {
        return instance;
    }
}
