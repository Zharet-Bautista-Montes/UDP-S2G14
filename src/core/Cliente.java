package core;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;

public class Cliente extends Thread
{	
	private Socket principal; 
	
	private int id, counter;
	
	private BufferedReader entrada; 
	
	private PrintWriter salida; 
	
	private byte[] requested; 
	
	private byte[] proofhash = {}; 
	
	private String hashing; 
	
	private ArrayList<RegistroLog> reporte;
	
	public Cliente(String ipaddress, int port, String hashing, ArrayList<RegistroLog> reporte, int counter)
	{
		this.hashing = hashing; this.reporte = reporte; this.counter = counter;
		try 
		{		
			principal = new Socket(ipaddress, port);
			entrada = new BufferedReader(new InputStreamReader(principal.getInputStream()));
			salida = new PrintWriter(principal.getOutputStream(), true);
			System.out.println("Conexión establecida");
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
			file.close();
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
			String hash = dis.readUTF(); int conteo = 0, piece;
			long initime = System.currentTimeMillis(); 
			FileOutputStream fos = new FileOutputStream("clientfiles/" + id + "_" + filename); 
			while((piece = dis.read(requested)) != -1)
			{	fos.write(requested, 0, piece); conteo += piece; if(piece < 512) break;	}
			long fintime = System.currentTimeMillis();
			System.out.println("Archivo recibido y en verificación:");
			proofhash = hash.getBytes(); String neg = " "; 
			byte[] referenz = obtenerHash(hashing, "clientfiles/" + id + "_" + filename); 
			if(!referenz.equals(proofhash)) neg = " No "; 
			System.out.println("El archivo recibido por el cliente " + id + neg + "fue alterado"); 
			String komplett = "Recibió el archivo completamente";
			if(conteo < filesize) komplett = neg + komplett; System.out.println(komplett);
			salida.println(neg); double duration = (fintime-initime)/1000.0; 
			System.out.println("Tiempo de transferencia: " + duration + " s");
			RegistroLog log = new RegistroLog(id, filename, (double) filesize/1024, neg.equals(" No "), duration); 
			reporte.add(log); fos.close();
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
			System.out.println("Conexión exitosa, listo para recibir archivo");
			recibirArchivo();
			int FIN = 1; salida.println(FIN);
			String[] endservidor = entrada.readLine().split(";");
			if(Integer.parseInt(endservidor[0]) == 1 && Integer.parseInt(endservidor[1]) == 1)
				System.out.println("FIN Servidor. Conexión terminada");
			ACK = 1; salida.println(ACK);
			entrada.close();
			salida.close();
			principal.close();
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}
	}
}
