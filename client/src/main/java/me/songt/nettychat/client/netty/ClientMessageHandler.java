package me.songt.nettychat.client.netty;

import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import me.songt.nettychat.Constants;
import me.songt.nettychat.client.proc.SharedData;
import me.songt.nettychat.entity.Message;

import java.util.concurrent.BlockingQueue;

public class ClientMessageHandler extends SimpleChannelInboundHandler<String>
{
    private final String nickName;
    private Gson gson = new Gson();

    public ClientMessageHandler(String nickName)
    {
        this.nickName = nickName;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        super.channelActive(ctx);
        Message message = new Message();
        message.setFrom(nickName);
        message.setTo(Constants.BROADCAST_MESSAGE);
        message.setContent(Constants.BOARDCAST_ONLINE_CONTENT);
        String initMessage = gson.toJson(message);
        System.out.println(initMessage);
        ctx.writeAndFlush(Unpooled.copiedBuffer(initMessage, CharsetUtil.UTF_8));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception
    {
        if (s != null && !s.isEmpty())
        {
//            System.out.println(s);
            Message message = gson.fromJson(s, Message.class);
            //put into income message queue.
            if (message != null)
            {
                BlockingQueue<Message> queue = SharedData.getInstance().getIncomeMessageQueue();
                queue.put(message);
            }
        }
    }
}
