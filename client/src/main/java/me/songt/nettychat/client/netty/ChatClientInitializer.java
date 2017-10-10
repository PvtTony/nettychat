package me.songt.nettychat.client.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

import static me.songt.nettychat.client.netty.ChatClient.WRITE_WAIT_SECONDS;

public class ChatClientInitializer extends ChannelInitializer<Channel>
{
    private final String nickName;

    public ChatClientInitializer(String nickName)
    {
        this.nickName = nickName;
    }

    @Override
    protected void initChannel(Channel channel) throws Exception
    {

        ChannelPipeline channelPipeline = channel.pipeline();
        channelPipeline.addLast("decoder", new StringDecoder());
        channelPipeline.addLast("encoder", new StringEncoder());
        channelPipeline.addLast("ping", new IdleStateHandler(0, WRITE_WAIT_SECONDS, 0, TimeUnit.SECONDS));
        channelPipeline.addLast("handler", new ClientMessageHandler(nickName));
    }
}
