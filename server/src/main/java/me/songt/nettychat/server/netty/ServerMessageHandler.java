package me.songt.nettychat.server.netty;

import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import me.songt.nettychat.Constants;
import me.songt.nettychat.entity.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Sharable
public class ServerMessageHandler extends SimpleChannelInboundHandler<String>
{
    private Logger logger = LoggerFactory.getLogger(ServerMessageHandler.class);

    public static Map<String, ChannelId> onlineUser = new HashMap<>();

    public static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private Gson gson = new Gson();

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception
    {
        super.handlerAdded(ctx);
        Channel income = ctx.channel();
        logger.info("Client " + income.remoteAddress().toString() + " connected.");
        group.add(income);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception
    {
        super.handlerRemoved(ctx);
        Channel out = ctx.channel();
        logger.info("Client " + out.remoteAddress().toString() + " disconnected.");
        group.remove(out);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, String s) throws Exception
    {
        if (s != null && !s.isEmpty())
        {
            Message message = gson.fromJson(s, Message.class);
            //Boardcast message
            if (message.getTo().equals(Constants.BROADCAST_MESSAGE))
            {
                ChannelId incomeId = channelHandlerContext.channel().id();
                String nick = message.getFrom();
                String content = message.getContent();
                if (content.equals(Constants.BOARDCAST_ONLINE_CONTENT))
                {
                    if (onlineUser.containsKey(nick))
                    {
                        channelHandlerContext.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                        logger.warn("Client nick " + nick + " does exist.");
                        return;
                    }
                    onlineUser.put(nick, incomeId);
                    logger.info(nick + " Online");
                    Message userBoardcast = new Message();
                    userBoardcast.setFrom(Constants.BROADCAST_MESSAGE);
                    userBoardcast.setContent(gson.toJson(onlineUser.keySet().toArray()));
                    String serializedMessage = gson.toJson(userBoardcast);
                    group.writeAndFlush(Unpooled.copiedBuffer(serializedMessage, CharsetUtil.UTF_8));
//                    channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer(serializedMessage, CharsetUtil.UTF_8));
                }
                else if (content.equals(Constants.BOARDCAST_OFFLINE_CONTENT))
                {
                    onlineUser.remove(nick);
                    logger.info(nick + " Offline");
                    Message userBoardcast = new Message();
                    userBoardcast.setFrom(Constants.BROADCAST_MESSAGE);
                    userBoardcast.setContent(gson.toJson(onlineUser.keySet().toArray()));
                    String serializedMessage = gson.toJson(userBoardcast);
                    group.remove(channelHandlerContext.channel());
                    group.writeAndFlush(Unpooled.copiedBuffer(serializedMessage, CharsetUtil.UTF_8));
//                    channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer(serializedMessage, CharsetUtil.UTF_8));
                }
                else if (content.equals(Constants.BOARDCAST_LIST_ONLINE_CONTENT))
                {
                    Message userBoardcast = new Message();
                    userBoardcast.setFrom(Constants.BROADCAST_MESSAGE);
                    userBoardcast.setContent(gson.toJson(onlineUser.keySet().toArray()));
                    String serializedMessage = gson.toJson(userBoardcast);
                    channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer(serializedMessage, CharsetUtil.UTF_8));
                }
            }
            else
            {
                String to = message.getTo();
                ChannelId toChannelId = onlineUser.get(to);
                Channel toChannel = group.find(toChannelId);
                if (toChannel != null)
                {
                    toChannel.writeAndFlush(Unpooled.copiedBuffer(s, CharsetUtil.UTF_8));
                }
            }
        }
    }
}
