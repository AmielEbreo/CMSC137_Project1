/*
Amiel O. Ebreo
2012-48245
CMSC 137 Project 1: UDP implemented to work as TCP


*/
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

class client{
	final private static int s_port = 5432;
	final private static int c_port = 5431;
	public static void main(String args[]) throws Exception{
		Random r = new Random();
		//DatagramSocket clientSocket = new DatagramSocket(c_port);
		System.out.println("Type 'disconnect' to terminate the connection with the server.");
		while(true){
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			DatagramSocket clientSocket = new DatagramSocket(c_port);
			InetAddress IP = InetAddress.getByName("127.0.0.1");
			byte[] client_msg = new byte[1024]; //message to server
			byte[] server_msg = new byte[1024]; //message from client
			byte[] data = new byte[1024]; //var to hold received daata
			byte[] sendSYN;
			byte[] sendACK;
			byte[] sendFIN;
			
			String[] temp_ACK;
			String SYN;
			String ACK;
			String FIN;
			
			DatagramPacket packet;

			System.out.println("Message: ");
			String message = in.readLine();
			if(!message.equals("disconnect")){
				
				client_msg = message.getBytes();
				//establish three-way handshake before sending packet
				//randomize SYN
				SYN = (r.nextInt(1000000)) + "";
				//send SYN to server
				sendSYN = SYN.getBytes("UTF-8");
				packet = new DatagramPacket(sendSYN, sendSYN.length, IP, s_port);
				clientSocket.send(packet); //send SYN bit
				System.out.println("SYN to Server: "+SYN+"\n"); 
				
				//receive SYN-ACK from Server
				packet = new DatagramPacket(data, data.length);
				try{
					clientSocket.receive(packet);	
				}catch(SocketTimeoutException e){
					System.out.println("Server not available.");
					break;					    	   
				}
				TimeUnit.SECONDS.sleep(2);
				System.out.println("SYN-ACK from Server: "+new String(packet.getData()).trim()+"\n");
				
				//send ACK to Server to establish connection
				temp_ACK = (new String(packet.getData()).trim()).split("-");
				ACK = (Integer.parseInt(temp_ACK[0])+1) + "";
				sendACK = ACK.getBytes("UTF-8");
				packet = new DatagramPacket(sendACK, sendACK.length, IP, s_port);
				clientSocket.send(packet); //send SYN bit
				System.out.println("ACK to Server: "+ACK+"\n"); 
				
				
				//send the message to the Server
				packet = new DatagramPacket(client_msg, client_msg.length, IP, s_port);
				clientSocket.send(packet);
				
				//receive back the echoed message from the Server
				packet = new DatagramPacket(server_msg, server_msg.length);
				clientSocket.receive(packet);
				message = new String(packet.getData());
				System.out.println("Your message was:" + message);
				clientSocket.close();
			}
			else{ //if client disconnected, four-way handshake
				//notify the server that client wants to disconnect
				FIN = "0";
				sendFIN = FIN.getBytes("UTF-8");
				packet = new DatagramPacket(sendFIN, sendFIN.length, IP, s_port);
				clientSocket.send(packet); //send FIN bit
				
				//send FIN segment
				FIN = (r.nextInt(1000000)) + "";
				sendFIN = FIN.getBytes("UTF-8");
				packet = new DatagramPacket(sendFIN, sendFIN.length, IP, s_port);
				clientSocket.send(packet); //send SYN bit
				System.out.println("FIN to Server: "+FIN+"\n"); 
				
				
				//send ACK
				ACK = (Integer.parseInt(FIN)+1 + "");
				sendACK = ACK.getBytes("UTF-8");
				packet = new DatagramPacket(sendACK, sendACK.length, IP, s_port);
				clientSocket.send(packet); //send SYN bit
				System.out.println("ACK to Server: "+ACK+"\n"); 
				
				//receive FIN segment
				packet = new DatagramPacket(data, data.length);						
				try{
					clientSocket.receive(packet);	
				}catch(SocketTimeoutException e){
					System.out.println("Did not receive ACK from Client");
					break;					    	   
				}
				TimeUnit.SECONDS.sleep(2);
				System.out.println("FIN from Server: "+new String(packet.getData()).trim()+"\n");
				
				//resend ACK to server
				packet = new DatagramPacket(sendACK, sendACK.length, IP, s_port);
				clientSocket.send(packet); //send SYN bit
				System.out.println("ACK to Server: "+ACK+"\n"); 
				
				System.out.println("Successfully disconnected from Server.");
				
				clientSocket.close();
				break;
				
			}
		}
   }
}