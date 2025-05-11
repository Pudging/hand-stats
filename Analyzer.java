import java.awt.Dimension;
import java.io.IOException;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.text.*;
public class Analyzer {
    // Card roles (tags)

    // Helper class for combo rules
    static class ComboRule {
        Set<String> requiredRoles;
        double bonus;

        ComboRule(Set<String> requiredRoles, double bonus) {
            this.requiredRoles = requiredRoles;
            this.bonus = bonus;
        }
    }

    static List<ComboRule> comboRules = List.of(
            new ComboRule(Set.of("Mitsurugi Ritual", "Ame no Murakumo no Mitsurugi"), 2.0),
            new ComboRule(Set.of("Ext Ryzeal", "Ice Ryzeal", "Sword Ryzeal"), -1.0),
            new ComboRule(Set.of("Ext Ryzeal", "Node Ryzeal", "Sword Ryzeal"), -1.0)

    );

    // bestScore analysis (put this somewhere else later)
    static double bestScore = Double.NEGATIVE_INFINITY;
    static List<String> bestHand = new ArrayList<>();

    public static void checkBestHand(List<String> hand, double score) {
        if (score > bestScore) {
            bestScore = score;
            bestHand = new ArrayList<>(hand); // Copy the list to avoid mutation

        }
    }

