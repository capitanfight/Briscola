package game;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

public class Player {
    private final Long id;
    private final Card[] hand;
    private final CopyOnWriteArrayList<Card> collectedCards;

    /**
     * Constructor for player.
     * @param id Unique identifier for the player. Expressed as an alphanumerical value.
     * @throws IllegalArgumentException If id is negative or zero.
     */
    public Player(Long id) throws IllegalArgumentException {
        if (id <= 0)
            throw new IllegalArgumentException("id is not valid");
        this.id = id;
        hand = new Card[3];
        collectedCards = new CopyOnWriteArrayList<>();
    }

    /**
     * Play a card and remove it from the hand.
     * @param card Card that is played.
     * @throws IllegalArgumentException If the parameter card is null or if the card is not in the hand.
     */
    public void playCard(Card card) throws IllegalArgumentException {
        if (card == null)
            throw new IllegalArgumentException("Card cannot be null");

        boolean found = false;

        for (int i = 0; i < hand.length; i++) {
            if (card.equals(hand[i])) {
                hand[i] = null;
                found = true;
                break;
            }
        }

        if (!found)
            throw new IllegalArgumentException("Player does not have that card");
    }

    /**
     * Draw a card and add it to the hand.
     * @param card Card that is drawn.
     * @throws IllegalArgumentException If the card-parameter is null.
     */
    public void drawCard(Card card) throws IllegalArgumentException {
        if (card == null)
            throw new IllegalArgumentException("Card cannot be null");

        int nCards = hand.length;
        for (int i = 0; i < hand.length; i++) {
            if (hand[i] == null) {
                hand[i] = card;
                break;
            } else
                nCards--;
        }

        if (nCards == 0)
            throw new RuntimeException("Hand is already full");
    }

    /**
     * Collect the cards and add them to collectedCards.
     * @param cards Array of cards collected.
     * @throws IllegalArgumentException If the cards-parameter is null or empty.
     */
    public void collectCard(Card[] cards) throws IllegalArgumentException {
        if (cards == null || cards.length == 0)
            throw new IllegalArgumentException("Cards cannot be null or empty");

        Collections.addAll(collectedCards, cards);
    }

    /**
     * Collect the card and add it to collectedCards.
     * @param card Card collected.
     * @throws IllegalArgumentException If the card-parameter is null.
     */
    public void collectCard(Card card) throws IllegalArgumentException {
        if (card == null)
            throw new IllegalArgumentException("Card cannot be null");

        collectedCards.add(card);
    }

    /**
     * Count the points in collectedCards.
     * @return Total of the points.
     * @link <a href="https://en.wikipedia.org/wiki/Briscola#The_cards">Documentation about the point system</a>
     * @see game.Card.Value
     */
    public int countPoints() {
        int points = 0;
        for (Card card : collectedCards) {
            if (card.getValue().isCountedForPoints)
                points += card.getValue().getValue();
        }

        return points;
    }

    /**
     * Clear the hand replacing all the cards with null.
     */
    public void clearHand() {
        Arrays.fill(hand, null);
    }

    /**
     * clear collectedCards.
     */
    public void clearCollectedCards() {
        collectedCards.clear();
    }

    /**
     * Check if the hand is empty.
     * @return True if the hand is empty, otherwise false.
     */
    public boolean isHandEmpty() {
        boolean res = true;

        for (Card c: hand)
            res = res && c == null;

        return res;
    }

    public Long getId() {
        return id;
    }

    public Card[] getHand() {
        return hand;
    }

    @Override
    public String toString() {
        return "Player{" +
                "id='" + id + '\'' +
                ", hand=" + Arrays.toString(hand) +
                ", collectedCards=" + collectedCards +
                '}';
    }
}
