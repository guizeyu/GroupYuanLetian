package com.advancedweb2022groupylt.finalpj.util;

import cn.hutool.core.codec.Base64;
import cn.hutool.log.StaticLog;

import javax.websocket.Session;
import java.io.IOException;

public class SessionUtils
{
    private SessionUtils(){}

    public static void closeSessionWithoutException(Session session)
    {
//        StaticLog.info("closing session {}",session);
        try
        {
            session.close();
        }
        catch (IOException | RuntimeException exception)
        {
            StaticLog.error(exception);
        }
    }

/*    public static void sendMessage(Session session,String message) todo:delete this code
    {
        sendMessage(session, message,false);
    }

    public static void sendMessage(Session session,String message,boolean base64encode)
    {
        if (base64encode) message = Base64.encode(message);
        try
        {
            session.getBasicRemote().sendText(message);
        }
        catch (IOException exception)
        {
            StaticLog.error(exception);
        }
    }*/

    public static void sendMessage(Session session,String message)
    {
        if(session.isOpen())
        {
            try
            {
                session.getBasicRemote().sendText(message);
            }
            catch (IOException | RuntimeException exception)
            {
                StaticLog.error(exception);
            }
        }
    }

    public static void sendMessage(Session session,String message,boolean base64encode)
    {
        sendMessage(session, message);
    }
}
