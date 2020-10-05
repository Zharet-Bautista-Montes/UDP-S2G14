package core;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;
public class Servidor 
{
	private static Scanner consola = new Scanner(System.in);

	private static ServerSocket receptor; 

	private static ArrayList<Conexion> pool;

	private static InetAddress ip;
	
	private static File archivo;

	private static int idassigner = 0; 

	private static int puerto;

	private static int clients; 

	private static int filedigit;

	private static String cifrado;

	private static String hashing;

	private static ArrayList<RegistroLog> logservidor; 

	private static byte[] obtenerHash(String algorithm, String filename)
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

	public static void ejecutar()
	{
		String fileloc = ""; byte[] filehash = null;
		if (filedigit==1) fileloc = "prooffiles/Electrocardiograma funcional.txt";
		else if (filedigit==2) fileloc = "prooffiles/Tarea_HTML.txt";
		archivo = new File(fileloc);
		if(archivo != null)
		{	
			filehash = obtenerHash(hashing, fileloc);	
			while(clients >= (idassigner + 1))
			{
				try 
				{
					if(pool.size() < clients)
					{
						//String orden = consola.next();
						//if(orden.equals("STOP")) forzarTerminacion();
						Socket newconn = receptor.accept();
						Conexion actual = new Conexion(newconn, idassigner, archivo, filehash, cifrado, logservidor);
						pool.add(actual); actual.start(); idassigner++;
					}					
				} 
				catch (IOException e) 
				{	e.printStackTrace();	}

			}
		}
	}
	
	public static void registrarLog()
	{
		File reporteC = new File("serverlog/Prueba_" + logservidor.get(0).getDate());
		try
		{
			PrintWriter reportador = new PrintWriter(reporteC);
			reportador.println(new Date() + " REPORT");
			reportador.println("File Name: " + archivo.getName());
			reportador.println("File Size: " + archivo.length() + " MB");
			for(RegistroLog logS : logservidor)
				reportador.print(logS.toString());
			reportador.flush();	reportador.close();
		}
		catch(Exception e)
		{	e.printStackTrace();	}
	}

	public static void forzarTerminacion()
	{	idassigner = clients;	}

	public static void main(String[] args) 
	{
		// TODO Auto-generated method stub
		pool = new ArrayList<Conexion>();
		System.out.println("Bienvenido al servidor TCP. Por favor, configure su puerto");
		puerto = consola.nextInt(); 
		System.out.println("Perfecto. Diga a cuántos clientes en simultáneo va a conectarse el servidor");
		clients = consola.nextInt(); boolean wrong = true;
		System.out.println("Ahora defina el archivo que desea que el servidor transfiera: ");
		while(wrong)
		{	
			System.out.println("1 para el de 100 MB y 2 para el de 250 MB");
			filedigit = consola.nextInt();
			wrong = (filedigit == 1 || filedigit == 2) ? false : true;
		}
		if(clients > 0)
		{
			cifrado = "RSA"; hashing = "MD5"; logservidor = new ArrayList<RegistroLog>();
			try 
			{	
				ip = InetAddress.getLocalHost();
				System.out.println("La dirección IP del servidor es: " + ip.toString());
				receptor = new ServerSocket(puerto);
				System.out.println("Si en algún momento desea detener el servidor, solo ingrese STOP");
				ejecutar();
				//registrarLog();
			} 
			catch (Exception e) 
			{	e.printStackTrace(); 	}
		}
	}
}
