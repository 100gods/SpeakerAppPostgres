import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Date;

import marf.MARF;
import marf.util.Debug;
import marf.util.MARFException;


/**
 * Sound Source Feature 
 * <p> Sound Source Identification App using MARF Lib & postgres as DB .
 * @author saurabh
 *
 */
public class SoundSource {
	
	/**
	 * Marf sound source train 
	 * <p> This will train each file for the given resource path .
	 * TODO : postgres support   
	 * @param filePath (Directory , Single files support is not available )
	 * @return void 
	 * @throws SQLException 
	 * @throws IOException 
	 * 
	 */
	public static void train(String table,String path) throws SQLException, IOException
	{
		File[] trainFiles=new File(path).listFiles();
		for (File file : trainFiles) {
			//postgres code . update the speakers table for the given file 
		    System.out.println("Enter category name of "+file.getName());
		    System.in.read();
		    String name=new BufferedReader(new InputStreamReader(System.in)).readLine();
			SpeakerPgDB.update(table,name,name.toLowerCase().hashCode(), file.getName());
			// supports only .wav files
			if(file.isFile() && file.getName().toLowerCase().contains(".wav") ){
				MARF.setSampleFile(file.getAbsolutePath());
				MARF.setCurrentSubject(name.toLowerCase().hashCode());
				try {
					MARF.train();
					System.out.println("taining complete for "+file.getName());
				} catch (MARFException e) {
					// TODO : LOGGER 
					System.out.println(e.getMessage());
				}
				
			}
			
		}
		
	}
	public static void ident(String table,String path) throws MARFException, SQLException{
		File[] trainFiles=new File(path).listFiles();
		int count=0;
		int vaild=0;
		for (File file : trainFiles) {
			// supports only .wav files
		 // supports only .wav files
		 			if(file.isFile() && file.getName().toLowerCase().contains(".wav") ){
		 			    
		 			    	MARF.setSampleFile(file.getAbsolutePath());
		 				MARF.recognize();
		 				int iIdentifiedID = MARF.queryResultID();
		 				count++;
		 				// Second best
		 				int iSecondClosestID = MARF.getResultSet().getSecondClosestID();
		 				System.out.println("----------------------------------------------------------");
		 				System.out.println("                 File: " + file.getName());
		 				System.out.println("         Speaker's ID: " + iIdentifiedID);
		 				System.out.println("   Speaker identified: " + SpeakerPgDB.getName(table,iIdentifiedID));
		 				System.out.println("       Second Best ID: " + iSecondClosestID);
		 				System.out.println("     Second Best Name: " + SpeakerPgDB.getName(table,iSecondClosestID) );
		 				System.out.println("            Date/time: " + new Date());
		 				System.out.println("----------------------------------------------------------");System.out.println("----------------------------------------------------------");
		 				System.out
							.println("| Expected Speaker's ID | Identified ID |");
		 				System.out.println("|     " + SpeakerPgDB.getName(table,iIdentifiedID).toLowerCase().hashCode()+"      	| "+iIdentifiedID+"     |");
		 				if(SpeakerPgDB.getName(table,iIdentifiedID).toLowerCase().hashCode() == iIdentifiedID)vaild++;
		 				System.out.println("----------------------------------------------------------");System.out.println("----------------------------------------------------------");
		 			}		
			
			
		}
		System.out.println("Correct Identification ="+(vaild/(float)count)*100+"%");
		
	}
	public static final void setDefaultConfig()
		throws MARFException
		{
			/*
			 * Default MARF setup
			 */
			MARF.setPreprocessingMethod(MARF.DUMMY);
			MARF.setFeatureExtractionMethod(MARF.FFT);
			MARF.setClassificationMethod(MARF.EUCLIDEAN_DISTANCE);
			MARF.setDumpSpectrogram(false);
			MARF.setSampleFormat(MARF.WAV);

			Debug.enableDebug(false);
		}
	public static void main(String[] args) throws SQLException, MARFException, IOException {
	    // args[] = ["trian/test",productID,dsname]
	    
	    if(args.length < 3)
		try {
		    throw new Exception("Params error");
		} catch (Exception e) {
		    // TODO Auto-generated catch block
		    e.printStackTrace();
		    System.exit(-1);
		}
	    
	    String table=SpeakerPgDB.setTable(args[1],args[2]);
	    setDefaultConfig();
	   if(args[0].equalsIgnoreCase("train"))
	    train(table,"//home//saurabh//Desktop//Sound-samples//testing-samples");
	   else if (args[0].equalsIgnoreCase("test")) {
	       ident(table,"//home//saurabh//Desktop//Sound-samples//testing-samples");
	    
	   
		    
	    
	}
/*	   File file=new File("//home//saurabh//Desktop//Sound-samples//testing-samples//aihua5.wav");
	    BufferedReader reader=new BufferedReader(new FileReader(file));
	    String current;
	    while((current =reader.readLine())!=null)
	    {
		String[] wo=current.split(" ");
		for (int i = 0; i < wo.length; i++) {
		    System.out.println(Integer.parseInt(wo[i],16));
		}
	    }*/
	 
	}

}
