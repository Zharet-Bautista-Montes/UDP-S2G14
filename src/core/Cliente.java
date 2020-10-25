package core;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.*;

public class Cliente extends Thread
{	
	private DatagramSocket principal; 
	
	private int id;
	
	private int port;
	
	private String IPown; 
	
	private boolean done; 
	
	private byte[] requested; 
	
	private byte[] proofhash = {}; 
	
	private String hashing; 
	
	private RegistroLog reporte;
	
	public Cliente(String ipaddress, int Sport, String hashing)
	{
		this.hashing = hashing; done = false; reporte = null;
		try 
		{		
			IPown = InetAddress.getLocalHost().getHostAddress();
			principal = new DatagramSocket(Sport, InetAddress.getByName(ipaddress));
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
			{	 hash.update(buffer, 0, length);	}
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
			reporte = new RegistroLog(id, filename, (double) filesize/(Math.pow(1024, 2)), neg.equals(" No "), duration); 
			fos.close();
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}				
	}
	
	public boolean isDone() 
	{	return done;	}
	
	public int getPuerto()
	{	return port;	}
	
	public String getDireccionIP()
	{	return IPown;	}
	
	public RegistroLog getReporte()
	{	return reporte;		}

	public void run() 
	{
		try 
		{
			//byte[] identificador = new byte[4];
			//DatagramPacket newid = new DatagramPacket(identificador, identificador.length);
			//principal.receive(newid); this.id = ByteBuffer.wrap(identificador).getInt();
			recibirDatagrama(id, 4);
			System.out.println("Conexión exitosa, listo para recibir archivo");
			recibirArchivo();
			System.out.println("Conexión terminada");
			principal.close();
			done = true;
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}
	}
	
	private void enviarDatagrama(Object outdata, int lange)
	{
		try 
		{
			byte[] byter;
			if(outdata instanceof String)
				byter = ((String) outdata).getBytes();
			else if(outdata instanceof Integer)
				byter = ByteBuffer.allocate(lange).putInt((int) outdata).array();
			else byter = (byte[]) outdata;
			DatagramPacket DP = new DatagramPacket(byter, lange);
			principal.send(DP);
		}
		catch (Exception e) 
		{	e.printStackTrace();	}
	}
	
	private void recibirDatagrama(Object indata, int lange)
	{
		try 
		{
			byte[] byter = new byte[lange];
			DatagramPacket DP = new DatagramPacket(byter, lange);
			principal.receive(DP);
			if(indata instanceof String)
				indata = new String(byter);
			else if(indata instanceof Integer)
				indata = ByteBuffer.wrap(byter).getInt();
			else indata = byter;
		}
		catch (Exception e) 
		{	e.printStackTrace();	}
	}
}
