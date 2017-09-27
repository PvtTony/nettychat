package me.songt.nettychat.server;

import me.songt.nettychat.server.netty.ChatServer;

public class MainServer
{
    public static void main(String[] args)
    {
        ChatServer chatServer = new ChatServer();
        try
        {
            chatServer.start();
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
