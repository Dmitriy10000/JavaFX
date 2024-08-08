package com.example.javafx;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;

import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class ComPortController {

    private static SerialPort comPort;
    private static PrintWriter output;
    private StringBuilder buffer = new StringBuilder();
    public static String portName = "";


    public void initializeSerialPort() {
        comPort = SerialPort.getCommPort(portName);
        comPort.setBaudRate(115200);
        comPort.setNumDataBits(8);
        comPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        comPort.setParity(SerialPort.NO_PARITY);
        if (comPort.openPort()) {
            System.out.println("Port is opened.");
            output = new PrintWriter(comPort.getOutputStream());

            comPort.addDataListener(new SerialPortDataListener() {
                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE)
                        return;

                    byte[] newData = new byte[comPort.bytesAvailable()];
                    int numRead = comPort.readBytes(newData, newData.length);
                    String data = new String(newData, 0, numRead, StandardCharsets.UTF_8);
                    buffer.append(data);
                    processBuffer();
                }
            });
        } else {
            System.out.println("Unable to open the port.");
        }
    }

    private void processBuffer() {
        int index;
        while ((index = buffer.indexOf("\n")) != -1) {
            String line = buffer.substring(0, index).trim();
            buffer.delete(0, index + 1);
            DataController.parseData(line);
        }
        System.out.println("dc: " + DataController.getString());
        DBController.insertData(DataController.getData());
    }


    public static void sendServoCommands(int servo1Angle, int servo2Angle) {
        String command1 = "SERVO1:" + servo1Angle;
        String command2 = "SERVO2:" + servo2Angle;

        if (output != null) {
            //output.println(command1);
            //output.println(command2);
            //output.flush();
            System.out.println("Sent command: " + command1);

            // Отправляем команду для второго сервопривода несколько раз
            for (int i = 0; i < 2; i++) {
                output.println(command1);
                output.println(command2);
                output.flush();
                System.out.println("Sent command: " + command1);
                System.out.println("Sent command: " + command2);
                try {
                    Thread.sleep(50); // Небольшая задержка между командами
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    // Функция посылает сигнал на ардуино включить пищалку
    public static void sendWarningCommand(){
        String command = "WARNING";
        System.out.println("Sent command: Warning ");
        //System.out.println(command);
        output.println(command);
        output.flush();
    }
    public static void closePort() {
        if (comPort != null && comPort.isOpen()) {
            comPort.closePort();
        }
    }
}
