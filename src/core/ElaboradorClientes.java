package core;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class ElaboradorClientes 
{
	private static Scanner parametrizador = new Scanner(System.in);

	private static int totalClients = 0; 

	private static ArrayList<Cliente> encargo; 

	private static String ipaddress;

	private static int port;

	private static String hashing;
	
	public static int contador = 0; 

	public static ArrayList<RegistroLog> logcliente;
	
	public static ArrayList<Integer> assignedports;
	
	private static void registrarLog()
	{
		File reporteC = new File("clientlog/Prueba_" + totalClients);
		try
		{
			PrintWriter reportador = new PrintWriter(reporteC);
			reportador.println("LOG FOR " + new Date());
			reportador.println("File Name: " + logcliente.get(0).getFileName());
			reportador.println("File Size: " + logcliente.get(0).getFileSize() + " MB");
			for(RegistroLog logC : logcliente)
				reportador.print(logC.toString());
			reportador.flush();	reportador.close();
			System.out.println("Archivo log de clientes creado y guardado");
		}
		catch(Exception e)
		{	e.printStackTrace();	}
	}

	public static void main(String[] args) 
	{
		encargo = new ArrayList<Cliente>(); hashing = "MD5";
		System.out.println("Bienvenido al Elaborador. Diga cuántos clientes va a crear");
		totalClients = parametrizador.nextInt(); 
		System.out.println("Por favor, ingrese la dirección IP del servidor");
		ipaddress = parametrizador.next(); 
		System.out.println("Ahora indique el puerto de conexion");
		port = parametrizador.nextInt();		
		assignedports = new ArrayList<Integer>(); assignedports.add(port);
		try 
		{
			Socket acuerdo = new Socket(InetAddress.getByName(ipaddress), port);
			PrintWriter pw = new PrintWriter(acuerdo.getOutputStream(), true);
			pw.println(InetAddress.getLocalHost().getHostName());
			if(totalClients > 0)
			{
				logcliente = new ArrayList<RegistroLog>(); int i = 0;
				while(encargo.size() < totalClients)
				{	
					int u = 0;
					while(u == 0 || assignedports.contains(u))
						u = (int) (49152 + Math.random()*16383);
					assignedports.add(u); pw.println(u);
					Cliente neu = new Cliente(i, ipaddress, port, u, hashing); 
					encargo.add(neu); neu.start(); i++; 
				}
				pw.close(); acuerdo.close();
				while(encargo.size() > 0)
				{
					Cliente c = encargo.get(0);
					if(c.isDone()) { logcliente.add(c.getReporte()); encargo.remove(c); }
				}
				registrarLog();
			}
		}
		catch (Exception e) 
		{	e.printStackTrace(); 	}
	}
}
