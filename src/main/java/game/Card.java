package game;

import java.util.Objects;

public class Card {
    public enum Suit {
        COINS, SWORDS, CUPS, BATONS
    }

    public enum Value {
        ACE(11, true),
        THREE(10, true),
        KING(4, true),
        KNIGHT(3, true),
        KNAVE(2, true),
        SEVEN(7, false),
        SIX(6, false),
        FIVE(5, false),
        FOUR(4, false),
        TWO(2, false);

        final int value;
        final boolean isCountedForPoints;

        Value(int value, boolean isCountedForPoints) {
            this.value = value;
            this.isCountedForPoints = isCountedForPoints;
        }

        public int getValue() {
            return value;
        }

        public boolean isCountedForPoints() {
            return isCountedForPoints;
        }
    }

    private final Suit suit;
    private final Value value;

    /**
     * Constructor for class card.
     * @param suit The suit of the card.
     * @param value The value if the card.
     * @see Suit
     * @see Value
     */
    public Card(Suit suit, Value value) {
        this.suit = suit;
        this.value = value;
    }

    public static Card getWinningCard(Card card1, Card card2) {
        return card1.getSuit() == card2.getSuit() ?
                (card1.getValue().isCountedForPoints() && card2.getValue().isCountedForPoints()) ?
                        (card1.getValue().getValue() > card2.getValue().getValue() ? card1 : card2) :
                        (card1.getValue().isCountedForPoints()) ?
                            card1 :
                            card2 :
                card1;
    }

    /**
     * Get the suit of the card.
     * @return The suit of the card.
     * @see Suit
     */
    public Suit getSuit() {
        return suit;
    }

    /**
     * Get the value of the card.
     * @return The value of the card.
     * @see Value
     */
    public Value getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card card)) return false;
        return getSuit() == card.getSuit() && getValue() == card.getValue();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getSuit(), getValue());
    }

    @Override
    public String toString() {
        return value + " of " + suit;
    }
}
