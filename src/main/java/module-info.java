module com.example.javafx {
    requires javafx.controls;
    requires javafx.fxml;
    requires jssc;
    requires org.xerial.sqlitejdbc;
    requires spring.security.crypto;
    requires com.fazecast.jSerialComm;


    opens com.example.javafx to javafx.fxml;
    exports com.example.javafx;
}