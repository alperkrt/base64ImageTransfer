package Base64ImageTransfer;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Scanner;

import com.fazecast.jSerialComm.*;

public class Base64Alici{
	static SerialPort chosenPort;
	static SerialPort recieverPort;
	
	public static void main(String[] args) {
		String base64DecodedString = ""; //To keep reading data
		recieverPort = SerialPort.getCommPort("COM5");//Receiver Arduino Port
		recieverPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
		recieverPort.openPort();//Open Port
		Scanner data = new Scanner(recieverPort.getInputStream());//Read the value in Port
		while(true){
			System.out.println("veri Alýnmaya Baþlýyor");
			String read = data.nextLine();//Read data on the port
			base64DecodedString = read;//Make base64DecodedString equal to data
			break;
		}
		System.out.println("Veri Alýndý!!");
		recieverPort.closePort();
		System.out.println(base64DecodedString);
		String deco = base64DecodedString.substring(1, base64DecodedString.length());//Since there is a error in the first index of data, get rid of that
		System.out.println(deco.length());//checking for data loses (compare with encoded data's length
		decoder(deco,"C:\\base64\\DecodedImage.jpg");//decode the data and create a jpg file 
		System.out.println("DecodedImage Oluþturuldu");
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
}

