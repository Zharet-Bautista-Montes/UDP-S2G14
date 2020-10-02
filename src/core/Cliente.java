package core;

import java.io.*;
import java.net.Socket;
import java.security.*;

public class Cliente 
{	
	private Socket socket;
	
	private String nombre; 
	
	private BufferedReader entrada; 
	
	private PrintWriter salida; 
	
	private static String hashing; 
	
	private static String descifrado;
	
	private static PrivateKey privada;
	
	public static byte[] obtenerHash(String algorithm, String filename)
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
	
	public Key crearLlave()
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
	
	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub

	}

}
