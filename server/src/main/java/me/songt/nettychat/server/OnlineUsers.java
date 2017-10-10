package me.songt.nettychat.server;

import io.netty.channel.ChannelId;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class OnlineUsers
{
    private Map<String, ChannelId> nickToChannel;
    private Map<ChannelId, String> channelToNick;

    public OnlineUsers()
    {
        this.nickToChannel = new ConcurrentHashMap<>();
        this.channelToNick = new ConcurrentHashMap<>();
    }

    public boolean containsUser(String nickname)
    {
        return nickToChannel.containsKey(nickname);
    }

    public boolean containsChannel(ChannelId channelId)
    {
        return channelToNick.containsKey(channelId);
    }

    public boolean put(String nickname, ChannelId channelId)
    {
        if (!containsUser(nickname) && !containsChannel(channelId))
        {
            nickToChannel.put(nickname, channelId);
            channelToNick.put(channelId, nickname);
            return true;
        }
        return false;
    }

    public ChannelId get(String nickName)
    {
        if (containsUser(nickName))
        {
            return nickToChannel.get(nickName);
        }
        return null;
    }

    public String get(ChannelId channelId)
    {
        if (containsChannel(channelId))
        {
            return channelToNick.get(channelId);
        }
        return null;
    }

    public void remove(String nickname)
    {
        if (containsUser(nickname))
        {
            ChannelId channelId = nickToChannel.get(nickname);
            if (containsChannel(channelId))
            {
                channelToNick.remove(channelId);
            }
            nickToChannel.remove(nickname);
        }
    }

    public void remove(ChannelId channelId)
    {
        if (containsChannel(channelId))
        {
            String nick = channelToNick.get(channelId);
            if (containsUser(nick))
            {
                nickToChannel.remove(nick);
            }
            channelToNick.remove(channelId);
        }
    }

    public Object[] getNickArray()
    {
        return nickToChannel.keySet().toArray();
    }


}
