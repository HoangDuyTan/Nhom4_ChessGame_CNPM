package view;

import controller.GameController;
import controller.SaveLoadController;
import model.Board;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class StartWindow extends JFrame {
    private static final Color BUTTON_COLOR = new Color(60, 130, 200);
    private Image bgImage;

    public StartWindow() {
        setTitle("GAME CỜ VUA");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        try {
            File imgFile = new File("assets/img/chess.jpg");
            if (imgFile.exists()) {
                bgImage = ImageIO.read(imgFile);
            } else {
                System.err.println("Không tìm thấy file ảnh tại: " + imgFile.getAbsolutePath());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (bgImage != null) {
                    g.drawImage(bgImage, 0, 0, getWidth(), getHeight(), this);
                } else {
                    g.setColor(new Color(30, 30, 30));
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
                g.setColor(new Color(0, 0, 0, 120));
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("CHESS GAME");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 44));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton startButton = new JButton("BẮT ĐẦU");
        styleButton(startButton);
        startButton.addActionListener(e -> {
            if (SaveManager.hasSaveFile()) {
                Object[] options = {"Tiếp tục ván cũ", "Tạo ván mới", "Hủy"};
                int n = JOptionPane.showOptionDialog(this,
                        "Bạn có một ván chơi chưa hoàn thành. Bạn muốn tiếp tục không?",
                        "Thông báo bản lưu",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null, options, options[0]);

                if (n == JOptionPane.YES_OPTION) {
                    runGame(true);
                } else if (n == JOptionPane.NO_OPTION) {
                    SaveManager.deleteSaveFile();
                    runGame(false);
                }
            } else {
                new GameWindow();
                dispose();
            }
        });

        JButton guideButton = new JButton("HƯỚNG DẪN");
        JButton aiButton = new JButton("CHƠI VỚI MÁY");
        styleButton(aiButton);
        aiButton.addActionListener(e -> {
            SaveManager.deleteSaveFile();
            new GameWindow(true);
            dispose();
        });

        styleButton(guideButton);
        guideButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    """
                            Chào mừng bạn đến với Game Cờ Vua
                            
                            LUẬT CHƠI CỜ VUA CƠ BẢN
                            - Mỗi bên có 16 quân cờ
                            - Mục tiêu là chiếu bí vua đối thủ
                            
                            CÁC BƯỚC DI CHUYỂN CÁC QUÂN CỜ:
                            - Vua (King): Di chuyển 1 ô bất kỳ.
                            - Hậu (Queen): Đi thẳng, ngang, chéo bao nhiêu ô tùy ý.
                            - Xe (Rook): Đi thẳng và ngang bao nhiêu ô tùy ý.
                            - Tượng (Bishop): Đi chéo bao nhiêu ô tùy ý.
                            - Mã (Knight): Di chuyển hình chữ L.
                            - Tốt (Pawn): Đi thẳng 1 ô, bắt quân chéo 1 ô.
                            
                            CHÚC BẠN CHƠI VUI VẺ !
                            """,
                    "Hướng Dẫn",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        JButton exitButton = new JButton("THOÁT");
        styleButton(exitButton);
        exitButton.addActionListener(e -> System.exit(0));

        panel.add(Box.createVerticalStrut(120));
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(90));
        panel.add(startButton);
        panel.add(Box.createVerticalStrut(25));
        panel.add(aiButton);
        panel.add(Box.createVerticalStrut(25));
        panel.add(guideButton);
        panel.add(Box.createVerticalStrut(25));
        panel.add(exitButton);

        add(panel);
        setVisible(true);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Segoe UI", Font.BOLD, 20));
        button.setMaximumSize(new Dimension(260, 60));
        button.setPreferredSize(new Dimension(260, 60));
        button.setBackground(BUTTON_COLOR);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void runGame(boolean isResume) {
        this.dispose();
        GameWindow gameWindow = new GameWindow();

        if (isResume) {
            /* * [TRIGGER LOAD-GAME]: Kích hoạt Use Case gốc UC-04.2 (Tải ván đấu cũ)
             * Điều kiện: Người chơi chọn "Tiếp tục ván cũ" trên UI và có file save hợp lệ.
             */
            Board currentBoard = gameWindow.getBoard();
            GameController controller = gameWindow.getController();
            SaveLoadController.loadGame(currentBoard, controller);
            gameWindow.updateBoardGUI();
        }
        gameWindow.setVisible(true);
    }
}
