package core;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;

public class Conexion extends Thread
{
	private int idassigned;
	
	private boolean end;
	
	private byte[] temphash;

	private DatagramSocket vigente;
	
	private File fileToSend;
	
	private RegistroLog reporte;

	public Conexion(DatagramSocket StoC, int idassigned, File archiv, byte[] hash)
	{
		vigente = StoC; fileToSend = archiv; this.idassigned = idassigned; 
		temphash = hash; reporte = null;
	}
	
	public void transmitirArchivo()
	{
		try 
		{
			byte[] filebytes = new byte[512]; 
			DataOutputStream dos = new DataOutputStream(vigente.getOutputStream());
			DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fileToSend)));
			dos.writeUTF(fileToSend.getName()); dos.writeLong(fileToSend.length());
			dos.writeUTF(new String(temphash));
			long initime = System.currentTimeMillis();
			dis.read(filebytes, 0, 0); int chunk;
			while((chunk = dis.read(filebytes)) != -1) dos.write(filebytes, 0, chunk);
			long fintime = System.currentTimeMillis();
			String confirmado = recibos.readLine(); 
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
			byte[] id = ByteBuffer.allocate(4).putInt(idassigned).array();
			DatagramPacket identificacion = new DatagramPacket(id, id.length);
			vigente.send(identificacion);
			transmitirArchivo();
			System.out.println("¡Hecho!");
			vigente.close();
			end = true;
		}
		catch (Exception e) 
		{	e.printStackTrace();	}
	}
	
	private void enviarDatagrama(Object indata, int lange)
	{
		try 
		{
			byte[] byter;
			if(indata instanceof String)
				byter = ((String) indata).getBytes();
			else if(indata instanceof Integer)
				byter = ByteBuffer.allocate(lange).putInt((int) indata).array();
			else byter = (byte[]) indata;
			DatagramPacket DP = new DatagramPacket(byter, lange);
			vigente.send(DP);
		}
		catch (Exception e) 
		{	e.printStackTrace();	}
	}
	
	private void recibirDatagrama(Object outdata, int lange)
	{
		try 
		{
			byte[] byter = new byte[lange];
			DatagramPacket DP = new DatagramPacket(byter, lange);
			vigente.receive(DP);
			if(outdata instanceof String)
				outdata = new String(byter);
			else if(outdata instanceof Integer)
				outdata = ByteBuffer.wrap(byter).getInt();
			else outdata = byter;
		}
		catch (Exception e) 
		{	e.printStackTrace();	}
	}
}
