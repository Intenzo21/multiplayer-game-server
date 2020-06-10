/*******************************************************************
 * cs3524.solutions.mud.MUDClientInterface                          *
 *******************************************************************/
package cs3524.solutions.mud;

import java.util.List;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Scanner;

/**
 * MUD game client interface class (almost all tasks are carried out on the server side)
 */
public interface MUDClientInterface 
	extends Remote {
	
	/**
	 * Returns the client (player) name
	 */
	public String getClientName() 
		throws RemoteException;
	
	/**
	 * Client stub setter method
	 */
	public void setStub( MUDClientInterface clientStub ) 
  		throws RemoteException;
	
	/**
	 * Prints a message to the client (player) 
	 * (a callback method used for notifications and broadcasting)
	 */	
	public void notify( String message ) 
		throws RemoteException;
	
	/**
	 * Shows a menu to pick from already running MUDs or create your own
	 */
	public void choice( Scanner userIn )
		throws RemoteException;
	
	/**
	 * Runs the switch statements handling player input commands in the MUD
	 */
	public void commandLoop( Scanner userIn ) 
		throws RemoteException;
	
}
