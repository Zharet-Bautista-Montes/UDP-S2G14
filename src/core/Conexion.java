package core;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Conexion extends Thread
{
	private PrintWriter envios; 

	private BufferedReader recibos;
	
	private int idassigned;
	
	private byte[] temphash;

	private Socket vigente;
	
	private File fileToSend;
	
	private ArrayList<RegistroLog> reporte;

	public Conexion(Socket StoC, int idassigned, File archiv, byte[] hash, ArrayList<RegistroLog> rlc)
	{
		vigente = StoC; fileToSend = archiv; this.idassigned = idassigned; temphash = hash; reporte = rlc;
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
			dos.writeUTF(fileToSend.getName()); dos.writeLong(fileToSend.length());
			dos.writeUTF(new String(temphash));
			long initime = System.currentTimeMillis();
			dis.read(filebytes, 0, 0); int chunk;
			while((chunk = dis.read(filebytes)) != -1) dos.write(filebytes, 0, chunk);
			long fintime = System.currentTimeMillis();
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
			String[] syncliente = recibos.readLine().split(";");
			if(Integer.parseInt(syncliente[0]) == 1) System.out.println("SYN Cliente en 1");
			int SYN = 1, NSI=0, ACK = Integer.parseInt(syncliente[1]) + 1; 
			envios.println(SYN + ";" + NSI + ";" + ACK);
			String[] achieved = recibos.readLine().split(";");
			if(Integer.parseInt(achieved[2]) == NSI + 1) System.out.println("ACK del Cliente");
			if(Integer.parseInt(achieved[0]) == 0) System.out.println("¡Listo!");
			envios.println(idassigned);
			transmitirArchivo();
			recibos.readLine();
			int FIN = 1; ACK = 1; envios.println(FIN + ";" + ACK);
			if(Integer.parseInt(recibos.readLine()) == 1) System.out.println("¡Hecho!");
			recibos.close();
			envios.close();
			vigente.close();
		}
		catch (Exception e) 
		{	e.printStackTrace();	}
	}

}
