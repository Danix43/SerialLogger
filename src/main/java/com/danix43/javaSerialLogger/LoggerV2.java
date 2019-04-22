package com.danix43.javaSerialLogger;

import gnu.io.*;
import java.io.*;
import java.util.*;

public class LoggerV2 implements SerialPortEventListener {

	// for containing the ports that will be found
	private Enumeration<?> ports = null;
	// map the port names to CommPortIdentifiers
	private HashMap<String, CommPortIdentifier> portMap = new HashMap<String, CommPortIdentifier>();

	// this is the object that contains the opened port
	private CommPortIdentifier selectedPortIdentifier = null;
	private SerialPort serialPort = null;

	// input and output streams for sending and receiving data
	private InputStream input = null;
	private OutputStream output = null;
	private Scanner procesor = null;

	// the timeout value for connecting with the port
	final static int TIMEOUT = 2000;
	private static final int BAUD_RATE = 9600;
	
	// for the data getted from arduino
	private String result;
	private String temperature;
	private String humidity;

	final static int NEW_LINE_ASCII = 10;

	public void searchForPorts() {
		ports = CommPortIdentifier.getPortIdentifiers();

		while (ports.hasMoreElements()) {
			CommPortIdentifier curPort = (CommPortIdentifier) ports.nextElement();

			if (curPort.getPortType() == CommPortIdentifier.PORT_SERIAL) {
				System.out.println("Serial ports : " + curPort.getName());
				portMap.put(curPort.getName(), curPort);
			}
		}
	}

	public void connect() {
		String selectedPort = "COM3";
		selectedPortIdentifier = (CommPortIdentifier) portMap.get(selectedPort);

		CommPort commPort = null;

		try {
			commPort = selectedPortIdentifier.open("Arduino Uno", TIMEOUT);
			serialPort = (SerialPort) commPort;
			serialPort.setSerialPortParams(BAUD_RATE,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);
			
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();
			
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			
			System.out.println(selectedPort + " opened succesfully");
			Thread.sleep(10000);
		} catch (PortInUseException e) {
			System.out.println(selectedPort + " is in use already");
		} catch (Exception e) {
			System.out.println(selectedPort + " failed to open");
		}

	}

	public void disconnect() {
		try {
			serialPort.removeEventListener();
			serialPort.close();
			input.close();
			output.close();
			System.out.println("Port succesfully closed");
		} catch (Exception e) {
			System.out.println("Fail to close");
		}

	}

	public void serialEvent(SerialPortEvent evt) {
		switch (evt.getEventType()) {
        	case SerialPortEvent.DATA_AVAILABLE:
        		readSerial();
        		break;
		}
	}
	
	private void readSerial() {
		Scanner scanner = new Scanner(input);
		procesor = scanner.useDelimiter("\\A");
		String result = procesor.hasNext() ? procesor.next() : "";
		System.out.println(result);
		scanner.close();
		procesor.close();
	}
	
	public String getTemperature() {
		return temperature;
	}

	public String getHumidity() {
		return humidity;
	}

	public static void main(String args[]) {
		LoggerV2 system = new LoggerV2();
		system.searchForPorts();
		system.connect();
	}

}
