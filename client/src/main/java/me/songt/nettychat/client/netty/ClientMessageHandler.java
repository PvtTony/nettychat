package me.songt.nettychat.client.netty;

import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import me.songt.nettychat.Constants;
import me.songt.nettychat.client.model.SharedData;
import me.songt.nettychat.entity.Message;

import java.util.concurrent.BlockingQueue;

import static me.songt.nettychat.client.netty.ChatClient.MAX_UNRECV_PONG_COUNT;

public class ClientMessageHandler extends SimpleChannelInboundHandler<String>
{
    private final String nickName;
    private Gson gson = new Gson();
    private int unRecvPongCount = 0;
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
//        System.out.println(initMessage);
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
                if (message.getFrom().equals(Constants.BROADCAST_MESSAGE) && message.getContent().equals(Constants.BOARDCAST_PONG_CONTENT))
                {
                    System.out.println("Received pong!");
                    unRecvPongCount = 0;
                }
                else
                {
                    BlockingQueue<Message> queue = SharedData.getInstance().getIncomeMessageQueue();
                    queue.put(message);
                }
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {
//        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent)
        {
            IdleStateEvent event = (IdleStateEvent) evt;
            switch (event.state())
            {
                case READER_IDLE:
                    System.out.println("Read timeout.");
                    break;
                case WRITER_IDLE:
                    System.out.println("Write timeout. Sending ping.");
                    if (unRecvPongCount < MAX_UNRECV_PONG_COUNT && SharedData.getInstance().isOnline() && ctx.channel().isActive())
                    {
                        Message ping = new Message();
                        ping.setFrom(nickName);
                        ping.setTo(Constants.BROADCAST_MESSAGE);
                        ping.setContent(Constants.BOARDCAST_PING_CONTENT);
//                        SharedData.getInstance().getOutgoMessageQueue().put(ping);
                        ctx.channel().writeAndFlush(Unpooled.copiedBuffer(gson.toJson(ping), CharsetUtil.UTF_8));
                        unRecvPongCount++;
                    }
                    else
                    {
                        System.out.println("Too much unreceived pong! disconnected.");
                        ctx.channel().close();
                        SharedData.getInstance().setOnline(false);
                    }
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception
    {
        super.channelInactive(ctx);
        SharedData.getInstance().setOnline(false);
        System.out.println("Channel deactivated.");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
}
