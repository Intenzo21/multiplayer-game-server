/*******************************************************************
 * cs3524.solutions.mud.MUDServerMainline                          *
 *******************************************************************/
package cs3524.solutions.mud;

import java.rmi.Naming;
import java.lang.SecurityManager;
import java.rmi.server.UnicastRemoteObject;
import java.net.InetAddress;

/**
 *  MUDServerMainline class to register remote objects with the rmiregistry
 */
public class MUDServerMainline {

    	public static void main(String args[]) {

		if (args.length < 2) {
			System.err.println( "Usage:\njava MUDServerMainline <registryport> <serverport>" ) ;
			return;
		}

		try {
			String hostname = (InetAddress.getLocalHost()).getCanonicalHostName() ;
			int registryport = Integer.parseInt( args[0] ) ;
			int serverport = Integer.parseInt( args[1] );

			// Grant Java Virtual Machine permission to contact DNS servers and 
			// interact with other JVMs via Internet protocols.
			System.setProperty( "java.security.policy", "mud.policy" ) ;
			System.setSecurityManager( new SecurityManager() ) ;

			// Incidentally, you can find out what system properties there
			// are in your JVM by doing this:
			// System.getProperties().list( System.out );

			// Generate the remote object(s) that will reside on this
			// server.
			MUDServerImpl mudServ = new MUDServerImpl();
			MUDServerInterface mudStub = (MUDServerInterface)UnicastRemoteObject.exportObject( mudServ, serverport );
			
			String regURL = "rmi://" + hostname + ":" + registryport + "/MUDServer";
			System.out.println("Registering " + regURL );
			Naming.rebind( regURL, mudStub );

			// Note the server will not shut down!
		}
		catch(java.net.UnknownHostException e) {
			System.err.println( "Cannot get local host name." );
			System.err.println( e.getMessage() );
		}
		catch (java.io.IOException e) {
			System.err.println( "Failed to register." );
			System.err.println( e.getMessage() );
		}
	}
}
