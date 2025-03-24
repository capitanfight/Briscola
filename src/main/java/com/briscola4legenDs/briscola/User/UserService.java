package com.briscola4legenDs.briscola.User;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUser(long id) {
        return userRepository.findById(id);
    }

    public void addUser(User user) {
        Optional<User> userOptional = userRepository.findByUsername(user.getUsername());
        if (userOptional.isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        userRepository.save(user);
    }

    public void deleteUser(long id) {
        if (!userRepository.existsById(id))
            throw new IllegalArgumentException("User with id: " + id + " not found");
        userRepository.deleteById(id);
    }

    @Transactional
    public void updateUser(User user) {
        if (userRepository.findById(user.getId()).isEmpty())
            throw new IllegalArgumentException("User with id: " + user.getId() + " not found");

        User oldUser = userRepository.findById(user.getId()).get();
        if (!oldUser.getUsername().equals(user.getUsername()) &&
                user.getUsername() != null &&
                !user.getUsername().isEmpty())
            if (userRepository.findByUsername(user.getUsername()).isEmpty())
                oldUser.setUsername(user.getUsername());
            else
                throw new IllegalArgumentException("Username already exists");

        if (!oldUser.getPassword().equals(user.getPassword()) &&
                user.getPassword() != null &&
                !user.getPassword().isEmpty())
            oldUser.setPassword(user.getPassword());
    }
}
