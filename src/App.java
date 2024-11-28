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
            }
        });
        connect.addActionListener(e -> {
            if (selectConnectionType.getSelectedItem().equals("Join a Game")) {
                connectToHost(ipAddressInput.getText(), Integer.parseInt(portAddressInput.getText()));
            } else if (selectConnectionType.getSelectedItem().equals("Host a Game")) {
                startHost();
            }
            frame.getContentPane().remove(selectConnectionTypePanel);
            frame.add(new TicTacToePanel());
            frame.revalidate();
            frame.repaint();
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

            // Listen for moves
            new Thread(() -> {
                try {
                    while (true) {
                        String message = br.readLine();
                        if (message.startsWith("MOVE:")) {
                            String[] parts = message.substring(5).split(",");
                            int row = Integer.parseInt(parts[0]);
                            int col = Integer.parseInt(parts[1]);
                            // Update the local board state
                            SwingUtilities.invokeLater(() -> panel.handleMove(row, col)); // Sync with GUI
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

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

            while (true) {
                String move = br.readLine();
                if (move != null) {
                    String[] parts = move.split(",");
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);
                    // Update the game state and send the new board state to the client
                    game.makeMove(row, col);
                    pw.println("MOVE:" + row + "," + col);
                }
            }
            } catch (Exception e) {
                
            }

        }
        catch(IOException e){
    
        }
        
    }
}
class TicTacToePanel extends JPanel {
    private JButton[][] buttons = new JButton[3][3];
    private TicTacToeGame game = new TicTacToeGame();

    public TicTacToePanel() {
        setLayout(new GridLayout(3, 3));
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j] = new JButton(" ");
                final int row = i, col = j;
                buttons[i][j].addActionListener(e -> handleMove(row, col));
                add(buttons[i][j]);
            }
        }
    }

    private void handleMove(int row, int col) {
        if (game.makeMove(row, col)) {
            buttons[row][col].setText(String.valueOf(game.getBoard()[row][col]));
            checkGameState();
        }
    }

    private void checkGameState() {
        char winner = game.checkWinner();
        if (winner != ' ') {
            JOptionPane.showMessageDialog(this, "Player " + winner + " wins!");
            resetGame();
        } else if (game.isDraw()) {
            JOptionPane.showMessageDialog(this, "It's a draw!");
            resetGame();
        }
    }

    private void resetGame() {
        game = new TicTacToeGame();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText(" ");
            }
        }
    }
}
class TicTacToeGame {
    private char[][] board = new char[3][3];
    private char currentPlayer = 'X';

    public TicTacToeGame() {
        for (int i = 0; i < 3; i++) {
            Arrays.fill(board[i], ' ');
        }
    }

    public boolean makeMove(int row, int col) {
        if (row < 0 || row > 2 || col < 0 || col > 2 || board[row][col] != ' ') {
            return false;
        }
        board[row][col] = currentPlayer;
        currentPlayer = (currentPlayer == 'X') ? 'O' : 'X';
        return true;
    }

    public char checkWinner() {
        // Check rows, columns, and diagonals for a winner
        for (int i = 0; i < 3; i++) {
            if (board[i][0] != ' ' && board[i][0] == board[i][1] && board[i][1] == board[i][2]) return board[i][0];
            if (board[0][i] != ' ' && board[0][i] == board[1][i] && board[1][i] == board[2][i]) return board[0][i];
        }
        if (board[0][0] != ' ' && board[0][0] == board[1][1] && board[1][1] == board[2][2]) return board[0][0];
        if (board[0][2] != ' ' && board[0][2] == board[1][1] && board[1][1] == board[2][0]) return board[0][2];
        return ' '; // No winner yet
    }

    public boolean isDraw() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == ' ') return false;
            }
        }
        return true;
    }

    public char getCurrentPlayer() {
        return currentPlayer;
    }

    public char[][] getBoard() {
        return board;
    }
}

