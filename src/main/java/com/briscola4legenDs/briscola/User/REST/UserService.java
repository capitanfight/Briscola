package com.briscola4legenDs.briscola.User.REST;

import com.briscola4legenDs.briscola.Assets.PayloadBuilder;
import com.briscola4legenDs.briscola.User.Friends.*;
import com.briscola4legenDs.briscola.User.User;
import com.briscola4legenDs.briscola.User.WebSocket.UserSocketHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FriendRelationRepository friendRelationRepository;
    private final FriendRequestRepository friendRequestRepository;

    private final UserSocketHandler userSocketHandler;

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

    public void sendFriendRequest(FriendRequest friendRequest) {
        if (!userRepository.existsById(friendRequest.getRequesterId()))
            throw new FriendException("User with id: %s does not exist".formatted(friendRequest.getRequesterId()), FriendException.Type.USER_ID_NOT_FOUND);
        if (!userRepository.existsById(friendRequest.getFriendId()))
            throw new FriendException("User with id: %s does not exist".formatted(friendRequest.getFriendId()), FriendException.Type.USER_ID_NOT_FOUND);

        if (friendRequest.getRequesterId() == friendRequest.getFriendId())
            throw new FriendException("You cannot be friend of yourself", FriendException.Type.CANNOT_BE_FRIEND);

        List<FriendRequest> friends = friendRequestRepository.findByFriendId(friendRequest.getFriendId());
        if (friends.contains(friendRequest))
            throw new FriendException("Friend request with id: %s already exists".formatted(friendRequest.getFriendId()), FriendException.Type.FRIEND_ALREADY_EXISTS);

        friendRequestRepository.save(friendRequest);

        sendUpdateListMsg(friendRequest.getFriendId());
    }

    public void acceptFriendRequest(FriendRequest friendRequest) {
        if (!userRepository.existsById(friendRequest.getRequesterId()))
            throw new FriendException("User with id: %s does not exist".formatted(friendRequest.getRequesterId()), FriendException.Type.USER_ID_NOT_FOUND);
        if (!userRepository.existsById(friendRequest.getFriendId()))
            throw new FriendException("User with id: %s does not exist".formatted(friendRequest.getFriendId()), FriendException.Type.USER_ID_NOT_FOUND);

        List<FriendRequest> friends = friendRequestRepository.findByFriendId(friendRequest.getFriendId());
        if (!friends.contains(friendRequest))
            throw new FriendException("Insisting friend request", FriendException.Type.CANNOT_BE_FRIEND);

        friendRequest.setId(friends.get(friends.indexOf(friendRequest)).getId());

        addFriend(FriendRelation.builder()
                .userId(friendRequest.getRequesterId())
                .friendId(friendRequest.getFriendId())
                .build());

        friendRequestRepository.delete(friendRequest);

        sendUpdateListMsg(friendRequest.getRequesterId());
    }

    public void rejectFriendRequest(FriendRequest friendRequest) {
        if (!userRepository.existsById(friendRequest.getRequesterId()))
            throw new FriendException("User with id: %s does not exist".formatted(friendRequest.getRequesterId()), FriendException.Type.USER_ID_NOT_FOUND);
        if (!userRepository.existsById(friendRequest.getFriendId()))
            throw new FriendException("User with id: %s does not exist".formatted(friendRequest.getFriendId()), FriendException.Type.USER_ID_NOT_FOUND);

        List<FriendRequest> friends = friendRequestRepository.findByFriendId(friendRequest.getFriendId());
        if (!friends.contains(friendRequest))
            throw new FriendException("Insisting friend request", FriendException.Type.CANNOT_BE_FRIEND);

        friendRequest.setId(friends.get(friends.indexOf(friendRequest)).getId());

        friendRequestRepository.delete(friendRequest);

        sendUpdateListMsg(friendRequest.getRequesterId());
    }

    private void addFriend(FriendRelation friendRelation) {
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

        sendUpdateListMsg(friendRelation);
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


    public List<FriendRequest> getFriendRequests(long id) {
        return friendRequestRepository.findByFriendId(id);
    }

    private void sendUpdateListMsg(FriendRelation friendRelation) {
        try {
            userSocketHandler.multicastMessage(new Long[]{ friendRelation.getUserId(), friendRelation.getFriendId() }, PayloadBuilder.createJsonMessage(
                    UserSocketHandler.Code.UPDATE_FRIEND_LIST, null));
        } catch (IOException ignored) {}
    }

    private void sendUpdateListMsg(long id) {
        try {
            userSocketHandler.multicastMessage(new Long[]{ id }, PayloadBuilder.createJsonMessage(
                UserSocketHandler.Code.UPDATE_FRIEND_LIST, null));
        } catch (IOException ignored) {}
    }
}
