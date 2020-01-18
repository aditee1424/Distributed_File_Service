//Student Name: Aditee Dnyaneshwar Dakhane
//Student ID: 1001745502


import java.net.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.*;

public class FileClient {

	private Socket sock;                     //Declare a variable of type socket for establishing connection of client over socket
	private String clientName;               //Declare a variable name clientName for storing the name of clients obtained from GUI 


	public FileClient(String clientName) {      //Constructor to initialize the clientName variable by passing the value of argument obtained from GUI
		// TODO Auto-generated constructor stub
		this.clientName = clientName;

	}

	public void Start(String directoryPath,String cName) throws IOException, InterruptedException{

		/*Client connects to the socket. Pass the name of the client connected to the server. Then directory is watched 
		 * for any kind of file creation. As soon as a file is created in the directory, that file is uploaded to server
		 * using sendfile method*/

		sock = new Socket("localhost", 13267);// Connecting to socket on the localhost with given port number

		System.out.println("Client named:" +cName+ "- is connected");  //Print the name of the client connected
		DataOutputStream dosname = new DataOutputStream(sock.getOutputStream());  // Creates a new data output stream to write data
		dosname.writeUTF(cName);     //Writes the name of the client to be read by Server
		dosname.flush();             //Flush the outputstream

		//Site: https://www.baeldung.com/java-nio2-watchservice
		//Referred for line 45 to line 60
		WatchService watchService
		= FileSystems.getDefault().newWatchService();  //WatchService Instance is created using java.nio.file.FileSystems class:

		Path path = Paths.get(directoryPath);     //Path to the Client Directory to be monitored
		path.register(                            //Register path with WatchService
				watchService, 
				StandardWatchEventKinds.ENTRY_CREATE);  //Event is triggered whenever a new file is added in the client directory

		WatchKey key;
		while ((key = watchService.take()) != null) {     //blocks for timeout units to give more time within which an event may occur instead of returning null right away.
			try {
				for (WatchEvent<?> event : key.pollEvents()) {      // gives the next queued watch key any of whose events have occurred or null if no registered events have occurred
					System.out.println(
							"Event kind:" + event.kind() 
							+ ". File affected: " + event.context() + ".");  //Print the event occurred and the file affected
					String filepath1 = directoryPath + event.context();      //Get the file path
					//System.out.println("File path is: " + filepath1);
					File myFile = new File(filepath1);
					if(myFile.exists())                        //If file is added in the directory then only upload file to server
					{
						sendFile(filepath1,directoryPath);     //calls sendFile method

					}
					else{
						System.out.println("File already present on client side..");    //Print if file already exists in the directory of client

					}
					System.out.println("File uploaded to server successfully");   //Print when the file is uploaded to server


				}
			} catch (Exception e) {
				// TODO: handle exception
				System.out.println("Server Exception:"+e.getMessage());  //Catch exception when thrown
			}


			key.reset();       //watchkey instance is put back in the queue for more events
		}


	}


	public void sendFile(String filepath,String directoryPath) throws IOException,SocketException{
		/*Reads the file from client directory and send that file to the Server*/

		//Sites Referred for line 98 to line 119
		//https://coderanch.com/t/556838/java/Transferring-file-file-data-socket
		//https://gist.github.com/CarlEkerot/2693246
		//https://www.rgagnon.com/javadetails/java-0542.html
		try{

			File myFile = new File(filepath);  //Creates instance of file from given path of the file

			byte[] mybytearray = new byte[(int) myFile.length()];  //Take the length of file in the byte array


			FileInputStream fis = new FileInputStream(myFile);   //To Read data from file in the sequence of bytes
			BufferedInputStream bis = new BufferedInputStream(fis);   //To read data from FileInputStream in big chunks
			//bis.read(mybytearray, 0, mybytearray.length);

			DataInputStream dis = new DataInputStream(bis);     //To read data from buffer  
			dis.readFully(mybytearray, 0, mybytearray.length);  //Reads bytes from an inputstream and stores them into the byetarray. 
			//Number of bytes should be equal to mybytearray.length
			OutputStream os = sock.getOutputStream();           // To accept output bytes

			//Sending file name and file size to the server
			DataOutputStream dos = new DataOutputStream(os);    //Creates DataOuputStream to data

		//	dos.writeUTF(directoryPath);               //write the path of directory for the server to read
			dos.writeUTF(myFile.getName());            //sends the name of file for the server to read
			dos.writeLong(mybytearray.length);         //write the length of bytearray

			dos.write(mybytearray, 0, mybytearray.length);    //write all the bytes to bytearray for the server to read

		}catch (Exception e) {
			// TODO: handle exception
			System.out.println("Server exception: " + e.getMessage());     //Gives server exception when thrown
		}
	}


}
















