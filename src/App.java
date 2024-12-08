import org.java_websocket.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.handshake.ServerHandshake;
import org.java_websocket.server.WebSocketServer;
import org.json.JSONObject;

import java.net.*;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

public class App{  
       
    public static boolean player = true; 
    static WebSocketServer server;
    static WebSocketClient client;
    public static void main(String[] args) {
        
        JFrame frame = new JFrame("Select Connection");
        JPanel selectGamePanel = new JPanel();
        JPanel tictactoePanel = new JPanel();
        App me = new App();
        
///////////////////////////////////////////////////////////////////////////////////////Connection Panel\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

        selectGamePanel.setLayout(null);
    
        JTextField ipAddressInput = new JTextField();
        selectGamePanel.add(ipAddressInput);
        ipAddressInput.setBounds(50, 110, 100, 25);
    
        JTextField portAddressInput = new JTextField();
        selectGamePanel.add(portAddressInput);
        portAddressInput.setBounds(160, 110, 90, 25);
        
        JButton connectButton = new JButton("Join Game");
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    try {
                        String serverUrl = "ws://" + ipAddressInput.getText() + ":" + portAddressInput.getText();
                        client = new WebSocketClient(new URI(serverUrl)) {
                            @Override
                            public void onOpen(ServerHandshake handshakeData){
                                System.out.println("[Client] Connection Successfull");
                                me.startGame(frame, selectGamePanel, tictactoePanel);
                            }
                            @Override
                            public void onError(Exception e){
                                System.out.println("[Client] Error:" + e);
                            }
                            @Override
                            public void onMessage(String Message){
                                System.out.println(Message);
                            }
                            @Override
                            public void onClose(int code, String reason, boolean remote){
                                System.out.println("[Client] Server disconnected" + reason);
                            }
                        };
                        client.connectBlocking();
                        System.out.println("Connected successfully!");
                    } catch (URISyntaxException | InterruptedException ex) {
                        System.out.println("Connection failed: " + ex);
                    }
                }).start();
            }
        });
        selectGamePanel.add(connectButton);
        connectButton.setBounds(100, 150, 100, 30);
        
        
        JButton hostButton = new JButton("Host new game");
        selectGamePanel.add(hostButton);
        hostButton.setBounds(50, 30, 200, 30);
        hostButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    System.out.println("[Server] Starting...");
                    server = new WebSocketServer(new InetSocketAddress(8080)) {
                        @Override
                        public void onOpen(WebSocket newClient, ClientHandshake handshake) {
                            me.startGame(frame, selectGamePanel, tictactoePanel);
                        }
                        @Override
                        public void onError(WebSocket Client, Exception e){
                            System.out.println("[Server] Error:" + e);
                        }
                        @Override
                        public void onMessage(WebSocket Client, String Message){
                            System.out.println(Message);
                        }
                        @Override
                        public void onClose(WebSocket Client, int code, String reason, boolean remote){
                            System.out.println("[Server] Client disconnected " + reason);
                        }
                        @Override
                        public void onStart() {
                            System.out.println("Server started on port 8080!");
                        }
                    };
                    server.start();
                }).start();
            }
        });

///////////////////////////////////////////////////////////////////////////////////////Game Panel\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
/// 
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
                        me.sendMove(e);
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
        frame.setSize(400, 400);
    }

    public void startGame(JFrame frame, JPanel selectGamePanel, JPanel tictactoePanel){
        frame.add(tictactoePanel);
        frame.remove(selectGamePanel);
        System.out.println("done");
        frame.setSize(305, 350);
    }
    public void sendMove(ActionEvent e){
        JSONObject sendMove = new JSONObject();
        sendMove.put("requestType", "Move");
        sendMove.put("buttonStateChanged", (JButton) e.getSource());

        if(client != null){
            client.send(sendMove.toString());
        }
        else{
            server.broadcast(sendMove.toString());  
        }

    }
}

