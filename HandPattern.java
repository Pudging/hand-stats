import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HandPattern {
    private List<String> requiredCardNames; // e.g. ["Ex Ryzeal", "Ice Ryzeal"]
    private Map<String, Integer> requiredRoles; // e.g. {"handtrap": 2}

    private double handValue = 0;

    public HandPattern(List<String> cardNames, Map<String, Integer> roles) {
        this.requiredCardNames = cardNames;
        this.requiredRoles = roles;
    }

    public HandPattern(List<String> cardNames, Map<String, Integer> roles, double handValue) {
        this.requiredCardNames = cardNames;
        this.requiredRoles = roles;
        this.handValue = handValue;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder("HandPattern: ");
        sb.append("Required Cards: ").append(requiredCardNames).append(", ");
        sb.append("Required Roles: ").append(requiredRoles);
        return sb.toString();
    }

    public double getHandValue() {
        return handValue;
    }
    public void setHandValue(double handValue) {
        this.handValue = handValue;
    }


    public List<String> getRequiredCards() {
        return requiredCardNames;
    }

    public Map<String, Integer> getRoleCounts() {
        return requiredRoles;
    }

    public boolean matches(List<String> hand) {

        List<String> handCopy = List.copyOf(hand); // Create a copy of the hand to avoid mutation
       List<String> handCopy2 = new ArrayList<>(hand);

        // Check specific card names
        for (String name : requiredCardNames) {
            if (!handCopy2.remove(name))
                return false;
        }

        // Check roles
        Map<String, Integer> roleCounts = new HashMap<>();
        Set<String> seenCards = new HashSet<>();

        for (String card : handCopy) {
            CardRoles.CardInfo info = CardRoles.getRole(card);
            String role = info.role;
            boolean isOPT = info.isOPT;

            boolean alreadySeen = seenCards.contains(card);

            // If it's OPT and we've already seen it, skip adding its role again
            if (isOPT && alreadySeen) {
                continue;
            }

            // Add role to count
            roleCounts.put(role, roleCounts.getOrDefault(role, 0) + 1);

            // Mark the card as seen
            seenCards.add(card);
        }

        // Check required roles
        for (Map.Entry<String, Integer> entry : requiredRoles.entrySet()) {
            String role = entry.getKey();
            int requiredCount = entry.getValue();
            if (roleCounts.getOrDefault(role, 0) < requiredCount)
                return false;
        }

        return true;
    }

    public void setRequiredCardNames(List<String> requiredCardNames) {
        this.requiredCardNames = requiredCardNames;
    }

    public void setRequiredRoles(Map<String, Integer> requiredRoles) {
        this.requiredRoles = requiredRoles;
    }
}