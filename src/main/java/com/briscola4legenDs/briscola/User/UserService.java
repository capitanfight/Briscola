package com.briscola4legenDs.briscola.User;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final static String USER_NOT_FOUND_MSG = "user with identifier %s not found";

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Optional<User> optionalUserByEmail = userRepository.findByEmail(identifier);
        Optional<User> optionalUserByUsername = userRepository.findByUsername(identifier);

        if (optionalUserByEmail.isPresent() || optionalUserByUsername.isPresent()) {
            Optional<User> optionalUser = optionalUserByEmail.isPresent() ? optionalUserByEmail : optionalUserByUsername;

            User user = optionalUser.get();
            return User.builder()
                    .email(user.getEmail())
                    .username(user.getUsername())
                    .password(passwordEncoder.encode(user.getPassword()))
                    .build();
        } else {
            throw new UsernameNotFoundException(String.format(USER_NOT_FOUND_MSG, identifier));
        }
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUser(long id) {
        return userRepository.findById(id);
    }

    public void signUpUser(User user) {
        Optional<User> userOptional = userRepository.findByUsername(user.getUsername());
        if (userOptional.isPresent())
            throw new UserException("Username already exists", UserException.Type.UsernameAlreadyTaken);
        else {
            userOptional = userRepository.findByEmail(user.getUsername());
            if (userOptional.isPresent())
                throw new UserException("Email already exists", UserException.Type.EmailAlreadyTaken);
        }

//        String encodedPassword = passwordEncoder.encode(user.getPassword());
//        user.setPassword(encodedPassword);

        userRepository.save(user);
    }

    public void deleteUser(long id) {
        if (!userRepository.existsById(id))
            throw new IllegalArgumentException("User with id: " + id + " not found");
        userRepository.deleteById(id);
    }

    @Transactional
    public boolean updateUser(User user) {
        boolean updated = false;

        if (userRepository.findById(user.getId()).isEmpty())
            throw new IllegalArgumentException("User with id: " + user.getId() + " not found");

        User oldUser = userRepository.findById(user.getId()).get();
        if (!oldUser.getEmail().equals(user.getEmail()) &&
                user.getEmail() != null &&
                !user.getEmail().isEmpty())
            if (userRepository.findByEmail(user.getEmail()).isEmpty()) {
                oldUser.setUsername(user.getEmail());
                updated = true;
            } else
                throw new IllegalArgumentException("Email already exists");

        if (!oldUser.getUsername().equals(user.getUsername()) &&
                user.getUsername() != null &&
                !user.getUsername().isEmpty())
            if (userRepository.findByUsername(user.getUsername()).isEmpty()) {
                oldUser.setUsername(user.getUsername());
                updated = true;
            } else
                throw new IllegalArgumentException("Username already exists");

        if (!oldUser.getPassword().equals(user.getPassword()) &&
                user.getPassword() != null &&
                !user.getPassword().isEmpty()) {
            oldUser.setPassword(user.getPassword());
            updated = true;
        }

        return updated;
    }
}
