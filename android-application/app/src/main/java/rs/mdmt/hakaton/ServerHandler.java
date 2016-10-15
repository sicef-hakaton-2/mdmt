package rs.mdmt.hakaton;

import android.app.*;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.wifi.WifiConfiguration;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import de.tavendo.autobahn.WebSocketHandler;

public class ServerHandler extends WebSocketHandler
{
    private Task.Waitable waitingTask;
    private ResponseCallback responseCallback;

    public interface ResponseCallback
    {
        void shelterUpdate(JSONArray data);
        void routeUpdate(JSONArray data);
    }

    public void setResponseCallback(ResponseCallback responseCallback)
    {
        this.responseCallback = responseCallback;
    }

    @Override
    public void onOpen()
    {
        super.onOpen();

        final SharedPreferences prefs = Application.getContext()
                .getSharedPreferences(Application.SHARED_PREF_KEY, Context.MODE_PRIVATE);
        if (!prefs.contains("userId"))
        {
            Task.Data task = new Task.Data(new Task.Data.DataReadyCallback()
            {
                @Override
                public void dataReady()
                {
                    JSONArray data = getData();

                    try
                    {
                        JSONObject obj = (JSONObject) data.get(0);

                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putString("userId", obj.getString("userId"));
                        editor.commit();
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            })
            {
                @Override
                public void executeImpl()
                {
                    ServerConnection.getUserId();
                }
            };

            TaskManager.getInstance().execute(task);
        }

    }

    @Override
    public void onTextMessage(String payload)
    {
        super.onTextMessage(payload);

        Log.d("WebSocket", payload);

        if (waitingTask != null)
        {
            synchronized (waitingTask)
            {
                waitingTask.setResponse(payload);
                waitingTask.notify();
            }
            waitingTask = null;
        }
        else
            asyncUpdate(payload);
    }

    @Override
    public void onRawTextMessage(byte[] payload)
    {
        super.onRawTextMessage(payload);
    }

    @Override
    public void onBinaryMessage(byte[] payload)
    {
        super.onBinaryMessage(payload);
    }

    @Override
    public void onClose(int code, String reason)
    {
        super.onClose(code, reason);
    }

    public void setWaitingTask(Task.Waitable task)
    {
        waitingTask = task;
    }

    public void asyncUpdate(String payload)
    {
        try
        {
            JSONObject json = new JSONObject(payload);

            int type = json.getInt("type");

            switch(type)
            {
                case ServerConnection.Request.DATA:
                    responseCallback.shelterUpdate(json.getJSONArray("data"));
                    break;
                case ServerConnection.Request.GET_ROUTES:
                    responseCallback.shelterUpdate(json.getJSONArray("data"));

            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }
}
