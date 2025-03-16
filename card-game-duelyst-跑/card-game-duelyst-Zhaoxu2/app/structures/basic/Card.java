package structures.basic;

import java.util.Objects; // Import Objects class for hashCode() and equals()

/**
 * This is the base representation of a Card which is rendered in the player's hand.
 * A card has an id, a name (cardname) and a manacost. A card then has a large and mini
 * version. The mini version is what is rendered at the bottom of the screen. The big
 * version is what is rendered when the player clicks on a card in their hand.
 *
 * @author Dr. Richard McCreadie
 *
 */
public class Card {

    int id;
    private String spellType;
    private int spellValue;
    String cardname;
    int manacost;

    MiniCard miniCard;
    BigCard bigCard;

    boolean isCreature;
    String unitConfig;

    public Card() {
    }

    public String getSpellType() {
        return spellType;
    }

    public int getSpellValue() {
        return spellValue;
    }

    public Card(int id, String cardname, int manacost, MiniCard miniCard, BigCard bigCard, boolean isCreature, String unitConfig) {
        super();
        this.id = id;
        this.cardname = cardname;
        this.manacost = manacost;
        this.miniCard = miniCard;
        this.bigCard = bigCard;
        this.isCreature = isCreature;
        this.unitConfig = unitConfig;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCardname() {
        return cardname;
    }

    public void setCardname(String cardname) {
        this.cardname = cardname;
    }

    public int getManacost() {
        return manacost;
    }

    public void setManacost(int manacost) {
        this.manacost = manacost;
    }

    public MiniCard getMiniCard() {
        return miniCard;
    }

    public void setMiniCard(MiniCard miniCard) {
        this.miniCard = miniCard;
    }

    public BigCard getBigCard() {
        return bigCard;
    }

    public void setBigCard(BigCard bigCard) {
        this.bigCard = bigCard;
    }

    public boolean getIsCreature() {
        return isCreature;
    }

    public void setIsCreature(boolean isCreature) {
        this.isCreature = isCreature;
    }

    public void setCreature(boolean isCreature) {
        this.isCreature = isCreature;
    }

    public boolean isCreature() {
        return isCreature;
    }

    public String getUnitConfig() {
        return unitConfig;
    }

    public void setUnitConfig(String unitConfig) {
        this.unitConfig = unitConfig;
    }

@Override
public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    Card card = (Card) obj;
    return cardname != null ? cardname.equals(card.cardname) : card.cardname == null;
}

@Override
public int hashCode() {
    return Objects.hash(cardname);
}
}