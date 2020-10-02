package core;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.*;
import java.util.Scanner;


public class Servidor 
{
	private static Scanner consola = new Scanner(System.in);

	private static ServerSocket receptor; 

	private static int puerto;

	private static int clients; 

	private static int filedigit;

	private static String cifrado;
	
	private static String hashing; 

	private static PrivateKey privada;
	
	private static PublicKey alterpublica; 

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

	public static Key crearLlave()
	{
		KeyPairGenerator generator; PublicKey publica = null;
		try 
		{
			generator = KeyPairGenerator.getInstance(cifrado);
			generator.initialize(1024);
			KeyPair par = generator.generateKeyPair();
			publica = par.getPublic();
			privada = par.getPrivate(); 
		} 
		catch (NoSuchAlgorithmException e) 
		{	e.printStackTrace();	}
		return publica;
	}

	public static void transferir()
	{ 
		String fileloc = "";
		if (filedigit==1) fileloc = "prooffiles/Tarea_HTML.txt";
		else if (filedigit==2) fileloc = "prooffiles/Tarea_HTML.txt";
		File archivo = new File(fileloc);
		if(archivo != null)
		{
			byte[] filehash = obtenerHash(hashing, fileloc); 
		}
	}

	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		System.out.println("Bienvenido al servidor TCP. Por favor, configure su puerto");
		puerto = consola.nextInt(); 
		System.out.println("Perfecto. Diga a cuántos clientes va a conectarse el servidor");
		clients = consola.nextInt(); boolean wrong = true;
		System.out.println("Ahora defina el archivo que desea que el servidor transfiera: ");
		while(wrong)
		{	
			System.out.println("1 para el de 100 MB y 2 para el de 250 MB");
			filedigit = consola.nextInt();
			wrong = (filedigit == 1 || filedigit == 2) ? false : true;
		}
		cifrado = "RSA"; hashing = "MD5";
		try 
		{	
			InetAddress ip = InetAddress.getLocalHost();
			System.out.println("La dirección IP del servidor es: " + ip.toString());
			receptor = new ServerSocket();
			crearLlave();
			transferir();
		} 
		catch (Exception e) {	e.printStackTrace(); }
	}
}
