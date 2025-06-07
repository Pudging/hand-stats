
import java.util.HashMap;
import java.util.Map;

public class CardWeights {

    static final Map<String, Double> cardWeightsGoingFirst = new HashMap<>();
    static final Map<String, Double> cardWeightsGoingSecond = new HashMap<>();

    static {
        // —— Going First raw scores —— //
        cardWeightsGoingFirst.put("Ash Blossom & Joyous Spring", 1.0);
        cardWeightsGoingFirst.put("Effect Veiler", 1.0);
        cardWeightsGoingFirst.put("Infinite Impermanence", 1.0);
        cardWeightsGoingFirst.put("Droll & Lock Bird", 2.0);
        cardWeightsGoingFirst.put("Called by the Grave", 1.0);
        cardWeightsGoingFirst.put("Pot of Prosperity", 1.5);
        cardWeightsGoingFirst.put("Ghost Belle and Haunted Mansion", 1.0);
        cardWeightsGoingFirst.put("Nibiru, the Primal Being", 1.5);
        cardWeightsGoingFirst.put("Mulcharmy Fuwalos", 1.5);
        cardWeightsGoingFirst.put("Crossout Designator", 1.0);
        cardWeightsGoingFirst.put("Triple Tactics Talent", 3.0);
        cardWeightsGoingFirst.put("Triple Tactics Trust", 1.0);
        cardWeightsGoingFirst.put("Mulcharmy Meowls", 0.5);
        cardWeightsGoingFirst.put("Mulcharmy Purulia", 0.5);
        cardWeightsGoingFirst.put("D.D Crow", 1.0);
        cardWeightsGoingFirst.put("Bystial magnamut", 4.0);
        cardWeightsGoingFirst.put("Bystial druiswurm", 2.5);

        // —— Corrected Ryzeal entries —— //
        cardWeightsGoingFirst.put("Ext Ryzeal", 2.0);
        cardWeightsGoingFirst.put("Bonfire", 1.0);
        cardWeightsGoingFirst.put("Ice Ryzeal", 1.5);
        cardWeightsGoingFirst.put("Node Ryzeal", 0.5);
        cardWeightsGoingFirst.put("Ryzeal Cross", 0.5);
        cardWeightsGoingFirst.put("Sword Ryzeal", 1.5);
        cardWeightsGoingFirst.put("Seventh Tachyon", 1.0);

        // —— Corrected Mitsurugi entries —— //
        cardWeightsGoingFirst.put("Mitsurugi Ritual", 0.25);
        cardWeightsGoingFirst.put("Mitsurugi Prayers", 1.5);
        cardWeightsGoingFirst.put("Ame no Habakiri no Mitsurugi", 1.0);
        cardWeightsGoingFirst.put("Ame no Murakumo no Mitsurugi", 0.5);
        cardWeightsGoingFirst.put("Futsu no Mitama no Mitsurugi", 1.5);
        cardWeightsGoingFirst.put("Mitsurugi no Mikoto, Kusanagi", 0.0);
        cardWeightsGoingFirst.put("Mitsurugi no Mikoto, Saji", 1.0);
        cardWeightsGoingFirst.put("Mitsurugi no Mikoto, Aramasa", 0.0);

        // —— Initialize Second map as a copy —— //
        cardWeightsGoingSecond.putAll(cardWeightsGoingFirst);

        // —— Override Going-Second raw scores —— //
        cardWeightsGoingSecond.put("Mulcharmy Fuwalos", 3.0);
        cardWeightsGoingSecond.put("Triple Tactics Talent", 1.0);
        cardWeightsGoingSecond.put("Mulcharmy Meowls", 2.0);
        cardWeightsGoingSecond.put("Mulcharmy Purulia", 2.0);
        cardWeightsGoingSecond.put("Ext Ryzeal", 2.5);

        cardWeightsGoingSecond.put("Bonfire", 1.0);
        cardWeightsGoingSecond.put("Seventh Tachyon", 0.9);
        cardWeightsGoingSecond.put("Ice Ryzeal", 1.0);
        cardWeightsGoingSecond.put("Node Ryzeal", 0.5);
        cardWeightsGoingSecond.put("Ryzeal Cross", 0.0);
        cardWeightsGoingSecond.put("Sword Ryzeal", 1.0);
        cardWeightsGoingSecond.put("Mitsurugi Ritual", 0.0);
        cardWeightsGoingSecond.put("Ame no Habakiri no Mitsurugi", 1.0);
        cardWeightsGoingSecond.put("Ame no Murakumo no Mitsurugi", 1.0);
        cardWeightsGoingSecond.put("Futsu no Mitama no Mitsurugi", 2.0);
        cardWeightsGoingSecond.put("Mitsurugi no Mikoto, Kusanagi", 0.0);
        cardWeightsGoingSecond.put("Mitsurugi no Mikoto, Saji", 1.0);
        cardWeightsGoingSecond.put("Mitsurugi no Mikoto, Aramasa", 0.0);
    }

    public static double getWeightGoingFirst(String name, Map<String, Double> overrideWeightsFirst) {
        if (overrideWeightsFirst != null && overrideWeightsFirst.containsKey(name)) {
            return overrideWeightsFirst.get(name);
        }
        return cardWeightsGoingFirst.getOrDefault(name, 0.0);
    }

    public static double getWeightGoingSecond(String name, Map<String, Double> overrideWeightsSecond) {
        if (overrideWeightsSecond != null && overrideWeightsSecond.containsKey(name)) {
            return overrideWeightsSecond.get(name);
        }
        return cardWeightsGoingSecond.getOrDefault(name, 0.0);
    }
}