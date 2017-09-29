package me.songt.nettychat.client.netty;

import com.google.gson.Gson;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import me.songt.nettychat.Constants;
import me.songt.nettychat.client.proc.SharedData;
import me.songt.nettychat.entity.Message;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;

public class ChatClient
{
    private final String host;
    private final int port;
    private final String nickName;
    private EventLoopGroup group = new NioEventLoopGroup();
    private Channel channel;
    private ChannelFuture future;
    Gson gson = new Gson();

    public ChatClient(String host, int port, String nickName)
    {
        this.host = host;
        this.port = port;
        this.nickName = nickName;
    }

    public ChannelFuture start() throws InterruptedException, ConnectException
    {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(host, port))
                .handler(new ChatClientInitializer(nickName));
        future = bootstrap.connect().sync();
        channel = future.channel();
        return future;
    }

    public void close()
    {
        if (channel != null)
        {
            channel.close();
        }
    }

    public void destory()
    {
        group.shutdownGracefully();
    }

    public void putMessage(Message message) throws InterruptedException
    {
        BlockingQueue<Message> out = SharedData.getInstance().getOutgoMessageQueue();
        out.put(message);
    }

    public ChannelFuture getFuture()
    {
        return future;
    }

    public Channel getChannel()
    {
        return channel;
    }

    public boolean isConnectionActive()
    {
        return (future != null) && (channel != null) && (channel.isOpen());
    }

    public void offline() throws InterruptedException
    {
        Message message = new Message();
        message.setFrom(nickName);
        message.setTo(Constants.BROADCAST_MESSAGE);
        message.setContent(Constants.BOARDCAST_OFFLINE_CONTENT);
        channel.writeAndFlush(Unpooled.copiedBuffer(gson.toJson(message), CharsetUtil.UTF_8)).addListener(ChannelFutureListener.CLOSE);
    }

    public void listOnlineUser() throws InterruptedException
    {
        Message message = new Message();
        message.setFrom(nickName);
        message.setTo(Constants.BROADCAST_MESSAGE);
        message.setContent(Constants.BOARDCAST_LIST_ONLINE_CONTENT);
        putMessage(message);
    }

}
