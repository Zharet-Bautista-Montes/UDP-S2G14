package core;

import java.io.*;
import java.net.Socket;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
//import javax.net.ssl.SSLSocket;

public class Cliente extends Thread
{	
	private Socket principal; 
	
	private String ipaddress;
	
	private int id, port, ACK, SYN, FIN;
	
	private BufferedReader entrada; 
	
	private PrintWriter salida; 
	
	private byte[] requested; 
	
	private byte[] proofhash = {};
	
	private File logcliente; 
	
	private String hashing; 
	
	private String descifrado;
	
	private PrivateKey privada = null;
	
	private PublicKey publica = null;
	
	private PublicKey alterpublica = null; 
	
	public Cliente(String ipaddress, int port, String hashing, String descifrado)
	{
		this.ipaddress = ipaddress;
		this.port = port;
		this.hashing = hashing;
		this.descifrado = descifrado; 
		try 
		{		
			principal = new Socket(ipaddress, port);
			entrada = new BufferedReader(new InputStreamReader(principal.getInputStream()));
			salida = new PrintWriter(principal.getOutputStream(), true);	
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
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}
		return hash.digest(); 
	}
	
	private Key crearLlave()
	{
		KeyPairGenerator generator; 
		try 
		{
			generator = KeyPairGenerator.getInstance(descifrado);
			generator.initialize(1024);
			KeyPair par = generator.generateKeyPair();
			publica = par.getPublic();
			privada = par.getPrivate(); 
		} 
		catch (NoSuchAlgorithmException e) 
		{	e.printStackTrace();	}
		return publica;
	}
	
	private void recibirArchivo()
	{		
		try 
		{
			requested = new byte[512];
			DataInputStream dis = new DataInputStream(principal.getInputStream());
			String filename = dis.readUTF(); long filesize = dis.readLong();
			FileOutputStream fos = new FileOutputStream("clientfiles/" + id + filename); int piece; 
			while((piece = dis.read(requested)) != -1) fos.write(requested, 0, piece);
			fos.close(); fos = null; System.gc();
			dis.read(proofhash); String neg = " ";
			byte[] referenz = obtenerHash(hashing, "clientfiles/" + id + filename);
			if(referenz.equals(proofhash)) neg = " no ";
			System.out.println("El archivo enviado al cliente " + id + neg + "fue alterado");
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}				
	}
	
	public void run() 
	{
		try 
		{
			//SYN = 0; salida.println(SYN);
			System.out.println("C");
			this.id = Integer.parseInt(entrada.readLine()); System.out.println(id);
			logcliente = new File("clientlog/Log" + id + "_" + this.toString());
			/**
			crearLlave(); byte[] pubkey = publica.getEncoded(); 
			salida.println(pubkey.length);
			salida.println(pubkey);
			int lS = Integer.parseInt(entrada.readLine());
			byte[] llaveC = new byte[lS];
			principal.getInputStream().read(llaveC, 0, lS);
			X509EncodedKeySpec ks = new X509EncodedKeySpec(llaveC);
			KeyFactory kf = KeyFactory.getInstance(descifrado);
			alterpublica = kf.generatePublic(ks);
			*/
			recibirArchivo();
			System.out.println(id + " Yes!");
			entrada.close();
			salida.close();
			principal.close();
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}
	}
}
