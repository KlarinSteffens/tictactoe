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
    public static int moveCount;
    static int boardDimenions = 3;

    static WebSocketServer server;
    static WebSocketClient client;
        
    static JPanel selectGamePanel = new JPanel();
    static JPanel tictactoePanel = new JPanel();
    static JPanel p2 = new JPanel(); 

    static JButton jbutton[][] = new JButton[3][3];
    static JLabel connectionInfo = new JLabel();
    static JLabel gewonnen = new JLabel("Gewonnen hat... ");
    static JScrollBar boardScrollBar = new JScrollBar(Scrollbar.HORIZONTAL, 3, 1, 3, 10);

    public static void main(String[] args) {
        
        JFrame frame = new JFrame("Select Connection");
        App me = new App();
        
///////////////////////////////////////////////////////////////////////////////////////Connection Panel\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

        selectGamePanel.setLayout(null);
        JLabel boardDimensionLabel = new JLabel("Bord Dimension = " + boardDimenions + "x" + boardDimenions);
        selectGamePanel.add(boardDimensionLabel);
        boardDimensionLabel.setBounds(50,100, 400, 30);

        selectGamePanel.add(boardScrollBar);
        boardScrollBar.addAdjustmentListener(new AdjustmentListener() {
            public void adjustmentValueChanged(AdjustmentEvent e) {
                boardDimenions = e.getValue();
                boardDimensionLabel.setText("Bord Dimension = " + boardDimenions + "x" + boardDimenions);
            }
        });
        boardScrollBar.setBounds(50, 70, 200, 20);

        JTextField ipAddressInput = new JTextField();
        selectGamePanel.add(ipAddressInput);
        ipAddressInput.setBounds(50, 140, 100, 25);
    
        JTextField portAddressInput = new JTextField();
        selectGamePanel.add(portAddressInput);
        portAddressInput.setBounds(150, 140, 100, 25);

        frame.add(selectGamePanel);
        selectGamePanel.setBounds(0,0, 1000, 1000);

///////////////////////////////////////////////////////////////////////////////////////Join Game\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\        

        JButton connectButton = new JButton("Join Game");
        selectGamePanel.add(connectButton);
        connectButton.setBounds(50, 165, 200, 30);
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> {
                    try {
                        String serverUrl = "ws://" + ipAddressInput.getText() + ":" + portAddressInput.getText();
                        client = new WebSocketClient(new URI(serverUrl)) {
                            @Override
                            public void onOpen(ServerHandshake handshakeData){
                                System.out.println("[Client] Connection Successfull");
                                frame.setTitle("TicTacToe " + boardDimenions + "x" + boardDimenions);
                                gewonnen.setText("its your turn");
                            }
                            @Override
                            public void onError(Exception e){
                                System.out.println("[Client] Error:" + e);
                            }
                            @Override
                            public void onMessage(String Message){
                                System.out.println(Message);

                                JSONObject json = new JSONObject(Message);

                                if(json.getString("requestType").equals("Move")){
                                    me.syncMove(json);
                                    moveCount++;
                                }
                                else if(json.getString("requestType").equals("syncBoard")){
                                    boardDimenions = json.getInt("size");
                                    tictactoePanel.add(p2, BorderLayout.CENTER);
                                    me.startGame(frame);
                                }
                                else if(json.getString("requestType").equals("sendWin")){
                                    gewonnen.setText("Server has Won!");
                                    for(int i = 0; i < boardDimenions; i++){
                                        for(int j = 0; j < boardDimenions; j++){
                                                jbutton[i][j].setEnabled(false);
                                        }
                                    }
                                }
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
        
 ///////////////////////////////////////////////////////////////////////////////////////Host Game\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
        
        JButton hostButton = new JButton("Host new game");
        selectGamePanel.add(hostButton);
        hostButton.setBounds(50, 30, 200, 30);
        hostButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                player = false;
                selectGamePanel.remove(boardScrollBar);
                selectGamePanel.remove(ipAddressInput);
                selectGamePanel.remove(portAddressInput);
                selectGamePanel.remove(connectButton);
                selectGamePanel.add(connectionInfo);
                connectionInfo.setBounds(50, 70, 500, 20);
                new Thread(() -> {
                    System.out.println("[Server] Starting...");
                    server = new WebSocketServer(new InetSocketAddress(8080)) {
                        @Override
                        public void onOpen(WebSocket newClient, ClientHandshake handshake) {
                            me.syncBoardDimensions(boardDimenions);
                            frame.setTitle("TicTacToe " + boardDimenions + "x" + boardDimenions);
                            gewonnen.setText("its the opponents turn");
                            tictactoePanel.add(p2, BorderLayout.CENTER);
                            me.startGame(frame);
                        }
                        @Override
                        public void onError(WebSocket Client, Exception e){
                            System.out.println("[Server] Error:" + e);
                        }
                        @Override
                        public void onMessage(WebSocket Client, String Message){
                            System.out.println(Message);

                            JSONObject json = new JSONObject(Message);

                            if(json.getString("requestType").equals("Move")){
                                me.syncMove(json);
                                moveCount++;
                            }
                            else if(json.getString("requestType").equals("sendWin")){
                                gewonnen.setText("Client has Won!");
                                for(int i = 0; i < boardDimenions; i++){
                                    for(int j = 0; j < boardDimenions; j++){
                                            jbutton[i][j].setEnabled(false);
                                    }
                                }
                            }
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
                try {
                    connectionInfo.setText(InetAddress.getLocalHost() + ":8080");
                } catch (Exception ex) {
                    
                }
            }

        });

///////////////////////////////////////////////////////////////////////////////////////Game Panel\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
        tictactoePanel.setLayout(new BorderLayout());
        JPanel p3 = new JPanel(); 
        p3.add(gewonnen); 
        gewonnen.setBounds(10,10,200,20);

        tictactoePanel.add(p3, BorderLayout.SOUTH);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(320, 300);
    }

    public void startGame(JFrame frame){
        frame.add(tictactoePanel);
        frame.remove(selectGamePanel);
        jbutton = new JButton[boardDimenions][boardDimenions];
        p2.setLayout(new GridLayout(0,boardDimenions,5,5));
        for (int i = 0;i < boardDimenions; i++ ) {
            for (int j = 0; j < boardDimenions; j++){
                jbutton[i][j] = new JButton();
                p2.add(jbutton[i][j]);
                jbutton[i][j].addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {      
                        if (player == true) {
                            for(int i = 0; i < boardDimenions; i++){
                                for(int j = 0; j < boardDimenions; j++){
                                    if (jbutton[i][j] == (JButton) e.getSource()) {
                                        jbutton[i][j].setBackground(Color.BLUE);
                                        jbutton[i][j].setEnabled(false);
                                        jbutton[i][j].setText("X");
                                        jbutton[i][j].setFont(new Font("Serif", Font.PLAIN, 14));
                                        player = false;
                                        gewonnen.setText("its the opponents turn");
                                        sendMove(i, j);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
        frame.setSize(((80*boardDimenions) + (5* boardDimenions - 1)), (((80*boardDimenions) + (5* boardDimenions - 1)) + 40));
    }
    public void sendMove(int x, int y){
        JSONObject sendMove = new JSONObject();
        sendMove.put("requestType", "Move");
        sendMove.put("xCord", y);
        sendMove.put("yCord", x);

        if(client != null){
            client.send(sendMove.toString());
        }
        else{
            server.broadcast(sendMove.toString());  
        }

        moveCount++;
        for(int i = 0; i < boardDimenions; i++){
            if(jbutton[x][i].getText() != "X")
                break;
            if(i == boardDimenions - 1){
                sendWin(jbutton, gewonnen);
            }
        }
        for(int i = 0; i <boardDimenions; i++){
            if(jbutton[i][y].getText() != "X")
                break;
            if(i == boardDimenions - 1){
                sendWin(jbutton, gewonnen);
            }
        }
        if(x == y){
            for(int i = 0; i < boardDimenions; i++){
                if(jbutton[i][i].getText() != "X")
                    break;
                if(i == boardDimenions-1){
                    sendWin(jbutton, gewonnen);
                }
            }
        }
        if(x + y == boardDimenions - 1){
            for(int i = 0; i < boardDimenions; i++){
                if(jbutton[i][(boardDimenions - 1) - i].getText() != "X")
                    break;
                if(i == boardDimenions -1){
                    sendWin(jbutton, gewonnen);
                }
            }
        }
        if(moveCount == (Math.pow(boardDimenions, 2) - 1)){
            gewonnen.setText("draw");
        }
    }
    public void syncMove(JSONObject json){
        int j = json.getInt("xCord");
        int i = json.getInt("yCord");
        jbutton[i][j].setBackground(Color.RED);
        jbutton[i][j].setEnabled(false);
        jbutton[i][j].setText("O");
        player = true;

        gewonnen.setText("its your turn");
    }
    public void syncBoardDimensions(int boardDimenions){
        JSONObject syncBoardDimensions = new JSONObject();
        syncBoardDimensions.put("requestType", "syncBoard");
        syncBoardDimensions.put("size", boardDimenions);

        server.broadcast(syncBoardDimensions.toString());
    }

    public void sendWin(JButton[][] jbutton, JLabel winner){
        JSONObject sendWin = new JSONObject();
        sendWin.put("requestType", "sendWin");

        if(client != null){
            client.send(sendWin.toString());
        }
        else{
            server.broadcast(sendWin.toString());  
        }

        for(int i = 0; i < boardDimenions; i++){
            for(int j = 0; j < boardDimenions; j++){
                    jbutton[i][j].setEnabled(false);
            }
        }
        winner.setText("You have won");
    }
}

