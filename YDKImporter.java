import java.io.*;
import java.util.*;

public class YDKImporter {

    private Map<Integer, String> cardMap;

    public YDKImporter(String csvPath) throws IOException {
        this.cardMap = loadCardMap(csvPath);
    }

    private Map<Integer, String> loadCardMap(String csvPath) throws IOException {
        Map<Integer, String> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvPath))) {
            String line;
            boolean isFirstLine = true;
            while ((line = reader.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false; // Skip header
                    continue;
                }
                String[] parts = line.split(",", 2);
                if (parts.length == 2) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        String name = parts[1].trim();
                        map.put(id, name);
                    } catch (NumberFormatException e) {
                        // Skip lines with invalid ID format
                    }
                }
            }
        }
        return map;
    }

    public List<String> getCardNamesFromYDK(String ydkPath) throws IOException {
        List<String> cardNames = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(ydkPath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.equalsIgnoreCase("#extra")) {
                    break; // Stop reading at the #extra section
                }
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("!")) {
                    continue; // Skip comments and section headers
                }
                try {
                    int id = Integer.parseInt(line);
                    String name = cardMap.getOrDefault(id, "Unknown Card ID: " + id);
                    cardNames.add(name);
                } catch (NumberFormatException e) {
                    // Skip lines that are not numeric
                }
            }
        }
        return cardNames;
    }
}
