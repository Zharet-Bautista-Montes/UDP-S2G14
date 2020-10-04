package core;

import java.io.*;
import java.net.Socket;
import java.security.*;

public class Cliente extends Thread
{	
	private Socket principal;
	
	private int id; 
	
	private String ipaddress;
	
	private int port;
	
	private BufferedReader entrada; 
	
	private PrintWriter salida; 
	
	private File requested; 
	
	private String hashing; 
	
	private String descifrado;
	
	private PrivateKey privada;
	
	public Cliente(String ipaddress, int port, String hashing, String descifrado)
	{
		this.ipaddress = ipaddress;
		this.port = port;
		this.hashing = hashing;
		this.descifrado = descifrado;
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
		KeyPairGenerator generator; PublicKey publica = null;
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
	
	private void setID(int id)
	{	this.id = id;	}
	
	public void run() 
	{
		System.out.println("Es läuft");
		try 
		{
			principal = new Socket(ipaddress, port);
			entrada = new BufferedReader(new InputStreamReader(principal.getInputStream()));
			salida = new PrintWriter(principal.getOutputStream(), true);
		} 
		catch (Exception e) 
		{	e.printStackTrace();	} 
	}
}
