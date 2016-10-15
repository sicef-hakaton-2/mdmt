package rs.mdmt.hakaton;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class Task
{
    public static final int TOTAL = 2;

    public static final int SERVER = 0;
    public static final int GENERAL = 1;

    public int type;

    public Task()
    {
    }

    public Task(int t)
    {
        type = t;
    }

    public abstract void execute();

    public static abstract class Ui extends Task
    {
        public Ui()
        {
        }

        public Ui(int t)
        {
            super(t);
        }

        public abstract void uiExecute();
    }

    public static abstract class Waitable extends Ui
    {
        protected int responseCode;
        protected String responseMessage;

        public Waitable()
        {
            type = SERVER;
        }

        @Override
        public void execute()
        {
            if (ServerConnection.isOpen())
            {
                ServerConnection.getHandler().setWaitingTask(this);
                executeImpl();
                synchronize();
            }
            else
            {
                setResponseCode(ServerConnection.Response.FAILURE);
                setResponseMessage("Server not available, try again");
            }
        }

        public abstract void executeImpl();

        public void setResponse(String r)
        {
            try
            {
                JSONObject json = new JSONObject(r);
                JSONObject data = json.getJSONObject("data");

                responseCode = data.getInt("code");
                responseMessage = data.getString("message");
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        public void setResponseCode(int code)
        {
            responseCode = code;
        }

        public int getResponseCode()
        {
            return responseCode;
        }

        public void setResponseMessage(String msg)
        {
            responseMessage = msg;
        }

        public String getResponseMessage()
        {
            return responseMessage;
        }

        protected void synchronize()
        {
            synchronized (this)
            {
                try
                {
                    this.wait();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    public static class Data extends Waitable
    {
        public static abstract class DataReadyCallback
        {
            private JSONArray data;

            public abstract void dataReady();

            public void setData(JSONArray d)
            {
                data = d;
            }

            public JSONArray getData()
            {
                return data;
            }
        }

        protected JSONArray data;
        protected DataReadyCallback callback;

        public Data(DataReadyCallback c)
        {
            callback = c;
        }

        @Override
        public void executeImpl()
        {
            ServerConnection.getData();
        }

        @Override
        public void uiExecute()
        {
        }

        @Override
        public void setResponse(String r)
        {
            try
            {
                JSONObject json = new JSONObject(r);

//                setResponseCode(json.getInt("code"));
//                setResponseMessage(json.getString("message"));

                data = json.getJSONArray("data");
                callback.setData(data);
                callback.dataReady();
            }
            catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        public JSONArray getData()
        {
            return data;
        }

    }
}
