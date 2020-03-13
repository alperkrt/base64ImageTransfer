package Base64ImageTransfer;
import com.fazecast.jSerialComm.*;
import java.util.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.FileWriter;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;

 
public class Base64Verici {
	static SerialPort chosenPort;
	static int packageNum = 5000;
	static int value;
  public static void main(String[] args) {
	  
	  // create and configure the window
	  JFrame windowT = new JFrame();
	  windowT.setTitle("Arduino Transmitter");
	  windowT.setSize(400, 75);
	  windowT.setLayout(new BorderLayout());
	  windowT.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	  
	  // create a drop-down box and connect button, then place them at the top of the window
	  JComboBox<String> portList = new JComboBox<String>();
	  JButton connectButton = new JButton("Connect");
	  JPanel topPanel = new JPanel();
	  topPanel.add(portList);
	  topPanel.add(connectButton);
	  windowT.add(topPanel, BorderLayout.NORTH);
	  
	  // populate the drop-down box
	  SerialPort[] portNames = SerialPort.getCommPorts();
	  for(int i = 0; i < portNames.length; i++) {
		  portList.addItem(portNames[i].getSystemPortName());
	  }
	 
	  //Choose the image
	  Scanner input = new Scanner(System.in);
	  String name = input.nextLine();
	  String imagePath = "C:\\base64\\"+name+".jpg"; // choose the image path
	  
	  //Encoding part
	  System.out.println("=================Encoder Image to Base 64!=================");
	  String base64ImageString = encoder(imagePath); //Image was encoded and stored as String
	  
	  System.out.println(base64ImageString.length()); // Number of chars (optional)
	  System.out.println("Base64ImageString = " + base64ImageString); // In order to see the String (optional)
	  
	  //CRC32 Calculation of base64ImageString
	  CrcCalculator calculator = new CrcCalculator(Crc32.Crc32);
	  byte[] base64ImageStringByte = base64ImageString.getBytes();
  	  long crc = calculator.Calc(base64ImageStringByte, 0, base64ImageStringByte.length);
	  
	
	  
	  // configure the connect button and use another thread to send data
		connectButton.addActionListener(new ActionListener(){
			@Override public void actionPerformed(ActionEvent arg0) {
				if(connectButton.getText().equals("Connect")) {
					
					// attempt to connect to the serial port
					chosenPort = SerialPort.getCommPort(portList.getSelectedItem().toString());
					chosenPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
					if(chosenPort.openPort()) {
						connectButton.setText("Disconnect");
						portList.setEnabled(false);
						// create a new thread for sending data to the arduino
						Thread thread = new Thread(){
							@Override public void run() {
									//Create writer
									PrintWriter output = new PrintWriter(chosenPort.getOutputStream());
									// enter an infinite loop that sends text to the arduino
									/*
									while(true) {	// data will be splitted to 1000 thousands of chars and will be sent
										System.out.println("Data Package has started");
										output.println(base64ImageString);
										output.flush();
										try {Thread.sleep(100); } catch(Exception e) {}
										System.out.println("Mesaj Ýletildi");
										break;
									
								}
									*/
								
								boolean check = true;
								int i = 0;
								while(i<base64ImageString.length()) {
									if(check) {
										if(i+packageNum > base64ImageString.length()) {
											check = false;
											String sentData = base64ImageString.substring(i, base64ImageString.length());
											byte[] sentDataByte = sentData.getBytes();
											long CRC32 = calculator.Calc(sentDataByte, 0, sentDataByte.length);
											String sentDataCRC32 = sentData + CRC32;
											output.println(sentDataCRC32);
											output.flush();
										}else {
											check = false;
											String sentData = base64ImageString.substring(i, i+packageNum);
											byte[] sentDataByte = sentData.getBytes();
											long CRC32 = calculator.Calc(sentDataByte, 0, sentDataByte.length);
											String sentDataCRC32 = sentData + CRC32;
											output.println(sentDataCRC32);
											output.flush();
											i += packageNum;
										}
									}			
									
								}
						}
						};	
						thread.start();		
					}
					} else {
					// disconnect from the serial port
					chosenPort.closePort();
					portList.setEnabled(true);
					connectButton.setText("Connect");
				}
			}
		});
		
		
	  
	  
	  //make a Txt file of base64ImageString (Optional)
	  writeUsingFileWriter(base64ImageString, name);
	  
	 
	  // show the window
	  input.close();
	  windowT.setVisible(true);
  }
 
  public static String encoder(String imagePath) {
    String base64Image = "";
    File file = new File(imagePath);
    try (FileInputStream imageInFile = new FileInputStream(file)) {
      // Reading a Image file from file system
      byte imageData[] = new byte[(int) file.length()];
      imageInFile.read(imageData);
      base64Image = Base64.getEncoder().encodeToString(imageData);
      
    } catch (FileNotFoundException e) {
      System.out.println("Image not found" + e);
    } catch (IOException ioe) {
      System.out.println("Exception while reading the Image " + ioe);
    }
    return base64Image;
  }
  public static void decoder(String base64Image, String pathFile) {
	    try (FileOutputStream imageOutFile = new FileOutputStream(pathFile)) {
	      // Converting a Base64 String into Image byte array
	      byte[] imageByteArray = Base64.getDecoder().decode(base64Image);
	      imageOutFile.write(imageByteArray);
	    } catch (FileNotFoundException e) {
	      System.out.println("Image not found" + e);
	    } catch (IOException ioe) {
	      System.out.println("Exception while reading the Image " + ioe);
	    }
	  }
  
  private static void writeUsingFileWriter(String data, String name) {
      File file = new File("C:\\base64\\"+name+".txt");
      FileWriter fr = null;
      try {
          fr = new FileWriter(file);
          fr.write(data);
      } catch (IOException e) {
          e.printStackTrace();
      }finally{
          //close resources
          try {
              fr.close();
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
  }  
}

