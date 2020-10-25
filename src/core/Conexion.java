package core;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class Conexion extends Thread
{
	private int idassigned;

	private InetAddress clientIP;

	private int clientPort;

	private boolean end;

	private byte[] temphash;

	private DatagramSocket vigente;

	private File fileToSend;

	private RegistroLog reporte;

	public Conexion(String ip, int port, DatagramSocket StoC, int idassigned, File archiv, byte[] hash)
	{
		vigente = StoC; fileToSend = archiv; this.idassigned = idassigned; 
		temphash = hash; reporte = null; clientPort = port; 
		try
		{	clientIP = InetAddress.getByName(ip);	}
		catch (Exception e) 
		{	e.printStackTrace();	}
	}

	public void transmitirArchivo()
	{
		try 
		{
			byte[] filebytes = new byte[512]; 
			DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fileToSend)));
			enviarDatagrama(fileToSend.getName(), 1);
			enviarDatagrama((int) fileToSend.length(), 4);
			enviarDatagrama(temphash, 1);
			long initime = System.currentTimeMillis();
			dis.read(filebytes, 0, 0); int chunk;
			while((chunk = dis.read(filebytes)) != -1)
			{
				enviarDatagrama(filebytes, chunk);				
			}
			long fintime = System.currentTimeMillis();
			String confirmado = ""; recibirDatagrama(confirmado, 4); 
			System.out.println("El cliente " + idassigned + confirmado + "recibió el archivo incompleto");
			double duration = (fintime-initime)/1000.0; 
			System.out.println("Tiempo de transferencia: " + duration + " s");
			reporte = new RegistroLog(idassigned, confirmado.equals(" No "), duration); 
			dis.close();
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}
	}

	public boolean hasEnded() 
	{	return end; 	}

	public RegistroLog getReporte()
	{	return reporte; 	}	

	public void run()
	{
		try 
		{
			System.out.println("¡Listo!");
			transmitirArchivo();
			System.out.println("¡Hecho!");
			end = true;
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
			DatagramPacket DP = new DatagramPacket(byter, lange, clientIP, clientPort);
			vigente.send(DP);
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
			vigente.receive(DP);
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
