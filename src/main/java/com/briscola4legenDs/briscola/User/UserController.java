package com.briscola4legenDs.briscola.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

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

    @GetMapping(path = "{id:\\d+}")
    public ResponseEntity<User> getUserById(@PathVariable long id) {
        return userService.getUser(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping(path = "register")
    public RedirectView registerUser(@ModelAttribute User user) {
        try {
            userService.signUpUser(user);
        } catch (UserException e) {
            return new RedirectView("/register?" + e.getType());
        }
        return new RedirectView("/register?success");
    }

    @GetMapping(path = "login")
    public String loginUser() {
        return "login";
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
}
