import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class SimulationResult {
    int trials;
    double avg, median, variance, stdDev;
    double bestScore, worstScore;
    List<String> bestHand, worstHand;
    Set<String> bestStarters, bestExtenders, bestHandtraps, bestSoftGarnets;
    double avgStarters, avgExtenders, avgHandtraps, avgSoftGarnets;
    Map<HandPattern, Integer> patternMatches;

    public SimulationResult(int trials, double avg, double median, double variance, double stdDev,
                            double bestScore, List<String> bestHand,
                            Set<String> starters, Set<String> extenders, Set<String> handtraps, Set<String> garnets,
                            double worstScore, List<String> worstHand,
                            double avgStarters, double avgExtenders, double avgHandtraps, double avgGarnets,
                            Map<HandPattern, Integer> patternMatches) {
        this.trials = trials; this.avg = avg; this.median = median;
        this.variance = variance; this.stdDev = stdDev;
        this.bestScore = bestScore; this.bestHand = bestHand;
        this.bestStarters = starters; this.bestExtenders = extenders;
        this.bestHandtraps = handtraps; this.bestSoftGarnets = garnets;
        this.worstScore = worstScore; this.worstHand = worstHand;
        this.avgStarters = avgStarters; this.avgExtenders = avgExtenders;
        this.avgHandtraps = avgHandtraps; this.avgSoftGarnets = avgGarnets;
        this.patternMatches = patternMatches;
    }

    public JTextPane toTextPane() {
        JTextPane pane = new JTextPane();
        pane.setEditable(false);
        StyledDocument doc = pane.getStyledDocument();
        Style style = pane.addStyle("default", null);

        try {
            StyleConstants.setBold(style, true);
            StyleConstants.setFontSize(style, 14);
            doc.insertString(doc.getLength(), "Simulation Results\n\n", style);

            StyleConstants.setBold(style, false);
            doc.insertString(doc.getLength(), String.format("Trials: %,d\n", trials), style);
            doc.insertString(doc.getLength(), String.format("Average Hand Score: %.3f\nMedian: %.3f\nVariance: %.3f\nStandard Deviation: %.3f\n\n",
                avg, median, variance, stdDev), style);

            StyleConstants.setBold(style, true);
            doc.insertString(doc.getLength(), "Best Hand\n", style);
            StyleConstants.setBold(style, false);
            doc.insertString(doc.getLength(), String.format("Score: %.3f\nCards: %s\nStarters (%d): %s\nExtenders (%d): %s\nHandtraps (%d): %s\nSoft Garnets (%d): %s\n\n",
                bestScore, bestHand, bestStarters.size(), bestStarters, bestExtenders.size(), bestExtenders,
                bestHandtraps.size(), bestHandtraps, bestSoftGarnets.size(), bestSoftGarnets), style);

            StyleConstants.setBold(style, true);
            doc.insertString(doc.getLength(), "Worst Hand\n", style);
            StyleConstants.setBold(style, false);
            doc.insertString(doc.getLength(), String.format("Score: %.3f\nCards: %s\n\n", worstScore, worstHand), style);

            StyleConstants.setBold(style, true);
            doc.insertString(doc.getLength(), "Average Unique Roles per Hand\n", style);
            StyleConstants.setBold(style, false);
            doc.insertString(doc.getLength(), String.format("Starters: %.2f\nExtenders: %.2f\nHandtraps: %.2f\nSoft Garnets: %.2f\n\n",
                avgStarters, avgExtenders, avgHandtraps, avgSoftGarnets), style);

            StyleConstants.setBold(style, true);
            doc.insertString(doc.getLength(), "Pattern Matches\n", style);
            StyleConstants.setBold(style, false);
            for (Map.Entry<HandPattern, Integer> entry : patternMatches.entrySet()) {
                double freq = (double) entry.getValue() / trials * 100;
                doc.insertString(doc.getLength(), String.format("Pattern %s matched %,d times (%.2f%%)\n",
                    entry.getKey(), entry.getValue(), freq), style);
            }

        } catch (BadLocationException e) {
            e.printStackTrace();
        }

        return pane;
    }
}