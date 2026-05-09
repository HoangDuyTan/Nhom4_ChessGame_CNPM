package view;

import controller.GameController;
import model.Board;
import model.King;
import model.Piece;
import model.Position;

import javax.swing.JOptionPane;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class GameWindow extends JFrame {
    private Board board;
    private JButton[][] chessSquares = new JButton[8][8];
    private Color currentTurn = Color.WHITE;
    private Color DARK_SQUARE_COLOR = Theme.DARK_SQUARE_COLOR;
    private Color LIGHT_SQUARE_COLOR = Theme.LIGHT_SQUARE_COLOR;
    private Color BORDER_COLOR = Theme.BORDER_COLOR;
    private Color CONTROL_PANEL_BG = Theme.CONTROL_PANEL_BG;
    private Color BUTTON_COLOR = Theme.BUTTON_COLOR;
    private final Color SELECT_COLOR = new Color(255, 255, 100);
    private final Color MOVE_COLOR = new Color(144, 238, 144);
    private final Color CAPTURE_COLOR = new Color(255, 100, 100);
    private GameController controller;
    private JLabel timerLabel;
    private JButton pauseButton;

    public GameWindow() {
        this.board = new Board();
        this.controller = new GameController(this.board, this);
        setTitle("CỜ VUA");
        setSize(1000, 700);
        setMinimumSize(new Dimension(850, 650));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 0));
        JPanel mainBoardContainer = new JPanel(new BorderLayout(5, 5));
        mainBoardContainer.setBackground(CONTROL_PANEL_BG);
        JPanel boardPanel = new JPanel(new GridLayout(8, 8));
        for (int r = 7; r >= 0; r--) {
            for (int c = 0; c < 8; c++) {
                JButton square = new JButton();
                if ((r + c) % 2 == 0) {
                    square.setBackground(LIGHT_SQUARE_COLOR);
                } else {
                    square.setBackground(DARK_SQUARE_COLOR);
                }

                square.setPreferredSize(new Dimension(75, 75));
                square.setFocusPainted(false);
                square.setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 50), 1));
                square.setBorderPainted(true);
                chessSquares[r][c] = square;
                int row = r;
                int col = c;
                square.addActionListener(e -> {
                    controller.handleSquareClick(row, col);
                });
                boardPanel.add(square);
            }
        }

        boardPanel.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 3));
        JPanel rowLabels = new JPanel(new GridLayout(8, 1));
        rowLabels.setBackground(CONTROL_PANEL_BG);
        rowLabels.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
        Font labelFont = new Font("Arial", Font.BOLD, 16);

        for (int r = 8; r >= 1; r--) {
            JLabel label = new JLabel(String.valueOf(r), JLabel.CENTER);
            label.setFont(labelFont);
            label.setForeground(BORDER_COLOR);
            rowLabels.add(label);
        }
        JPanel colLabels = new JPanel(new GridLayout(1, 8));
        colLabels.setBackground(CONTROL_PANEL_BG);
        colLabels.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        for (char c = 'a'; c <= 'h'; c++) {
            JLabel label = new JLabel(String.valueOf(c), JLabel.CENTER);
            label.setFont(labelFont);
            label.setForeground(BORDER_COLOR);
            colLabels.add(label);
        }

        mainBoardContainer.add(rowLabels, BorderLayout.WEST);
        mainBoardContainer.add(boardPanel, BorderLayout.CENTER);
        mainBoardContainer.add(colLabels, BorderLayout.SOUTH);

        JPanel centerWrapper = new JPanel();
        centerWrapper.setBackground(CONTROL_PANEL_BG);
        centerWrapper.add(mainBoardContainer);
        add(centerWrapper, BorderLayout.CENTER);
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(CONTROL_PANEL_BG);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 20));
        rightPanel.setPreferredSize(new Dimension(220, 0));

        JLabel titleLabel = new JLabel("CHỨC NĂNG");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        rightPanel.add(titleLabel);
        rightPanel.add(Box.createVerticalStrut(30));

        String[] buttonNames = {"Quay Lại Menu", "Đi Lại", "Đi Tiếp", "Tạm Dừng", "Đầu Hàng", "Chơi Game Mới", "Cài Đặt"};
        for (String name : buttonNames) {
            JButton btn = new JButton(name);
            btn.setFont(new Font("Arial", Font.PLAIN, 16));
            btn.setMaximumSize(new Dimension(190, 45));
            btn.setBackground(BUTTON_COLOR);
            btn.setForeground(Color.WHITE);
            btn.setFocusPainted(false);
            btn.setAlignmentX(CENTER_ALIGNMENT);

            if (name.equals("Quay Lại Menu")) {
                btn.addActionListener(e -> {
                    new StartWindow();
                    dispose();
                });
            } else if (name.equals("Đi Lại")) {
                btn.addActionListener(e -> controller.undo());
            } else if (name.equals("Đi Tiếp")) {
                btn.addActionListener(e -> controller.redo());
            } else if (name.equals("Tạm Dừng")) {

                pauseButton = btn;

                btn.addActionListener(e -> {
                    controller.togglePause();
                });
            } else if (name.equals("Đầu Hàng")) {

                btn.addActionListener(e -> {
                    controller.resignGame();
                });
            } else if (name.equals("Cài Đặt")) {
                btn.addActionListener(e -> new SettingWindow(this));
            }

            rightPanel.add(btn);
            rightPanel.add(Box.createVerticalStrut(15));
        }
        timerLabel = new JLabel("Time: 00:00");
        timerLabel.setFont(new Font("Arial", Font.BOLD, 18));
        timerLabel.setAlignmentX(CENTER_ALIGNMENT);

        rightPanel.add(timerLabel);
        rightPanel.add(Box.createVerticalStrut(20));

        add(rightPanel, BorderLayout.EAST);
        updateBoardGUI();

        setVisible(true);
    }

    public void updateBoardGUI() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece piece = board.get(pos);
                JButton square = chessSquares[r][c];

                if (piece != null) {
                    String colorPrefix = (piece.getColor() == Color.WHITE) ? "w" : "b";
                    String pieceName = String.valueOf(piece.getShortName()).toUpperCase();
                    String imagePath = "assets/img/" + colorPrefix + pieceName + ".png";
                    try {
                        File imgFile = new File(imagePath);
                        if (imgFile.exists()) {
                            Image rawImage = ImageIO.read(imgFile);
                            Image scaledImage = rawImage.getScaledInstance(65, 65, Image.SCALE_SMOOTH);
                            square.setIcon(new ImageIcon(scaledImage));
                        } else {
                            square.setIcon(null);
                            System.err.println("Miss: " + imagePath);
                        }
                    } catch (Exception e) {
                        square.setIcon(null);
                    }
                } else {
                    square.setIcon(null);
                }
            }
        }
        revalidate();
        repaint();
    }

    public void resetBoardColors() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if ((r + c) % 2 == 0) {
                    chessSquares[r][c].setBackground(LIGHT_SQUARE_COLOR);
                } else {
                    chessSquares[r][c].setBackground(DARK_SQUARE_COLOR);
                }
                chessSquares[r][c].setBorder(BorderFactory.createLineBorder(new Color(0, 0, 0, 50), 1));
            }
        }
    }

    public void highlightValidMoves(Position from, Board board) {
        Piece piece = board.get(from);
        if (piece == null) return;
        chessSquares[from.getR()][from.getC()].setBackground(SELECT_COLOR);
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position to = new Position(r, c);
                if (piece.isValidMove(from, to, board)) {
                    if (!board.simulateMoveAndCheck(from, to, piece.getColor())) {
                        Piece targetPiece = board.get(to);
                        if (targetPiece instanceof King) continue;
                        if (targetPiece != null) {
                            chessSquares[r][c].setBackground(CAPTURE_COLOR);
                        } else {
                            chessSquares[r][c].setBackground(MOVE_COLOR);
                        }
                        chessSquares[r][c].setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
                    }
                }
            }
        }
    }

    public Board getBoard() {
        return board;
    }

    public void setBoard(Board board) {
        this.board = board;
        updateBoardGUI();
    }

    public void updateTimer(int seconds) {

        int minutes = seconds / 60;
        int remainSeconds = seconds % 60;

        timerLabel.setText(String.format("Time: %02d:%02d",
                minutes,
                remainSeconds));
    }

    public void updatePauseButton(boolean paused) {

        if (paused) {
            pauseButton.setText("Tiếp Tục");
        } else {
            pauseButton.setText("Tạm Dừng");
        }
    }
    public GameController getController() {
        return controller;
    }
}