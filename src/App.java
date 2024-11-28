import org.java_websocket.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.awt.image.BufferedImage;
import java.util.*;
import java.net.*;
import java.security.*;
import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import javax.swing.*;


public class App{
    public static void main(String[] args) {
        JFrame frame = new JFrame("TicTacToe");
        frame.setSize(300, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //frame.setLayout(null);
        frame.setVisible(true);
        
        JPanel selectConnectionTypePanel = new JPanel();
        selectConnectionTypePanel.setLayout(new GridBagLayout());
       
        String[] connectionChoices = {"Host a Game" , "Join a Game"};
        JComboBox selectConnectionType = new JComboBox<String>(connectionChoices);
        selectConnectionTypePanel.add(selectConnectionType);
        JTextField ipAddressInput = new JTextField();
        ipAddressInput.setPreferredSize(new Dimension(40, 40));
        JTextField portAddressInput = new JTextField();
        portAddressInput.setPreferredSize(new Dimension(40, 40));
        JButton connect = new JButton("");
        JLabel connectionInfo = new JLabel("Test TEst TEst");
        selectConnectionType.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e){
                if (selectConnectionType.getSelectedItem().equals("Join a Game")) {
                    selectConnectionTypePanel.add(ipAddressInput);
                    selectConnectionTypePanel.add(portAddressInput);
                    selectConnectionTypePanel.add(connect);
                    connect.setText("connect to host");
                }
                else if (selectConnectionType.getSelectedItem().equals("Host a Game")) {
                    selectConnectionTypePanel.add(connect);
                    selectConnectionTypePanel.add(connectionInfo);
                    connect.setText("create new Host");
                    try {
                        connectionInfo.setText("Connection will be available through " + InetAddress.getLocalHost().getHostAddress() + " and port:" + 8080); 
                    } catch (Exception E) {
                        
                    }
                }
            }
        });
        connect.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                if (selectConnectionType.getSelectedItem().equals("Join a Game")) {
                    connectToHost(ipAddressInput.getText(), Integer.valueOf(portAddressInput.getText()));
                }
                else if (selectConnectionType.getSelectedItem().equals("Host a Game")) {
                    startHost();
                }
            }
        });

        frame.add(selectConnectionTypePanel);
    }
    public static void connectToHost(String ip, int port){
        System.out.println("[Client] Searching for Server...");
        try{
            Socket serverSocket = new Socket(ip, port);
            System.out.println("[Client] Successfuly connected to server");
            BufferedReader br = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            PrintWriter pw = new PrintWriter(serverSocket.getOutputStream(), true);
        }
        catch(IOException e){
    
        }
    }
    
    public static void startHost(){
        System.out.println("[Server] Starting...");
        try{
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("[Server] Started. Waiting for connection...");
            try {
                Socket clientSocket = serverSocket.accept();
                BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
            } catch (Exception e) {
                
            }

        }
        catch(IOException e){
    
        }
    }
}
