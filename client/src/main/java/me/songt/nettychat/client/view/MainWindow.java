package me.songt.nettychat.client.view;

import me.songt.nettychat.client.netty.ChatClient;
import me.songt.nettychat.client.proc.IncomeProc;
import me.songt.nettychat.client.proc.OutgoProc;
import me.songt.nettychat.client.proc.SharedData;
import me.songt.nettychat.entity.Message;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.net.ConnectException;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainWindow implements ChatWindow
{
    private JPanel mainPanel;
    private JButton sendBtn;
    private JComboBox toBox;
    private JTextArea contentTxt;
    private JTextField userNickBox;
    private JButton conBtn;
    private JTextField hostTxt;
    private JButton disconBtn;
    private JSpinner portSpn;
    private JButton refreshBtn;
    private JScrollPane inboxPane;
    private JTable inboxTable;
    private JTable sendTable;
    private JScrollPane sendBoxPane;

    private ChatClient chatClient;

    private SpinnerModel spinnerModel;

    private String nickName;

    private ExecutorService executorService;

    public MainWindow()
    {
        System.out.println("Initializing...");
        this.initExecutor();
        this.initIncomeProcessor();
        this.initOutgoProcessor();
        sendBtn.addActionListener(e -> {
            Message message = new Message();
            message.setFrom(nickName);
            message.setTo((String) toBox.getSelectedItem());
            message.setContent(contentTxt.getText());
            try
            {
                chatClient.putMessage(message);
            } catch (InterruptedException e1)
            {
                e1.printStackTrace();
            }
            contentTxt.setText("");
        });
        conBtn.addActionListener(e -> {
            conBtn.setText("Connecting");
            SwingUtilities.invokeLater(() -> {
                connect();
                //Not a good way but effective
                /*try
                {
                    Thread.sleep(1000);
                } catch (InterruptedException e1)
                {
                    e1.printStackTrace();
                }*/
                if (SharedData.getInstance().checkOnlineStatus(5))
                {
                    conBtn.setText("Connected");
                    conBtn.setEnabled(false);
                    hostTxt.setEnabled(false);
                    portSpn.setEnabled(false);
                    userNickBox.setEnabled(false);
                }
                else
                {
                    JOptionPane.showMessageDialog(mainPanel, "Connection failed.", "Error", JOptionPane.ERROR_MESSAGE);
                    conBtn.setText("Connect");
                }
            });
        });
        disconBtn.addActionListener(e -> {
            if (!conBtn.isEnabled())
            {
                SwingUtilities.invokeLater(() ->
                {
                    try
                    {
                        SharedData.getInstance().setOnline(false);
                        chatClient.offline();
                    } catch (InterruptedException e1)
                    {
                        e1.printStackTrace();
                    }
                    System.out.println("Disconnected.");
                    conBtn.setText("Connect");
                    conBtn.setEnabled(!conBtn.isEnabled());
                    hostTxt.setEnabled(!hostTxt.isEnabled());
                    portSpn.setEnabled(!portSpn.isEnabled());
                    userNickBox.setEnabled(!userNickBox.isEnabled());
                });
            }
        });
        refreshBtn.addActionListener(e -> {
            try
            {
                chatClient.listOnlineUser();
            } catch (InterruptedException e1)
            {
                e1.printStackTrace();
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (chatClient == null)
            {
                return;
            }
            if (chatClient.isConnectionActive())
            {
                try
                {
                    chatClient.offline();
                } catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
            if (chatClient.getFuture() != null && chatClient.getChannel() != null)
            {
                chatClient.close();
                chatClient.destory();
                System.out.println("Bye");
            }
        }));
    }

    @Override
    public JPanel getMainPanel()
    {
        return mainPanel;
    }

    @Override
    public void insertInboxMessageItem(Message message)
    {
        insertItemToTable((DefaultTableModel) inboxTable.getModel(), message);
    }

    @Override
    public void updateOnlineUser(String[] onlineUserArr)
    {
        SwingUtilities.invokeLater(() ->
        {
            if (toBox.getItemCount() > 0)
            {
                toBox.removeAllItems();
            }
            Arrays.stream(onlineUserArr).forEach(toBox::addItem);
        });
    }

    @Override
    public void insertSentMessageItem(Message message)
    {
        insertItemToTable((DefaultTableModel) sendTable.getModel(), message);
    }

    private void insertItemToTable(DefaultTableModel model, Message message)
    {
        SwingUtilities.invokeLater(() ->
        {
            String[] msgStr = new String[3];
            msgStr[0] = message.getFrom();
            msgStr[1] = message.getTo();
            msgStr[2] = message.getContent();
            model.addRow(msgStr);
        });
    }

    private void connect()
    {
        String host = hostTxt.getText();
        int port = ((SpinnerNumberModel) spinnerModel).getNumber().intValue();
        nickName = userNickBox.getText();
        chatClient = new ChatClient(host, port, nickName);
        try
        {
            chatClient.start();
        } catch (InterruptedException | ConnectException e1)
        {
            e1.printStackTrace();
        }
    }

    private void initExecutor()
    {
        executorService = Executors.newCachedThreadPool();
    }

    private void initIncomeProcessor()
    {
        IncomeProc consumer = new IncomeProc(chatClient, this);
        executorService.execute(consumer);

    }

    private void initOutgoProcessor()
    {
        OutgoProc outgoProc = new OutgoProc(chatClient, this);
        executorService.execute(outgoProc);
    }

    private void createUIComponents()
    {
        System.out.println("Creating UI...");
        spinnerModel = new SpinnerNumberModel(8009, 0, 99999, 1);
        portSpn = new JSpinner(spinnerModel);
        final String[] mailHeaders = {"From", "To", "Content"};
        final String[][] mails = {{"Test", "Test", "Test"}};
        DefaultTableModel inboxTableModel = new DefaultTableModel(mails, mailHeaders);
        inboxTableModel.setRowCount(0);
        DefaultTableModel sentTableModel = new DefaultTableModel(mails, mailHeaders);
        sentTableModel.setRowCount(0);
        inboxTable = new JTable(inboxTableModel);
        sendTable = new JTable(sentTableModel);

    }
}
