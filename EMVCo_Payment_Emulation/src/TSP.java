import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.util.Random;
import java.security.SecureRandom;
import java.math.BigInteger;
import java.util.Date;
import java.util.Random;
import org.apache.commons.codec.binary.Base64;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import org.hibernate.annotations.ForeignKey;
import com.chilkatsoft.*;
import java.text.SimpleDateFormat;
import java.util.UUID;
import java.text.ParseException;
import java.util.*;
public class TSP {
    static final String HOST = "localhost";
    static final int PREPORT = 3333;
    static final int PORT = 4444;
    static final int NEXTPORT = 5555;
    static final int CLIENTPORT = 1111;
    static final int MERCHANT = 2222;
    String pending_card;
    String expiry_date;
    static final SecureRandom random = new SecureRandom();
    
    TSPDBService tspDB;
    
    public TSP() {
        tspDB = new TSPDBService();
        tspDB.makeConnection();
        pending_card = null;
    }
    
    public String nextSessionId() {
        return new BigInteger(130, random).toString(32);
    }
    
    
    
    public void handleRequest(String[] packet) {
        String origin = packet[0];
        String type = packet[1];
        String data = packet[2];
        String date = packet[3];
        
        
        String token = null;
        
        if (type.equals("ADD") && origin.equals("FROM_CLIENT")) {
            try {
                // TODO Validate card with issuer first
                pending_card = data;
                expiry_date = date;
                sendPacket(packet, NEXTPORT);
                System.out.println("Packet is sent sent to ISSUER for validation");
                
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        
        else if (type.equals("ISSUER_SIGNED") && origin.equals("FROM_ISSUER_ADD")) {
            if (packet[2].equals("Approved")) {
                try {
                    Calendar cal = Calendar.getInstance();
                    cal.add(Calendar.MONTH, 12);
                    Date date1 = cal.getTime();
                    SimpleDateFormat format1 = new SimpleDateFormat("MMYY");
                    String inActiveDate = null;
                    inActiveDate = format1.format(date1);
                    System.out.println(inActiveDate );
                    token = tokenGeneration(data);
                    //						System.out.println(token);
                    
                    
                    //should add expiry date in the DB too.
                    
                    packet[0] = "FROM_TSP";
                    packet[2] = token;
                    
                    //Check how to generate this
                    String token_requestor_id=getClientTokenRequestorID1();
                    packet[3] = token_requestor_id;
                    packet[4] = expiry_date;
                    tspDB.insertToken(pending_card, token, expiry_date, inActiveDate, token_requestor_id);
                    sendPacket(packet, CLIENTPORT);
                    System.out.println("Packet is sent back to client with ADD Card Confirmation");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                packet[0] = "FROM_TSP";
            sendPacket(packet, CLIENTPORT);
        }
        
        else if (type.equals("USE")) {
            String requestor_id = packet[3];
            if (validateRequestor(requestor_id) == true) {
                packet[2] = getCard(data);
                sendPacket(packet, NEXTPORT);
                
                System.out.println("Packet is sent to ISSUER for validation");
            } else
                System.out.print("data not found");
        }
        
        else if (type.equals("REQUEST_NONCE")) {
            String nonce = nextSessionId();
            packet[0] = "FROM_TSP";
            packet[1] = "NONCE_RESPONSE";
            packet[2] = nonce;
            sendPacket(packet, CLIENTPORT);
            System.out.println("Packet is sent back to client with a randomly generated Nonce");
        }
        
    }
    //the requestor id
    private String getClientTokenRequestorID1() {
        UUID idOne = UUID.randomUUID();
        
        return idOne.toString();
    }
    private String tokenGeneration(String card) throws Exception {
	       
        String token = "" ;
        
        try {
            String md5 = getBinaryToken(card);
            
            
        } catch (Exception e){
            e.printStackTrace();
        }
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(card.getBytes());
        return Integer.toString(md.hashCode());
    }
    private static String getBinaryToken(String card) throws UnsupportedEncodingException, NoSuchAlgorithmException{
        
        String TokenData = card;
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] messageDigest = md.digest(TokenData.getBytes());
        String token = Base64.encodeBase64URLSafeString(messageDigest);
        
        return token;
        
    }
    
    private boolean validateRequestor(String requestor) {
        String result = null;
        
        try {
            result = tspDB.valiateRequestor(requestor);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (result.equals(requestor))
            return true;
        else
            return false;
    }
    
    private String getCard(String token) {
        String result = null;
        try {
            result = tspDB.retriveToken(token);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return result;
    }
    
    //public String tokenRequestorRegistration() {
    //	String token_requestor_id = null;
    //	Random rand = new Random();
    //	token_requestor_id = Integer.toString(rand.nextInt(99999) + 10000);
    
    // return token_requestor_id;
    // }
    public String[] socketConnection() throws Exception {
        
        while (true) {
            
            String[] client_packet = null;
            ServerSocket serverSocket = new ServerSocket(PORT);
            Socket socket = serverSocket.accept();
            ObjectInputStream objectInput = null;
            
            InputStream in = socket.getInputStream();
            objectInput = new ObjectInputStream(in);
            client_packet = (String[]) objectInput.readObject();
            
            //			if (client_packet[0].equals("FROM_ACQUIRER")) {
            //				System.out.println("TSP received a packet from ACQUIRER for payment request");
            //				client_packet[0] = "FROM_TSP";
            //				handleRequest(client_packet);
            //				objectInput.close();
            //				serverSocket.close();
            //			}
            
            if (client_packet[0].equals("FROM_MERCHANT")) {
                System.out.println("TSP received a packet from MERCHANT for payment request");
                client_packet[0] = "FROM_TSP";
                handleRequest(client_packet);
                objectInput.close();
                serverSocket.close();
            }
            
            //			else if (client_packet[0].equals("FROM_ISSUER")) {
            //				System.out.println("TSP received a packet from ISSUER for payment decision");
            //				client_packet[0] = "FROM_TSP";
            //				sendPacket(client_packet, PREPORT);
            //				objectInput.close();
            //				serverSocket.close();
            //
            //			}
            
            else if (client_packet[0].equals("FROM_ISSUER")) {
                System.out.println("TSP received a packet from ISSUER for payment decision");
                client_packet[0] = "FROM_TSP";
                sendPacket(client_packet, MERCHANT);
                objectInput.close();
                serverSocket.close();
                
            }
            
            else if (client_packet[0].equals("FROM_ISSUER_ADD")) {
                System.out.println("TSP received a packet from ISSUER for ADD Card decision");
                handleRequest(client_packet);
                objectInput.close();
                serverSocket.close();
            }
            
            else if (client_packet[0].equals("FROM_CLIENT") && client_packet[1].equals("ADD")) {
                System.out.println("TSP received a packet from Client for ADD Card request");
                handleRequest(client_packet);
                objectInput.close();
                serverSocket.close();
            }
            
            else if (client_packet[0].equals("FROM_CLIENT") && client_packet[1].equals("REQUEST_NONCE")) {
                //For Nonce Generation
                System.out.println("TSP received a packet from Client for generating NONCE request");
                handleRequest(client_packet);
                objectInput.close();
                serverSocket.close();
            }
        }
        
    }
    
    public void sendPacket(String[] packet, int port) {
        try {
            Socket socket = new Socket(HOST, port);
            OutputStream output = socket.getOutputStream();
            ObjectOutputStream objectOutput = new ObjectOutputStream(output);
            objectOutput.writeObject(packet);
            objectOutput.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void main(String[] args) {
        
        TSP tsp = new TSP();
        
        try {
            tsp.socketConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
