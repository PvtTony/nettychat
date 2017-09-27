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
import me.songt.nettychat.entity.Message;

import java.net.InetSocketAddress;

public class ChatClient
{
    private final String host;
    private final int port;
    private final String nickName;
    private EventLoopGroup group = new NioEventLoopGroup();
    private Channel channel;
    private ChannelFuture future;
    private Gson gson = new Gson();

    public ChatClient(String host, int port, String nickName)
    {
        this.host = host;
        this.port = port;
        this.nickName = nickName;
    }

    public ChannelFuture start() throws InterruptedException
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

    public ChannelFuture sendMessage(Message message)
    {
        if (channel != null && channel.isActive())
        {
            String msgSerialized = gson.toJson(message);
            return channel.writeAndFlush(Unpooled.copiedBuffer(msgSerialized, CharsetUtil.UTF_8));
        }
        return null;
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
        return (future != null) && (channel != null) && (channel.isActive());
    }

    public void disconnect()
    {
        Message message = new Message();
        message.setFrom(nickName);
        message.setTo(Constants.BROADCAST_MESSAGE);
        message.setContent("Offline");
        sendMessage(message).addListener(ChannelFutureListener.CLOSE);
    }

}
