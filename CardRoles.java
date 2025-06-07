import java.util.HashMap;
import java.util.Map;

public class CardRoles {
    static class CardInfo {
        String role;
        boolean isOPT;

        CardInfo(String role, boolean isOPT) {
            this.role = role;
            this.isOPT = isOPT;
        }
    }

    static final Map<String, CardInfo> cardRoles = new HashMap<>();

    static {
        // — Starters — //
        cardRoles.put("Ice Ryzeal", new CardInfo("starter", true));
        cardRoles.put("Bonfire", new CardInfo("starter", true));
        cardRoles.put("Seventh Tachyon", new CardInfo("starter", true));
        cardRoles.put("Mitsurugi no Mikoto, Saji", new CardInfo("starter", true));


        // — Extenders — //
        cardRoles.put("Node Ryzeal", new CardInfo("extender", true));
        cardRoles.put("Ext Ryzeal", new CardInfo("extender", true));
        cardRoles.put("Ryzeal Plugin", new CardInfo("extender", true));
        cardRoles.put("Sword Ryzeal", new CardInfo("extender", true));
        cardRoles.put("Mitsurugi Prayers", new CardInfo("extender", true));
       

        // — Handtraps — //
        cardRoles.put("Ash Blossom & Joyous Spring", new CardInfo("handtrap", true));
        cardRoles.put("Ghost Ogre & Snow Rabbit", new CardInfo("handtrap", true));
        cardRoles.put("Effect Veiler", new CardInfo("handtrap", false));
        cardRoles.put("Infinite Impermanence", new CardInfo("handtrap", false));
        cardRoles.put("Nibiru, the Primal Being", new CardInfo("handtrap", true));
        cardRoles.put("Droll & Lock Bird", new CardInfo("handtrap", true));
        cardRoles.put("Mulcharmy Purulia", new CardInfo("handtrap", false));
        cardRoles.put("Mulcharmy Fuwalos", new CardInfo("handtrap", false));
        // — Defensive — //
        cardRoles.put("Called by the Grave", new CardInfo("defensive", true));
        cardRoles.put("Solemn Strike", new CardInfo("defensive", true));
        cardRoles.put("Solemn Judgment", new CardInfo("defensive", true));
        cardRoles.put("Crossout Designator", new CardInfo("defensive", true));

        // — Breakers — //
        cardRoles.put("Evenly Matched", new CardInfo("breaker", true));
        cardRoles.put("Lava Golem", new CardInfo("breaker", true));
        cardRoles.put("Dark Ruler No More", new CardInfo("breaker", true));
        cardRoles.put("Santa Claws", new CardInfo("breaker", true));
        cardRoles.put("Cosmic Cyclone", new CardInfo("breaker", true));
        cardRoles.put("Lightning Storm", new CardInfo("breaker", true));
        cardRoles.put("Harpie's Feather Duster", new CardInfo("breaker", true));
        cardRoles.put("Forbidden Droplet", new CardInfo("breaker", true));
        cardRoles.put("Pot of Prosperity", new CardInfo("extender", true));

        // — Utility — //
        cardRoles.put("Triple Tactics Talent", new CardInfo("breaker", true));
        cardRoles.put("Triple Tactics Thrust", new CardInfo("breaker", true));


        // — Soft Garnets (Mitsurugi pieces) — //
        cardRoles.put("Mitsurugi Ritual", new CardInfo("soft garnet", true));
        cardRoles.put("Ame no Habakiri no Mitsurugi", new CardInfo("soft garnet", true));
        cardRoles.put("Ame no Murakumo no Mitsurugi", new CardInfo("soft garnet", true));
        cardRoles.put("Futsu no Mitama no Mitsurugi", new CardInfo("soft garnet", true));
        cardRoles.put("Mitsurugi no Mikoto, Kusanagi", new CardInfo("soft garnet", true));
   
        cardRoles.put("Mitsurugi no Mikoto, Aramasa", new CardInfo("soft garnet", true));
        cardRoles.put("Ryzeal Cross", new CardInfo("soft garnet", true));
    }

    public static CardInfo getRole(String cardName) {
        return cardRoles.getOrDefault(cardName, new CardInfo("unknown", true));
    }

    
    public static CardInfo getEffectiveInfo(String cardName, Map<String, CardInfo> overrides) {
        if (overrides.containsKey(cardName)) {
            return overrides.get(cardName);
        }
        return cardRoles.getOrDefault(cardName, new CardInfo("unknown", true));
    }
}
