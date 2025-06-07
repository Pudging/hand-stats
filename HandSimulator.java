import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.*;

public class HandSimulator {
    // Models to store configs in-memory for GUI tables
    private static final Map<String, CardRoles.CardInfo> cardInfos = new HashMap<>();
    private static final Map<String, Double> weightsFirst = new HashMap<>();
    private static final Map<String, Double> weightsSecond = new HashMap<>();
    private static List<String> deck = new ArrayList<>();
    private static final List<HandPattern> handPatterns = new ArrayList<>();

    // Table models for each config tab
    private static CardRolesTableModel cardRolesTableModel;
    private static WeightsTableModel weightsTableModel;
    private static HandPatternsTableModel handPatternsTableModel;

    // UI Components
    private static JTextArea outputPane;
    private static JTextField trialsField;
    private static JComboBox<String> turnPreferenceDropdown;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HandSimulator::createAndShowGUI);
    }

    private static void createAndShowGUI() {

        JFrame frame = new JFrame("Yu-Gi-Oh! Hand Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 750);
        frame.setLayout(new BorderLayout());

        // Top panel for deck and load/save config buttons
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JButton deckButton = new JButton("Select .ydk Deck File");
        JButton loadConfigButton = new JButton("Load Config");
        JButton saveConfigButton = new JButton("Save Config");

        topPanel.add(deckButton);
        topPanel.add(loadConfigButton);
        topPanel.add(saveConfigButton);

        frame.add(topPanel, BorderLayout.NORTH);

        // Center panel: tabbed pane for config editors
        JTabbedPane tabbedPane = new JTabbedPane();

        // Card Roles tab
        cardRolesTableModel = new CardRolesTableModel();
        JTable cardRolesTable = new JTable(cardRolesTableModel);
        JScrollPane cardRolesScroll = new JScrollPane(cardRolesTable);
        JPanel cardRolesPanel = new JPanel(new BorderLayout());
        cardRolesPanel.add(cardRolesScroll, BorderLayout.CENTER);
        JPanel cardRolesButtons = new JPanel();
        JButton addRoleRow = new JButton("Add Row");
        JButton removeRoleRow = new JButton("Remove Selected");
        cardRolesButtons.add(addRoleRow);
        cardRolesButtons.add(removeRoleRow);
        cardRolesPanel.add(cardRolesButtons, BorderLayout.SOUTH);
        tabbedPane.addTab("Card Roles", cardRolesPanel);

        // Weights tab
        weightsTableModel = new WeightsTableModel();
        JTable weightsTable = new JTable(weightsTableModel);
        JScrollPane weightsScroll = new JScrollPane(weightsTable);
        JPanel weightsPanel = new JPanel(new BorderLayout());
        weightsPanel.add(weightsScroll, BorderLayout.CENTER);
        JPanel weightsButtons = new JPanel();
        JButton addWeightRow = new JButton("Add Row");
        JButton removeWeightRow = new JButton("Remove Selected");
        weightsButtons.add(addWeightRow);
        weightsButtons.add(removeWeightRow);
        weightsPanel.add(weightsButtons, BorderLayout.SOUTH);
        tabbedPane.addTab("Weights", weightsPanel);

        // Hand Patterns tab
        handPatternsTableModel = new HandPatternsTableModel();
        JTable handPatternsTable = new JTable(handPatternsTableModel);
        JScrollPane handPatternsScroll = new JScrollPane(handPatternsTable);
        JPanel handPatternsPanel = new JPanel(new BorderLayout());
        handPatternsPanel.add(handPatternsScroll, BorderLayout.CENTER);
        JPanel handPatternsButtons = new JPanel();
        JButton addPatternRow = new JButton("Add Row");
        JButton removePatternRow = new JButton("Remove Selected");
        handPatternsButtons.add(addPatternRow);
        handPatternsButtons.add(removePatternRow);
        handPatternsPanel.add(handPatternsButtons, BorderLayout.SOUTH);
        tabbedPane.addTab("Hand Patterns", handPatternsPanel);

        frame.add(tabbedPane, BorderLayout.WEST);

        // Right panel: simulation parameters and output
        JPanel rightPanel = new JPanel(new BorderLayout());

        JPanel simParams = new JPanel(new GridLayout(5, 2, 10, 10));
        simParams.setBorder(BorderFactory.createTitledBorder("Simulation Parameters"));

        simParams.add(new JLabel("Number of Trials:"));
        trialsField = new JTextField("100000");
        simParams.add(trialsField);

        simParams.add(new JLabel("Turn Preference:"));
        turnPreferenceDropdown = new JComboBox<>(new String[] { "Going First", "Going Second" });
        simParams.add(turnPreferenceDropdown);

        JButton runButton = new JButton("Run Simulation");
        simParams.add(runButton);

        rightPanel.add(simParams, BorderLayout.NORTH);

        outputPane = new JTextArea(20, 40);
        outputPane.setEditable(false);
        JScrollPane outputScroll = new JScrollPane(outputPane);
        outputScroll.setBorder(BorderFactory.createTitledBorder("Simulation Output"));
        rightPanel.add(outputScroll, BorderLayout.CENTER);

        frame.add(rightPanel, BorderLayout.CENTER);

        // Button actions
        deckButton.addActionListener(e -> {
            File file = chooseFile();
            if (file != null) {
                try {
                    YDKImporter importer = new YDKImporter("src/cards.csv");
                    deck = importer.getCardNamesFromYDK(file.getAbsolutePath());
                    showMessage("Deck loaded: " + deck.size() + " cards.");
                } catch (IOException ex) {
                    showError("Failed to read deck file");
                }
            }
        });

        loadConfigButton.addActionListener(e -> {
            File file = chooseFile();
            if (file != null) {
                try {
                    String content = Files.readString(file.toPath());
                    parseConfigFile(content);
                    refreshTablesFromData();
                    showMessage("Config loaded.");
                } catch (IOException ex) {
                    showError("Failed to load config file");
                }
            }
        });

        saveConfigButton.addActionListener(e -> {
            File file = chooseSaveFile();
            if (file != null) {
                try {
                    String content = buildConfigFileContent();
                    Files.writeString(file.toPath(), content);
                    showMessage("Config saved.");
                } catch (IOException ex) {
                    showError("Failed to save config file");
                }
            }
        });

        addRoleRow.addActionListener(e -> {
            cardRolesTableModel.addRow("", new CardRoles.CardInfo("", false));
        });

        removeRoleRow.addActionListener(e -> {
            int selected = cardRolesTable.getSelectedRow();
            if (selected >= 0) {
                cardRolesTableModel.removeRow(selected);
            }
        });

        addWeightRow.addActionListener(e -> {
            weightsTableModel.addRow(new WeightEntry("", 0.0, 0.0));
        });

        removeWeightRow.addActionListener(e -> {
            int selected = weightsTable.getSelectedRow();
            if (selected >= 0) {
                weightsTableModel.removeRow(selected);
            }
        });

        addPatternRow.addActionListener(e -> {
            handPatternsTableModel.addRow(new HandPattern(new ArrayList<>(), new HashMap<>(), 1.0));
        });

        removePatternRow.addActionListener(e -> {
            int selected = handPatternsTable.getSelectedRow();
            if (selected >= 0) {
                handPatternsTableModel.removeRow(selected);
            }
        });

        runButton.addActionListener(e -> runSimulationAction());

        frame.setVisible(true);
    }

    // File choosers
    private static File chooseFile() {
        JFileChooser chooser = new JFileChooser();
        return chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
    }

    private static File chooseSaveFile() {
        JFileChooser chooser = new JFileChooser();
        return chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
    }

    // Show error popup
    private static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static void showMessage(String message) {
        JOptionPane.showMessageDialog(null, message, "Info", JOptionPane.INFORMATION_MESSAGE);
    }

    // Parse the whole config file into the data structures
    private static void parseConfigFile(String text) {
        cardInfos.clear();
        weightsFirst.clear();
        weightsSecond.clear();
        handPatterns.clear();

        Arrays.stream(text.split("\\R"))
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .forEach(line -> {
                    try {
                        if (line.contains("=")) {
                            String[] parts = line.split("=");
                            if (parts.length < 2)
                                return;
                            String key = parts[0].trim();
                            String val = parts[1].trim();
                            if (val.contains(",") && (val.contains("handtrap") || val.contains("starter")
                                    || val.contains("extender") || val.contains("soft garnet"))) {
                                // Card Role line
                                String[] roleParts = val.split(",");
                                cardInfos.put(key, new CardRoles.CardInfo(roleParts[0].trim(),
                                        Boolean.parseBoolean(roleParts[1].trim())));
                            } else if (val.contains(",")) {
                                // Weights line
                                String[] weights = val.split(",");
                                if (weights.length < 2)
                                    return;
                                weightsFirst.put(key, Double.parseDouble(weights[0].trim()));
                                weightsSecond.put(key, Double.parseDouble(weights[1].trim()));
                            }
                        } else if (line.contains("|")) {
                            // Hand pattern line: cards|roleCounts|weight
                            String[] parts = line.split("\\|");
                            List<String> cardsList = Arrays.asList(parts[0].split(","));
                            Map<String, Integer> roleCounts = new HashMap<>();
                            for (String rc : parts[1].split(",")) {
                                String[] rolePair = rc.split(":");
                                roleCounts.put(rolePair[0].trim(), Integer.parseInt(rolePair[1].trim()));
                            }
                            double weight = parts.length > 2 ? Double.parseDouble(parts[2].trim()) : 1.0;
                            handPatterns.add(new HandPattern(cardsList, roleCounts, weight));
                        }
                    } catch (Exception e) {
                        // Ignore malformed lines
                    }
                });
    }

    // Refresh GUI tables from current data maps/lists
    private static void refreshTablesFromData() {
        // Card Roles
        cardRolesTableModel.setData(cardInfos);

        // Weights
        List<WeightEntry> weightEntries = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        keys.addAll(weightsFirst.keySet());
        keys.addAll(weightsSecond.keySet());
        for (String key : keys) {
            double wf = weightsFirst.getOrDefault(key, 0.0);
            double ws = weightsSecond.getOrDefault(key, 0.0);
            weightEntries.add(new WeightEntry(key, wf, ws));
        }
        weightsTableModel.setData(weightEntries);
        // Hand Patterns
        handPatternsTableModel.setData(handPatterns);
    }

    // Build config file content from current GUI table data
    private static String buildConfigFileContent() {
        StringBuilder sb = new StringBuilder();

        // Card Roles
        for (int i = 0; i < cardRolesTableModel.getRowCount(); i++) {
            String card = (String) cardRolesTableModel.getValueAt(i, 0);
            String role = (String) cardRolesTableModel.getValueAt(i, 1);
            Boolean isOPT = (Boolean) cardRolesTableModel.getValueAt(i, 2);
            if (card == null || card.isEmpty() || role == null || role.isEmpty())
                continue;
            sb.append(card).append("=").append(role).append(",").append(isOPT).append("\n");
        }

        // Weights
        for (int i = 0; i < weightsTableModel.getRowCount(); i++) {
            String card = (String) weightsTableModel.getValueAt(i, 0);
            Double wf = (Double) weightsTableModel.getValueAt(i, 1);
            Double ws = (Double) weightsTableModel.getValueAt(i, 2);
            if (card == null || card.isEmpty())
                continue;
            sb.append(card).append("=").append(wf).append(",").append(ws).append("\n");
        }

        // Hand Patterns
        for (int i = 0; i < handPatternsTableModel.getRowCount(); i++) {
            HandPattern hp = handPatternsTableModel.getHandPatternAt(i);
            if (hp == null)
                continue;
            // cards separated by comma
            String cardsPart = String.join(",", hp.getRequiredCards());
            // role counts key:val comma separated
            StringBuilder roleCountsPart = new StringBuilder();
            hp.getRoleCounts().forEach((k, v) -> {
                if (roleCountsPart.length() > 0)
                    roleCountsPart.append(",");
                roleCountsPart.append(k).append(":").append(v);
            });
            sb.append(cardsPart).append("|").append(roleCountsPart).append("|").append(hp.getHandValue()).append("\n");
        }

        return sb.toString();
    }

    // Run the simulation with current data from tables
    private static void runSimulationAction() {
        if (deck.isEmpty()) {
            showError("No deck loaded. Please select a .ydk deck file first.");
            return;
        }
        int trials;
        try {
            trials = Integer.parseInt(trialsField.getText());
            if (trials <= 0)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            showError("Invalid number of trials.");
            return;
        }
        boolean goingSecond = turnPreferenceDropdown.getSelectedItem().equals("Going Second");

        // Sync GUI data back to in-memory structures before running
        syncTablesToData();

        SimulationResult result = runSimulation(deck, trials, goingSecond, cardInfos, weightsFirst, weightsSecond,
                handPatterns);
        outputPane.setDocument(result.toTextPane().getStyledDocument());
    }

    // Sync data from GUI tables back into cardInfos, weightsFirst, weightsSecond,
    // handPatterns
    private static void syncTablesToData() {
        cardInfos.clear();
        for (int i = 0; i < cardRolesTableModel.getRowCount(); i++) {
            String card = (String) cardRolesTableModel.getValueAt(i, 0);
            String role = (String) cardRolesTableModel.getValueAt(i, 1);
            Boolean isOPT = (Boolean) cardRolesTableModel.getValueAt(i, 2);
            if (card != null && !card.isEmpty() && role != null && !role.isEmpty()) {
                cardInfos.put(card, new CardRoles.CardInfo(role, isOPT != null && isOPT));
            }
        }

        weightsFirst.clear();
        weightsSecond.clear();
        for (int i = 0; i < weightsTableModel.getRowCount(); i++) {
            String card = (String) weightsTableModel.getValueAt(i, 0);
            Double wf = (Double) weightsTableModel.getValueAt(i, 1);
            Double ws = (Double) weightsTableModel.getValueAt(i, 2);
            if (card != null && !card.isEmpty() && wf != null && ws != null) {
                weightsFirst.put(card, wf);
                weightsSecond.put(card, ws);
            }
        }

        handPatterns.clear();
        for (int i = 0; i < handPatternsTableModel.getRowCount(); i++) {
            HandPattern hp = handPatternsTableModel.getHandPatternAt(i);
            if (hp != null)
                handPatterns.add(hp);
        }
    }

    // --------- Table Models ---------

    // CardRoles Table Model: card name, role, isOPT boolean
    static class CardRolesTableModel extends AbstractTableModel {
        private final String[] columns = { "Card Name", "Role", "Is OPT" };
        private final Map<String, CardRoles.CardInfo> overriddenRoles = new LinkedHashMap<>(); // preserve order

        private final List<String> keys = new ArrayList<>();

        public void setData(Map<String, CardRoles.CardInfo> map) {
            overriddenRoles.clear();
            overriddenRoles.putAll(map);
            refreshKeys();
            fireTableDataChanged();
        }

        public Map<String, CardRoles.CardInfo> getData() {
            return overriddenRoles;
        }

        private void refreshKeys() {
            keys.clear();
            keys.addAll(overriddenRoles.keySet());
        }

        public void addRow(String cardName, CardRoles.CardInfo info) {
            if (!overriddenRoles.containsKey(cardName)) {
                overriddenRoles.put(cardName, info);
                refreshKeys();
                fireTableRowsInserted(keys.size() - 1, keys.size() - 1);
            }
        }

        public void removeRow(int row) {
            if (row >= 0 && row < keys.size()) {
                String key = keys.get(row);
                overriddenRoles.remove(key);
                refreshKeys();
                fireTableRowsDeleted(row, row);
            }
        }

        @Override
        public int getRowCount() {
            return overriddenRoles.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int col) {
            return columns[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            if (row < 0 || row >= keys.size())
                return null;
            String cardName = keys.get(row);
            CardRoles.CardInfo info = overriddenRoles.get(cardName);
            if (info == null)
                return null;

            return switch (col) {
                case 0 -> cardName;
                case 1 -> info.role;
                case 2 -> info.isOPT;
                default -> null;
            };
        }

        @Override
        public void setValueAt(Object val, int row, int col) {
            if (row < 0 || row >= keys.size())
                return;
            String cardName = keys.get(row);
            CardRoles.CardInfo info = overriddenRoles.get(cardName);
            if (info == null)
                return;

            switch (col) {
                case 0 -> {
                    // Changing card name requires rekeying the map
                    String newName = val != null ? val.toString() : "";
                    if (!newName.isEmpty() && !overriddenRoles.containsKey(newName)) {
                        overriddenRoles.remove(cardName);
                        overriddenRoles.put(newName, info);
                        refreshKeys();
                        fireTableDataChanged();
                    }
                }
                case 1 -> info.role = val != null ? val.toString() : "";
                case 2 -> {
                    if (val instanceof Boolean) {
                        info.isOPT = (Boolean) val;
                    } else if (val instanceof String) {
                        info.isOPT = Boolean.parseBoolean(((String) val).trim());
                    } else {
                        info.isOPT = false; // default fallback
                    }
                }

            }
            fireTableCellUpdated(row, col);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }
    }

    // Weights Table Model: card name, weightFirst, weightSecond
    static class WeightsTableModel extends AbstractTableModel {
        private final String[] columns = { "Card Name", "Weight First", "Weight Second" };
        private final List<WeightEntry> data = new ArrayList<>();

        public void setData(List<WeightEntry> entries) {
            data.clear();
            data.addAll(entries);
            fireTableDataChanged();
        }

        public void addRow(WeightEntry entry) {
            data.add(entry);
            fireTableRowsInserted(data.size() - 1, data.size() - 1);
        }

        public void removeRow(int idx) {
            if (idx >= 0 && idx < data.size()) {
                data.remove(idx);
                fireTableRowsDeleted(idx, idx);
            }
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int col) {
            return columns[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            WeightEntry entry = data.get(row);
            return switch (col) {
                case 0 -> entry.cardName;
                case 1 -> entry.weightFirst;
                case 2 -> entry.weightSecond;
                default -> null;
            };
        }

        @Override
        public void setValueAt(Object val, int row, int col) {
            WeightEntry entry = data.get(row);
            try {
                switch (col) {
                    case 0 -> entry.cardName = val != null ? val.toString() : "";
                    case 1 -> entry.weightFirst = Double.parseDouble(val.toString());
                    case 2 -> entry.weightSecond = Double.parseDouble(val.toString());
                }
            } catch (NumberFormatException e) {
                // ignore invalid double input
            }
            fireTableCellUpdated(row, col);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }
    }

    static class WeightEntry {
        String cardName;
        double weightFirst;
        double weightSecond;

        WeightEntry(String cardName, double weightFirst, double weightSecond) {
            this.cardName = cardName;
            this.weightFirst = weightFirst;
            this.weightSecond = weightSecond;
        }
    }

    // Hand Patterns Table Model: cards list, role counts map, weight (hand value)
    static class HandPatternsTableModel extends AbstractTableModel {
        private final String[] columns = { "Cards (comma separated)", "Role Counts (role:count, comma separated)",
                "Weight" };
        private final List<HandPattern> data = new ArrayList<>();

        public void setData(List<HandPattern> patterns) {
            data.clear();
            data.addAll(patterns);
            fireTableDataChanged();
        }

        public void addRow(HandPattern pattern) {
            data.add(pattern);
            fireTableRowsInserted(data.size() - 1, data.size() - 1);
        }

        public void removeRow(int idx) {
            if (idx >= 0 && idx < data.size()) {
                data.remove(idx);
                fireTableRowsDeleted(idx, idx);
            }
        }

        public HandPattern getHandPatternAt(int row) {
            return row >= 0 && row < data.size() ? data.get(row) : null;
        }

        @Override
        public int getRowCount() {
            return data.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int col) {
            return columns[col];
        }

        @Override
        public Object getValueAt(int row, int col) {
            HandPattern hp = data.get(row);
            return switch (col) {
                case 0 -> String.join(",", hp.getRequiredCards());
                case 1 -> {
                    // format roleCounts as role:count, comma separated
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, Integer> e : hp.getRoleCounts().entrySet()) {
                        if (sb.length() > 0)
                            sb.append(",");
                        sb.append(e.getKey()).append(":").append(e.getValue());
                    }
                    yield sb.toString();
                }
                case 2 -> hp.getHandValue();
                default -> null;
            };
        }

        @Override
        public void setValueAt(Object val, int row, int col) {
            HandPattern hp = data.get(row);
            try {
                switch (col) {
                    case 0 -> {
                        String s = val != null ? val.toString().trim() : "";
                        List<String> cardsList = s.isEmpty() ? new ArrayList<>() : Arrays.asList(s.split("\\s*,\\s*"));
                        hp.setRequiredCardNames(cardsList);
                    }
                    case 1 -> {
                        String s = val != null ? val.toString().trim() : "";
                        Map<String, Integer> roleCounts = new HashMap<>();
                        if (!s.isEmpty()) {
                            for (String part : s.split("\\s*,\\s*")) {
                                String[] pair = part.split(":");
                                if (pair.length == 2) {
                                    roleCounts.put(pair[0], Integer.parseInt(pair[1]));
                                }
                            }
                        }
                        hp.setRequiredRoles(roleCounts);
                    }
                    case 2 -> {
                        hp.setHandValue(Double.parseDouble(val.toString()));
                    }
                }
            } catch (Exception e) {
                // ignore invalid input
            }
            fireTableCellUpdated(row, col);
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }
    }

    // put in seperate class later
    private static SimulationResult runSimulation(List<String> deck, int trials, boolean goingSecond,
            Map<String, CardRoles.CardInfo> cardInfos, Map<String, Double> weightsFirst,
            Map<String, Double> weightsSecond, List<HandPattern> patterns) {
        List<Double> handScores = new ArrayList<>();
        double totalScore = 0.0;
        double bestScore = Double.NEGATIVE_INFINITY;
        List<String> bestHand = new ArrayList<>();
        double worstScore = Double.POSITIVE_INFINITY;
        List<String> worstHand = new ArrayList<>();
        int handSize = goingSecond ? 6 : 5; // 6 cards for going second, 5 for going first

        double totalStarters = 0.0, totalExtenders = 0.0, totalHandtraps = 0.0, totalSoftGarnets = 0.0;
        Set<String> bestHandStarters = new HashSet<>(), bestHandExtenders = new HashSet<>(),
                bestHandHandtraps = new HashSet<>(), bestHandSoftGarnets = new HashSet<>();

        Map<HandPattern, Integer> patternMatchCounts = new HashMap<>();
        for (HandPattern pattern : patterns)
            patternMatchCounts.put(pattern, 0);

        for (int i = 0; i < trials; i++) {
            Collections.shuffle(deck);
            List<String> hand = new ArrayList<>(deck.subList(0, handSize));
            double score = evaluateHand(hand, goingSecond, patterns, weightsFirst, weightsSecond);
            handScores.add(score);
            totalScore += score;

            Set<String> starters = new HashSet<>();
            Set<String> extenders = new HashSet<>();
            Set<String> handtraps = new HashSet<>();
            Set<String> softGarnets = new HashSet<>();

            Set<String> seenCards = new HashSet<>();
            for (String card : hand) {
                CardRoles.CardInfo info = CardRoles.getEffectiveInfo(card, cardInfos);
                String role = info.role;
                if (info.isOPT && seenCards.contains(card))
                    continue;

                switch (role) {
                    case "starter" -> starters.add(card);
                    case "extender" -> extenders.add(card);
                    case "handtrap" -> handtraps.add(card);
                    case "soft garnet" -> softGarnets.add(card);
                }

                seenCards.add(card);
            }

            totalStarters += starters.size();
            totalExtenders += extenders.size();
            totalHandtraps += handtraps.size();
            totalSoftGarnets += softGarnets.size();

            if (score > bestScore) {
                bestScore = score;
                bestHand = new ArrayList<>(hand);
                bestHandStarters = new HashSet<>(starters);
                bestHandExtenders = new HashSet<>(extenders);
                bestHandHandtraps = new HashSet<>(handtraps);
                bestHandSoftGarnets = new HashSet<>(softGarnets);
            }

            if (score < worstScore) {
                worstScore = score;
                worstHand = new ArrayList<>(hand);
            }

            for (HandPattern pattern : patterns) {
                if (pattern.matches(new ArrayList<>(hand))) {
                    patternMatchCounts.put(pattern, patternMatchCounts.get(pattern) + 1);
                }
            }
        }

        double avg = totalScore / trials;
        double median = calculateMedian(handScores);
        double var = calculateVariance(handScores, avg);
        double stdDev = Math.sqrt(var);

        return new SimulationResult(trials, avg, median, var, stdDev,
                bestScore, bestHand, bestHandStarters, bestHandExtenders, bestHandHandtraps, bestHandSoftGarnets,
                worstScore, worstHand,
                totalStarters / trials, totalExtenders / trials, totalHandtraps / trials, totalSoftGarnets / trials,
                patternMatchCounts);
    }

    private static double calculateMedian(List<Double> scores) {
        Collections.sort(scores);
        int mid = scores.size() / 2;
        return scores.size() % 2 == 0 ? (scores.get(mid - 1) + scores.get(mid)) / 2.0 : scores.get(mid);
    }

    private static double calculateVariance(List<Double> scores, double mean) {
        double sumSq = 0.0;
        for (double d : scores)
            sumSq += (d - mean) * (d - mean);
        return sumSq / scores.size();
    }

    // Evaluate a hand
    public static double evaluateHand(List<String> hand, boolean goingSecond, List<HandPattern> patterns,
            Map<String, Double> weightsFirst, Map<String, Double> weightsSecond) {
        double score = 0;
        Set<String> uniqueCards = new HashSet<>();
        Set<String> rolesInHand = new HashSet<>();

        for (String card : hand) {
            if (uniqueCards.add(card) || !CardRoles.getEffectiveInfo(card, cardInfos).isOPT) {
                if (goingSecond) {
                    score += CardWeights.getWeightGoingSecond(card, weightsSecond);
                } else {
                    score += CardWeights.getWeightGoingFirst(card, weightsFirst);
                }
                String role = CardRoles.getEffectiveInfo(card, cardInfos).role;
                if (role != null) {
                    rolesInHand.add(role);
                }
            }
        }

        for (HandPattern pattern : patterns) {
            if (pattern.matches(hand)) {
                score += pattern.getHandValue();
            }

        }

        return score;
    }
}
