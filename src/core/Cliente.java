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
	
	private File requested; 
	
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
			salida = new PrintWriter(principal.getOutputStream(), true); System.out.println("C");	
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
			//Se puede definir un número en lugar de principal.getReceiveBufferSize()
			byte[] buffer = new byte[principal.getReceiveBufferSize()];
			DataInputStream dis = new DataInputStream(principal.getInputStream());
			String filename = dis.readUTF(); long filesize = dis.readLong();
			FileOutputStream fos = new FileOutputStream(filename); int piece; 
			while((piece = dis.read(buffer)) != -1) fos.write(buffer, 0, piece);
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}				
	}
	
	public void run() 
	{
		try 
		{
			//SYN = 0; salida.println(SYN);
			System.out.println("F");
			this.id = Integer.parseInt(entrada.readLine()); System.out.println(id); 
			crearLlave(); byte[] pubkey = publica.getEncoded(); 
			salida.println(pubkey.length);
			salida.print(pubkey);
			int lS = Integer.parseInt(entrada.readLine());
			byte[] llaveC = new byte[lS];
			principal.getInputStream().read(llaveC, 0, lS);
			X509EncodedKeySpec ks = new X509EncodedKeySpec(llaveC);
			KeyFactory kf = KeyFactory.getInstance(descifrado);
			alterpublica = kf.generatePublic(ks);
			recibirArchivo();
			entrada.close();
			salida.close();
			principal.close();
		} 
		catch (Exception e) 
		{	e.printStackTrace();	}
	}
}
