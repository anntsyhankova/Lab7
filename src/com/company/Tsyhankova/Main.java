package com.company.Tsyhankova;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Main extends JFrame {

    private static final String FRAME_TITLE = "Клиент мнгновенных сообщений";

    private static final int FRAME_MINIMUM_WIDTH = 500;
    private static final int FRAME_MINIMUM_HEIGHT = 500;
    private static final int FROM_FIELD_DEFAULT_COLUMNS = 10;
    private static final int TO_FIELD_DEFAULT_COLUMNS = 20;
    private static final int OUTGOING_AREA_DEFAULT_ROWS = 5;
    private static final int SMALL_GAP = 5;
    private static final int MEDIUM_GAP = 10;
    private static final int LARGE_GAP = 15;
    private static final int SERVER_PORT = 4567;
    private final JTextField textFieldFrom;
    private final JTextField textFieldTo;
    private final JEditorPane textAreaIncoming;
    private final JTextArea textAreaOutgoing;

    public Main() {

        super(FRAME_TITLE);
        setMinimumSize(new Dimension(FRAME_MINIMUM_WIDTH, FRAME_MINIMUM_HEIGHT));
        final Toolkit kit = Toolkit.getDefaultToolkit();
        setLocation((kit.getScreenSize().width - getWidth()) / 2, (kit.getScreenSize().height - getHeight()) / 2);

        textAreaIncoming = new JEditorPane();
        textAreaIncoming.setContentType("text/html");
        textAreaIncoming.setEditable(false);

        final JScrollPane scrollPaneIncoming = new JScrollPane(textAreaIncoming);

        final JLabel labelFrom = new JLabel("От");
        final JLabel labelTo = new JLabel("Получатель");

        textFieldFrom = new JTextField(FROM_FIELD_DEFAULT_COLUMNS);
        textFieldTo = new JTextField(TO_FIELD_DEFAULT_COLUMNS);

        textAreaOutgoing = new JTextArea(OUTGOING_AREA_DEFAULT_ROWS, 0);

        final JScrollPane scrollPaneOutgoing = new JScrollPane(textAreaOutgoing);

        final JEditorPane messagePanel = new JEditorPane();
        messagePanel.setBorder(BorderFactory.createTitledBorder("Сообщение"));
        final JButton sendButton = new JButton("Отправить");

        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        HyperlinkListener hyperlinkListener = new HyperlinkListener(){
            @Override
            public void hyperlinkUpdate(HyperlinkEvent hyperlinkEvent) {
                if (hyperlinkEvent.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)){
                    textFieldTo.setText(hyperlinkEvent.getDescription());
                }
            }
        };
        textAreaIncoming.addHyperlinkListener(hyperlinkListener);


        final GroupLayout layout2 = new GroupLayout(messagePanel);
        messagePanel.setLayout(layout2);
        layout2.setHorizontalGroup(layout2.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout2.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(layout2.createSequentialGroup()
                                .addComponent(labelFrom)
                                .addGap(SMALL_GAP)
                                .addComponent(textFieldFrom)
                                .addGap(LARGE_GAP)
                                .addComponent(labelTo)
                                .addGap(SMALL_GAP)
                                .addComponent(textFieldTo))
                        .addComponent(scrollPaneOutgoing)
                        .addComponent(sendButton))

                .addContainerGap());
        layout2.setVerticalGroup(layout2.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout2.createParallelGroup(GroupLayout.Alignment.BASELINE)

                        .addComponent(labelFrom)
                        .addComponent(textFieldFrom)
                        .addComponent(labelTo)
                        .addComponent(textFieldTo))

                .addGap(MEDIUM_GAP)
                .addComponent(scrollPaneOutgoing)
                .addGap(MEDIUM_GAP)
                .addComponent(sendButton)
                .addContainerGap());

        final GroupLayout layout1 = new GroupLayout(getContentPane());
        setLayout(layout1);
        layout1.setHorizontalGroup(layout1.createSequentialGroup()

                .addContainerGap()
                .addGroup(layout1.createParallelGroup()
                        .addComponent(scrollPaneIncoming)
                        .addComponent(messagePanel))

                .addContainerGap());

        layout1.setVerticalGroup(layout1.createSequentialGroup()

                .addContainerGap()
                .addComponent(scrollPaneIncoming)
                .addGap(MEDIUM_GAP)
                .addComponent(messagePanel)
                .addContainerGap());


        new Thread(new Runnable() {
            @Override
            public void run() {
                    try {
                        final ServerSocket serverSocket =
                                new ServerSocket(SERVER_PORT);

                        while (!Thread.interrupted()) {
                            final Socket socket = serverSocket.accept();
                            final DataInputStream in = new DataInputStream(

                                    socket.getInputStream());

                            final String senderName = in.readUTF();

                            final String message = in.readUTF();

                            socket.close();

                            final String address =
                                    ((InetSocketAddress) socket
                                            .getRemoteSocketAddress())
                                            .getAddress()
                                            .getHostAddress();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        JOptionPane.showMessageDialog(Main.this, "Ошибка в работе сервера", "Ошибка", JOptionPane.ERROR_MESSAGE);
                    }
            }
        }).start();
    }

    private void sendMessage() {
        try {
            final String senderName = textFieldFrom.getText();
            final String destinationAddress = textFieldTo.getText();
            final String message = textAreaOutgoing.getText();
            if (senderName.isEmpty()) {
                JOptionPane.showMessageDialog(Main.this, "Введите имя отправителя", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (destinationAddress.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите адрес узла-получателя", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (message.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Введите текст сообщения", "Ошибка", JOptionPane.ERROR_MESSAGE);
                return;
            }
            final Socket socket = new Socket(destinationAddress, SERVER_PORT);
            final DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(senderName);
            out.writeUTF(message);
            socket.close();

            try {
                Document doc = textAreaIncoming.getDocument();
                doc.insertString(doc.getLength(), "<html><body><a href=" + destinationAddress + ">" + senderName + "</a>" + " : " + "<b>" + message + "</b></body></html>", null);
                textAreaIncoming.setText(doc.getText(0,doc.getLength()));
            }catch (BadLocationException e){
                e.printStackTrace();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(Main.this, "Не удалось отправить сообщение: узел-адресат не найден", "Ошибка", JOptionPane.ERROR_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(Main.this, "Не удалось отправить сообщение", "Ошибка", JOptionPane.ERROR_MESSAGE);

        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                final Main frame = new Main();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
    }
}
