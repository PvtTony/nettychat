package me.songt.nettychat.server.netty;

import com.google.gson.Gson;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import me.songt.nettychat.Constants;
import me.songt.nettychat.entity.Message;
import me.songt.nettychat.server.model.OnlineUsers;
import me.songt.nettychat.server.model.SharedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static me.songt.nettychat.server.netty.ChatServer.MAX_UNREPLY_COUNT;

@Sharable
public class ServerMessageHandler extends SimpleChannelInboundHandler<String>
{
    private Map<String, Integer> unrecvPingCountMap = new ConcurrentHashMap<>();

    private Logger logger = LoggerFactory.getLogger(ServerMessageHandler.class);

    private OnlineUsers onlineUser = SharedData.getInstance().getOnlineUser();

    private ChannelGroup group = SharedData.getInstance().getGroup();

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
        ChannelId channelId = out.id();
        onlineUser.remove(channelId);
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
                switch (content)
                {
                    case Constants.BOARDCAST_ONLINE_CONTENT:
                    {
                        if (onlineUser.containsUser(nick))
                        {
                            channelHandlerContext.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
                            logger.warn("Client nick " + nick + " does exist.");
                            return;
                        }
                        onlineUser.put(nick, incomeId);
                        unrecvPingCountMap.put(nick, 0);
                        logger.info(nick + " Online");
                        Message userBoardcast = new Message();
                        userBoardcast.setFrom(Constants.BROADCAST_MESSAGE);
                        userBoardcast.setContent(gson.toJson(onlineUser.getNickArray()));
                        String serializedMessage = gson.toJson(userBoardcast);
                        group.writeAndFlush(Unpooled.copiedBuffer(serializedMessage, CharsetUtil.UTF_8));
                        break;
                    }
                    case Constants.BOARDCAST_OFFLINE_CONTENT:
                    {
                        onlineUser.remove(nick);
                        unrecvPingCountMap.remove(nick);
                        logger.info(nick + " Offline");
                        Message userBoardcast = new Message();
                        userBoardcast.setFrom(Constants.BROADCAST_MESSAGE);
                        userBoardcast.setContent(gson.toJson(onlineUser.getNickArray()));
                        String serializedMessage = gson.toJson(userBoardcast);
                        group.remove(channelHandlerContext.channel());
                        group.writeAndFlush(Unpooled.copiedBuffer(serializedMessage, CharsetUtil.UTF_8));
                        break;
                    }
                    case Constants.BOARDCAST_LIST_ONLINE_CONTENT:
                    {
                        Message userBoardcast = new Message();
                        userBoardcast.setFrom(Constants.BROADCAST_MESSAGE);
                        userBoardcast.setContent(gson.toJson(onlineUser.getNickArray()));
                        String serializedMessage = gson.toJson(userBoardcast);
                        channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer(serializedMessage, CharsetUtil.UTF_8));
                        break;
                    }
                    case Constants.BOARDCAST_PING_CONTENT:
                    {
                        unrecvPingCountMap.put(nick, 0);
                        Message pong = new Message();
                        pong.setFrom(Constants.BROADCAST_MESSAGE);
                        pong.setContent(Constants.BOARDCAST_PONG_CONTENT);
                        String serializedMessage = gson.toJson(pong);
                        channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer(serializedMessage, CharsetUtil.UTF_8));
                        break;
                    }
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

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception
    {
        super.userEventTriggered(ctx, evt);
        if (evt instanceof IdleStateEvent)
        {
            IdleStateEvent event = (IdleStateEvent) evt;
            ChannelId channelId = ctx.channel().id();
            String nickname = onlineUser.get(channelId);
            int unrecvPingCount = unrecvPingCountMap.get(nickname);
            switch (event.state())
            {
                case READER_IDLE:
                    logger.warn(String.format("Read timeout. Nick: %s, count: %d", nickname, unrecvPingCount + 1));
                    if (unrecvPingCount > MAX_UNREPLY_COUNT)
                    {
                        logger.warn(String.format("Too much ping packets unreceived. Connection of user %s closed.", nickname));
                        onlineUser.remove(channelId);
                        ctx.close();
                    }
                    else
                    {
                        unrecvPingCountMap.put(nickname, ++unrecvPingCount);
                    }
                    break;
                case WRITER_IDLE:
                    logger.warn(String.format("Write timeout. Nick: %s", nickname));
                    break;
                case ALL_IDLE:
                    logger.warn(String.format("All timeout. Nick: %s", nickname));
                    break;
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception
    {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
}
