package com.briscola4legenDs.briscola.User.REST;

import com.briscola4legenDs.briscola.Assets.RESTInfo;
import com.briscola4legenDs.briscola.User.Friends.FriendException;
import com.briscola4legenDs.briscola.User.Friends.FriendRelation;
import com.briscola4legenDs.briscola.User.Friends.FriendRequest;
import com.briscola4legenDs.briscola.User.Stats.Stats;
import com.briscola4legenDs.briscola.User.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping(path = "{id:\\d+}")
    public ResponseEntity<User> getUserById(@PathVariable long id) {
        return userService.getUser(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("{username:^(?=.*\\D).+$}")
    public long getUserIdByUsername(@PathVariable String username) {
        return userService.getUserIdByUsername(username);
    }

    @DeleteMapping(path = "{id:\\d+}/delete")
    public void deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
    }

    @PutMapping(path = "update")
    public ResponseEntity<Void> updateUser(@RequestBody User user) {
        if (userService.updateUser(user)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/friend/request/{id:\\d+}")
    public List<FriendRequest> getFriendRequests(@PathVariable long id) {
        return userService.getFriendRequests(id);
    }

    @PostMapping("/friend/request/send")
    public ResponseEntity<Void> sendFriendRequest(@RequestBody FriendRequest friendRequest) {
        try {
            userService.sendFriendRequest(friendRequest);
            return ResponseEntity.ok().build();
        } catch (FriendException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/friend/request/accept")
    public ResponseEntity<Void> acceptFriendRequest(@RequestBody FriendRequest friendRequest) {
        try {
            userService.acceptFriendRequest(friendRequest);
            return ResponseEntity.ok().build();
        } catch (FriendException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/friend/request/reject")
    public ResponseEntity<Void> rejectFriendRequest(@RequestBody FriendRequest friendRequest) {
        try {
            userService.rejectFriendRequest(friendRequest);
            return ResponseEntity.ok().build();
        } catch (FriendException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/friend")
    public void removeFriend(@RequestBody FriendRelation friendRelation) {
        userService.removeFriend(friendRelation);
    }

    @GetMapping("/friend/{id:\\d+}")
    public List<FriendRelation> getFriends(@PathVariable long id) {
        return userService.getFriends(id);
    }

    @GetMapping("/stats/{id:\\d+}")
    public Stats getStats(@PathVariable long id) {
        return userService.getStats(id);
    }

    @PutMapping("/stats")
    public ResponseEntity<Void> updateStats(@RequestBody Stats stats) {
        if (userService.updateStats(stats))
            return ResponseEntity.ok().build();
        else
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @GetMapping("/info")
    public RESTInfo[] info() {
        return new RESTInfo[]{
                new RESTInfo(
                        "api/user/",
                        "GET",
                        "GetUsers(): List<User>",
                        "Get all the Users."
                ),
                new RESTInfo(
                        "api/user/{id}",
                        "GET",
                        "getUserById(id): User",
                        "id: long -> Id of user",
                        "Get the specified User."
                ),
                new RESTInfo(
                        "api/user/{username}",
                        "GET",
                        "getUserIdByUsername(username): long",
                        "username: String -> username of the user",
                        "Get the id of the specified User."
                ),
                new RESTInfo(
                        "api/user/{id}/delete",
                        "DELETE",
                        "deleteUser(id): void",
                        "id: long -> Id of user",
                        "Delete the specified User."
                ),
                new RESTInfo(
                        "api/user/update",
                        "PUT",
                        "updateUser(user): ResponseEntity<Void>",
                        "user: User -> modified user",
                        "Update the specified User."
                ),
                new RESTInfo(
                        "api/user/friend",
                        "POST",
                        "addFriend(FriendRelation): void",
                        "FriendRelation -> { userId: long -> id of the user, friendId: long -> id of the friend }",
                        "Add the specified User(friendId) to the Friend list of the specified User(userId) and the opposite."
                ),
                new RESTInfo(
                        "api/user/friend",
                        "DELETE",
                        "removeFriend(FriendRelation): void",
                        "FriendRelation: [RequestBody ~(json)~] -> { userId: long -> id of the user, friendId: long -> id of the friend }",
                        "Delete the specified User(friendId) to the Friend list of the specified User(userId) and the opposite."
                ),
                new RESTInfo(
                        "api/user//friend/{id}",
                        "GET",
                        "getFriends(id): List<FriendRelation>",
                        "id: long -> Id of user",
                        "Get all the Users who are friend with the specified User."
                ),
        };
    }
}
