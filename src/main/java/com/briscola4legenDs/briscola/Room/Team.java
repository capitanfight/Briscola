package com.briscola4legenDs.briscola.Room;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
@Getter
public class Team {
    private final long teamId;
    private final List<Long> playerIds;
}
