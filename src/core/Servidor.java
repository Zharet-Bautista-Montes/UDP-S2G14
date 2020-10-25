package core;

import java.io.*;
import java.net.*;
import java.security.*;
import java.util.*;

public class Servidor 
{
	private static Scanner consola = new Scanner(System.in);

	private static DatagramSocket receptor; 

	private static ArrayList<Conexion> pool;

	private static InetAddress ip;

	private static File archivo;

	private static int idassigner = 0; 

	private static int puerto;

	private static int clients; 

	private static int filedigit;

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
			{	 hash.update(buffer, 0, length);	}
			file.close();
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
			try
			{
				ServerSocket contexter = new ServerSocket(puerto);
				Socket acuerdo = contexter.accept(); contexter.close();
				BufferedReader br = new BufferedReader(new InputStreamReader(acuerdo.getInputStream()));
				String CIP = br.readLine(); int[] portset = new int[clients]; 
				for(int z = 0; z < clients; z++)
					portset[z] = Integer.parseInt(br.readLine());
				
				while(clients >= (idassigner + 1))
				{
					if(pool.size() < clients)
					{
						Conexion actual = new Conexion(CIP, portset[idassigner], receptor, idassigner, archivo, filehash);
						pool.add(actual); actual.start(); idassigner++;
						System.out.println("Clientes en simultáneo: " + pool.size() + " en el puerto " + portset[idassigner-1]);
					}
				}
				br.close(); acuerdo.close();
			}
			catch (Exception e) 
			{	e.printStackTrace();	}
			
			while(pool.size() > 0)
			{
				Conexion s = pool.get(0);
				if(s.hasEnded()) { logservidor.add(s.getReporte()); pool.remove(s); }
			}
		}
	}

	public static void registrarLog()
	{
		File reporteC = new File("serverlog/Prueba_" + idassigner);
		try
		{
			PrintWriter reportador = new PrintWriter(reporteC);
			reportador.println("LOG FOR " + new Date());
			reportador.println("File Name: " + archivo.getName());
			reportador.println("File Size: " + (double) archivo.length()/(Math.pow(1024, 2)) + " MB");
			for(RegistroLog logS : logservidor)
				reportador.print(logS.toString());
			reportador.flush();	reportador.close();
			System.out.println("Archivo log de servidor creado y guardado");
		}
		catch(Exception e)
		{	e.printStackTrace();	}
	}

	public static void main(String[] args) 
	{
		pool = new ArrayList<Conexion>();
		System.out.println("Bienvenido al servidor UDP. Por favor, configure su puerto");
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
			hashing = "MD5"; logservidor = new ArrayList<RegistroLog>();
			try 
			{	
				ip = InetAddress.getLocalHost();
				System.out.println("La dirección IP del servidor es: " + ip.getHostAddress());
				receptor = new DatagramSocket(puerto, ip);
				ejecutar();  receptor.close();
				registrarLog();
			} 
			catch (Exception e) 
			{	e.printStackTrace(); 	}
		}
	}
}
