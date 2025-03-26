package com.briscola4legenDs.briscola.Room.REST;

import com.briscola4legenDs.briscola.Room.Room;
import com.briscola4legenDs.briscola.Room.Token;
import game.Card;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;

@RestController
@RequestMapping(path = "api/room")
@CrossOrigin(origins = "*")
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
    public Room getRoomById(@PathVariable long id) {
        return roomService.getRoomById(id);
    }

    @GetMapping(path = "{id:\\d+}/name")
    public String getName(@PathVariable long id) {
        return roomService.getName(id);
    }

    @GetMapping(path = "{name:^(?=.*\\D).+$}")
    public long createRoom(@PathVariable String name) {
        return roomService.createRoom(name);
    }

    @PostMapping(path = "player")
    public void addPlayer(@RequestBody Token token) {
        roomService.addPlayer(token);
    }

    @DeleteMapping(path = "player")
    public void removePlayer(@RequestBody Token token) {
        roomService.rmvPlayer(token);
    }

    @PutMapping(path = "player/{state}")
    public void setPlayerReady(@RequestBody Token token, @PathVariable boolean state) {
        roomService.setPlayerReady(token, state);
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

    @PostMapping(path = "{id:\\d+}/playCard")
    public void playCard(@PathVariable long id, @RequestBody Card card) {
        roomService.playCard(id, card);
    }

    @GetMapping(path = "{id:\\d+}/board")
    public Card[] getBoard(@PathVariable long id) {
        return roomService.getBoard(id);
    }

    @GetMapping(path = "{id:\\d+}/gameOver")
    public boolean isGameOver(@PathVariable long id) {
        return roomService.isGameOver(id);
    }
}
