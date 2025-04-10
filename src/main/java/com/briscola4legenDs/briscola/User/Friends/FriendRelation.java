package com.briscola4legenDs.briscola.User.Friends;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Friends")
public class FriendRelation {
    @Id
    @SequenceGenerator(
            name = "friend_sequence",
            sequenceName = "friend_sequence",
            allocationSize = 1
    )
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "friend_sequence"
    )
    @JsonIgnore
    private Long id;

    private long userId;
    private long friendId;

    public long getUserId() {
        return userId;
    }

    public long getFriendId() {
        return friendId;
    }

    public void setRelationId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FriendRelation that)) return false;
        return getUserId() == that.getUserId() && getFriendId() == that.getFriendId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getFriendId());
    }
}
