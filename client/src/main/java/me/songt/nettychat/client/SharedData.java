package me.songt.nettychat.client;

import me.songt.nettychat.entity.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SharedData
{
    public static String[] users;
    public static BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
}
