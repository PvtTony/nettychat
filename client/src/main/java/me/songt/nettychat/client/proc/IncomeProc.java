package me.songt.nettychat.client.proc;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.songt.nettychat.Constants;
import me.songt.nettychat.client.model.SharedData;
import me.songt.nettychat.client.netty.ChatClient;
import me.songt.nettychat.client.view.ChatWindow;
import me.songt.nettychat.entity.Message;

import java.lang.reflect.Type;
import java.util.concurrent.BlockingQueue;

;

public class IncomeProc implements Runnable
{
    private final ChatClient chatClient;
    private final ChatWindow window;
    private Gson gson = new Gson();

    public IncomeProc(ChatClient chatClient, ChatWindow window)
    {
        this.chatClient = chatClient;
        this.window = window;
    }

    @Override
    public void run()
    {
        System.out.println("Income Message Processor started.");
        try
        {
            while (true)
            {
                BlockingQueue<Message> queue = SharedData.getInstance().getIncomeMessageQueue();
                Message message = queue.take();
                if (message.getFrom() != null && message.getFrom().equals(Constants.BROADCAST_MESSAGE))
                {
                    String content = message.getContent();
                    Type arrayType = new TypeToken<String[]>()
                    {
                    }.getType();
                    String[] onlineUserArr = gson.fromJson(content, arrayType);
                    SharedData.getInstance().setOnline(true);
                    window.updateOnlineUser(onlineUserArr);
                }
                else
                {
                    window.insertInboxMessageItem(message);
                }
            }
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

}
