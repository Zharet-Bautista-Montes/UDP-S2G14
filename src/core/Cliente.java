package core;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;

public class Cliente extends Thread
{	
	private Socket principal; 
	
	private int id;
	
	private BufferedReader entrada; 
	
	private PrintWriter salida; 
	
	private byte[] requested; 
	
	private byte[] proofhash = {}; 
	
	private String hashing; 
	
	private ArrayList<RegistroLog> reporte;
	
	public Cliente(String ipaddress, int port, String hashing, String descifrado, ArrayList<RegistroLog> reporte)
	{
		this.hashing = hashing;
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
			String filename = dis.readUTF(); long filesize = dis.readLong();
			String hash = dis.readUTF();
			long initime = System.currentTimeMillis(); 
			FileOutputStream fos = new FileOutputStream("clientfiles/" + id + "_" + filename); int piece; 
			while((piece = dis.read(requested)) != -1)
			{	fos.write(requested, 0, piece); if(piece < 512) break;	}
			long fintime = System.currentTimeMillis();
			proofhash = hash.getBytes(); String neg = " "; 
			byte[] referenz = obtenerHash(hashing, "clientfiles/" + id + "_" + filename); 
			if(!referenz.equals(proofhash)) neg = " no "; 
			System.out.println("El archivo enviado al cliente " + id + neg + "fue alterado"); 
			RegistroLog log = new RegistroLog(id, filename, (double) filesize/1024, neg.equals(" no "), (fintime-initime)/1000); 
			reporte.add(log);
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}				
	}
	
	public void run() 
	{
		try 
		{
			int SYN = 1, NSI = 0; salida.println(SYN + ";" + NSI);
			String[] synservidor = entrada.readLine().split(";");
			if(Integer.parseInt(synservidor[0]) == 1) System.out.println("SYN Servidor en 1");
			if(Integer.parseInt(synservidor[2]) == NSI + 1) System.out.println("ACK del servidor");
			int ACK = Integer.parseInt(synservidor[1]) + 1; SYN = 0; NSI++;
			salida.println(SYN + ";" + NSI + ";" + ACK);
			this.id = Integer.parseInt(entrada.readLine());
			System.out.println("Conexión exitosa");
			recibirArchivo();
			int FIN = 1; salida.println(FIN);
			String[] endservidor = entrada.readLine().split(";");
			if(Integer.parseInt(endservidor[0]) == 1 && Integer.parseInt(endservidor[1]) == 1)
				System.out.println("Conexión terminada");
			ACK = 1; salida.println(ACK);
			entrada.close();
			salida.close();
			principal.close();
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}
	}
}
