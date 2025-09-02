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

    BATCH_DRAW_OPERATION,

    TEXT_OPERATION,
    TEXT_HISTORY,

    UNDO_OPERATION,
    REDO_OPERATION,

    CLEAR_OPERATION,

    HEARTBEAT,
    ERROR,
    ACK
}