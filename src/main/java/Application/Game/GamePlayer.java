package Application.Game;

import Application.GameException;
import Application.Models.Player;

import java.util.Arrays;

public class GamePlayer extends Player {
    private final Card[] hand;

    public GamePlayer(long id) {
        super(id);
        hand = new Card[3];
    }

    public void playCard(Card card) {
        if (card == null)
            throw new GameException("Card cannot be null", GameException.Type.CANNOT_BE_NULL);

        boolean found = false;

        for (int i = 0; i < hand.length; i++) {
            if (card.equals(hand[i])) {
                hand[i] = null;
                found = true;
                break;
            }
        }

        if (!found)
            throw new GameException("Player does not have that card", GameException.Type.CARD_NOT_FOUND);
    }

    public void drawCard(Card card) {
        if (card == null)
            throw new GameException("Card cannot be null", GameException.Type.CANNOT_BE_NULL);

        int nCards = hand.length;
        for (int i = 0; i < hand.length; i++) {
            if (hand[i] == null) {
                hand[i] = card;
                break;
            } else
                nCards--;
        }

        if (nCards == 0)
            throw new GameException("Hand is already full", GameException.Type.ALREADY_FULL);
    }

    public void clearHand() {
        Arrays.fill(hand, null);
    }

    public boolean isHandEmpty() {
        boolean res = true;

        for (Card c: hand)
            res = res && c == null;

        return res;
    }

    public Card[] getHand() {
        return hand;
    }

    @Override
    public String toString() {
        return "GamePlayer{\n hand= %s \n %s \n}".formatted(Arrays.toString(hand), super.toString());
    }
}
