package com.danix43.javaSerialLogger;

import gnu.io.*;
import java.io.*;
import java.util.*;

public class Logger implements SerialPortEventListener {

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

	// the timeout value for connecting with the port
	final static int TIMEOUT = 2000;
	private byte[] readBuffer = new byte[1000];
	private static final int BAUD_RATE = 9600;

	// for the data getted from arduino
	private String hum;
	private String temp;
	private String machine;

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
			
			
			System.out.println(selectedPort + " opened succesfully");
		} catch (PortInUseException e) {
			System.out.println(selectedPort + " is in use already");
		} catch (Exception e) {
			System.out.println(selectedPort + " failed to open");
		}

	}

	public boolean initIoStream() {
		boolean successful = false;
	
		try {
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();
	
			successful = true;
			return successful;
		} catch (IOException e) {
			System.out.println("Communications failed to start");
			return successful;
		}
	}

	public void initListener() {
		try {
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		} catch (TooManyListenersException e) {
			System.out.println("Too many listeners");
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
		if (evt.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				readSerial();
				
			} catch (Exception e) {
				System.out.println("Failed to read data");
			}
		}
	}
	
	private void readSerial() {
		try {
			int aBytes = input.available();
			if (aBytes > 0) {
				input.read(readBuffer, 0, aBytes);
				temp = new String(readBuffer);
				System.out.println(new String(readBuffer,0, aBytes));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	
	}

	public String getTemp() {
		return temp;
	}

}