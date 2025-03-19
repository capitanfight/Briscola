package game;

import java.util.ArrayList;
import java.util.Arrays;

public class Game {
    private static final int nOfCardShuffle = 10;

    private final String id;

    private final ArrayList<Player> players;

    private final Card[] deck;
    private Card[] board;
    private Card.Suit briscola;

    private int turn;
    private int firsPlayer;

    private int cardIdx;

    private boolean gameOver;

    private int winner;

    /**
     * Constructor for class game.
     * @param id Unique identifier for game.
     * @throws IllegalArgumentException If id is null or empty.
     */
    public Game(String id) throws IllegalArgumentException {
        if (id == null || id.isEmpty())
            throw new IllegalArgumentException("id is null");
        this.id = id;

        players = new ArrayList<>();

        deck = new Card[40];

        for (int suit = 0; suit < Card.Suit.values().length; suit++) {
            for (int value = 0; value < Card.Value.values().length; value++) {
                deck[suit * 10 + value] = new Card(Card.Suit.values()[suit], Card.Value.values()[value]);
            }
        }
    }

    /**
     * Add player to the game.
     * @param player Player to add.
     * @throws IllegalArgumentException If player is null or already contained.
     */
    public void addPlayer(Player player) throws IllegalArgumentException {
        if (player == null)
            throw new IllegalArgumentException("Player cannot be null");
        if (players.contains(player))
            throw new IllegalArgumentException("Cannot add the same player 2 times");
        players.add(player);
    }

    /**
     * Initialize the remaining variables and start the game.
     * @throws RuntimeException If players size is not 2 or 4.
     */
    public void start() throws RuntimeException {
        if (players.size() != 2 && players.size() != 4)
            throw new RuntimeException("Invalid number of players");

        gameOver = false;
        cardIdx = 0;

        turn = (int) Math.floor(Math.random() * players.size());
        firsPlayer = turn;

        board = new Card[players.size()];
        shuffleDeck();
        briscola = getBriscolaCard().getSuit();

        drawCards();
    }

    /**
     * Play a card and update the turn.
     * @param card Card to play.
     * @throws IllegalArgumentException If card-parameter is null.
     * @throws RuntimeException If the game is over.
     */
    public void playCard(Card card) throws IllegalArgumentException, RuntimeException {
        if (card == null)
            throw new IllegalArgumentException("Card cannot be null");
        if (gameOver)
            throw new RuntimeException("Game over");

        players.get(turn).playCard(card);
        board[turn] = card;

        incrementTurn();
        if (turn == firsPlayer)
            newTurn();
    }

    /**
     * Shuffle the deck
     */
    private void shuffleDeck() {
        for (int nOfShuffles = 0; nOfShuffles < nOfCardShuffle; nOfShuffles++) {
            for (int cards = 0; cards < deck.length; cards++) {
                int idx = (int) Math.floor(Math.random() * (deck.length - cards) + cards);

                Card temp = deck[cards];
                deck[cards] = deck[idx];
                deck[idx] = temp;
            }
        }
    }

    /**
     * Increment the turn maintaining it in the range of the players.
     */
    private void incrementTurn() {
        turn = (turn + 1) % players.size();
    }

    /**
     * Update the turn.
     */
    private void newTurn() {
        if (cardIdx != deck.length) {
            Player turnWinner = getTurnWinner();

            turnWinner.collectCard(board);
            clearBoard();

            firsPlayer = players.indexOf(turnWinner);
            turn = firsPlayer;

            drawCards();
        } else
            end();
    }

    /**
     * Make the player draw new cards.
     */
    private void drawCards() {
        for (Player player : players) {
            player.drawCard(deck[cardIdx]);
            cardIdx++;
        }
    }

    /**
     * Clear the board.
     */
    private void clearBoard() {
        board = new Card[players.size()];
    }

    /**
     * End the game.
     */
    private void end() {
        gameOver = true;

        int[] points = new int[] {0, 0};
        for (int i = 0; i < players.size(); i++) {
            points[i % 2] += players.get(i).countPoints();
        }

        winner = points[0] > points[1] ? 0 : 1;
    }

    private Player getTurnWinner() {
        Card winningCard = board[firsPlayer];
        int idx = 0;

        for (int i = 0; i < board.length; i++) {
            Card card = board[idx];

            if (card == null)
                throw new RuntimeException("Cannot get the turn winner because the turn is not over");

            if (card.equals(winningCard))
                continue;

            if (winningCard.getSuit() == briscola) {
                Card winner = Card.getWinningCard(winningCard, card);

                idx = winner.equals(card) ? i : idx;
                winningCard = winner;
            }
            else if (card.getSuit() == briscola) {
                winningCard = card;
                idx = i;
            }
        }

        return players.get(idx);
    }

    public Card getBriscolaCard() {
        return deck[deck.length - 1];
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public String getId() {
        return id;
    }

    /**
     * Get the number that represent the winning team/player.
     * @return Number that represent the winning team/player or -1 if the game is not over.
     */
    public int getWinner() {
        return gameOver ? -1 : winner;
    }

    @Override
    public String toString() {
        return "Game{" +
                "players=" + players + '\n' +
                "deck=" + Arrays.toString(deck) + '\n' +
                "board=" + Arrays.toString(board) + '\n' +
                "briscola=" + briscola + '\n' +
                "turn=" + turn + '\n' +
                "firsPlayer=" + firsPlayer + '\n' +
                "cardIdx=" + cardIdx + '\n' +
                "gameOver=" + gameOver + '\n' +
                "winner=" + winner + '\n' +
                '}';
    }
}
