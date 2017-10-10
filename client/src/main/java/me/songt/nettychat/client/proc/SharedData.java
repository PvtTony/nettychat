package me.songt.nettychat.client.proc;

import me.songt.nettychat.entity.Message;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class SharedData
{
    private static final SharedData sharedData = new SharedData();
    private BlockingQueue<Message> incomeMessageQueue;
    private BlockingQueue<Message> outgoMessageQueue;

    private boolean isOnline = false;
    private CountDownLatch onlineLatch = new CountDownLatch(1);

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

    public synchronized void setOnline(boolean status)
    {
        this.isOnline = status;
        if (status && this.onlineLatch.getCount() > 0)
        {
            onlineLatch.countDown();
        }
        else if (!status)
        {
            onlineLatch = new CountDownLatch(1);
        }
    }

    public boolean checkOnlineStatus(int timeOut)
    {
        if (this.isOnline)
        {
            return true;
        }
        else
        {
            try
            {
                this.onlineLatch.await(timeOut, TimeUnit.SECONDS);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
            return this.isOnline;
        }
    }

    public boolean isOnline()
    {
        return isOnline;
    }
}
