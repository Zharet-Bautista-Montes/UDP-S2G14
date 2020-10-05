package core;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
//import javax.net.ssl.SSLSocket;

public class Conexion extends Thread
{
	private PrintWriter envios; 

	private BufferedReader recibos;
	
	private int idassigned;
	
	private byte[] temphash;

	private Socket vigente;
	
	private File fileToSend;

	private PublicKey clientkey; 

	private PublicKey serverkey;
	
	private String cifrado;

	public Conexion(Socket StoC, int idassigned, File archiv, byte[] hash, PublicKey llave, String cipher)
	{
		vigente = StoC; serverkey = llave; cifrado = cipher; fileToSend = archiv;
		this.idassigned = idassigned; temphash = hash;
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
			dis.read(filebytes, 0, filebytes.length); int chunk;
			dos.writeUTF(fileToSend.getName()); dos.writeLong(filebytes.length);
			while((chunk = dis.read(filebytes)) != -1) dos.write(filebytes, 0, chunk);
			dos.write(temphash);
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}
	}

	public void run()
	{
		try 
		{
			//int syncliente = Integer.parseInt(recibos.readLine());
			System.out.println("L");
			envios.println(idassigned); 
			/**
			int lC = Integer.parseInt(recibos.readLine());
			byte[] llaveC = new byte[lC];
			vigente.getInputStream().read(llaveC, 0, lC);
			X509EncodedKeySpec ks = new X509EncodedKeySpec(llaveC);
			KeyFactory kf = KeyFactory.getInstance(cifrado);
			clientkey = kf.generatePublic(ks);
			byte[] llaveS = serverkey.getEncoded(); 
			envios.println(llaveS.length);
			envios.print(llaveS);
			*/
			transmitirArchivo();
			System.out.println("Done! " + idassigned);
			recibos.close();
			envios.close();
			vigente.close();
		}
		catch (Exception e) 
		{	e.printStackTrace();	}
	}

}
