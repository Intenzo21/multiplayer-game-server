/*******************************************************************
 * cs3524.solutions.mud.MUDServerInterface                         *
 *******************************************************************/
package cs3524.solutions.mud;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * MUD game server interface class
 */
public interface MUDServerInterface 
	extends Remote {
			
	/**
	 * Show available MUD worlds
	 */
	public List<String> showMuds() 
		throws RemoteException;
	
	/**
	 * Print the MUDs menu to the player
	 */
	public void printMenu( MUDClientInterface client ) 
		throws RemoteException;
	
	/**
	 * Print out all MUD names a player has joined
	 */
	public List<String> showJoined( String clientName )
		throws RemoteException;
	
	/**
	 * Join a MUD world
	 */
	public boolean joinMUD( String mudName, MUDClientInterface client ) 
		throws RemoteException;
	
	/**
	 * Leave a MUD world
	 */
	public void leaveMUD( String mudName, String clientName ) 
		throws RemoteException;

	/**
	 * Leave all MUD worlds a client (player) has joined (used when quitting)
	 */
	public void leaveAll( String clientName )
		throws RemoteException;

	/**
	 * Show things at location
	 */
	public void view( String mudName, String clientName ) 
		throws RemoteException;
	
	/**
	 * Move the client (player)
	 */
	public boolean movePlayer( String mudName, String clientName, String position ) 
		throws RemoteException;
	
	/**
	 * Pick a nearby thing in the MUD
	 */
	public boolean takeThing( String mudName, String clientName, String thing ) 
		throws RemoteException;
	
	/**
	 * Drop an item from the player's inventory at the current location
	 */
	public void dropThing( String mudName, String clientName, String thing ) 
		throws RemoteException;

	/**
	 * Show collected items so far
	 */
	public void showInventory( String mudName, String clientName ) 
		throws RemoteException;
	
	/**
	 * Show online clients (players)
	 */
	public boolean showPlayers( String mudName, String clientName ) 
		throws RemoteException;
	
	/**
	 * Send a message to a client (player)
	 */
	public void message( String mudName, String clientName, String to, String message ) 
		throws RemoteException;
	
	/**
	 * Delivers a message to players when an event occurs in the MUD (broadcasting)
	 */
	public void broadcast( String mudName, String msg, String excludeClient )
		throws RemoteException;

}