    public static void main(String[] args) {
        // Ryzeal deck list
        List<String> deck = new ArrayList<>();
        YDKImporter importer;
        try {
            importer = new YDKImporter("src/cards.csv");
            deck = importer.getCardNamesFromYDK("src/mitsu ryzeal handtrap.ydk");
        } catch (IOException e) {
            e.printStackTrace();
        }
    
        int trials = 1000000;
        int handSize = 5;
    
        List<Double> handScores = new ArrayList<>();
        double totalScore = 0.0;
        double bestScore = Double.NEGATIVE_INFINITY;
        List<String> bestHand = new ArrayList<>();
        double worstScore = Double.POSITIVE_INFINITY;
        List<String> worstHand = new ArrayList<>();
    
        double totalStarters = 0.0;
        double totalExtenders = 0.0;
        double totalHandtraps = 0.0;
        double totalSoftGarnets = 0.0;
    
        Set<String> bestHandStarters = new HashSet<>();
        Set<String> bestHandExtenders = new HashSet<>();
        Set<String> bestHandHandtraps = new HashSet<>();
        Set<String> bestHandSoftGarnets = new HashSet<>();
    
        List<HandPattern> patterns = List.of(
            new HandPattern(List.of(), Map.of("handtrap", 1, "extender", 2)),
            new HandPattern(List.of("Ext Ryzeal", "Ice Ryzeal"), Map.of("handtrap", 3)),
            new HandPattern(List.of("Ext Ryzeal"), Map.of("handtrap", 3))
        );
    
        Map<HandPattern, Integer> patternMatchCounts = new HashMap<>();
        for (HandPattern pattern : patterns) {
            patternMatchCounts.put(pattern, 0);
        }
    
        for (int i = 0; i < trials; i++) {
            Collections.shuffle(deck);
            List<String> hand = new ArrayList<>(deck.subList(0, handSize));
    
            double score = evaluateHand(hand, false);
            handScores.add(score);
            totalScore += score;
    
            Set<String> starters = new HashSet<>();
            Set<String> extenders = new HashSet<>();
            Set<String> handtraps = new HashSet<>();
            Set<String> softGarnets = new HashSet<>();
    
            for (String card : hand) {
                String role = CardRoles.getRole(card).role;
                if ("starter".equals(role)) starters.add(card);
                else if ("extender".equals(role)) extenders.add(card);
                else if ("handtrap".equals(role)) handtraps.add(card);
                else if ("soft garnet".equals(role)) softGarnets.add(card);
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
    
        double average = totalScore / trials;
        double median = calculateMedian(handScores);
        double variance = calculateVariance(handScores, average);
        double stdDeviation = Math.sqrt(variance);
    
        // Build styled output for GUI
        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        StyledDocument doc = textPane.getStyledDocument();
        Style style = textPane.addStyle("Bold", null);
    
        try {
            StyleConstants.setBold(style, true);
            StyleConstants.setFontSize(style, 14);
            doc.insertString(doc.getLength(), "Simulation Results\n\n", style);
    
            StyleConstants.setBold(style, false);
            doc.insertString(doc.getLength(), String.format("Trials: %,d\n", trials), style);
            doc.insertString(doc.getLength(), String.format("Average Hand Score: %.3f\n", average), style);
            doc.insertString(doc.getLength(), String.format("Median: %.3f\n", median), style);
            doc.insertString(doc.getLength(), String.format("Variance: %.3f\n", variance), style);
            doc.insertString(doc.getLength(), String.format("Standard Deviation: %.3f\n\n", stdDeviation), style);
    
            StyleConstants.setBold(style, true);
            doc.insertString(doc.getLength(), "Best Hand\n", style);
            StyleConstants.setBold(style, false);
            doc.insertString(doc.getLength(), String.format("Score: %.3f\n", bestScore), style);
            doc.insertString(doc.getLength(), "Cards: " + bestHand + "\n", style);
            doc.insertString(doc.getLength(), String.format("Starters (%d): %s\n", bestHandStarters.size(), bestHandStarters), style);
            doc.insertString(doc.getLength(), String.format("Extenders (%d): %s\n", bestHandExtenders.size(), bestHandExtenders), style);
            doc.insertString(doc.getLength(), String.format("Handtraps (%d): %s\n", bestHandHandtraps.size(), bestHandHandtraps), style);
            doc.insertString(doc.getLength(), String.format("Soft Garnets (%d): %s\n\n", bestHandSoftGarnets.size(), bestHandSoftGarnets), style);
    
            StyleConstants.setBold(style, true);
            doc.insertString(doc.getLength(), "Worst Hand\n", style);
            StyleConstants.setBold(style, false);
            doc.insertString(doc.getLength(), String.format("Score: %.3f\n", worstScore), style);
            doc.insertString(doc.getLength(), "Cards: " + worstHand + "\n\n", style);
    
            StyleConstants.setBold(style, true);
            doc.insertString(doc.getLength(), "Average Unique Roles per Hand\n", style);
            StyleConstants.setBold(style, false);
            doc.insertString(doc.getLength(), String.format("Starters: %.2f\n", totalStarters / trials), style);
            doc.insertString(doc.getLength(), String.format("Extenders: %.2f\n", totalExtenders / trials), style);
            doc.insertString(doc.getLength(), String.format("Handtraps: %.2f\n", totalHandtraps / trials), style);
            doc.insertString(doc.getLength(), String.format("Soft Garnets: %.2f\n\n", totalSoftGarnets / trials), style);
    
            StyleConstants.setBold(style, true);
            doc.insertString(doc.getLength(), "Pattern Matches\n", style);
            StyleConstants.setBold(style, false);
            for (HandPattern pattern : patterns) {
                int count = patternMatchCounts.get(pattern);
                double frequency = (double) count / trials * 100;
                doc.insertString(doc.getLength(), String.format("Pattern %s matched %,d times (%.2f%%)\n",
                    pattern, count, frequency), style);
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    
        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setPreferredSize(new Dimension(600, 500));
        JOptionPane.showMessageDialog(null, scrollPane, "Yu-Gi-Oh! Hand Simulation Summary", JOptionPane.INFORMATION_MESSAGE);
    }

    // Method to calculate the median
    private static double calculateMedian(List<Double> scores) {
        List<Double> sortedScores = new ArrayList<>(scores);
        Collections.sort(sortedScores);
        int n = sortedScores.size();
        if (n % 2 == 0) {
            return (sortedScores.get(n / 2 - 1) + sortedScores.get(n / 2)) / 2.0;
        } else {
            return sortedScores.get(n / 2);
        }
    }

    // Method to calculate the variance
    private static double calculateVariance(List<Double> scores, double mean) {
        double sumSquaredDiffs = 0.0;
        for (double score : scores) {
            sumSquaredDiffs += Math.pow(score - mean, 2);
        }
        return sumSquaredDiffs / scores.size();
    }

    // Evaluate a hand
    public static double evaluateHand(List<String> hand, boolean goingSecond) {
        double score = 0;
        Set<String> uniqueCards = new HashSet<>();
        Set<String> rolesInHand = new HashSet<>();

        for (String card : hand) {
            if (uniqueCards.add(card) || !CardRoles.getRole(card).isOPT) {
                if (goingSecond) {
                    score += CardWeights.getWeightGoingSecond(card);
                } else {
                    score += CardWeights.getWeightGoingFirst(card);
                }
                String role = CardRoles.getRole(card).role;
                if (role != null) {
                    rolesInHand.add(role);
                }
            }
        }

        for (ComboRule rule : comboRules) {
            if (rolesInHand.containsAll(rule.requiredRoles)) {
                score += rule.bonus;
            }
            if (uniqueCards.containsAll(rule.requiredRoles)) {
                score += rule.bonus;
            }

        }

        return score;
    }

}
