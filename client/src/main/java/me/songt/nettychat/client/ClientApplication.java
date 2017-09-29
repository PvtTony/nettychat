package me.songt.nettychat.client;

import me.songt.nettychat.client.view.MainWindow;

import javax.swing.*;

public class ClientApplication
{
    public static void main(String[] args)
    {
        JFrame frame = new JFrame("ChatClient");
        frame.setContentPane(new MainWindow().getMainPanel());
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
