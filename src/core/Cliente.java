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
	
	private InetAddress IPServer; 
	
	private boolean done; 
	
	private byte[] proofhash = {}; 
	
	private String hashing; 
	
	private RegistroLog reporte;
	
	public Cliente(int id, String ipaddress, int Sport, int Cport, String hashing)
	{
		this.hashing = hashing; done = false; reporte = null; this.id = id;
		try 
		{		
			IPServer = InetAddress.getByName(ipaddress); port = Sport;
			principal = new DatagramSocket(Cport);
			System.out.println("Conexión establecida en " + Cport);
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
			byte[] requested = new byte[512]; 
			String filename = ""; int filesize = 0, conteo = 0; 
			recibirDatagrama(filename, 1);
			recibirDatagrama(filesize, 4);
			recibirDatagrama(proofhash, 1);
			long initime = System.currentTimeMillis(); 
			FileOutputStream fos = new FileOutputStream("clientfiles/" + id + "_" + filename); 
			recibirDatagrama(requested, requested.length);
			while(conteo < filesize)
			{	
				fos.write(requested, 0, requested.length); conteo += requested.length;
				if(filesize - conteo < 512)
					requested = new byte[filesize - conteo];
				recibirDatagrama(requested, requested.length);	
			}
			long fintime = System.currentTimeMillis(); String neg = "    ";
			System.out.println("Archivo recibido y en verificación:");		 
			byte[] referenz = obtenerHash(hashing, "clientfiles/" + id + "_" + filename); 
			if(!referenz.equals(proofhash)) neg = " No "; 
			System.out.println("El archivo recibido por el cliente " + id + neg + "fue alterado"); 
			String komplett = "Recibió el archivo completamente";
			if(conteo < filesize) komplett = neg + komplett; System.out.println(komplett);
			enviarDatagrama(neg, 4);
			double duration = (fintime-initime)/1000.0; 
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
	{	return IPServer.getHostName();	}
	
	public RegistroLog getReporte()
	{	return reporte;		}

	public void run() 
	{
		try 
		{
			//recibirDatagrama(id, 4); System.out.println(id);
			enviarDatagrama(id, 4);
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
			DatagramPacket DP = new DatagramPacket(byter, lange, IPServer, port);
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
				indata = new String(DP.getData());
			else if(indata instanceof Integer)
				indata = ByteBuffer.wrap(DP.getData()).getInt();
			else indata = DP.getData();
		}
		catch (Exception e) 
		{	e.printStackTrace();	}
	}
}
