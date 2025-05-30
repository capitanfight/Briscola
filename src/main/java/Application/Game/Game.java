package Application.Game;

import Application.GameException;
import Application.Models.Container;
import com.briscola4legenDs.briscola.Room.Team;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class Game extends Container<GameTeam> {
    private static final int nOfCardShuffle = 10;

    private final Card[] deck;
    private int cardIdx;

    private Card[] board;
    private Card.Suit briscola;

    private int turn;
    private long firsPlayer;

    private boolean shouldBeNewTurn;

    private int winner;
    private int[] points;

    public Game(GameTeam[] teams) {
        this.teams = teams;

        deck = new Card[40];
        for (int suit = 0; suit < Card.Suit.values().length; suit++) {
            for (int value = 0; value < Card.Value.values().length; value++) {
                deck[suit * 10 + value] = new Card(Card.Suit.values()[suit].name(), Card.Value.values()[value].name());
            }
        }
    }

    public void start() throws RuntimeException {
        int totalPlayers = getNPlayers();
        if (totalPlayers != 2 && totalPlayers != 4)
            return;

        cardIdx = 0;

        turn = (int) Math.floor(Math.random() * totalPlayers);
        firsPlayer = getTurnPlayerId();

        shouldBeNewTurn = false;

        board = new Card[totalPlayers];
        shuffleDeck();

        briscola = getBriscolaCard().getSuit();
        drawMaxCards();
    }

    public void playCard(Card card, long playerId) {
        if (playerId != getTurnPlayerId())
            throw new GameException("It is not %s player turn".formatted(playerId), GameException.Type.NOT_YOUR_TURN);
        if (card == null)
            throw new GameException("Card cannot be null", GameException.Type.CANNOT_BE_NULL);

        GamePlayer p = getPlayerById(playerId)
                .orElseThrow(() -> new GameException("Player not found", GameException.Type.PLAYER_NOT_FOUND));
        p.playCard(card);
        board[turn] = card;

        incrementTurn();
        if (firsPlayer == getTurnPlayerId() && isBoardFull())
            shouldBeNewTurn = true;
    }

    public void newTurn() throws GameException {
        boolean allHandEmpty = true;
        for (GameTeam team: teams)
            for (GamePlayer player : team.getPlayers())
                allHandEmpty = allHandEmpty && player.isHandEmpty();

        int playerTurnWinnerTablePosition = getTurnWinnerTablePosition();
        GameTeam teamTurnWinner = teams[playerTurnWinnerTablePosition % 2];

        teamTurnWinner.collectCard(board);
        clearBoard();

        if (!allHandEmpty) {
            firsPlayer = getPlayersId()[playerTurnWinnerTablePosition];
            turn = playerTurnWinnerTablePosition;

            if (cardIdx != deck.length)
                drawCard();
        } else
            end();

        shouldBeNewTurn = false;
    }

    private boolean isBoardEmpty() {
        for (Card card: board)
            if (card != null)
                return false;
        return true;
    }

    private boolean isBoardFull() {
        for (Card card: board)
            if (card == null)
                return false;
        return true;
    }

    private int countBoardCards() {
        int count = 0;
        for (Card card: board)
            if (card != null)
                count++;
        return count;
    }

    private void drawMaxCards() {
        for (GameTeam team: teams)
            for (GamePlayer player : team.getPlayers())
                for (int i = 0; i < player.getHand().length; i++)
                    player.drawCard(deck[cardIdx++]);
    }

    private void drawCard() {
        for (int i = 0; i < getTeamSize() * teams.length; i++) {
            int teamIdx = (turn + i) % teams.length;
            int playerIdx = ((turn + i) % getNPlayers()) / teams.length;
            teams[teamIdx].getPlayer(playerIdx).drawCard(deck[cardIdx++]);
        }
    }

    private int getTeamSize() {
        return getNPlayers() / teams.length;
    }

    private void clearBoard() {
        board = new Card[getNPlayers()];
    }

    private void end() {
        points = new int[] {
                teams[0].countPoints(),
                teams[1].countPoints(),
        };

        winner = points[0] > points[1] ? 0 : 1;

        throw new GameException("", GameException.Type.GAME_END);
    }

    private void incrementTurn() {
        turn = (turn + 1) % getNPlayers();
    }

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

    public Long[] getPlayersId() {
        Long[] ids = new Long[getNPlayers()];
        int playerN = 0;
        for (int i = 0; i < teams[0].getPlayers().size(); i++)
            for (GameTeam team : teams)
                ids[playerN++] = team.getPlayers().get(i).getId();
        return ids;
    }

    public Card getBriscolaCard() {
        return deck[deck.length - 1];
    }

    private int getTurnWinnerTablePosition() {
        int tablePosition = getPlayerTablePosition(firsPlayer);
        Card winningCard = board[tablePosition];

        for (int i = 0; i < board.length; i++) {
            Card card = board[i];

            if (card == null)
                throw new RuntimeException("Cannot get the turn winner because the turn is not over");

            if (card.equals(winningCard))
                continue;

            if (winningCard.getSuit() == briscola) {
                Card winner = Card.getWinningCard(winningCard, card);

                tablePosition = winner.equals(card) ? i : tablePosition;
                winningCard = winner;
            } else if (card.getSuit() == briscola) {
                tablePosition = i;
                winningCard = card;
            } else {
                Card winner = Card.getWinningCard(winningCard, card);

                tablePosition = winner.equals(card) ? i : tablePosition;
                winningCard = winner;
            }
        }

        return tablePosition;
    }

    public long getTurnPlayerId() {
        return getPlayersId()[turn];
    }

    public List<Long> getWinner() {
        return teams[winner].getPlayerIds();
    }

    public Card[] getHand(long playerId) {
        GamePlayer player = getPlayerById(playerId).orElseThrow(() -> new GameException("Player not found", GameException.Type.PLAYER_NOT_FOUND));
        return player.getHand();
    }

    public int getPlayerTablePosition(long playerId) {
        int position = 0;
        for (int i = 0; i < teams[i].getPlayers().size(); i++)
            for (GameTeam team : teams)
                if (team.getPlayers().get(i).getId() == playerId)
                    return position;
                else
                    position++;
        return -1;
    }

    private Optional<GamePlayer> getPlayerById(long id) {
        for (GameTeam team : teams)
            for (GamePlayer player : team.getPlayers())
                if (player.getId() == id)
                    return Optional.of(player);
        return Optional.empty();
    }

    public GamePlayer[] getPlayers() {
        ArrayList<GamePlayer> players = new ArrayList<>();
        for (GameTeam team : teams)
            players.addAll(team.getPlayers());
        return players.toArray(new GamePlayer[0]);
    }

    public List<Team> getTeamPlayersId() {
        return List.of(
                new Team(0, teams[0].getPlayerIds()),
                new Team(1, teams[1].getPlayerIds())
        );
    }

    public int[] getTotalCollectedCards() {
        return new int[] {
            teams[0].getNCollectedCards(),
                teams[1].getNCollectedCards()
        };
    }

    @Override
    public String toString() {
        return "Game{\n %s \n}".formatted(super.toString());
    }

    public int getNRemainingCards() {
        return deck.length - cardIdx;
    }
}