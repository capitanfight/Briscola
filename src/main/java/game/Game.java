package game;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Game {
    private static final int nOfCardShuffle = 10;

    private final Long id;

    private final CopyOnWriteArrayList<Player> players;

    private final Card[] deck;
    private Card[] board;
    private Card.Suit briscola;

    private int turn;
    private int firsPlayer;

    private int cardIdx;

    private boolean gameOver;
    private boolean isGameStarted;

    private int winner;
    private int[] points;

    /**
     * Constructor for class game.
     * @param id Unique identifier for game.
     * @throws IllegalArgumentException If id negative or zero.
     */
    public Game(Long id) throws IllegalArgumentException {
        if (id <= 0)
            throw new IllegalArgumentException("id is not valid");
        this.id = id;

        players = new CopyOnWriteArrayList<>();
        gameOver = false;
        isGameStarted = false;

        deck = new Card[40];

        for (int suit = 0; suit < Card.Suit.values().length; suit++) {
            for (int value = 0; value < Card.Value.values().length; value++) {
                deck[suit * 10 + value] = new Card(Card.Suit.values()[suit].name(), Card.Value.values()[value].name());
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
        if (isGameStarted && !isGameOver())
            throw new RuntimeException("Cannot add player while the game is running");
        if (players.contains(player))
            throw new IllegalArgumentException("Cannot add the same player 2 times");
        players.add(player);
    }

    /**
     * Remove player from the game.
     * @param id Player to remove.
     * @throws IllegalArgumentException If player id is null, negative or zero.
     */
    public void removePlayer(Long id) throws IllegalArgumentException {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("Player id cannot be null, negative or zero");
        if (isGameStarted && !isGameOver())
            throw new RuntimeException("Cannot remove player while the game is running");

        Optional<Player> playerToRemove = getPlayer(id);

        if (playerToRemove.isEmpty())
            throw new IllegalArgumentException("Player with id " + id + " not found");
        players.remove(playerToRemove.get());
    }

    /**
     * Find the player.
     * @param id Id of the player to find.
     * @return The Optional of the player.
     * @throws IllegalArgumentException If player id is null, negative or zero.
     */
    public Optional<Player> getPlayer(Long id) throws IllegalArgumentException {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("Player id cannot be null, negative or zero");

        for (Player p: players)
            if (p.getId().equals(id))
                return Optional.of(p);
        return Optional.empty();
    }

    /**
     * Find the index of the player in players array.
     * @param id Id of the player to find.
     * @return The index of the player. If the player is not found it returns -1.
     * @throws IllegalArgumentException If player id is null, negative or zero.
     */
    public int getPlayerIdx(Long id) {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("Player id cannot be null, negative or zero");

        if (getPlayer(id).isPresent())
            return players.indexOf(getPlayer(id).get());
        return -1;
    }


    /**
     * Check if player exist by the id.
     * @param id Id of the player to find.
     * @return True if the player is found, otherwise false.
     * @throws IllegalArgumentException If player id is null, negative or zero.
     */
    public boolean playerExists(Long id) throws IllegalArgumentException {
        if (id == null || id <= 0)
            throw new IllegalArgumentException("Player id cannot be null, negative or zero");

        return getPlayer(id).isPresent();
    }

    /**
     * Check if there are no players in the game.
     * @return True if there are no players, otherwise false.
     */
    public boolean isEmpty() {
        return players.isEmpty();
    }

    /**
     * Initialize the remaining variables and start the game.
     * @throws RuntimeException If players size is not 2 or 4.
     */
    public void start() throws RuntimeException {
        if (players.size() != 2 && players.size() != 4)
            return;
            // throw new RuntimeException("Invalid number of players");

        isGameStarted = true;
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
        if (gameOver || !isGameStarted)
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
            for (int cards = 0; cards < deck.length - 1; cards++) {
                int idx = (int) Math.floor(Math.random() * (deck.length - cards - 1) + cards + 1);

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
        boolean allHandEmpty = true;
        for (Player p : players)
            allHandEmpty = allHandEmpty && p.isHandEmpty();

        Player turnWinner = getTurnWinner();

        turnWinner.collectCard(board);
        clearBoard();
        if (!allHandEmpty) {
            firsPlayer = players.indexOf(turnWinner);
            turn = firsPlayer;
            if (cardIdx != deck.length)
                drawCard();
        } else
            end();
    }

    /**
     * Make the player draw new cards.
     */
    private void drawCards() {
        for (Player player : players) {
            for (int i = 0; i < player.getHand().length; i++) {
                player.drawCard(deck[cardIdx]);
                cardIdx++;
            }
        }
    }

    /**
     * Make the player draw new card.
     */
    private void drawCard() {
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

        points = new int[] {0, 0};
        for (int i = 0; i < players.size(); i++) {
            points[i % 2] += players.get(i).countPoints();
        }

        winner = points[0] > points[1] ? 0 : 1;
    }

    /**
     * Get the player who won the turn.
     * @return Winning player.
     */
    private Player getTurnWinner() {
        Card winningCard = board[firsPlayer];
        int idx = firsPlayer;

        for (int i = 0; i < board.length; i++) {
            Card card = board[i];

            if (card == null)
                throw new RuntimeException("Cannot get the turn winner because the turn is not over");

            if (card.equals(winningCard))
                continue;

            if (winningCard.getSuit() == briscola) {
                Card winner = Card.getWinningCard(winningCard, card);

                idx = winner.equals(card) ? i : idx;
                winningCard = winner;
            } else if (card.getSuit() == briscola) {
                idx = i;
                winningCard = card;
            } else {
                Card winner = Card.getWinningCard(winningCard, card);

                idx = winner.equals(card) ? i : idx;
                winningCard = winner;
            }
        }

        return players.get(idx);
    }

    /**
     * Get the player id of the player who is about to play.
     * @return Player id of the player who is about to play
     */
    public Long getTurnPlayerId() {
        if (players.isEmpty() || !isGameStarted)
            return null;
        return players.get(turn).getId();
    }

    /**
     * Get the number/s that represent the winning team/player.
     * @return Number/s that represent the winning team/player or -1 if the game is not over.
     */
    public long[] getWinner() {
        long[] winnerTeam = new long[players.size()/2];

        if (!isGameStarted || gameOver)
            return null;

        for (int i = winner; i < players.size(); i += 2)
            winnerTeam[i / 2] = players.get(i).getId();

        return gameOver || !isGameStarted ? winnerTeam : null;
    }

    public Card[] getBoard() {
        return board;
    }

    public Card getBriscolaCard() {
        return deck[deck.length - 1];
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public long getId() {
        return id;
    }

    public int getNPlayers() {
        return players.size();
    }

    public int[] getPoints() {
        return points;
    }

    public boolean isGameStarted() {
        return isGameStarted;
    }

    public CopyOnWriteArrayList<Player> getPlayers() {
        return players;
    }

    @Override
    public String toString() {
        return "Game{\n" +
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
