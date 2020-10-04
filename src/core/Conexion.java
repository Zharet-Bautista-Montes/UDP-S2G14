package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Conexion extends Thread
{
	private PrintWriter envios; 
	
	private BufferedReader recibos;
	
	public Conexion(Socket StoC, int idassigned, File archiv, byte[] hash)
	{
		try 
		{
			envios = new PrintWriter(StoC.getOutputStream(), true);
			recibos = new BufferedReader(new InputStreamReader(StoC.getInputStream()));
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}
	}
	
	public void run()
	{
		
	}

}
