package me.songt.nettychat.client.view;

import me.songt.nettychat.entity.Message;

import javax.swing.*;

public interface ChatWindow
{
    JPanel getMainPanel();

    void insertInboxMessageItem(Message message);

    void updateOnlineUser(String[] onlineUserArr);

    void insertSentMessageItem(Message message);
}
