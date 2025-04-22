package com.briscola4legenDs.briscola.Room.REST;

import Application.Game.Card;
import com.briscola4legenDs.briscola.Assets.RESTInfo;
import com.briscola4legenDs.briscola.Room.Room;
import com.briscola4legenDs.briscola.Room.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping(path = "api/room")
public class RoomController {
    // TODO: cambiare il token facendolo diventare un singolo hash che verra' salvato nel db insieme a roomId e playerId

    private final RoomService roomService;

    @Autowired
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public Collection<Room> getAllRooms() {
        return roomService.getAllRooms();
    }

    @DeleteMapping
    public void removeAllRooms() {
        roomService.rmvRooms();
    }

    @GetMapping(path = "{id:\\d+}")
    public ResponseEntity<Room> getRoomById(@PathVariable long id) {
        Room room = roomService.getRoomById(id);
        return room == null ? ResponseEntity.badRequest().build() : ResponseEntity.ok(room);
    }

    @GetMapping(path = "{id:\\d+}/name")
    public String getName(@PathVariable long id) {
        return roomService.getName(id);
    }

    @GetMapping(path = "{name:^(?=.*\\D).+$}/{visibility}")
    public long createRoom(@PathVariable String name, @PathVariable Room.Visibility visibility) {
        return roomService.createRoom(name, visibility);
    }

    @GetMapping("/{id:\\d+}/host")
    public long getHostId(@PathVariable long id) {
        return roomService.getHostId(id);
    }

    @GetMapping("/{id:\\d+}/players")
    public List<Long> getPlayers(@PathVariable long id) {
        return roomService.getPlayers(id);
    }

