import org.java_websocket.*;
import java.util.*;
import java.net.*;
import java.security.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;


public class App extends JFrame implements ActionListener, ItemListener{
    public static void main(String[] args) {

///////////////////////////////////////////////////////////////////////////Game\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
        boolean player1 = true;

        JFrame game = new JFrame("TicTacToe");
        game.setSize(305,350);
        game.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        game.setLayout(null);
        
        JPanel status = new JPanel(); status.setBounds(0,0,290,70); 
        JPanel grid = new JPanel(); grid.setBounds(0,80,290,200);
        JPanel winner = new JPanel(); winner.setBounds(0,280,290,200); 
        status.setLayout(null);
        grid.setLayout(new GridLayout());
        
        JLabel player = new JLabel("Spieler 1");
        JLabel connected = new JLabel("Nicht Verbunden");
        JLabel currentTurn = new JLabel("Aktuell am Zug... ");
        JLabel won = new JLabel("Gewonnen hat... ");
        
        game.add(status);
        
        status.add(player); 
        player.setBounds(10,10,200,20);
        status.add(connected); 
        connect.setBounds(10,30,200,20);
        status.add(currentTurn); 
        currentTurn.setBounds(10,50,200,20);
        
        game.add(grid);
        JButton button[][] = new JButton[3][3];
        for (int i = 0;i < 3; i++ ) {
            for(int j = 0; j < 3; j++){
                button[i][j] = new JButton(String.valueOf(i + j));
                grid.add(button[i][j]); 
                button[i][j].addActionListener(this);
            }
        }
        
        game.add(winner);
        winner.add(won); won.setBounds(10,10,200,20);
        game.setVisible(false);
    }


    public static void joinGame(String ip, int port, JFrame frame, JFrame game){
        System.out.println("[Client] Searching for Server...");
        try{
            Socket serverSocket = new Socket(ip, port);
            System.out.println("[Client] Successfuly connected to server");
            frame.setVisible(false);
            game.setVisible(true);
            BufferedReader br = new BufferedReader(new InputStreamReader(serverSocket.getInputStream()));
            PrintWriter pw = new PrintWriter(serverSocket.getOutputStream(), true);
        }
        catch(IOException e){
    
        }
    }
    
    public static void hostGame(JFrame frame, JFrame game){
        System.out.println("[Server] Starting...");
        try{
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("[Server] Started. Waiting for connection...");
            try {
                Socket clientSocket = serverSocket.accept();
                frame.setVisible(false);
                game.setVisible(true);
                BufferedReader br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true);
                
            } catch (Exception e) {
                
            }

        }
        catch(IOException e){
    
        }
        
    }

    public void actionPerformed(ActionEvent e){

    }
    public void itemStateChanged(ItemEvent i){

    }
    /*public void actionPerformed(ActionEvent e){
        if (selectConnectionType.getSelectedItem().equals("Join a Game")) {
            joinGame(ipAddressInput.getText(), Integer.valueOf(portAddressInput.getText()), frame, game);
        }
        else if (selectConnectionType.getSelectedItem().equals("Host a Game")) {
            hostGame(frame, game);
        }
    }
    public void itemStateChanged(ItemEvent e){
        if (selectConnectionType.getSelectedItem().equals("Join a Game")) {
            selectConnectionTypePanel.add(ipAddressInput);
            selectConnectionTypePanel.add(portAddressInput);
            selectConnectionTypePanel.remove(connectionInfo);
            selectConnectionTypePanel.add(connect);
            connect.setText("connect to host");
        }
        else if (selectConnectionType.getSelectedItem().equals("Host a Game")) {
            selectConnectionTypePanel.add(connect);
            selectConnectionTypePanel.remove(ipAddressInput);
            selectConnectionTypePanel.remove(portAddressInput);
            selectConnectionTypePanel.add(connectionInfo);
            connect.setText("create new Host");
            try {
                connectionInfo.setText("Connection will be available through " + InetAddress.getLocalHost().getHostAddress() + " and port:" + 8080); 
            } catch (Exception E) {
                
            }
        }
    }*/
    ///////////////////////////////////////////////////////////////////////////Join or Host Game\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
    public SelectGameFrame(){
        JFrame frame = new JFrame("Choose Connection");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            
        JButton host = new JButton("Host new game");
        add(host);
        host.addActionListener(this);
        JButton join = new JButton("Join game");
        add(join);
        join.addActionListener(this);
        JTextField ipAddressInput = new JTextField();
        JTextField portAddressInput = new JTextField();
        JButton connect = new JButton("create new Host");
        connect.addActionListener(this);
        JLabel connectionInfo = new JLabel("Test TEst TEst");

        setVisible(true);  
    }


}
