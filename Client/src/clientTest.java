import java.awt.Toolkit;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import org.apache.commons.codec.binary.Base64;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
//import java.util.Base64;
import java.util.Scanner;
import java.security.MessageDigest;


public class clientTest {
	static final String CODE_SEPARATOR = "%";
	static final String HOST = "localhost";
	static final int PORT = 1111;
	static final int NEXTPORT = 2222;
	static final int TSPPORT = 4444;
	static final String DSC_SEPARATOR = "||";
	String[] final_packet = null;
	String Token_requestor_id = "VALIDREQUESTOR1";
	String Token;
	String Expiry_Date;
	String CliNonce;
	static final SecureRandom nonce = new SecureRandom();

	
	public String getClientNonce() {
		return new BigInteger(130, nonce).toString(32);
	}

	public void sendPacket(String[] packet, int port) {
	    try {
	    	Socket client_socket = new Socket(HOST, port);
	        OutputStream output = client_socket.getOutputStream();
	        ObjectOutputStream objectOutput = new ObjectOutputStream(output);
	        packet[0] = "FROM_CLIENT";
	        //String[] token_packet = {"USE","233530418", "FROM_CLIENT"};
	        objectOutput.writeObject(packet);
	        System.out.println("Your Request is sent. Please wait...");
	        //objectOutput.flush();
	        //String encode_packet = new String(Hex.encodeHex(output.toByteArray()));
	        objectOutput.close();
	        output.close();
	        client_socket.close();

	    } catch (Exception e) {
	        System.out.println(e.toString());
	    }
	}
	
	public void receivePacket(){
		try{
			ServerSocket serverSocket = new ServerSocket(PORT);
			Socket socket = serverSocket.accept();
			ObjectInputStream objectInput = null;
			InputStream in = socket.getInputStream();
			objectInput = new ObjectInputStream(in);
			final_packet = (String[]) objectInput.readObject();
			
			System.out.println("");
			System.out.println("Here is your receipt:");
			for (int i=0; i<2;i++){
				if (final_packet[i] != null){
					System.out.println(final_packet[i]);
				}
			}
			
			Token = final_packet[0];
			String TokenRequestor_ID = final_packet[2];
			Expiry_Date = final_packet[3];
			
			serverSocket.close();
			socket.close();	
			
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	 public String receiveNonce(){
	        try{
	        	ServerSocket serverSocket = new ServerSocket(PORT);
			Socket socket = serverSocket.accept();
			ObjectInputStream objectInput = null;
			InputStream in = socket.getInputStream();
			objectInput = new ObjectInputStream(in);
			final_packet = (String[]) objectInput.readObject();
			String nonce;
			
			serverSocket.close();
			socket.close();
			
			if (final_packet[0].equals("FROM_TSP") && final_packet[1].equals("NONCE_RESPONSE")){
				nonce = final_packet[2];
			}
			else {
				nonce = "";
			}
			return nonce;	
				            
	        }catch(Exception e){
	            e.printStackTrace();
	            return e.toString();
	        }
	    }

	public byte[] getDynamicSecurityCode(String nonceFromServer, String token) throws Exception{
		//DSC part, this is generated by hashing (TokenRequestorID + token
		//+ Token Expiry Date + client nonce + nonce)..
		String doHash;
		doHash = Token_requestor_id + DSC_SEPARATOR + token + DSC_SEPARATOR + Expiry_Date
				+ DSC_SEPARATOR + CliNonce + DSC_SEPARATOR + nonceFromServer;
		//System.out.println(doHash);
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		MessageDigest md1 = MessageDigest.getInstance("SHA-256");
		
		byte [] byteStr = doHash.getBytes();
		md.update(byteStr);
		md1.update(byteStr);
		byte [] mdBytes = md.digest();
		return mdBytes;
	}
    
	public static void main(String[] args){
		clientTest client = new clientTest();
		Scanner s = new Scanner(System.in);
		String[] packet = new String[8];
		String serverNonce = "";
		//String[] Nonce;
		System.out.println("What do you want to do:"
				+ "1. USE (ID) "
				+ "2. ADD ");
		String option = s.nextLine();
		s.close();
		
		if (option.equals("USE")){
			client.CliNonce = client.getClientNonce();
			//Request a nonce from Server
			packet[1] = "REQUEST_NONCE";
			packet[2] = client.CliNonce;
			client.sendPacket(packet,TSPPORT);
			//Receive Nonce
			serverNonce = client.receiveNonce();
			//System.out.println(serverNonce);
			
			//Send packet for payment
			//Token Requestor ID, Token, Token Expiry Date, Client Nonce, Dynamic Security Code 
			packet[1] = "USE";
			packet[2] = "105613178383458";
			packet[3] = client.Token_requestor_id;
			packet[4] = client.Token;
			packet[5] = client.Expiry_Date;
			packet[6] = serverNonce;
			//Check how to generate DSC
			try{
				byte [] DSC = client.getDynamicSecurityCode(serverNonce, client.Token);
				String s1 = Base64.encodeBase64String(DSC);
				packet[7] = s1;
			}
			catch (Exception e){
				System.out.println(e.getMessage());
			}
			
			client.sendPacket(packet,NEXTPORT);
		}
		
		else if(option.equals("ADD")){
			packet[1] = "ADD";
			packet[2] = "1234567890123458";
			packet[3] = "0712"; //MMYY
			client.sendPacket(packet,TSPPORT);
		}
		client.receivePacket();
	}

}

