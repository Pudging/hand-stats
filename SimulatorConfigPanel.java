import java.awt.*;
import java.io.*;
import javax.swing.*;

public class SimulatorConfigPanel extends JPanel {
    private JTextField trialCountField;
    private JComboBox<String> goingSecondBox;

    private JButton chooseCSVButton, chooseYDKButton;
    private JLabel csvLabel, ydkLabel;
    private File selectedCSVFile, selectedYDKFile;

    private JTextArea roleEditor;
    private JTextArea patternEditor;

    private static final String CONFIG_FILE = "simulator_config.txt";

    public SimulatorConfigPanel() {
        setLayout(new BorderLayout());

        // === Top Panel: File selectors and config ===
        JPanel topPanel = new JPanel(new GridLayout(3, 1));

        // Deck File Selection
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        chooseCSVButton = new JButton("Choose cards.csv");
        chooseYDKButton = new JButton("Choose deck.ydk");
        csvLabel = new JLabel("No file selected");
        ydkLabel = new JLabel("No deck selected");

        chooseCSVButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedCSVFile = chooser.getSelectedFile();
                csvLabel.setText(selectedCSVFile.getName());
            }
        });

        chooseYDKButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("YDK Deck", "ydk"));
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                selectedYDKFile = chooser.getSelectedFile();
                ydkLabel.setText(selectedYDKFile.getName());
            }
        });

        filePanel.add(chooseCSVButton); filePanel.add(csvLabel);
        filePanel.add(chooseYDKButton); filePanel.add(ydkLabel);

        // Trial count and going second
        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        settingsPanel.add(new JLabel("Trial Count:"));
        trialCountField = new JTextField("100000", 8);
        settingsPanel.add(trialCountField);

        settingsPanel.add(new JLabel("Going:"));
        goingSecondBox = new JComboBox<>(new String[]{"First", "Second"});
        settingsPanel.add(goingSecondBox);

        topPanel.add(filePanel);
        topPanel.add(settingsPanel);

        // === Center Panel: Roles and Hand Patterns ===
        JPanel centerPanel = new JPanel(new GridLayout(1, 2));

        roleEditor = new JTextArea(10, 30);
        patternEditor = new JTextArea(10, 30);

        roleEditor.setBorder(BorderFactory.createTitledBorder("Card Role Overrides (e.g., Ash Blossom=handtrap)"));
        patternEditor.setBorder(BorderFactory.createTitledBorder("Hand Patterns (e.g., Ext Ryzeal,Ice Ryzeal | handtrap=2)"));

        centerPanel.add(new JScrollPane(roleEditor));
        centerPanel.add(new JScrollPane(patternEditor));

        // Load config if exists
        loadConfig();

        // === Bottom Panel: Save Button ===
        JButton saveButton = new JButton("Save Config");
        saveButton.addActionListener(e -> saveConfig());

        // === Combine All ===
        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(saveButton, BorderLayout.SOUTH);
    }

    public File getSelectedCSVFile() {
        return selectedCSVFile;
    }

    public File getSelectedYDKFile() {
        return selectedYDKFile;
    }

    public int getTrialCount() {
        try {
            return Integer.parseInt(trialCountField.getText());
        } catch (NumberFormatException e) {
            return 100000;
        }
    }

    public boolean isGoingSecond() {
        return goingSecondBox.getSelectedIndex() == 1;
    }

    public String getRoleOverridesText() {
        return roleEditor.getText();
    }

    public String getHandPatternsText() {
        return patternEditor.getText();
    }

    private void loadConfig() {
        File file = new File(CONFIG_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder roles = new StringBuilder();
            StringBuilder patterns = new StringBuilder();
            String line;
            boolean inRoles = true;
            while ((line = reader.readLine()) != null) {
                if (line.equals("===PATTERNS===")) {
                    inRoles = false;
                    continue;
                }
                if (inRoles) roles.append(line).append("\n");
                else patterns.append(line).append("\n");
            }
            roleEditor.setText(roles.toString());
            patternEditor.setText(patterns.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveConfig() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(CONFIG_FILE))) {
            writer.write(roleEditor.getText());
            writer.write("\n===PATTERNS===\n");
            writer.write(patternEditor.getText());
            JOptionPane.showMessageDialog(this, "Configuration saved.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
