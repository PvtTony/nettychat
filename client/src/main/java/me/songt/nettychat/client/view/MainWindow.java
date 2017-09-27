package me.songt.nettychat.client.view;

import me.songt.nettychat.Constants;
import me.songt.nettychat.client.IncomeMsgProcessor;
import me.songt.nettychat.client.SharedData;
import me.songt.nettychat.client.netty.ChatClient;
import me.songt.nettychat.entity.Message;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainWindow
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
    private JTable mailTable;

    private ChatClient chatClient;

    private SpinnerModel spinnerModel;

    private String nickName;

    private ExecutorService executorService;

    public MainWindow()
    {
        sendBtn.addActionListener(e -> {
            Message message = new Message();
            message.setFrom(nickName);
            message.setTo((String) toBox.getSelectedItem());
            message.setContent(contentTxt.getText());
            chatClient.sendMessage(message);
            contentTxt.setText("");
        });
        conBtn.addActionListener(e -> {
            String host = hostTxt.getText();
            int port = ((SpinnerNumberModel) spinnerModel).getNumber().intValue();
            nickName = userNickBox.getText();
            System.out.println(host + ":" + port);
            chatClient = new ChatClient(host, port, nickName);
            System.out.println("Connected.");
            try
            {
                chatClient.start();
                if (chatClient.isConnectionActive())
                {
                    conBtn.setEnabled(false);
                    hostTxt.setEnabled(false);
                    portSpn.setEnabled(false);
                    userNickBox.setEnabled(false);
                }
            } catch (InterruptedException e1)
            {
                e1.printStackTrace();
            }
        });
        disconBtn.addActionListener(e -> {
            if (!conBtn.isEnabled())
            {

                conBtn.setEnabled(!conBtn.isEnabled());
                hostTxt.setEnabled(!hostTxt.isEnabled());
                portSpn.setEnabled(!portSpn.isEnabled());
                userNickBox.setEnabled(!userNickBox.isEnabled());
            }
        });
        refreshBtn.addActionListener(e -> {
            Message message = new Message();
            message.setFrom(nickName);
            message.setTo(Constants.BROADCAST_MESSAGE);
            message.setContent("List");
            chatClient.sendMessage(message);
            if (SharedData.users != null && SharedData.users.length > 0)
            {
                if (toBox.getItemCount() > 0)
                {
                    toBox.removeAllItems();
                }
                Arrays.stream(SharedData.users).forEach(toBox::addItem);
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (chatClient == null)
            {
                return;
            }
            if (chatClient.isConnectionActive())
            {
                chatClient.disconnect();
            }
            if (chatClient.getFuture() != null && chatClient.getChannel() != null)
            {
                chatClient.close();
                chatClient.destory();
                System.out.println("Bye");
            }
        }));
    }

    public static void main(String[] args)
    {
        JFrame frame = new JFrame("ChatClient");
        frame.setContentPane(new MainWindow().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }

    private void createUIComponents()
    {
        // TODO: place custom component creation code here
        spinnerModel = new SpinnerNumberModel(8009, 0, 99999, 1);
        portSpn = new JSpinner(spinnerModel);
        final String[] mailHeaders = {"From", "To", "Content"};
        final String[][] mails = {{"Test", "Test", "Test"}};
        DefaultTableModel inboxTableModel = new DefaultTableModel(mails, mailHeaders);
        inboxTableModel.setRowCount(0);
        mailTable = new JTable(inboxTableModel);
        IncomeMsgProcessor consumer = new IncomeMsgProcessor((DefaultTableModel) mailTable.getModel());
        executorService = Executors.newSingleThreadExecutor();
        executorService.execute(consumer);
    }
}
