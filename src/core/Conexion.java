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

	private Socket vigente;
	
	private File fileToSend;

	private PublicKey clientkey; 

	private PublicKey serverkey;
	
	private String cifrado;

	public Conexion(Socket StoC, int idassigned, File archiv, byte[] hash, PublicKey llave, String cipher)
	{
		vigente = StoC; serverkey = llave; cifrado = cipher; fileToSend = archiv;
		try 
		{
			envios = new PrintWriter(vigente.getOutputStream(), true);
			recibos = new BufferedReader(new InputStreamReader(vigente.getInputStream()));	
			envios.println(idassigned);
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}
	}
	
	public void transmitirArchivo()
	{
		try 
		{
			//Se puede definir un número en vez de vigente.getSendBufferSize()
			byte[] filebytes = new byte[vigente.getSendBufferSize()]; 
			DataOutputStream dos = new DataOutputStream(vigente.getOutputStream());
			DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(fileToSend)));
			dis.read(filebytes, 0, filebytes.length); int chunk;
			dos.writeUTF(fileToSend.getName()); dos.writeLong(filebytes.length);
			while((chunk = dis.read(filebytes)) != -1) dos.write(filebytes, 0, chunk);			
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}
	}

	public void run()
	{
		try 
		{
			//int syncliente = Integer.parseInt(recibos.readLine());
			
			int lC = Integer.parseInt(recibos.readLine());
			byte[] llaveC = new byte[lC];
			vigente.getInputStream().read(llaveC, 0, lC);
			X509EncodedKeySpec ks = new X509EncodedKeySpec(llaveC);
			KeyFactory kf = KeyFactory.getInstance(cifrado);
			clientkey = kf.generatePublic(ks);
			byte[] llaveS = serverkey.getEncoded(); 
			envios.println(llaveS.length);
			envios.print(llaveS);
			transmitirArchivo();
			recibos.close();
			envios.close();
			vigente.close();
		}
		catch (Exception e) 
		{	e.printStackTrace();	}
	}

}
