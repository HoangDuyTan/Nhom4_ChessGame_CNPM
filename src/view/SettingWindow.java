package view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class SettingWindow extends JFrame {
    private GameWindow gameWindow;
    private static final Color BACKGROUND_COLOR = new Color(28, 30, 33);
    private static final Color CARD_COLOR = new Color(45, 49, 54);
    private static final Color ACCENT_COLOR = new Color(112, 135, 163);
    private static final Color TEXT_COLOR = new Color(230, 230, 230);
    private static final Color BORDER_COLOR = new Color(70, 75, 80);

    public SettingWindow(GameWindow gameWindow) {
        this.gameWindow = gameWindow;
        setTitle("CÀI ĐẶT HỆ THỐNG");
        setSize(750, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(BACKGROUND_COLOR);
        container.setBorder(new EmptyBorder(30, 60, 30, 60));
        setContentPane(container);

        JLabel titleLabel = new JLabel("Cài Đặt", JLabel.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(TEXT_COLOR);
        titleLabel.setBorder(new EmptyBorder(0, 0, 25, 0));
        container.add(titleLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(BACKGROUND_COLOR);

        String[] themes = {"Màu nâu (Mặc định)", "Màu Hồng", "Màu Xanh dương", "Màu Xanh lá"};
        JComboBox<String> colorBox = new JComboBox<>(themes);

        styleComboBox(colorBox);
        String currentTheme = ThemeManager.loadTheme();

        switch (currentTheme) {

            case "Pink":
                colorBox.setSelectedItem("Màu Hồng");
                break;

            case "Blue":
                colorBox.setSelectedItem("Màu Xanh dương");
                break;

            case "Green":
                colorBox.setSelectedItem("Màu Xanh lá");
                break;

            default:
                colorBox.setSelectedItem("Màu nâu (Mặc định)");
                break;
        }
        colorBox.addActionListener(e -> {

            String selected = (String) colorBox.getSelectedItem();

            switch (selected) {

                case "Màu nâu (Mặc định)":

                    Theme.setBrownTheme();
                    ThemeManager.saveTheme("Brown");
                    break;

                case "Màu Hồng":

                    Theme.setPinkTheme();
                    ThemeManager.saveTheme("Pink");
                    break;

                case "Màu Xanh dương":

                    Theme.setBlueTheme();
                    ThemeManager.saveTheme("Blue");
                    break;

                case "Màu Xanh lá":
                    Theme.setGreenTheme();
                    ThemeManager.saveTheme("Green");
                    break;
            }

            gameWindow.dispose();

            new GameWindow();
        });
        contentPanel.add(createSettingCard("Màu sắc bàn cờ", "Thay đổi tông màu hiển thị của ô cờ và quân cờ.", colorBox));

        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JToggleButton soundToggle = new JToggleButton();

        soundToggle.setSelected(SoundManager.isSoundEnabled());

        updateSoundToggleUI(soundToggle);

        soundToggle.addActionListener(e -> {

            boolean enabled = soundToggle.isSelected();

            SoundManager.setSoundEnabled(enabled);
            SoundConfig.save(enabled);

            updateSoundToggleUI(soundToggle);

            if (enabled) {
                SoundManager.playButton();
            }
        });

        contentPanel.add(createSettingCard("Âm thanh", "Bật hoặc tắt toàn bộ hiệu ứng âm thanh của trò chơi.", soundToggle));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        contentPanel.add(createSettingCard("Cài đặt 3", "Mô tả", createPlaceholderLabel()));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        contentPanel.add(createSettingCard("Cài đặt 4", "Mô tả", createPlaceholderLabel()));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(null);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.getViewport().setBackground(BACKGROUND_COLOR);
        container.add(scrollPane, BorderLayout.CENTER);

        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footerPanel.setBackground(BACKGROUND_COLOR);
        footerPanel.setBorder(new EmptyBorder(20, 0, 0, 0));

        JButton closeButton = new JButton("Thoát");
        styleButton(closeButton);
        closeButton.addActionListener(e -> dispose());

        footerPanel.add(closeButton);
        container.add(footerPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private JPanel createSettingCard(String title, String description, Component control) {
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1), new EmptyBorder(20, 25, 20, 25)));

        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);

        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 5));
        textPanel.setOpaque(false);
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(TEXT_COLOR);
        JLabel lblDesc = new JLabel(description);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblDesc.setForeground(new Color(160, 160, 160));
        textPanel.add(lblTitle);
        textPanel.add(lblDesc);

        card.add(textPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 20, 0, 0);
        card.add(control, gbc);

        return card;
    }

    private JLabel createPlaceholderLabel() {
        JLabel label = new JLabel("Chưa thiết lập");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(100, 100, 100));
        return label;
    }

    private void styleButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btn.setBackground(ACCENT_COLOR);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(12, 30, 12, 30));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void styleComboBox(JComboBox<?> box) {
        box.setPreferredSize(new Dimension(200, 38));
        box.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        box.setFocusable(false);
    }

    private void updateSoundToggleUI(JToggleButton toggle) {

        if (toggle.isSelected()) {
            toggle.setText("ON");
            toggle.setBackground(new Color(40, 167, 69));
        } else {
            toggle.setText("OFF");
            toggle.setBackground(new Color(220, 53, 69));
        }

        toggle.setForeground(Color.WHITE);
        toggle.setFocusPainted(false);
        toggle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        toggle.setPreferredSize(new Dimension(80, 35));
    }
}