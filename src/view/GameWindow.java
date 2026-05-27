package view;

import controller.GameController;
import model.Board;
import model.King;
import model.Piece;
import model.Position;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
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

    private JLabel whiteTimerLabel;
    private JLabel blackTimerLabel;
    private JButton pauseButton;
    private JLayeredPane layeredPane;
    private JPanel pauseOverlay;

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

        // BẮT ĐẦU: CẤU TRÚC LAYERED PANE CHO UC05: PAUSE/RESUME
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(new OverlayLayout(layeredPane));
        mainBoardContainer.setPreferredSize(new Dimension(630, 630));
        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setBackground(CONTROL_PANEL_BG);
        centerWrapper.add(mainBoardContainer);
        layeredPane.add(centerWrapper, JLayeredPane.DEFAULT_LAYER);

        pauseOverlay = new JPanel(new GridBagLayout());
        pauseOverlay.setBackground(new Color(0, 0, 0, 180));
        JLabel pauseLabel = new JLabel("TẠM DỪNG");
        pauseLabel.setFont(new Font("Consolas", Font.BOLD, 45));
        pauseLabel.setForeground(new Color(255, 100, 100));
        pauseOverlay.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        pauseOverlay.add(pauseLabel);

        pauseOverlay.addMouseListener(new MouseAdapter() {});
        pauseOverlay.addMouseMotionListener(new MouseMotionAdapter() {});
        pauseOverlay.setVisible(false);
        pauseLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        layeredPane.add(pauseOverlay, JLayeredPane.PALETTE_LAYER);
        add(layeredPane, BorderLayout.CENTER);

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
                /*
                 * MÃ USE CASE: Bước 1 (UC-05.1) và Bước 6 (UC-05.2) - Trigger
                 * Mô tả: Bắt sự kiện khi người chơi nhấn nút "Tạm Dừng" / "Tiếp Tục"
                 * trên giao diện điều khiển.
                 */
                btn.addActionListener(e -> {
                    controller.togglePause();
                });
            } else if (name.equals("Đầu Hàng")) {
                // (UC-07): Người chơi bấm chọn chức năng "Đầu Hàng" trên giao diện màn hình thi đấu.
                btn.addActionListener(e -> {
                    controller.resignGame();
                });
            } else if (name.equals("Cài Đặt")) {
                btn.addActionListener(e -> new SettingWindow(this));
            }

            rightPanel.add(btn);
            rightPanel.add(Box.createVerticalStrut(15));
        }

        // --- BẮT ĐẦU: GIAO DIỆN ĐỒNG HỒ ĐÔI ---
        JPanel blackTimerPanel = new JPanel();
        blackTimerPanel.setBackground(CONTROL_PANEL_BG);
        blackTimerPanel.setBorder(BorderFactory.createTitledBorder("Thời gian ĐEN"));
        blackTimerLabel = new JLabel("10:00");
        blackTimerLabel.setFont(new Font("Consolas", Font.BOLD, 28));
        blackTimerPanel.add(blackTimerLabel);

        JPanel whiteTimerPanel = new JPanel();
        whiteTimerPanel.setBackground(CONTROL_PANEL_BG);
        whiteTimerPanel.setBorder(BorderFactory.createTitledBorder("Thời gian TRẮNG"));
        whiteTimerLabel = new JLabel("10:00");
        whiteTimerLabel.setFont(new Font("Consolas", Font.BOLD, 28));
        whiteTimerPanel.add(whiteTimerLabel);

        rightPanel.add(blackTimerPanel);
        rightPanel.add(Box.createVerticalStrut(15));
        rightPanel.add(whiteTimerPanel);
        rightPanel.add(Box.createVerticalStrut(20));
        // --- KẾT THÚC: GIAO DIỆN ĐỒNG HỒ ĐÔI ---

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

    public void updateTimer(int whiteSeconds, int blackSeconds, Color currentTurn) {
        int wMin = whiteSeconds / 60;
        int wSec = whiteSeconds % 60;
        whiteTimerLabel.setText(String.format("%02d:%02d", wMin, wSec));

        int bMin = blackSeconds / 60;
        int bSec = blackSeconds % 60;
        blackTimerLabel.setText(String.format("%02d:%02d", bMin, bSec));

        if (currentTurn == Color.WHITE) {
            whiteTimerLabel.setForeground(new Color(40, 167, 69));
            blackTimerLabel.setForeground(Color.GRAY);
        } else {
            blackTimerLabel.setForeground(new Color(40, 167, 69));
            whiteTimerLabel.setForeground(Color.GRAY);
        }

        if (whiteSeconds <= 30) whiteTimerLabel.setForeground(new Color(220, 53, 69));
        if (blackSeconds <= 30) blackTimerLabel.setForeground(new Color(220, 53, 69));
    }

    public void updatePauseButton(boolean paused) {
        if (paused) {
            pauseButton.setText("Tiếp Tục");
            /*
             * MÃ USE CASE: UC-05.1.4 & Yêu cầu đặc biệt SR2
             * Mô tả: Hiển thị lớp phủ (Overlay) mờ đè lên màn cờ để che đi các
             * quân cờ/nước đi cốt lõi, ngăn hành vi tính toán nước đi gian lận.
             */
            pauseOverlay.setVisible(true);
        } else {
            pauseButton.setText("Tạm Dừng");
            /*
             * MÃ USE CASE: UC-05.2.2
             * Mô tả: Ẩn lớp phủ "Tạm dừng", mở khóa lại tương tác hình ảnh trên bàn cờ.
             */
            pauseOverlay.setVisible(false);
        }
        layeredPane.repaint();
        layeredPane.revalidate();
    }

    public GameController getController() {
        return controller;
    }
}