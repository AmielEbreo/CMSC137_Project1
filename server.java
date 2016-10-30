/*
Amiel O. Ebreo
2012-48245
CMSC 137 Project 1: UDP implemented to work as TCP


*/
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

class server{
	final private static int s_port = 5432;
	final private static int c_port = 5431;
	
	public static void main(String args[]) throws Exception{
		Random r = new Random();
		DatagramSocket serverSocket = new DatagramSocket(s_port);
		while(true){
			InetAddress IP = InetAddress.getByName("127.0.0.1");
			byte[] client_msg = new byte[1024]; //message from client
			byte[] server_msg = new byte[1024]; //message from server
			byte[] data = new byte[1024];
			byte[] sendSynAck;
			
			String ACK;
			String SYN;
			String message;
			
			DatagramPacket packet;
			
			System.out.println("Waiting for client...");
			
			//establish three-way handshake before receiving packet
			//receive SYN from Client
			packet = new DatagramPacket(data, data.length);						
			try{
				serverSocket.receive(packet);	
			}catch(SocketTimeoutException e){
				System.out.println("Did not receive SYN from Client");
				break;					    	   
			}
			TimeUnit.SECONDS.sleep(2);
			
			if(Integer.parseInt(new String(packet.getData()).trim()) != 0){
				System.out.println("SYN from Client: "+new String(packet.getData()).trim()+"\n");
				
				//SEND SYN-ACK to Client
				ACK = (Integer.parseInt(new String(packet.getData()).trim())+1)+""; //ACK BIT
				SYN = (r.nextInt(1000000))+""; //SYN BIT
				sendSynAck = (SYN+"-"+ACK).getBytes("UTF-8");
				packet = new DatagramPacket(sendSynAck, sendSynAck.length, IP, c_port); 
				serverSocket.send(packet);
				System.out.println("SYN-ACK to Client: "+SYN+"-"+ACK+"\n");
				
				//receive ACK from Client
				packet = new DatagramPacket(data, data.length);						
				try{
					serverSocket.receive(packet);	
				}catch(SocketTimeoutException e){
					System.out.println("Did not receive ACK from Client");
					break;					    	   
				}
				TimeUnit.SECONDS.sleep(2);
				System.out.println("ACK from Client: "+new String(packet.getData()).trim()+"\n");
				
				//receive the message from the Client and echo back
				System.out.println("Connection Established!");
				packet = new DatagramPacket(client_msg, client_msg.length);
				serverSocket.receive(packet);
				
				message = new String(packet.getData());
				System.out.println("Client said: " + message);
				server_msg = message.getBytes();
				packet = new DatagramPacket(server_msg, server_msg.length, IP, c_port);
				serverSocket.send(packet);
			}
			else{
				System.out.println("Client is disconnecting...");
				
				//receive FIN segment
				packet = new DatagramPacket(data, data.length);						
				try{
					serverSocket.receive(packet);
					message = new String(packet.getData());
					server_msg = message.getBytes();					
				}catch(SocketTimeoutException e){
					System.out.println("Did not receive ACK from Client");
					break;					    	   
				}
				
				TimeUnit.SECONDS.sleep(2);
				System.out.println("FIN from Client: "+new String(packet.getData()).trim()+"\n");
				
				//receive ACK segment
				packet = new DatagramPacket(data, data.length);						
				try{
					serverSocket.receive(packet);	
				}catch(SocketTimeoutException e){
					System.out.println("Did not receive ACK from Client");
					break;					    	   
				}
				TimeUnit.SECONDS.sleep(2);
				System.out.println("ACK from Client: "+new String(packet.getData()).trim()+"\n");
				
				//resend FIN segment
				packet = new DatagramPacket(server_msg, server_msg.length, IP, c_port);
				serverSocket.send(packet);
				
				//receive ACK again
				packet = new DatagramPacket(data, data.length);						
				try{
					serverSocket.receive(packet);	
				}catch(SocketTimeoutException e){
					System.out.println("Did not receive ACK from Client");
					break;					    	   
				}
				TimeUnit.SECONDS.sleep(2);
				System.out.println("ACK from Client: "+new String(packet.getData()).trim()+"\n");
				
				System.out.println("Client successfully disconnected.");
				
			}
		}
	}
}