package me.songt.nettychat.client.proc;

import me.songt.nettychat.entity.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class SharedData
{
    private static final SharedData sharedData = new SharedData();
    private BlockingQueue<Message> incomeMessageQueue;
    private BlockingQueue<Message> outgoMessageQueue;

    public SharedData()
    {
        incomeMessageQueue = new LinkedBlockingQueue<>();
        outgoMessageQueue = new LinkedBlockingQueue<>();
    }

    public static SharedData getInstance()
    {
        return sharedData;
    }

    public BlockingQueue<Message> getIncomeMessageQueue()
    {
        return incomeMessageQueue;
    }

    public BlockingQueue<Message> getOutgoMessageQueue()
    {
        return outgoMessageQueue;
    }
}
