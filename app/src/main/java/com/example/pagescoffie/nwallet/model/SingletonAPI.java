package com.example.pagescoffie.nwallet.model;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Created by Pages Coffie on 5/22/2018.
 */

public class SingletonAPI {
    private RequestQueue requestQueue;
    private static SingletonAPI classinstance;
    private  static Context context;

    private SingletonAPI(Context cntxt)
    {
        context = cntxt;
        requestQueue = getRequestQueue();
    }

    public RequestQueue getRequestQueue()
    {
        if (requestQueue == null)
        {
            requestQueue = Volley.newRequestQueue(context.getApplicationContext());
        }
        return requestQueue;
    }

    public static synchronized SingletonAPI getClassinstance( Context mycontext)
    {
        if (classinstance == null)
        {
            classinstance = new SingletonAPI(mycontext);
        }
        return classinstance;
    }

    public<T> void addToRequest(Request<T> request)
    {
        requestQueue.add(request);
    }

}
