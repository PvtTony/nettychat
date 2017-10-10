package me.songt.nettychat.server.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import static me.songt.nettychat.server.netty.ChatServer.READ_WAIT_SECONDS;

public class ChatServerInitializer extends ChannelInitializer<Channel>
{
    @Override
    protected void initChannel(Channel channel) throws Exception
    {
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("decoder", new StringDecoder());
        pipeline.addLast("encoder", new StringEncoder());
        pipeline.addLast("pong", new IdleStateHandler(READ_WAIT_SECONDS, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast("handler", new ServerMessageHandler());

    }
}
