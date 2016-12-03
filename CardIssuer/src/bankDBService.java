import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class bankDBService {
    Connection conn;
    
    public bankDBService(){
        this.conn = null;
    }
    
    public void makeConnection(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            conn =  DriverManager.getConnection("jdbc:mysql://localhost:3306/card_validation","root","root");
        }catch(Exception e){
            System.out.println(e);
        }
    }
    
    public boolean validateCard(String user_card, String user_card_expiry_date){
        String query = "select card, card_expiry_date from cardvalidation where card = " + "'" + user_card + "' and card_expiry_date = '" + user_card_expiry_date + "'";
        String ID=null;
        String exp=null;
        boolean result=true;
        try{
            
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            if(rs.next()){
                ID = rs.getString(1);
                exp=rs.getString(2);
                
            }
            if (user_card.equals(ID) && user_card_expiry_date.equals(exp))
            {
                result =true;		 
            }
            else { result=false;}
            
        }catch(SQLException e){
            System.out.println(e);
        }
        
        return result;
        
    }
    
}



