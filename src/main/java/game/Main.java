package game;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:mariadb://192.168.178.55:3306/testdb";
        String user = "admin";
        String password = "4DinForLife";

        try {
            Connection connection = DriverManager.getConnection(url, user, password);
            System.out.println("Connected successfully!");
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Game game = new Game(1L);

        game.addPlayer(new Player(10L));
        game.addPlayer(new Player(100L));

        game.start();

        System.out.println(game);

        HashMap<Long, String> a  = new HashMap<>();
        Optional.of(a.get(1L));

    }
}
