package com.example.omercohen.minichat.Receivers;

import com.backendless.push.BackendlessBroadcastReceiver;
import com.backendless.push.BackendlessPushService;


import com.example.omercohen.minichat.Services.MyPushService;

public class MyPushReceiver extends BackendlessBroadcastReceiver
{
    @Override
    public Class <? extends BackendlessPushService> getServiceClass()
    {
        return MyPushService.class;
    }
}