import java.sql.*;

public class TSPDBService {
    Connection conn;
    PreparedStatement p;
    ResultSet rs;
    
    public TSPDBService(){
        this.conn = null;
        this.p = null;
        this.rs = null;
    }
    
    public void makeConnection(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/tokenvault","root","root");
        }catch(Exception e){
            System.out.println(e);
        }
    }
    
    public void insertToken( String card, String token,String card_exp_date, String token_exp_date, String token_reqestor_id){
        try{p = conn.prepareStatement("INSERT INTO token_vault VALUES(?,?,?,?,?)");
            p.setString(1, card);
            p.setString(2, token);
            p.setString(3, card_exp_date);
            p.setString(4, token_exp_date);
            p.setString(5, token_reqestor_id);
            p.executeUpdate();
            
        }catch(SQLException e){
            System.out.println(e);
        }
        
    }
    
    public String retriveToken(String token){
        String query = "select card from token_card where token = " + "'" + token + "'";
        String result = null;
        try{
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()){
                result = rs.getString(1);
            }
        }catch(SQLException e){
            System.out.println(e);
        }
        return result;
    }
    
    public String valiateRequestor(String requestor){
        String query = "select requestorid from requestor where requestorid = " + "'" + requestor + "'";
        Statement stmt = null;
        String result = null;
        
        try{
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()){
                result = rs.getString(1);
            }
        }catch(SQLException e){
            System.out.println(e);
        }
        return result;
    }
}



