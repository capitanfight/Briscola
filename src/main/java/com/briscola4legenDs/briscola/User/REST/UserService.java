package com.briscola4legenDs.briscola.User.REST;

import com.briscola4legenDs.briscola.Assets.PayloadBuilder;
import com.briscola4legenDs.briscola.Assets.WebSocket.IncludeMulticastMessageStrategy;
import com.briscola4legenDs.briscola.User.Friends.*;
import com.briscola4legenDs.briscola.User.Stats.Stats;
import com.briscola4legenDs.briscola.User.Stats.StatsRepository;
import com.briscola4legenDs.briscola.User.User;
import com.briscola4legenDs.briscola.User.UserDTO;
import com.briscola4legenDs.briscola.User.WebSocket.UserSocketHandler;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FriendRelationRepository friendRelationRepository;
    private final FriendRequestRepository friendRequestRepository;

    private final UserSocketHandler userSocketHandler;
    private final StatsRepository statsRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public List<User> getUsers(List<Long> userIds) {
        return userRepository.findAllById(userIds);
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
                oldUser.setEmail(user.getEmail());
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

    public void sendFriendRequest(FriendRequestUsername friendRequest) {
        sendFriendRequest(FriendRequest.builder()
                .requesterId(friendRequest.getRequesterId())
                .friendId(getUserIdByUsername(friendRequest.getPlayerUsername()))
                .build());
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

        sendUpdateFriendRequestsMsg(friendRequest.getFriendId());
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

        sendUpdateFriendRequestsMsg(friendRequest.getFriendId());
        sendUpdateListMsg(new Long[]{ friendRequest.getRequesterId(), friendRequest.getFriendId()});
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

        sendUpdateFriendRequestsMsg(friendRequest.getFriendId());
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

        FriendRelation oppositeRelation = friendRelationRepository.findByUserIdAndFriendId(friendRelation.getFriendId(), friendRelation.getUserId());

        friendRelationRepository.delete(friendRelation);
        friendRelationRepository.delete(oppositeRelation);

        sendUpdateListMsg(friendRelation);
    }

    public List<UserDTO> getFriends(long userId) {
        if (!userRepository.existsById(userId))
            throw new IllegalArgumentException("User with id: %s does not exist".formatted(userId));

        List<User> users = userRepository
                .findAllById(friendRelationRepository
                        .findByUserId(userId)
                        .stream()
                        .map(FriendRelation::getFriendId)
                        .collect(Collectors.toList()));

        List<UserDTO> userDTOs = new ArrayList<>();
        for (User user : users)
            userDTOs.add(UserDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .imageUrl(user.getImageUrl())
                    .build());

        return userDTOs;
    }

    public List<FriendRelation> getFriendRelations(long userId) {
        if (!userRepository.existsById(userId))
            throw new IllegalArgumentException("User with id: %s does not exist".formatted(userId));

        return friendRelationRepository.findByUserId(userId);
    }

    public long getUserIdByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User with username: %s not found".formatted(username)));

        return user.getId();
    }

    public List<UserDTO> getFriendRequests(long id) {
        List<User> users = userRepository
                .findAllById(friendRequestRepository
                        .findByFriendId(id)
                        .stream()
                        .map(FriendRequest::getRequesterId)
                        .collect(Collectors.toList()));

        List<UserDTO> userDTOs = new ArrayList<>();
        for (User user : users)
            userDTOs.add(UserDTO.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .imageUrl(user.getImageUrl())
                    .build());

        return userDTOs;
    }

    public List<FriendRequest> getFriendRequestsRelations(long id) {
        return friendRequestRepository.findByFriendId(id);
    }

    private void sendUpdateListMsg(FriendRelation friendRelation) {
        try {
            userSocketHandler.multicastMessage(
                    new Long[]{ friendRelation.getUserId(), friendRelation.getFriendId() },
                    PayloadBuilder.createJsonMessage(
                        UserSocketHandler.Code.UPDATE_FRIEND_LIST, null),
                    new IncludeMulticastMessageStrategy());
        } catch (IOException ignored) {}
    }

    private void sendUpdateListMsg(long id) {
        try {
            userSocketHandler.unicastMessage(
                    id,
                    PayloadBuilder.createJsonMessage(
                        UserSocketHandler.Code.UPDATE_FRIEND_LIST, null));
        } catch (IOException ignored) {}
    }

    private void sendUpdateListMsg(Long[] ids) {
        try {
            userSocketHandler.multicastMessage(
                    ids,
                    PayloadBuilder.createJsonMessage(
                        UserSocketHandler.Code.UPDATE_FRIEND_LIST, null),
                    new IncludeMulticastMessageStrategy());
        } catch (IOException ignored) {}
    }

    private void sendUpdateFriendRequestsMsg(long id) {
        try {
            userSocketHandler.unicastMessage(id,
                    PayloadBuilder.createJsonMessage(
                        UserSocketHandler.Code.UPDATE_FRIEND_REQUESTS, null));
        } catch (IOException ignored) {}
    }

    public Stats getStats(long id) {
        Optional<Stats> optionalStats = statsRepository.findById(id);

        if (optionalStats.isEmpty()) {
            Stats stats = Stats.builder()
                    .id(id)
                    .loss(0)
                    .win(0)
                    .matches(0)
                    .build();

            statsRepository.save(stats);
            return stats;
        }

        return optionalStats.get();
    }

    @Transactional
    public boolean updateStats(Stats stats) {
        boolean updated = false;

        if (statsRepository.findById(stats.getId()).isEmpty())
            throw new IllegalArgumentException("User with id: " + stats.getId() + " not found");

        Stats oldStats = statsRepository.findById(stats.getId()).get();
        if (!(oldStats.getMatches() == stats.getMatches()) &&
                stats.getMatches() > 0) {
            oldStats.setMatches(stats.getMatches());
            updated = true;
        }

        if (!(oldStats.getWin() == stats.getWin()) &&
                stats.getWin() > 0) {
            oldStats.setWin(stats.getWin());
            updated = true;
        }

        if (!(oldStats.getLoss() == stats.getLoss()) &&
                stats.getLoss() > 0) {
            oldStats.setLoss(stats.getLoss());
            updated = true;
        }

        if (!(oldStats.getMaxPoints() == stats.getMaxPoints()) &&
                stats.getMaxPoints() > 0) {
            oldStats.setMaxPoints(stats.getMaxPoints());
            updated = true;
        }

        if (!(oldStats.getTotalPoints() == stats.getTotalPoints()) &&
                stats.getTotalPoints() > 0) {
            oldStats.setTotalPoints(stats.getTotalPoints());
            updated = true;
        }

        return updated;
    }

    public List<Stats> getAllStats() {
        return statsRepository.findAllByOrderByWinDesc();
    }
}
