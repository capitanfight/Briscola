package game;

public class Main {
    public static void main(String[] args) {
        Game game = new Game("aywudv");

        game.addPlayer(new Player("ayw"));
        game.addPlayer(new Player("aaw"));

        game.start();

        System.out.println(game);
    }
}
