package core;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;

public class Conexion extends Thread
{
	private PrintWriter envios; 

	private BufferedReader recibos;
	
	private int idassigned;
	
	private byte[] temphash;

	private Socket vigente;
	
	private File fileToSend;
	
	private String cifrado;
	
	private ArrayList<RegistroLog> reporte;

	public Conexion(Socket StoC, int idassigned, File archiv, byte[] hash, String cipher, ArrayList<RegistroLog> rlc)
	{
		vigente = StoC; cifrado = cipher; fileToSend = archiv;
		this.idassigned = idassigned; temphash = hash; reporte = rlc;
		try 
		{
			envios = new PrintWriter(vigente.getOutputStream(), true);
			recibos = new BufferedReader(new InputStreamReader(vigente.getInputStream()));	
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}
	}
	
	public void transmitirArchivo()
	{
		try 
		{
			byte[] filebytes = new byte[512]; 
			DataOutputStream dos = new DataOutputStream(vigente.getOutputStream());
			DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fileToSend)));
			dis.read(filebytes, 0, 0); int chunk;
			dos.writeUTF(fileToSend.getName()); dos.writeLong(fileToSend.length());
			long initime = System.currentTimeMillis();
			while((chunk = dis.read(filebytes)) != -1) dos.write(filebytes, 0, chunk);
			long fintime = System.currentTimeMillis();
			/** Hay problemas con el hash
			//dos.writeUTF("Hash"); System.out.println("Hash");
			OutputStream os = vigente.getOutputStream(); os.write(temphash);*/
			boolean confirmado = false; 
			//confirmado = Boolean.parseBoolean(recibos.readLine());
			RegistroLog log = new RegistroLog(idassigned, confirmado, (fintime-initime)/1000); 
			reporte.add(log);
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}
	}

	public void run()
	{
		try 
		{
			//int syncliente = Integer.parseInt(recibos.readLine());
			envios.println(idassigned);
			transmitirArchivo();
			recibos.close();
			envios.close();
			vigente.close();
		}
		catch (Exception e) 
		{	e.printStackTrace();	}
	}

}
