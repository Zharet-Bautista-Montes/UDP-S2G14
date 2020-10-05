package core;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class ElaboradorClientes 
{
	private static Scanner parametrizador = new Scanner(System.in);

	private static int totalClients = 0; 

	private static ArrayList<Cliente> encargo; 

	private static String ipaddress;

	private static int port;

	private static String hashing; 

	private static String descifrado;

	private static ArrayList<RegistroLog> logcliente;
	
	private static void registrarLog()
	{
		File reporteC = new File("clientlog/Prueba_" + logcliente.get(0).getDate());
		try
		{
			PrintWriter reportador = new PrintWriter(reporteC);
			reportador.println(logcliente.get(0).getDate() + " REPORT");
			reportador.println("File Name: " + logcliente.get(0).getFileName());
			reportador.println("File Size: " + logcliente.get(0).getFileSize() + " MB");
			for(RegistroLog logC : logcliente)
				reportador.print(logC.toString());
			reportador.flush();	reportador.close();
		}
		catch(Exception e)
		{	e.printStackTrace();	}
	}

	public static void main(String[] args) 
	{
		encargo = new ArrayList<Cliente>(); hashing = "MD5"; descifrado = "RSA";
		System.out.println("Bienvenido al Elaborador. Diga cuántos clientes va a crear");
		totalClients = parametrizador.nextInt(); 
		System.out.println("Por favor, ingrese la dirección IP del servidor");
		ipaddress = parametrizador.next(); 
		System.out.println("Ahora indique el puerto de conexion");
		port = parametrizador.nextInt();
		if(totalClients > 0)
		{
			logcliente = new ArrayList<RegistroLog>();
			while(encargo.size() < totalClients)
			{
				Cliente neu = new Cliente(ipaddress, port, hashing, descifrado, logcliente);
				encargo.add(neu); neu.start();
			}
			//registrarLog();
		}
	}
}
