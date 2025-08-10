package com.hinote.shared.protocol;

public enum MessageType {
    JOIN_ROOM,
    LEAVE_ROOM,
    ROOM_CREATED,
    ROOM_JOINED,
    ROOM_LEFT,
    USER_JOINED,
    USER_LEFT,

    CHAT_MESSAGE,
    CHAT_HISTORY,

    DRAW_OPERATION,
    DRAW_HISTORY,

    TEXT_OPERATION,
    TEXT_HISTORY,

    HEARTBEAT,
    ERROR,
    ACK
}