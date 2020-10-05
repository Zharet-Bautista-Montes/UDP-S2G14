package core;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.ArrayList;

public class Cliente extends Thread
{	
	private Socket principal; 
	
	private String ipaddress;
	
	private int id, port, ACK, SYN, FIN;
	
	private BufferedReader entrada; 
	
	private PrintWriter salida; 
	
	private byte[] requested; 
	
	private byte[] proofhash = {}; 
	
	private String hashing; 
	
	private String descifrado; 
	
	private ArrayList<RegistroLog> reporte;
	
	public Cliente(String ipaddress, int port, String hashing, String descifrado, ArrayList<RegistroLog> reporte)
	{
		this.ipaddress = ipaddress;
		this.port = port;
		this.hashing = hashing;
		this.descifrado = descifrado; 
		this.reporte = reporte;
		try 
		{		
			principal = new Socket(ipaddress, port);
			entrada = new BufferedReader(new InputStreamReader(principal.getInputStream()));
			salida = new PrintWriter(principal.getOutputStream(), true);	
		}
		catch (Exception e) 
		{	e.printStackTrace();	}
	}
	
	private byte[] obtenerHash(String algorithm, String filename)
	{
		MessageDigest hash = null; 
		try 
		{
			hash = MessageDigest.getInstance(algorithm);
			FileInputStream file = new FileInputStream(filename);
			byte[] buffer = new byte [1024]; int length;
			while ((length = file.read(buffer)) != -1)
			{	hash.update(buffer, 0, length);	}
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}
		return hash.digest(); 
	}
	
	private void recibirArchivo()
	{		
		try 
		{
			requested = new byte[512];
			DataInputStream dis = new DataInputStream(principal.getInputStream());
			String filename = dis.readUTF(); long filesize = dis.readLong(), avance = 0;
			String hash = dis.readUTF(); System.out.println(hash);
			long initime = System.currentTimeMillis(); 
			FileOutputStream fos = new FileOutputStream("clientfiles/" + id + filename); int piece; 
			while((piece = dis.read(requested)) != -1)
			{
				fos.write(requested, 0, piece); avance += piece;
			}
			long fintime = System.currentTimeMillis();
			proofhash = hash.getBytes(); String neg = " "; 
			byte[] referenz = obtenerHash(hashing, "clientfiles/" + id + filename); 
			if(!referenz.equals(proofhash)) neg = " no "; 
			System.out.println("El archivo enviado al cliente " + id + neg + "fue alterado"); 
			RegistroLog log = new RegistroLog(id, filename, (double) filesize/1024, neg.equals(" "), (fintime-initime)/1000); 
			reporte.add(log);
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}				
	}
	
	public void run() 
	{
		try 
		{
			//SYN = 0; salida.println(SYN);
			this.id = Integer.parseInt(entrada.readLine());
			recibirArchivo();
			entrada.close();
			salida.close();
			principal.close();
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}
	}
}
