import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameFrame::new);
    }
}

class GameFrame extends JFrame {
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private Board board;
    private JButton[][] buttons;
    private JLabel[][] statsLabels;

    public GameFrame() {
        player1 = new Player("Vermelho", Color.RED);
        player2 = new Player("Azul", Color.BLUE);
        currentPlayer = player1;
        board = new Board();

        setTitle("Jogo de Cartas");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel boardPanel = new JPanel(new GridLayout(3, 3));
        buttons = new JButton[3][3];
        statsLabels = new JLabel[3][3];

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                JPanel panel = new JPanel(new BorderLayout());
                buttons[i][j] = new JButton();
                buttons[i][j].setFont(new Font("Arial", Font.PLAIN, 20));
                buttons[i][j].addActionListener(new BoardButtonListener(i, j));
                panel.add(buttons[i][j], BorderLayout.CENTER);

                statsLabels[i][j] = new JLabel("UP: - | DOWN: - | LEFT: - | RIGHT: -", SwingConstants.CENTER);
                panel.add(statsLabels[i][j], BorderLayout.SOUTH);
                boardPanel.add(panel);
            }
        }

        add(boardPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        JButton resetButton = new JButton("Reiniciar Jogo");
        resetButton.addActionListener(e -> resetGame());
        controlPanel.add(resetButton);
        add(controlPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private class BoardButtonListener implements ActionListener {
        private int x, y;

        public BoardButtonListener(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (board.isPositionEmpty(x, y)) {
                Card selectedCard = selectCard();
                if (selectedCard != null) {
                    board.addCardOnBoard(selectedCard, x, y);
                    updateBoard();
                    if (board.isFull()) {
                        checkWinner();
                        return;
                    }
                    currentPlayer = (currentPlayer == player1) ? player2 : player1;
                }
            } else {
                JOptionPane.showMessageDialog(null, "Esta posição já tem uma carta. Escolha outra.");
            }
        }

        private Card selectCard() {
            Card[] cards = currentPlayer.getCards();
            String[] options = new String[5];
            for (int i = 0; i < 5; i++) {
                options[i] = (cards[i] != null) ? cards[i].toString() : "Vazio";
            }

            int choice = JOptionPane.showOptionDialog(null, "Escolha uma carta", "Seleção de Carta",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

            if (choice >= 0 && choice < 5 && cards[choice] != null) {
                Card selectedCard = cards[choice];
                currentPlayer.removeCard(choice);
                return selectedCard;
            }

            return null;
        }

        private void updateBoard() {
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board.getCardAtPosition(i, j) != null) {
                        Card card = board.getCardAtPosition(i, j);
                        buttons[i][j].setText(card.toString());
                        buttons[i][j].setForeground(card.getColor());
                        statsLabels[i][j].setText(String.format("UP: %d | DOWN: %d | LEFT: %d | RIGHT: %d", card.getUp(), card.getDown(), card.getLeft(), card.getRight()));
                    } else {
                        buttons[i][j].setText("");
                        statsLabels[i][j].setText("UP: - | DOWN: - | LEFT: - | RIGHT: -");
                    }
                }
            }
        }

        private void checkWinner() {
            if (board.checkWinner(player1)) {
                JOptionPane.showMessageDialog(null, "Parabéns " + player1.getName() + "!");
            } else if (board.checkWinner(player2)) {
                JOptionPane.showMessageDialog(null, "Parabéns " + player2.getName() + "!");
            } else {
                JOptionPane.showMessageDialog(null, "Empate!");
            }

            int option = JOptionPane.showConfirmDialog(null, "Deseja jogar novamente?", "Reiniciar", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                resetGame();
            } else {
                System.exit(0);
            }
        }
    }

    private void resetGame() {
        board.resetBoard();
        currentPlayer = player1;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                buttons[i][j].setText("");
                statsLabels[i][j].setText("UP: - | DOWN: - | LEFT: - | RIGHT: -");
            }
        }
    }
}

class Board {
    private Card[][] board;

    public Board() {
        board = new Card[3][3];
    }

    public boolean isPositionEmpty(int x, int y) {
        return board[x][y] == null;
    }

    public boolean addCardOnBoard(Card card, int x, int y) {
        if (board[x][y] == null) {
            board[x][y] = card;
            checkCapture(x, y);
            return true;
        }
        return false;
    }

    private void checkCapture(int x, int y) {
        Card card = board[x][y];
        if (card == null) return;

        // Check above
        if (x > 0 && board[x - 1][y] != null && card.getUp() > board[x - 1][y].getDown()) {
            board[x - 1][y].setColor(card.getColor());
        }
        // Check below
        if (x < 2 && board[x + 1][y] != null && card.getDown() > board[x + 1][y].getUp()) {
            board[x + 1][y].setColor(card.getColor());
        }
        // Check left
        if (y > 0 && board[x][y - 1] != null && card.getLeft() > board[x][y - 1].getRight()) {
            board[x][y - 1].setColor(card.getColor());
        }
        // Check right
        if (y < 2 && board[x][y + 1] != null && card.getRight() > board[x][y + 1].getLeft()) {
            board[x][y + 1].setColor(card.getColor());
        }
    }

    public boolean isFull() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == null) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean checkWinner(Player player) {
        int count = 0;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] != null && board[i][j].getColor().equals(player.getColor())) {
                    count++;
                }
            }
        }
        return count >= 5;
    }

    public Card getCardAtPosition(int x, int y) {
        return board[x][y];
    }

    public void resetBoard() {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                board[i][j] = null;
            }
        }
    }
}

class Card {
    private int up, down, left, right;
    private Color color;

    public Card(int up, int down, int left, int right, Color color) {
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
        this.color = color;
    }

    public int getUp() {
        return up;
    }

    public int getDown() {
        return down;
    }

    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }
    @Override
    public String toString() {
        return String.format("[%d, %d, %d, %d]", up, down, left, right);
    }
}

class Player {
    private String name;
    private Color color;
    private Card[] cards;

    public Player(String name, Color color) {
        this.name = name;
        this.color = color;
        this.cards = new Card[5];
        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            this.cards[i] = new Card(rand.nextInt(10) + 1, rand.nextInt(10) + 1, rand.nextInt(10) + 1, rand.nextInt(10) + 1, color);
        }
    }

    public String getName() {
        return name;
    }

    public Card[] getCards() {
        return cards;
    }

    public Color getColor() {
        return color;
    }

    public void removeCard(int index) {
        this.cards[index] = null;
    }
}
