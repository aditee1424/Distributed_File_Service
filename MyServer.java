//Student Name: Aditee Dnyaneshwar Dakhane
//Student ID: 1001745502

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class MyServer {


	int i=1;              //Declaring variable i to keep count of each incoming client request
	String cName;         // Declaration of varible to store client Name.
	List<Thread> threads = new ArrayList<>();// Declaring List to manage list of threads for every client getting connected to server
	ArrayList<String> clientnames = new ArrayList<>();// Declaration of ArrayList to store the names of clients.



	public void start() throws InterruptedException {
		// TODO Auto-generated method stub
		/*In This method, Server starts running on a port number.Server accepts connection with all the three clients.
		 *Server gets username of all the clients and check whether the username of any client matches with any 
		 *other client. Socket connection is closed when username matches. Else threads are started for each client.
		 *Runnable method is implemented to receive file from clients in the Server Directory*/

        //Site :: https://coderanch.com/t/556838/java/Transferring-file-file-data-socket-- site referred for line 55
		try (ServerSocket serverSocket = new ServerSocket(13267)) //Server starts running on Port number 13267
		{
			System.out.println("Server is listening on port ");
			while (true) {
				Socket clientSocket = serverSocket.accept(); //Server accepts connection with client
				//Site referred for line 59--https://coderanch.com/t/556838/java/Transferring-file-file-data-socket

				DataInputStream disname = new DataInputStream(clientSocket.getInputStream()); // Instance of DataInputStream is created to read data from client's inputstream
				cName = disname.readUTF(); //reads the data written by client in the outputstream and stores the data in variable cName 

				if(i==1) //If condition for first client
				{

					clientnames.add(cName);                                  //Add the name of client to arraylist of clientnames
					String clientname = cName;                               // Store the name of client read from outputstream in a temporary variable called clientname
					System.out.println("Clientname :"+cName+" :is connected");  //Print the name of client on server side
					System.out.println("Client has unique name..go ahead");              // It is not duplicate as it is the first thread
					cName = null;                                            //empty the cName variable to store the name of next Client.
					Thread t = new ServerThread(clientSocket,clientname);    // Creates a thread for client having unique name
					threads.add(t);                                         //Add that thread to the list of threads
					t.start();                                            //Start the thread method implementing runnable


				}
				if(i>1 && cName!= null)               //If it is not first client then check for duplicate username
				{
					if(clientnames.contains(cName))     //If arraylist of clientnames contains the current clientname
					{                                   //Site: https://stackoverflow.com/questions/8936141/java-how-to-compare-strings-with-string-arrays
						System.out.println("ClientName already present--Submit name again");

						DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream());  //Creates instance of dataoutputstream to write the duplicate name of client
						dos.writeUTF(cName);       //writes the name of client which then be read by client side by datainputstream
						cName = null;              //empty the cName variable to store the name of next client.
						clientSocket.close();     //Username is duplicate so close the connection of socket for that client.
                         
					}else{
						clientnames.add(cName);                              //Add the name of client to arraylist of clientnames
						String client_name = cName;                             //Store the name of client read from outputstream in a temporary variable called clientname
						System.out.println("Clientname :"+cName+" :is connected"); //Print the name of client connected with unique name
						System.out.println("Client has unique name ....go ahead");
						cName = null;                                           //empty the cName variable to store the name of next client.
						Thread t = new ServerThread(clientSocket,client_name);  // Creates a thread for client having unique name
						threads.add(t);                                        //Add that thread to the list of threads
						t.start();                                             //Start the thread method implementing runnable
					}
					
				}
				 i++;         //Increment counter i for each coming client request.
	
			}

		} catch (IOException ex) {
		System.out.println("Server exception: " + ex.getMessage());   //Catches an exception if one of the client disconnects
		ex.printStackTrace();
		}
	}

}
class ServerThread extends Thread {
	private Socket clientSocket;      //Declare variable of type Socket.
	int bytesRead;                    //Declare variable to read bytes
	private String clientName;        //Declare a string to store Client name


	//Constructor ServerThread having parameters Socket and String
	public ServerThread(Socket clientSocket,String clientName) throws IOException {    
		this.clientSocket = clientSocket;                                              //Initialize clientSocket variable declared by passing the values of arguments
		this.clientName = clientName;                                                 //Initialize clientName variable declared by passing the values of arguments
	}

	public void run() {   //Run method that contains the code executed by each thread
		/*A file received from the clientside will be read. All the content of the file along with file is uploaded to server
		 * The file is saved in the server directory with the same filename as the client side*/
		try {
			while(true){      //while the process is continued 
                         
				//Sites referred for the following code from line 136-line 156
				//https://coderanch.com/t/556838/java/Transferring-file-file-data-socket
				//https://gist.github.com/CarlEkerot/2693246
				//https://www.rgagnon.com/javadetails/java-0542.html
				
				InputStream in = clientSocket.getInputStream();                          // read data from socket  
				DataInputStream clientData = new DataInputStream(in);    
				//String clientPath = clientData.readUTF();      					         //Reads path of the client directory from which file is uploaded
				
				String fileName = clientData.readUTF();                                  //Reads the name of the file obtained from client
				String filePath = "/Users/Ankit/Documents/ServerDirectory/" +fileName;  

				File myFile = new File("/Users/Ankit/Documents/ServerDirectory/" +fileName);

				if(!myFile.exists()){          																		  //proceed only if file does not exists on server directory 
					OutputStream output = new FileOutputStream("/Users/Ankit/Documents/ServerDirectory/" +fileName);  //Sets file for the server to write data

					long size = clientData.readLong();    						 //read size of file
					byte[] buffer = new byte[13826];      				          //set a buffer size
					while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1)  //while size of the file greater than 0 and buffer length is not -1 
					{   
						output.write(buffer, 0, bytesRead); 			        //read the bytes and write to the output
						size -= bytesRead;                                      //decrement size after reading bytes
					}

					output.close();                                                                                //close outputstream
					System.out.println("A new file "+fileName+" is uploaded to server by client: -"+clientName);

				}    				

			}	



		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("Server exception: " + e.getMessage());      //catches and exception if thrown
			e.printStackTrace();
		}

	}



}
