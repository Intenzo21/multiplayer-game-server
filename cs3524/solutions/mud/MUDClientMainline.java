/*******************************************************************
 * cs3524.solutions.mud.MUDClientMainline                          *
 *******************************************************************/
package cs3524.solutions.mud;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.lang.SecurityManager;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;

/**
 * MUDClientMainline class 
 */
public class MUDClientMainline {
	
	public static void main(String args[]) {
		if (args.length < 3) {
			System.err.println( "Usage:\njava MUDClientMainline <registryhost> <registryport> <callbackport>" ) ;
			return;
		}

		try {
			
			// Extract the hostname, registry port and callback port from the command line
			String hostname = args[0];
			int registryport = Integer.parseInt( args[1] ) ;
			int callbackport = Integer.parseInt( args[2] ) ;

			// Specify the security policy and set the security manager.
			System.setProperty( "java.security.policy", "mud.policy" ) ;
			System.setSecurityManager( new SecurityManager() ) ;
			
			// Obtain the server handle from the RMI registry
			// listening at hostname:port.	
			String regURL = "rmi://" + hostname + ":" + registryport + "/MUDServer";
			MUDServerInterface serverStub = ( MUDServerInterface )Naming.lookup( regURL );
			
			// Prompt for name
			Scanner userIn = new Scanner(System.in); // Using Scanner to read user input
			String clientName = ""; // To hold the client name 
			while ( clientName.isEmpty() ) { // If no name is provided then prompt again
			
				System.out.print( "\n>> Your name: " );
				clientName = userIn.nextLine().trim();
			}
			
			MUDClientImpl newClient = new MUDClientImpl( clientName, serverStub ); // Create the client instance
			MUDClientInterface clientStub = ( MUDClientInterface )UnicastRemoteObject.exportObject( newClient, callbackport ); // Get the client stub
			newClient.setStub( clientStub ); // Set the client stub
			
			

			// Prompt the menu to pick from already created MUDs or create your own
			newClient.choice( userIn );
			
			// Shutdown hook to handle the cases where the JVM stops unexpectedly.
			// For example, when CTRL+C key combination is pressed
			Runtime.getRuntime().addShutdownHook(new Thread() { 
			
				public void run() {
					try { // Leave all muds and close all resources 
						serverStub.leaveAll( clientStub.getClientName() );
						// userIn.close(); // Requires 'Enter' key press
						// System.out.println("\nScanner Closed.\n"); 
					}
					
					catch ( RemoteException re ) {
						System.out.println("\nServer side error.\n"); 
					}
					return;
				}
			});
				
			newClient.commandLoop( userIn ); // Call the MUD command loop on the client (player)
			
		}
		catch (java.io.IOException e) {
		    System.err.println( "I/O error." );
		    System.err.println( e.getMessage() );
		}
		catch (java.rmi.NotBoundException e) {
		    System.err.println( "Server not bound." );
		    System.err.println( e.getMessage() );
		}
    	}
}
