package me.songt.nettychat.client;

import me.songt.nettychat.entity.Message;

import javax.swing.table.DefaultTableModel;

;

public class IncomeMsgProcessor implements Runnable
{

    final DefaultTableModel model;

    public IncomeMsgProcessor(DefaultTableModel model)
    {
        this.model = model;
    }

    @Override
    public void run()
    {

        try
        {
            while (true)
            {
                Message message = SharedData.messageQueue.take();
                String[] msgStr = new String[3];
                msgStr[0] = message.getFrom();
                msgStr[1] = message.getTo();
                msgStr[2] = message.getContent();
                model.addRow(msgStr);
                System.out.println(String.format("From: %s To %s Content: %s",
                        message.getFrom(), message.getTo(), message.getContent()));
            }
        } catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }

}
