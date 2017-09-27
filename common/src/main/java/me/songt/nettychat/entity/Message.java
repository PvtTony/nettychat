package me.songt.nettychat.entity;

import java.io.Serializable;

public class Message implements Serializable
{
    private String from;
    private String to;
    private String content;

    public Message()
    {
    }

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getTo()
    {
        return to;
    }

    public void setTo(String to)
    {
        this.to = to;
    }

    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }
}
