module com.hinote {
    requires javafx.controls;
    requires javafx.fxml;

    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.slf4j;
    requires org.java_websocket.client;
    requires org.java_websocket.handshake;

    opens com.hinote to javafx.fxml;
    opens com.hinote.shared.protocol;
    opens com.hinote.shared.utils;

    exports com.hinote;
}
