package me.songt.nettychat.client.netty;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.CharsetUtil;
import me.songt.nettychat.Constants;
import me.songt.nettychat.client.SharedData;
import me.songt.nettychat.entity.Message;

import java.lang.reflect.Type;

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
        message.setContent("Online");
        String initMessage = gson.toJson(message);
        System.out.println(initMessage);
        ctx.writeAndFlush(Unpooled.copiedBuffer(initMessage, CharsetUtil.UTF_8));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception
    {
        if (s != null && !s.isEmpty())
        {
            System.out.println(s);
            Message message = gson.fromJson(s, Message.class);
            //Boardcast
            if (message.getFrom() != null && message.getFrom().equals(Constants.BROADCAST_MESSAGE))
            {
                if (message.getContent() != null)
                {
                    String content = message.getContent();
                    Type arrayType = new TypeToken<String[]>()
                    {
                    }.getType();
                    SharedData.users = gson.fromJson(content, arrayType);
                }
            }
            else
            {
                if (message.getFrom() != null && message.getTo().equals(nickName))
                {
//                    SharedData.messageList.add(message);
                    SharedData.messageQueue.put(message);
                }
            }
        }
    }
}
