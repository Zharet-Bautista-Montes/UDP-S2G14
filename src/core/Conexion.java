package core;

import java.io.*;
import java.net.Socket;

public class Conexion extends Thread
{
	private PrintWriter envios; 

	private BufferedReader recibos;
	
	private int idassigned;
	
	private boolean end;
	
	private byte[] temphash;

	private Socket vigente;
	
	private File fileToSend;
	
	private RegistroLog reporte;

	public Conexion(Socket StoC, int idassigned, File archiv, byte[] hash)
	{
		vigente = StoC; fileToSend = archiv; this.idassigned = idassigned; 
		temphash = hash; reporte = null;
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
			String confirmado = recibos.readLine(); 
			System.out.println("El cliente " + idassigned + confirmado + "recibi� el archivo incompleto");
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
			String[] syncliente = recibos.readLine().split(";");
			if(Integer.parseInt(syncliente[0]) == 1) System.out.println("SYN Cliente en 1");
			int SYN = 1, NSI=0, ACK = Integer.parseInt(syncliente[1]) + 1; 
			envios.println(SYN + ";" + NSI + ";" + ACK);
			String[] achieved = recibos.readLine().split(";");
			if(Integer.parseInt(achieved[2]) == NSI + 1) System.out.println("ACK del Cliente");
			if(Integer.parseInt(achieved[0]) == 0) System.out.println("�Listo!");
			envios.println(idassigned);
			transmitirArchivo();
			int endcliente = Integer.parseInt(recibos.readLine());
			if(endcliente == 1) System.out.println("FIN Cliente");
			int FIN = 1; ACK = 1; envios.println(FIN + ";" + ACK);
			if(Integer.parseInt(recibos.readLine()) == 1) System.out.println("�Hecho!");
			recibos.close();
			envios.close();
			vigente.close();
			end = true;
		}
		catch (Exception e) 
		{	e.printStackTrace();	}
	}
}
