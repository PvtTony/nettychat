package me.songt.nettychat.client.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

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
        channelPipeline.addLast("handler", new ClientMessageHandler(nickName));
    }
}
