import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


public class SpeakerPgDB {
    public static String tableName=null;
	
	public static Connection open(){
		Connection connection=null;
		try {
			connection= DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/postgres", "postgres","postgres");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return connection;
	}
	public static  void update(String table,String name,int id,String fileName) throws SQLException{
		PreparedStatement st = null;
		ResultSet rs = null;
		boolean flag=true;
		Connection connection=open();
		st=connection.prepareStatement("select filename from "+table+" where id =?");
		st.setInt(1, id);
		rs=st.executeQuery();
		String old="";
		while(rs.next()){
			old=rs.getString(1);
					if(! old.contains(fileName)){
					    System.out.println(name + "found . Updating the entry with "+fileName);
						old+="|"+fileName;
						st=null;
						st=connection.prepareStatement("update "+table+" set filename=? where id=?");
						st.setString(1, old);
						st.setInt(2, id);
						st.executeUpdate();
						flag=false;
						//System.out.println(old);
					}
					
		}
		if(flag){
			// insert new entry 
			st=null;
			st=connection.prepareStatement("insert into "+table+" values (?,?,?)");
			st.setInt(1, id);
			st.setString(2, name);
			st.setString(3, fileName);
			st.executeUpdate();			
		}
		connection.close();
		
	}
	public static String getName(String table,int id) throws SQLException{
	    	PreparedStatement st = null;
	    	String name="Unknown";
		ResultSet rs = null;
		Connection connection=open();
		st=connection.prepareStatement("select name from "+table+" where id =?");
		st.setInt(1, id);
		rs=st.executeQuery();
		while(rs.next()){
		    name=rs.getString(1);
		}
		//System.out.println("id="+id+"name="+name);
		connection.close();
		return name;
		
	}
	public static String setTable(String productId,String dsName) throws SQLException{
	/*    CREATE TABLE soundtest
	    (
		    id integer,
		    name text,
		    filename text
		  )*/
	    PreparedStatement st=null;
	    Connection connection=open();
	    st=connection.prepareStatement("Create table IF NOT EXISTS "+(dsName+productId+"sourceTrain")+"(id integer,name text,filename text)");
	    
	    System.out.println(st.toString());
	    st.execute();
	    tableName=(dsName+productId+"sourceTrain");
	    return tableName;
	    
	}
	public static void main(String[] args) throws SQLException {
		//update("cameron",200,"test2.wav");
	   //System.out.println(getName(108103));
	}

}
