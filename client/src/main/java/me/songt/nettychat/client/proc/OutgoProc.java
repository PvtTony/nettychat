package me.songt.nettychat.client.proc;

import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import me.songt.nettychat.client.netty.ChatClient;
import me.songt.nettychat.client.view.ChatWindow;
import me.songt.nettychat.entity.Message;

import java.util.concurrent.BlockingQueue;

public class OutgoProc implements Runnable
{
    private final ChatClient chatClient;
    private final ChatWindow window;
    private Gson gson = new Gson();

    public OutgoProc(ChatClient chatClient, ChatWindow window)
    {
        this.chatClient = chatClient;
        this.window = window;
    }

    @Override
    public void run()
    {
        System.out.println("Outgo Message Processor started.");
        BlockingQueue<Message> queue = SharedData.getInstance().getOutgoMessageQueue();
        while (true)
        {
            try
            {
                Message outMessage = queue.take();
                if (chatClient.isConnectionActive() && outMessage != null)
                {
                    String msgSerialized = gson.toJson(outMessage);
                    chatClient.getChannel().writeAndFlush(Unpooled.copiedBuffer(msgSerialized, CharsetUtil.UTF_8));
                    window.insertSentMessageItem(outMessage);
                }
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

}
