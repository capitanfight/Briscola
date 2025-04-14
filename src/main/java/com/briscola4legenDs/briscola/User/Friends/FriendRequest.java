package com.briscola4legenDs.briscola.User.Friends;

import jakarta.persistence.*;
import lombok.*;

import java.util.Objects;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table
@Getter
public class FriendRequest {
    @Id
    @SequenceGenerator(
            name = "friend_request_sequence",
            sequenceName = "friend_request_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "friend_request_sequence"
    )
    private long id;

    private long requesterId;
    private long friendId;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FriendRequest that)) return false;
        return getRequesterId() == that.getRequesterId() && getFriendId() == that.getFriendId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRequesterId(), getFriendId());
    }
}
