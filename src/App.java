import org.java_websocket.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.util.*;
import java.net.*;
import java.security.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;


public class App extends WebSocketClient{
    
    
    public App(URI serverUri) {
        super(serverUri);
    }
    
    public static boolean player = true; 
;
    public static void main(String[] args) {
        
        JFrame frame = new JFrame("Select Connection");
        
        JPanel selectGamePanel = new JPanel();
        selectGamePanel.setLayout(null);
    
        JTextField ipAddressInput = new JTextField();
    
        JTextField portAddressInput = new JTextField();
        
        JButton connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                try{
                    String serverUri = "ws://" + ipAddressInput.getText() + ":" + portAddressInput.getText();
                    App client = new App(new URI(serverUri));
                    client.connect();
                    System.out.println("yeaaah");
                    client.sendCheckAtConnection();
                    
                }
                catch(Exception er){
                    System.out.println("shit" + er);
                }
            }
        });
        
        JButton hostButton = new JButton("Host new game");
        hostButton.setBounds(50, 30, 200, 30);
        hostButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                hostGame();
            }
        });
        selectGamePanel.add(hostButton);
    
        JButton joinButton = new JButton("Join game");
        joinButton.setBounds(50, 70, 200, 30);
        joinButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e){
                System.out.println("pressed");
                selectGamePanel.add(ipAddressInput);
                ipAddressInput.setBounds(50, 110, 100, 25);
                selectGamePanel.add(portAddressInput);
                portAddressInput.setBounds(160, 110, 90, 25);
                selectGamePanel.add(connectButton);
                connectButton.setBounds(100, 150, 100, 30);
            }
        });
        selectGamePanel.add(joinButton);
    
      
        JPanel tictactoePanel = new JPanel();
        tictactoePanel.setLayout(null);

        JPanel p1 = new JPanel(); p1.setBounds(0,0,290,70); 
        JPanel p2 = new JPanel(); p2.setBounds(0,80,290,200);
        JPanel p3 = new JPanel(); p3.setBounds(0,280,290,200); 
        p1.setLayout(null);
        p2.setLayout(new GridLayout(0,3,5,5));
        
        JLabel spieler = new JLabel("Spieler 1");
        JLabel verbunden = new JLabel("Nicht Verbunden");
        JLabel istDran = new JLabel("Aktuell am Zug... ");
        JLabel gewonnen = new JLabel("Gewonnen hat... ");
        
        tictactoePanel.add(p1);
        
        p1.add(spieler); spieler.setBounds(10,10,200,20);
        p1.add(verbunden); verbunden.setBounds(10,30,200,20);
        p1.add(istDran); istDran.setBounds(10,50,200,20);
        
        tictactoePanel.add(p2);
        Button button[] = new Button[9];
        for (int i = 0;i < 9; i++ ) {
            button[i] = new Button(String.valueOf(i));
            p2.add(button[i]); button[i].addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {      
                    if (player == true) {
                        JButton gedrueckt = (JButton) e.getSource();
                        gedrueckt.setBackground(Color.RED);
                        gedrueckt.setEnabled(false);
                        player = false;
                    }
                }
            });
        }
        
        tictactoePanel.add(p3);
        p3.add(gewonnen); gewonnen.setBounds(10,10,200,20);

        frame.add(selectGamePanel);
        selectGamePanel.setBounds(0,0, 1000, 1000);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 400);
       }


    @Override
    public void onOpen(ServerHandshake handshakeData){

    }
    @Override
    public void onError(Exception e){

    }
    @Override
    public void onMessage(String Message){
        System.out.println(Message);
    }
    @Override
    public void onClose(int code, String reason, boolean remote){

    }
    public static void hostGame(){
        System.out.println("[Server] Starting...");
        try{
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("[Server] Started. Waiting for connection...");
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("succses");

                
            } catch (Exception e) {
                
            }

        }
        catch(IOException e){
        } 
    }
    ///////////////////////////////////////////////////////////////////////////Join or Host Game\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

    public void sendCheckAtConnection(){
        send("Hello there");
    }
}
