import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

public class HandSimulatorGUI {
    private static final Map<String, CardRoles.CardInfo> cardInfos = new HashMap<>();
    private static final Map<String, Double> weightsFirst = new HashMap<>();
    private static final Map<String, Double> weightsSecond = new HashMap<>();
    private static List<String> deck = new ArrayList<>();
    private static final List<HandPattern> handPatterns = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HandSimulatorGUI::createAndShowGUI);
    }

    private static void createAndShowGUI() {
        JFrame frame = new JFrame("Yu-Gi-Oh! Hand Simulator");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 700);
        frame.setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(8, 2, 10, 10));

        JButton deckButton = new JButton("Select .ydk Deck File");
        JButton roleFileButton = new JButton("Load Role Overrides File");
        JButton weightsFirstButton = new JButton("Load Weights (First)");
        JButton weightsSecondButton = new JButton("Load Weights (Second)");
        JButton saveConfigButton = new JButton("Save Config");
        JButton loadConfigButton = new JButton("Load Config");

        JTextField trialsField = new JTextField("100000");
        JComboBox<String> turnPreferenceDropdown = new JComboBox<>(new String[]{"Going First", "Going Second"});

        JButton runButton = new JButton("Run Simulation");

        inputPanel.add(deckButton);
        inputPanel.add(roleFileButton);
        inputPanel.add(weightsFirstButton);
        inputPanel.add(weightsSecondButton);
        inputPanel.add(new JLabel("Number of Trials:"));
        inputPanel.add(trialsField);
        inputPanel.add(new JLabel("Turn Preference:"));
        inputPanel.add(turnPreferenceDropdown);
        inputPanel.add(new JLabel("Custom Config Options:"));
        inputPanel.add(new JLabel("(Save/Load below)"));
        inputPanel.add(saveConfigButton);
        inputPanel.add(loadConfigButton);
        inputPanel.add(runButton);

        frame.add(inputPanel, BorderLayout.NORTH);

        JTextArea configArea = new JTextArea(12, 40);
        configArea.setText("Enter card overrides (name=role,opt)\nEnter weights (name=first,second)\nEnter hand patterns (names separated by comma | role:count | weight)\nExample:\nAsh Blossom=handtrap,true\nAsh Blossom=0.5,1.0\nAsh Blossom,Effect Veiler|handtrap:1|5.0\n");
        JScrollPane configScroll = new JScrollPane(configArea);
        frame.add(configScroll, BorderLayout.WEST);

        JTextPane outputPane = new JTextPane();
        outputPane.setEditable(false);
        frame.add(new JScrollPane(outputPane), BorderLayout.CENTER);

        // Action listeners
        deckButton.addActionListener(e -> {
            File file = chooseFile();
            if (file != null) {
                try {
                    YDKImporter importer = new YDKImporter("src/cards.csv");
                    deck = importer.getCardNamesFromYDK(file.getAbsolutePath());
                } catch (IOException ex) {
                    showError("Failed to read deck file");
                }
            }
        });

        roleFileButton.addActionListener(e -> loadRoleOverrides(chooseFile()));
        weightsFirstButton.addActionListener(e -> loadWeights(chooseFile(), weightsFirst));
        weightsSecondButton.addActionListener(e -> loadWeights(chooseFile(), weightsSecond));

        saveConfigButton.addActionListener(e -> saveConfig(configArea.getText()));
        loadConfigButton.addActionListener(e -> {
            File file = chooseFile();
            if (file != null) {
                try {
                    String content = Files.readString(file.toPath());
                    configArea.setText(content);
                    applyConfig(content);
                } catch (IOException ex) {
                    showError("Failed to load config file");
                }
            }
        });

        runButton.addActionListener(e -> {
            try {
                int trials = Integer.parseInt(trialsField.getText());
                boolean goingSecond = turnPreferenceDropdown.getSelectedItem().equals("Going Second");
                SimulationResult result = runSimulation(deck, trials, goingSecond, cardInfos, weightsFirst, weightsSecond, handPatterns);
                outputPane.setDocument(result.toTextPane().getStyledDocument());
            } catch (NumberFormatException ex) {
                showError("Number of trials must be a valid integer");
            }
        });

        JButton applyConfigButton = new JButton("Apply Config");
        inputPanel.add(applyConfigButton);
        applyConfigButton.addActionListener(e -> applyConfig(configArea.getText()));

        frame.setVisible(true);
    }

    private static File chooseFile() {
        JFileChooser chooser = new JFileChooser();
        return chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
    }

    private static void saveConfig(String text) {
        File file = chooseSaveFile();
        if (file != null) {
            try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
                writer.write(text);
            } catch (IOException ex) {
                showError("Failed to save config file");
            }
        }
    }

    private static File chooseSaveFile() {
        JFileChooser chooser = new JFileChooser();
        return chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION ? chooser.getSelectedFile() : null;
    }

    private static void loadRoleOverrides(File file) {
        if (file == null) return;
        try {
            Files.lines(file.toPath())
                .filter(line -> line.contains("="))
                .forEach(line -> {
                    String[] parts = line.split("=");
                    String[] roleParts = parts[1].split(",");
                    cardInfos.put(parts[0].trim(), new CardRoles.CardInfo(roleParts[0].trim(), Boolean.parseBoolean(roleParts[1].trim())));
                });
        } catch (IOException e) {
            showError("Failed to read role override file");
        }
    }

    private static void loadWeights(File file, Map<String, Double> map) {
        if (file == null) return;
        try {
            Files.lines(file.toPath())
                .filter(line -> line.contains("="))
                .forEach(line -> {
                    String[] parts = line.split("=");
                    map.put(parts[0].trim(), Double.parseDouble(parts[1].trim()));
                });
        } catch (IOException e) {
            showError("Failed to read weights file");
        }
    }

    private static void applyConfig(String text) {
        handPatterns.clear();
        Arrays.stream(text.split("\\n"))
            .map(String::trim)
            .filter(line -> !line.isEmpty() && !line.startsWith("#"))
            .forEach(line -> {
                try {
                    if (line.contains("=")) {
                        String[] parts = line.split("=");
                        if (parts[1].contains(",")) {
                            // weights line
                            String[] weights = parts[1].split(",");
                            weightsFirst.put(parts[0].trim(), Double.parseDouble(weights[0].trim()));
                            weightsSecond.put(parts[0].trim(), Double.parseDouble(weights[1].trim()));
                        } else if (parts[1].contains("handtrap") || parts[1].contains("starter") || parts[1].contains("extender")) {
                            // card role line
                            String[] roleParts = parts[1].split(",");
                            cardInfos.put(parts[0].trim(), new CardRoles.CardInfo(roleParts[0].trim(), Boolean.parseBoolean(roleParts[1].trim())));
                        }
                    } else if (line.contains("|")) {
                        // hand pattern line
                        String[] parts = line.split("\\|");
                        List<String> requiredCards = Arrays.asList(parts[0].split(","));
                        Map<String, Integer> roleCounts = new HashMap<>();
                        for (String rc : parts[1].split(",")) {
                            String[] rolePair = rc.split(":");
                            roleCounts.put(rolePair[0].trim(), Integer.parseInt(rolePair[1].trim()));
                        }
                        double weight = parts.length > 2 ? Double.parseDouble(parts[2].trim()) : 1.0;
                        handPatterns.add(new HandPattern(requiredCards, roleCounts, weight));
                    }
                } catch (Exception e) {
                    // Just ignore malformed lines in config
                }
            });
    }

    private static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }



    //put in seperate class later
    private static SimulationResult runSimulation(List<String> deck, int trials, boolean goingSecond, Map<String, CardRoles.CardInfo> cardInfos, Map<String, Double> weightsFirst, Map<String, Double> weightsSecond, List<HandPattern> patterns) {
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
    public static double evaluateHand(List<String> hand, boolean goingSecond, List<HandPattern> patterns, Map<String, Double> weightsFirst, Map<String, Double> weightsSecond) {
        double score = 0;
        Set<String> uniqueCards = new HashSet<>();
        Set<String> rolesInHand = new HashSet<>();

        for (String card : hand) {
            if (uniqueCards.add(card) || !CardRoles.getRole(card).isOPT) {
                if (goingSecond) {
                    score += CardWeights.getWeightGoingSecond(card, weightsSecond);
                } else {
                    score += CardWeights.getWeightGoingFirst(card, weightsFirst);
                }
                String role = CardRoles.getRole(card).role;
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