    @PostMapping(path = "player")
    public ResponseEntity<Void> addPlayer(@RequestBody Token token) {
        try {
            roomService.addPlayer(token);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping(path = "player")
    public void removePlayer(@RequestBody Token token) {
        roomService.rmvPlayer(token);
    }

    @GetMapping(path = "{roomId:\\d+}/player/{userId:\\d+}/state")
    public Boolean getPlayerState(@PathVariable long roomId, @PathVariable long userId) {
        return roomService.getPlayerState(new Token(roomId, userId));
    }

    @PutMapping(path = "player/{state}")
    public void setPlayerReady(@RequestBody Token token, @PathVariable boolean state) {
        roomService.setPlayerReady(token, state);
    }

    @GetMapping("{id:\\d+}/player/team")
    public long[][] getTeams(@PathVariable long id) {
        return roomService.getTeams(id);
    }

    @PutMapping("/player/team/{team:\\d+}")
    public void changeTeam(@RequestBody Token token, @PathVariable int team) {
        roomService.changeTeam(token, team);
    }
    
    @GetMapping(path = "{id:\\d+}/briscola")
    public Card getBriscolaCard(@PathVariable long id) {
        return roomService.getBriscolaCard(id);
    }

    @GetMapping(path = "{roomId:\\d+}/player/{playerId:\\d+}/hand")
    public Card[] getPlayerHand(@PathVariable long roomId, @PathVariable long playerId) {
        return roomService.getHand(new Token(roomId, playerId));
    }

    @GetMapping(path = "{id:\\d+}/turn/id")
    public long getTurnPlayerId(@PathVariable long id) {
        return roomService.getTurnPlayerId(id);
    }

    @PostMapping(path = "{roomId:\\d+}/{playerId:\\d+}/playCard")
    public void playCard(@PathVariable long roomId, @PathVariable long playerId, @RequestBody Card card) {
        roomService.playCard(new Token(roomId, playerId), card);
    }

    @GetMapping("/{id:\\d+}/nCollectedCards")
    public int[] getNCollectedCards(@PathVariable long id) {
        return roomService.getNCollectedCards(id);
    }

    @GetMapping("/{id:\\d+}/nRemainingCards")
    public int getNRemainingCards(@PathVariable long id) {
        return roomService.getNRemainingCards(id);
    }

    @GetMapping(path = "{id:\\d+}/board")
    public Card[] getBoard(@PathVariable long id) {
        return roomService.getBoard(id);
    }

    @GetMapping("/{id:\\d+}/canStart")
    public boolean canGameStart(@PathVariable long id) {
        return roomService.checkIfGameCanStart(id);
    }

    @GetMapping(path = "{id:\\d+}/gameOver")
    public boolean isGameOver(@PathVariable long id) {
        return roomService.isGameOver(id);
    }

    @GetMapping(path = "{id:\\d+}/winner")
    public long[] getWinner(@PathVariable long id) {
        return roomService.getWinner(id);
    }

    @GetMapping(path = "{id:\\d+}/points")
    public int[] getPoints(@PathVariable long id) {
        return roomService.getPoints(id);
    }

    @GetMapping(path = "info")
    public RESTInfo[] getInfo() {
        return new RESTInfo[] {
                new RESTInfo(
                        "api/room",
                        "GET",
                        "GetAll(): Collection<Room>",
                        "Get all the rooms."
                        ),
                new RESTInfo(
                        "api/room",
                        "DELETE",
                        "removeAllRooms(): void",
                        "Remove all the available rooms."
                ),
                new RESTInfo(
                        "api/room/{id}",
                        "GET",
                        "getRoomById(id): Room",
                        "Id: long -> The id of the room",
                        "Get the room with the specified ID."
                ),
                new RESTInfo(
                        "api/room/{id}/name",
                        "GET",
                        "getName(id): String",
                        "Id: long -> The id of the room",
                        "Get the name of the room with the specified ID."
                ),
                new RESTInfo(
                        "api/room/{name}/{visibility}",
                        "GET",
                        "createRoom(name, visibility): long",
                        "Name: String -> The name of the new room, visibility: Visibility -> Visibility of the room",
                        "Generate a new room and return the id."
                ),
                new RESTInfo(
                        "api/room/player",
                        "POST",
                        "addPlayer(token): void",
                        "body: { roomId: long -> The id of the room, playerId: long -> The id of the player }",
                        "Add the player to the desired room."
                ),
                new RESTInfo(
                        "api/room/player",
                        "REMOVE",
                        "removePlayer(token): void",
                        "body: { roomId: long -> The id of the room, playerId: long -> The id of the player }",
                        "Add the player to the desired room."
                ),
                new RESTInfo(
                        "api/room/player/{state}",
                        "PUT",
                        "setPlayerReady(token, state): void",
                        "body: { roomId: long -> The id of the room, playerId: long -> The id of the player }, state: boolean -> If the player is ready or not",
                        "Set the status of the player."
                ),
                new RESTInfo(
                        "api/room/{id}/briscola",
                        "GET",
                        "getBriscolaCard(id): Card",
                        "Id: long -> The id of the room",
                        "Get the briscola card of the specified room."
                ),
                new RESTInfo(
                        "api/room/{roomId}/player/{playerId}/hand",
                        "GET",
                        "getPlayerHand(roomId, playerId): Card[]",
                        "roomId: long -> The id of the room, playerId: long -> The id of the player",
                        "Get the hand of the specified player in the specified room."
                ),
                new RESTInfo(
                        "api/room/{id}/turn/id",
                        "GET",
                        "getTurnPlayerId(id): long",
                        "Id: long -> The id of the room",
                        "Get the if of the player who is about to play."
                ),
                new RESTInfo(
                        "api/room/{id}/playCard",
                        "POST",
                        "playCard(id, card): void",
                        "Id: long -> The id of the room, body: { suit: String -> Suit of the card, value: String -> Value of the card}",
                        "Play the specified card."
                ),
                new RESTInfo(
                        "api/room/{id}/turn/id",
                        "GET",
                        "getBoard(id): Card[]",
                        "Id: long -> The id of the room",
                        "Get the if of the player who is about to play."
                ),
                new RESTInfo(
                        "api/room/{id}/gameOver",
                        "GET",
                        "isGameOver(id): boolean",
                        "Id: long -> The id of the room",
                        "Get if the game is over."
                ),
                new RESTInfo(
                        "api/room/{id}/winner",
                        "GET",
                        "getWinner(id): long[]",
                        "Id: long -> The id of the room",
                        "Get the player/team of winner/s."
                ),
                new RESTInfo(
                        "api/room/{id}/points",
                        "GET",
                        "getPoints(id): int[]",
                        "Id: long -> The id of the room",
                        "Get points of each team."
                )
        };
    }
}
