package Application.Game;

import Application.GameException;
import Application.Models.Team;

import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;

public class GameTeam extends Team<GamePlayer> {
    private final CopyOnWriteArrayList<Card> stack;

    public GameTeam() {
        this.stack = new CopyOnWriteArrayList<>();
    }

    public void collectCard(Card[] cards) throws IllegalArgumentException {
        if (cards == null || cards.length == 0)
            throw new GameException("Cards cannot be null or empty", GameException.Type.CANNOT_BE_NULL);

        Collections.addAll(stack, cards);
    }

    public void collectCard(Card card) throws IllegalArgumentException {
        if (card == null)
            throw new GameException("Card cannot be null", GameException.Type.CANNOT_BE_NULL);

        stack.add(card);
    }

    public int countPoints() {
        int points = 0;
        for (Card card : stack) {
            if (card.getValue().isCountedForPoints)
                points += card.getValue().getValue();
        }

        return points;
    }

    public void clearCollectedCards() {
        stack.clear();
    }

    public int getNCollectedCards() {
        return stack.size();
    }

    @Override
    public String toString() {
        return "GameTeam{\n stack= %S \n %s \n}".formatted(stack, super.toString());
    }
}
