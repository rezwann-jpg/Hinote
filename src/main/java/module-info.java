module com.hinote {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.slf4j;
    requires org.java_websocket;

    opens com.hinote to javafx.fxml;
    opens com.hinote.client.ui to javafx.fxml;
    opens com.hinote.shared.protocol to com.fasterxml.jackson.databind;
    opens com.hinote.shared.utils to com.fasterxml.jackson.databind;

    exports com.hinote;
    exports com.hinote.server;
    exports com.hinote.client;
}
