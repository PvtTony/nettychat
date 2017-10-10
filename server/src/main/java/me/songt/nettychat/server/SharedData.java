package me.songt.nettychat.server;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

public class SharedData
{
    private static final SharedData sharedData = new SharedData();

    private OnlineUsers onlineUser = new OnlineUsers();

    private ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    public static SharedData getInstance()
    {
        return sharedData;
    }

    public OnlineUsers getOnlineUser()
    {
        return onlineUser;
    }

    public ChannelGroup getGroup()
    {
        return group;
    }

}
