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
    static JButton jbutton[][] = new JButton[3][3];
    static int boardDimenions = 3;
    static JPanel p2 = new JPanel(); 
    public static int moveCount;
    static JLabel gewonnen = new JLabel("Gewonnen hat... ");
    static JScrollBar boardScrollBar = new JScrollBar(Scrollbar.HORIZONTAL, 3, 1, 3, 10);
    static JLabel connectionInfo = new JLabel();
    public static void main(String[] args) {
        
        JFrame frame = new JFrame("Select Connection");
        JPanel selectGamePanel = new JPanel();
        JPanel tictactoePanel = new JPanel();
        App me = new App();
        
///////////////////////////////////////////////////////////////////////////////////////Connection Panel\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

        selectGamePanel.setLayout(null);
        JLabel boardDimensionLabel = new JLabel("Bord Dimension = " + boardDimenions + "x" + boardDimenions);
        selectGamePanel.add(boardDimensionLabel);
        boardDimensionLabel.setBounds(50,100, 400, 30);
        selectGamePanel.add(boardScrollBar);
        boardScrollBar.addAdjustmentListener(new AdjustmentListener() {
            @Override
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

///////////////////////////////////////////////////////////////////////////////////////Join Game\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\        

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
                                    me.drawBoard(p2, frame);
                                    tictactoePanel.add(p2, BorderLayout.CENTER);
                                    me.startGame(frame, selectGamePanel, tictactoePanel);
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
        selectGamePanel.add(connectButton);
        connectButton.setBounds(50, 165, 200, 30);
        
 ///////////////////////////////////////////////////////////////////////////////////////Host Game\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
        
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
                            me.syncBoardDimensions(boardDimenions);
                            me.drawBoard(p2, frame);
                            tictactoePanel.add(p2, BorderLayout.CENTER);
                            me.startGame(frame, selectGamePanel, tictactoePanel);
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
                player = false;
                selectGamePanel.remove(boardScrollBar);
                selectGamePanel.add(connectionInfo);
                connectionInfo.setBounds(50, 70, 500, 20);
                try {
                    connectionInfo.setText(InetAddress.getLocalHost() + ":8080");
                } catch (Exception ex) {
                    
                }
            }

        });

///////////////////////////////////////////////////////////////////////////////////////Game Panel\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
        tictactoePanel.setLayout(new BorderLayout());

        JPanel p1 = new JPanel(); 
        p1.setBounds(0,0,290,70); 
        p1.setLayout(null);

        JLabel spieler = new JLabel("Spieler 1");
        JLabel verbunden = new JLabel("Nicht Verbunden");
        JLabel istDran = new JLabel("Aktuell am Zug... ");

        p1.add(spieler); spieler.setBounds(10,10,200,20);
        p1.add(verbunden); verbunden.setBounds(10,30,200,20);
        p1.add(istDran); istDran.setBounds(10,50,200,20);

        tictactoePanel.add(p1, BorderLayout.NORTH);


        JPanel p3 = new JPanel(); 
        p3.setBounds(0,280,290,200); 
        p3.add(gewonnen); gewonnen.setBounds(10,10,200,20);

        tictactoePanel.add(p3, BorderLayout.SOUTH);

        frame.add(selectGamePanel);
        selectGamePanel.setBounds(0,0, 1000, 1000);

        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 400);
    }

    public void startGame(JFrame frame, JPanel selectGamePanel, JPanel tictactoePanel){
        frame.add(tictactoePanel);
        frame.remove(selectGamePanel);
        frame.setSize((90 * boardDimenions) + 10, (90 * boardDimenions) + 100);
    }
    public void sendMove(int i, int j){
        JSONObject sendMove = new JSONObject();
        sendMove.put("requestType", "Move");
        sendMove.put("xCord", j);
        sendMove.put("yCord", i);

        if(client != null){
            client.send(sendMove.toString());
        }
        else{
            server.broadcast(sendMove.toString());  
        }
    }
    public void syncMove(JSONObject json){
        int j = json.getInt("xCord");
        int i = json.getInt("yCord");
        jbutton[i][j].setBackground(Color.RED);
        jbutton[i][j].setEnabled(false);
        jbutton[i][j].setText("O");
        player = true;

    }
    public void WinCheck(int x, int y, int boardDimenions){
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
            sendWin(jbutton, gewonnen);
        }
    }
    public void syncBoardDimensions(int boardDimenions){
        JSONObject syncBoardDimensions = new JSONObject();
        syncBoardDimensions.put("requestType", "syncBoard");
        syncBoardDimensions.put("size", boardDimenions);

        server.broadcast(syncBoardDimensions.toString());
    }

    public void drawBoard(JPanel p2, JFrame frame){
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
                                        player = false;
                                        sendMove(i, j);
                                        WinCheck(i, j, boardDimenions);
                                    }
                                }
                            }
                        }
                    }
                });
            }
        }
        frame.setSize(((80*boardDimenions) + (5* boardDimenions - 1) + 200),   ((80*boardDimenions) + (5* boardDimenions - 1)));
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

