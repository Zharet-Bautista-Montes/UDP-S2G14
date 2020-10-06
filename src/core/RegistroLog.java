package core;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RegistroLog 
{
	private int idCliente;
	
	private String FileName; 
	
	private double FileSize;
	
	private Date fecha_hora;
	
	private boolean exitoso;
	
	private double transferTime; 
	
	public RegistroLog(int id, String fname, double fsize, boolean success, double ttime)
	{
		idCliente = id; 
		FileName = fname;
		FileSize = fsize; 
		exitoso = success;
		transferTime = ttime;
		fecha_hora = new Date();
	}
	
	public RegistroLog(int id, boolean success, double ttime)
	{
		idCliente = id; 
		exitoso = success;
		transferTime = ttime;
		fecha_hora = new Date();
	}
	
	public String getFileName() 
	{	return FileName;	}
	
	public double getFileSize() 
	{	return FileSize;	}
	
	public String getDate()
	{	return fecha_hora.toString();	}

	public String toString()
	{
		String check = (exitoso) ? "YES" : "NO"; 
		SimpleDateFormat date = new SimpleDateFormat("dd-MMM-yyyy");
		SimpleDateFormat time = new SimpleDateFormat("hh:mm:ss a");		
		String thislog = 
				"------------------------------" + "\n"
				+ "Client ID: " + idCliente + "\n"
				+ "Date: " + date.format(fecha_hora) + "\n" 
				+ "Time: " + time.format(fecha_hora) + "\n"
				+ "Transfer Time: " + transferTime + " s \n"
				+ "Successful: " + check;		
		return thislog;
	}
}
