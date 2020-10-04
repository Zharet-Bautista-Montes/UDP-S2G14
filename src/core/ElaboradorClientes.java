package core;

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

	public static void main(String[] args) 
	{
		encargo = new ArrayList<Cliente>(); hashing = "MD5"; descifrado = "RSA";
		System.out.println("Bienvenido al Elaborador. Diga cuántos clientes va a crear");
		totalClients = parametrizador.nextInt(); 
		System.out.println("Por favor, ingrese la dirección IP del servidor");
		ipaddress = parametrizador.next(); 
		System.out.println("Ahora indique el puerto de conexion");
		port = parametrizador.nextInt();
		while(encargo.size() < totalClients)
		{
			Cliente neu = new Cliente(ipaddress, port, hashing, descifrado);
			encargo.add(neu); //neu.start();
		}
	}
}
