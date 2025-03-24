package com.briscola4legenDs.briscola.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("api/user")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<User> getUsers() {
        return userService.getUsers();
    }

    @GetMapping(path = "/id/{id:\\d+}")
    public Optional<User> getUserById(@PathVariable long id) {
        return userService.getUser(id);
    }

    @PostMapping()
    public void registerUser(@RequestBody User user) {
        userService.addUser(user);
    }

    @DeleteMapping(path = "/id/{id:\\d+}")
    public void deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
    }

    @PutMapping(path = "/id/{id:\\d+}")
    public void updateUser(@PathVariable Long id, @RequestParam(required = false) String username, @RequestParam(required = false) String password) {
        userService.updateUser(new User(id, username, password));
    }

    @PutMapping()
    public void updateUser(@RequestBody User user) {
        userService.updateUser(user);
    }
}
