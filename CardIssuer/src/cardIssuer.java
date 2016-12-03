import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

public class cardIssuer {
	
	bankDBService bankDB;
	static final String HOST = "localhost";
	static final int PREPORT = 4444;
	static final int PORT = 5555;
	String[] tsp_packet = null;

	
	

	public cardIssuer(){
		bankDB = new bankDBService();
		bankDB.makeConnection();
	}
	
	public void cardIssuerSocket(){
		
		String result = null;
		String card = null;
		String card_expiry_date =null; 

		
		try{
			while(true){
				
				ServerSocket serverSocket = new ServerSocket(PORT);
				Socket socket = serverSocket.accept();
				InputStream in = socket.getInputStream();
				ObjectInputStream objectInput = new ObjectInputStream(in);
				tsp_packet = (String[]) objectInput.readObject();
				card = tsp_packet[2];
				card_expiry_date=tsp_packet[3];
				if(validateCard(card, card_expiry_date)){
					result = "Approved";
				}
				
				else
					result = "Declined";
				
				//serverSocket.close();
				serverSocket.close();
				
				sendResult(result);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void sendResult(String result){
		String[] packet = new String [8];
		if (tsp_packet[1].equals("ADD")){
			System.out.println("ISSUER received a packet for ADD Card validation");
			packet[0] = "FROM_ISSUER_ADD";
			packet[1] = "ISSUER_SIGNED";
			packet[2] = result;
			packet[3]=tsp_packet[2];
		}
		else{
			System.out.println("ISSUER received a packet for Payment validation");
			packet[0] = "FROM_ISSUER";
			packet[1]= "ISSUER_SIGNED";
			packet[2] = result;
			packet[3]=null;
		}
		
		try{
			
			Socket socket = new Socket(HOST, PREPORT);
			OutputStream output = socket.getOutputStream();
			ObjectOutputStream objectOutput = new ObjectOutputStream(output);
			objectOutput.writeObject(packet);
			System.out.println("Packet is sent back to TSP");
			objectOutput.close();
			output.close();
			//socket.close();	
			socket.close();
		}
		catch(Exception e){
		e.printStackTrace();
		}
	}
	
	
	public boolean validateCard(String card, String card_expiry_date){
		
		if (bankDB.validateCard(card, card_expiry_date) != false)
			return true;
		
		else
			return false;
		
	}
	
	public static void main(String[] args){
		cardIssuer issuer = new cardIssuer();
		issuer.cardIssuerSocket();
	}
}
