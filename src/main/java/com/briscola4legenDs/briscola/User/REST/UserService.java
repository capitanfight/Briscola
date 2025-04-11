package com.briscola4legenDs.briscola.User.REST;

import com.briscola4legenDs.briscola.User.Friends.FriendException;
import com.briscola4legenDs.briscola.User.Friends.FriendRelation;
import com.briscola4legenDs.briscola.User.Friends.FriendRelationRepository;
import com.briscola4legenDs.briscola.User.User;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FriendRelationRepository friendRelationRepository;

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUser(long id) {
        return userRepository.findById(id);
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

    public void addFriend(FriendRelation friendRelation) {
        if (!userRepository.existsById(friendRelation.getUserId()))
            throw new FriendException("User with id: %s does not exist".formatted(friendRelation.getUserId()), FriendException.Type.USER_ID_NOT_FOUND);
        if (!userRepository.existsById(friendRelation.getFriendId()))
            throw new FriendException("User with id: %s does not exist".formatted(friendRelation.getFriendId()), FriendException.Type.USER_ID_NOT_FOUND);

        if (friendRelation.getUserId() == friendRelation.getFriendId())
            throw new FriendException("You cannot be friend of yourself", FriendException.Type.CANNOT_BE_FRIEND);

        List<FriendRelation> friends = friendRelationRepository.findByUserId(friendRelation.getUserId());
        if (friends.contains(friendRelation))
            throw new FriendException("Friend with id: %s already exists".formatted(friendRelation.getFriendId()), FriendException.Type.FRIEND_ALREADY_EXISTS);

        FriendRelation oppositeRelation = FriendRelation.builder()
                        .userId(friendRelation.getFriendId())
                        .friendId(friendRelation.getUserId())
                        .build();

        friendRelationRepository.save(friendRelation);
        friendRelationRepository.save(oppositeRelation);
    }

    public void removeFriend(FriendRelation friendRelation) {
        if (!userRepository.existsById(friendRelation.getUserId()))
            throw new FriendException("User with id: %s does not exist".formatted(friendRelation.getUserId()), FriendException.Type.USER_ID_NOT_FOUND);
        if (!userRepository.existsById(friendRelation.getFriendId()))
            throw new FriendException("User with id: %s does not exist".formatted(friendRelation.getFriendId()), FriendException.Type.USER_ID_NOT_FOUND);

        List<FriendRelation> friends = friendRelationRepository.findByUserId(friendRelation.getUserId());
        if (!friends.contains(friendRelation))
            throw new FriendException("Friend with id: %s does not exist".formatted(friendRelation.getFriendId()), FriendException.Type.FRIEND_NOT_EXISTS);

        friendRelation.setId(friendRelationRepository.findIDByRelation(friendRelation.getUserId(), friendRelation.getFriendId()));

        FriendRelation oppositeRelation = FriendRelation.builder()
                .userId(friendRelation.getUserId())
                .friendId(friendRelation.getFriendId())
                .id(friendRelation.getId() + 1)
                .build();

        friendRelationRepository.delete(friendRelation);
        friendRelationRepository.delete(oppositeRelation);
    }

    public List<FriendRelation> getFriends(long userId) {
        if (!userRepository.existsById(userId))
            throw new IllegalArgumentException("User with id: %s does not exist".formatted(userId));

        return friendRelationRepository.findByUserId(userId);
    }

    public long getUserIdByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username: %s not found".formatted(username)));

        return user.getId();
    }
}
