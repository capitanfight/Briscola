package com.briscola4legenDs.briscola.User.Friends;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class FriendRequestUsername {
    private final int requesterId;
    private final String playerUsername;
}
