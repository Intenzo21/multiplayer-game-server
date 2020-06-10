/*******************************************************************
 * cs3524.solutions.mud.MUDServerImpl                              *
 *******************************************************************/
package cs3524.solutions.mud;

import java.rmi.*; 
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * MUDServerInterface implementation class
 */
public class MUDServerImpl
	implements MUDServerInterface {

	private int maxPlayers = 2;	// restrict number of players per server
	private int maxServers = 3;	// restrict number of MUDs which can be created
	
	// Map to hold all the MUDs. Key is a MUD name and value is MUD object (LinkedHashMap to preserve the order of insertion)
	private Map<String, MUD> mudWorlds = new LinkedHashMap<String, MUD>();
	
	// Holds the client name -> MUD names mapping
	private	Map<String, ArrayList<String>> clientMuds = new HashMap<String, ArrayList<String>>();

	// Map to hold all the clients (users) in a MUD ( MUD name -> [client name -> client] )
	private	Map<String, HashMap<String, MUDClientInterface>> mudClients = new HashMap<String, HashMap<String, MUDClientInterface>>();

	// Map client name -> [MUD name -> inventory]
	private Map<String, HashMap<String, ArrayList<String>>> mudInventories = new HashMap<String, HashMap<String, ArrayList<String>>>();

	// Map client name -> [MUD name -> position]
	private Map<String, HashMap<String,String>> mudPositions = new HashMap<String, HashMap<String, String>>(); 
	

	/**
	 * The constructor where we create MUD games on the server
	 */
	public MUDServerImpl()
		throws RemoteException {
		// Default (2x2) and Expanded (3x3) MUDs are initialized and provided in the MUDs menu
		mudWorlds.put("Default (2x2) MUD", new MUD( "servers/default/mymud.edg", "servers/default/mymud.msg", "servers/default/mymud.thg" ));
		mudClients.put("Default (2x2) MUD", new HashMap<String, MUDClientInterface>());
		mudWorlds.put("Expanded (3x3) MUD", new MUD( "servers/expanded/expanded.edg", "servers/expanded/expanded.msg", "servers/expanded/expanded.thg" ));
		mudClients.put("Expanded (3x3) MUD", new HashMap<String, MUDClientInterface>());
		
	}
	
	/**
	 * Returns a list of all online MUD worlds
	 */
	@Override
	public List<String> showMuds() 
		throws RemoteException {
		
		return new ArrayList<>( mudWorlds.keySet() );
	}
	
	/**
	 * Print the menu to the client (player)
	 */
	@Override
	public void printMenu( MUDClientInterface client ) 
		throws RemoteException {
		
		// Get the currently joinable MUDs in a list
		List<String> muds = showMuds();
		client.notify( "\n=== MUD Worlds ===\n" ); // Decoration purposes
		
		int i = 0; // Iteration integer
		for( String m : muds ) {  // Enumerate the MUDs and print them to the player
			
			client.notify("(" + (++i) + ") " + m);
			
			if ( muds.size() == i ){
			
				client.notify("(" + (++i) +") Create a new MUD");
			}	
		}
	}
	
	/**
	 * Returns a list of all MUDs a player has joined
	 */
	@Override
	public List<String> showJoined( String clientName )
		throws RemoteException {
		
		if ( clientMuds.get( clientName ) == null ) 
			return null;
		return new ArrayList<>( clientMuds.get( clientName ) );
	}
	
	/**
	 * Join a MUD server 
	 */
	@Override
	public boolean joinMUD( String mudName, MUDClientInterface client ) 
		throws RemoteException {
		
		// Extract MUD from the hashmap		
		MUD mudWorld = mudWorlds.get( mudName );
		
		// Get the client name	
		String clientName = client.getClientName();

		// Hashmap to hold all client name -> client instance mappings of a MUD
		// To be put into the MUD servers hashmap
		HashMap<String, MUDClientInterface> nameToClient;
		
		// List of all MUDs a player has joined
		List<String> joinedMuds = null;
		
		// Map MUD name -> player location associated with a player
		HashMap<String,String> clientLocs = null;
		
		// Map MUD name -> player inventory associated with a player
		HashMap<String, ArrayList<String>> clientInvs = null;
		
		// Message to notify the client
		String msg;
		
		// If MUD doesn't exist then create a new one
		if ( mudWorld == null ) {

			// Check if maximum number of servers is reached
			if ( mudWorlds.size() >= maxServers ) {
				
				client.notify( "\nMaximum number of MUDs reached!" );
				return false;
			}
			
			// Create the new MUD with the client's name
			mudWorlds.put( clientName + "'s MUD", new MUD( "servers/default/mymud.edg", "servers/default/mymud.msg", "servers/default/mymud.thg" ));
			
			// Map the client's name to the server name they have entered/created
			if ( clientMuds.get( clientName ) == null ) {
			
				clientMuds.put( clientName, new ArrayList<String>() );
			}
			joinedMuds = clientMuds.get( clientName );
			joinedMuds.add( clientName + "'s MUD" );
			
			// The MUD that the user is currently in equals the newly created one
			mudWorld = mudWorlds.get( clientName + "'s MUD" );
			
			// Put the newly created server's name and available user names of 
			// that MUD (just us) to the mudClients map
			nameToClient = new HashMap<>();
			nameToClient.put( clientName, client );
			mudClients.put( clientName + "'s MUD", nameToClient );
			
			if ( mudPositions.get( clientName ) == null ) { // If the MUD is the first one to be joined by the player
				// Create an empty hash map associated with the player's name
				mudPositions.put( clientName, new HashMap<String, String>() );
			}
			// Put the start location of the user to the MUD positions hash map
			clientLocs = mudPositions.get( clientName );
			clientLocs.put( clientName + "'s MUD", mudWorld.startLocation() );
			
			
			if ( mudInventories.get( clientName ) == null ) { // If the MUD is the first one to be joined by the player
				// Create an empty hash map associated with the player's name
				mudInventories.put( clientName, new HashMap<String, ArrayList<String>>() );
			}
			// Set the player's MUD inventory to an empty list 
			clientInvs = mudInventories.get( clientName );
			clientInvs.put( clientName + "'s MUD", new ArrayList<String>() );
			
			// Add the player as a thing in the server
			mudWorld.addThing( mudWorld.startLocation(), "player> " + clientName );
			
			// Message to notify the client
			msg = "\n=== Welcome to " + clientName + "'s MUD ===\n"
					+ "\nYour current location: " + clientLocs.get( clientName + "'s MUD" );
			
			// Notify the client
			client.notify( msg );
			return true;

			
		}
		
		// Get all online players in the given MUD
		Set<String> mudPlayerNames = mudClients.get( mudName ).keySet();
		
		if ( mudPlayerNames.contains( clientName ) ) { // If the player is already in the MUD
		
			client.notify( "You are already in this MUD!" );
			return false;
		}
		
		if ( mudClients.get( mudName ).size() >= maxPlayers ) { // If the maximum players number is reached
			
			client.notify( "\nMaximum players number reached! Join a different MUD or create your own!" );
			return false;
		}
	
		// If the option chosen by the user is one of the already existing servers then
		
		// If the MUD is the first one to be joined by the player
		if ( clientMuds.get( clientName ) == null ) { 
			// Make an empty list associated with the player's name
			clientMuds.put( clientName, new ArrayList<String>() );  
		}
		// Add the newly joined MUD's name to the player's joined MUDs list
		joinedMuds = clientMuds.get( clientName );
		joinedMuds.add( mudName );
		
		// Get clients hashmap of the chosen MUD
		nameToClient = mudClients.get( mudName );

		// Put the new user into the hashmap
		nameToClient.put( clientName, client );
		
		// If the MUD is the first one to be joined by the player
		if ( mudPositions.get( clientName ) == null ) { 
			// Create an empty hash map associated with the player's name
			mudPositions.put( clientName, new HashMap<String, String>() );
		}
		// Put the start location of the user to the MUD positions hash map
		clientLocs = mudPositions.get( clientName );
		clientLocs.put( mudName, mudWorld.startLocation() );
		
		// If the MUD is the first one to be joined by the player
		if ( mudInventories.get( clientName ) == null ) { 
			// Create an empty hash map associated with the player's name
			mudInventories.put( clientName, new HashMap<String, ArrayList<String>>() );
		}
		// Set the player's MUD inventory to an empty list 
		clientInvs = mudInventories.get( clientName );
		clientInvs.put( mudName, new ArrayList<String>() );
		
		// Add the user as a thing in the MUD
		mudWorld.addThing( mudWorld.startLocation(), "player> " + clientName );


		// Message to notify the client
		msg = "\n=== Welcome to " + mudName + " ===\n"
			+ "\nYour current location: " + clientLocs.get( mudName );
		
		// Notify the client
		client.notify( msg );
		
		if ( nameToClient.size() >= 1 ) { // If there are at least 2 players in the MUD
			// Broadcast the arrival of the new player
			msg = "\nPlayer '" + clientName + "' has joined " + mudName + ".";
			broadcast( mudName, msg, clientName );
		}
		
		return true;
	}
				
	/**
	 * Leave a MUD
	 */
	@Override
	public void leaveMUD( String mudName, String clientName )
		throws RemoteException {
		
		// If user does not exist (just in case)
		if ( clientName == null) return;
		
		// Get the MUD from the name
		MUD mudWorld = mudWorlds.get( mudName );
		
		// Holds all client name -> client instance mappings of the MUD
		// To be put into the MUD servers hashmap
		HashMap<String, MUDClientInterface> nameToClient = mudClients.get( mudName );
		
		// Get the client
		MUDClientInterface client = nameToClient.get( clientName );
		
		// Get the current location of the player
		HashMap<String, String> mudLocs = mudPositions.get( clientName );
		String currLocation = mudLocs.get( mudName );
		
		// Get all MUD inventories associated with a player in the server
		// MUD name -> inventory
		HashMap<String, ArrayList<String>> clientInventories = mudInventories.get( clientName );
		
		// Get the current MUD inventory of the player
		ArrayList<String> inventory = clientInventories.get( mudName );
		
		// List of MUDs associated with the given player name
		ArrayList<String> mudList = clientMuds.get( clientName );
		
		// Drop everything from the current MUD inventory and go to the next one
		for ( String t : new ArrayList<>( inventory ) ) { 
		
			dropThing( mudName, clientName, t );
		}
		
		// Delete user records and update changes to MUD
		mudWorld.delThing( currLocation, "player> " + clientName );
		mudWorlds.put( mudName, mudWorld );
		nameToClient.remove( clientName );
		mudClients.put( mudName, nameToClient );
		
	
		// Notify the client
		client.notify( "\nYou have left " + mudName + "!");
		
		if ( nameToClient.size() >= 1 ) { // If there are at least 2 players in the MUD
			// Broadcast the arrival of the new player
			String msg = "\nPlayer '" + clientName + "' has left " + mudName + ".";
			broadcast( mudName, msg, clientName );
		}
		
		// Remove the player's current MUD inventory from the hash
		clientInventories.remove( mudName );
		
		// If there are no inventories associated with the player's name
		if ( clientInventories.isEmpty() ) {  
		
			mudInventories.remove( clientName ); // Remove the name from the MUD inventories hash
		}
		else { // Update the hash otherwise
			mudInventories.put( clientName, clientInventories );
		}
		
		mudList.remove( mudName );
		
		if ( mudList.isEmpty() ) { // If the player has left all the previously joined MUDs
		
			clientMuds.remove( clientName ); // Remove player's name from the joined MUDs hash map
			mudPositions.remove( clientName ); // Remove player's name from the positions hash map
		}
		else { // Else update the hash maps
		
			mudLocs.remove( mudName );
			mudPositions.put( clientName, mudLocs );
			clientMuds.put( clientName, mudList );
		}
		
	}
	
	/**
	 * Leave all joined MUDs (when quitting the server)
	 */
	public void leaveAll( String clientName ) 
		throws RemoteException {
		
		// If user does not exist (just in case)
		if ( clientMuds.get( clientName ) == null ) return;
		
		// Get the list of MUDS of a given client (player)
		ArrayList<String> mudList = clientMuds.get( clientName );
		
		// Iterate over the list and leave all the MUDS
		for ( String mudName : new ArrayList<>(mudList) ) {

			leaveMUD( mudName, clientName );
		}
				
			
	}
				
	/**
	 * Show things at location
	 */
	@Override
	public void view( String mudName, String clientName ) 
		throws RemoteException {
		
		// Get the MUD from the name
		MUD mudWorld = mudWorlds.get( mudName );
		
		// Holds all client name -> client instance mappings of the MUD
		// To be put into the MUD servers hashmap
		HashMap<String, MUDClientInterface> nameToClient = mudClients.get( mudName );
		
		// Get the client
		MUDClientInterface client = nameToClient.get( clientName );
		
		
		// Get the current location of the player
		HashMap<String, String> currMudLoc = mudPositions.get( clientName );
		String currLocation = currMudLoc.get( mudName );
		
		// String to notify the client
		String msg = mudWorld.locationInfo( currLocation );
		
		// Notify the client
		client.notify( "\n=== " + mudName + " ===" );
		client.notify( msg );
	}
	
	/**
	 * Print the menu to the client (player)
	 */
	@Override	
	public boolean movePlayer( String mudName, String clientName, String position ) 
		throws RemoteException {
		
		// Get the MUD from the name
		MUD mudWorld = mudWorlds.get( mudName );

		// Holds all client name -> client instance mappings of the MUD
		// To be put into the MUD servers hashmap
		HashMap<String, MUDClientInterface> nameToClient = mudClients.get( mudName );
		
		// Get the client
		MUDClientInterface client = nameToClient.get( clientName );
		
		// Get the current location of the player
		HashMap<String, String> currMudLoc = mudPositions.get( clientName );
		String currLocation = currMudLoc.get( mudName );

		String msg = mudWorld.moveThing( currLocation, position, "player> " + clientName );
		
		// Update the new player position
		currMudLoc.put( mudName, msg );

		if ( msg.equals( currLocation ) ) { // If the player tries to move to the current location
			
			client.notify( "\nYou are already at that location. Please try different one!\n" );
			return false;
		}
		
		client.notify( "Your new location: " + msg + "\n" ); // Notify the client (player)
		
		if ( nameToClient.size() >= 1 ) { // If there are at least 2 players in the MUD
			// Broadcast the location of the moved player
			msg = "\nPlayer '" + clientName + "' has moved to location '" + msg + "' in " + mudName + ".";
			broadcast( mudName, msg , clientName );
		}
		
		return true;
	}
	
	/**
	 * Pick a nearby item in the MUD
	 */
	@Override
	public boolean takeThing( String mudName, String clientName, String thing )
		throws RemoteException {
		
		// Get the MUD from the name
		MUD mudWorld = mudWorlds.get( mudName );
		
		// Holds all client name -> client instance mappings of the MUD
		// To be put into the MUD servers hashmap
		HashMap<String, MUDClientInterface> nameToClient = mudClients.get( mudName );
		
		// Get the client
		MUDClientInterface client = nameToClient.get( clientName );

		// Get the current location of the player
		HashMap<String, String> currMudLoc = mudPositions.get( clientName );
		String currLocation = currMudLoc.get( mudName );
		
		// Get all MUD inventories associated with a player in the server
		// MUD name -> inventory
		HashMap<String, ArrayList<String>> clientInventories = mudInventories.get( clientName );
		
		// Get the current MUD inventory of the player
		ArrayList<String> inventory = clientInventories.get( mudName );
		
		// Get the nearby things
		List<String> things = mudWorld.locationThings( currLocation );

		if ( thing.contains( "player>" ) ) { // If a player tries to pick another player or him/herself
		
			client.notify( "Sorry, you cannot pick up a player! That is illegal and would be really awkward!" );
			return false;
		}
		else if ( !things.contains( thing ) ) { // If there was no such thing 
		
			client.notify( "Such thing does not exist in the MUD!" );
			return false;
		}
		
		// Remove the thing from the MUD and put it in the player's inventory
		mudWorld.delThing( currLocation, thing );
		inventory.add( thing );
		clientInventories.put( mudName, inventory);
		
		// Notify the player
		client.notify( "\n=== " + mudName + " ==="  );
		client.notify( "You picked up '" + thing + "'." );
		client.notify( "Your current inventory: " + inventory.toString() );
		
		if ( nameToClient.size() >= 1 ) { // If there are at least 2 players in the MUD
			// Broadcast that a thing has been picked up
			String msg = "\nPlayer '" + clientName + "' has picked up '" + thing + "' in " + mudName + ".";
			broadcast( mudName, msg, clientName );
		}
		return true;

	}
	
	/**
	 * Drop an item from the player's inventory at the current location
	 */
	@Override
	public void dropThing( String mudName, String clientName, String thing ) 
		throws RemoteException {
		
		// Get the MUD from the name
		MUD mudWorld = mudWorlds.get( mudName );
		
		// Holds all client name -> client instance mappings of the MUD
		// To be put into the MUD servers hashmap
		HashMap<String, MUDClientInterface> nameToClient = mudClients.get( mudName );
		
		// Get the client
		MUDClientInterface client = nameToClient.get( clientName );

		// Get the current location of the player
		HashMap<String, String> currMudLoc = mudPositions.get( clientName );
		String currLocation = currMudLoc.get( mudName );
		
		// Get all MUD inventories associated with a player in the server
		// MUD name -> inventory
		HashMap<String, ArrayList<String>> clientInventories = mudInventories.get( clientName );
		
		// Get the current MUD inventory of the player
		ArrayList<String> inventory = clientInventories.get( mudName );
		
		if ( inventory.isEmpty() ) { // If the player's inventory is empty
		
			client.notify( "Unlucky! Empty inventory, buddy!" );
			return;
		}
		// If there is no such thing at the current location of the player
		else if ( !inventory.contains( thing ) ) { 
			
			client.notify( "Sorry, you do not have such an item in your inventory!" );
			return;
		}
		
		// Add the thing to the MUD and remove it from the player's inventory
		mudWorld.addThing( currLocation, thing );
		inventory.remove( thing );
		clientInventories.put( mudName, inventory );
		
		// Notify the player
		client.notify( "\n=== " + mudName + " ==="  );
		client.notify( "\nYou dropped '" + thing + "'." );
		client.notify( "Your current inventory: " + inventory.toString() );
		
		if ( nameToClient.size() >= 1 ) { // If there are at least 2 players in the MUD
			// Broadcast that a thing has been picked up
			String msg = "\nPlayer '" + clientName + "' has dropped '" + thing + "' in " + mudName + ".";
			broadcast( mudName, msg, clientName );
		}
	}
			
	/**
	 * Print out current items in the inventory
	 */
	@Override
	public void showInventory( String mudName, String clientName ) 
		throws RemoteException {
		
		// Holds all client name -> client instance mappings of the MUD
		// To be put into the MUD servers hashmap
		HashMap<String, MUDClientInterface> nameToClient = mudClients.get( mudName );
		
		// Get the client
		MUDClientInterface client = nameToClient.get( clientName );
		
		// Get all MUD inventories associated with a player in the server
		// MUD name -> inventory
		HashMap<String, ArrayList<String>> clientInventories = mudInventories.get( clientName );
		// Get the current MUD inventory of the player
		ArrayList<String> inventory = clientInventories.get( mudName );
		
		// Print the inventory items to the player
		client.notify( "Your current inventory: " + inventory.toString() );
	}
	
	/**
	 * Print out currently online players
	 */
	@Override
	public boolean showPlayers( String mudName, String clientName ) 
		throws RemoteException {
		
		// Get the MUD from the name
		MUD mudWorld = mudWorlds.get( mudName );
		
		// Holds all client name -> client instance mappings of the MUD
		// To be put into the MUD servers hashmap
		HashMap<String, MUDClientInterface> nameToClient = mudClients.get( mudName );
		
		// Get the client
		MUDClientInterface client = nameToClient.get( clientName );
		
		String msg; // String to notify the player
		Set<String> players = nameToClient.keySet(); // Get current MUD player names
			
		
		if ( players.size() == 1 ) { // If only 1 player is present (you)
		
			msg = "You are the only player in the MUD!";
			client.notify( msg );
			return false;
		}
		
		msg = "Other online players in the MUD: ";
		for ( String player: players ) {
				
			if ( !player.equals( clientName ) ) msg += "\n\n> " + player;
		}
		
		client.notify( msg );
		return true;
	}
	
	/**
	 * Send a message to an online player
	 */
	@Override
	public void message( String mudName, String clientName, String to, String msg ) 
		throws RemoteException {
		
		// Get the MUD from the name
		MUD mudWorld = mudWorlds.get( mudName );
		
		// Holds all client name -> client instance mappings of the MUD
		// To be put into the MUD servers hashmap
		HashMap<String, MUDClientInterface> nameToClient = mudClients.get( mudName );
		
		// Get the message sender
		MUDClientInterface sender = nameToClient.get( clientName );
		
		if ( !nameToClient.keySet().contains( to ) ) { // If there is not such player in the MUD
			sender.notify( "There is no such player named '" + to + "' in " + mudName + " !" );
			return;
		}
		
		// Get the receiver
		MUDClientInterface receiver = nameToClient.get( to );
		
		// Print the message to the target player
		receiver.notify( "\n[" + clientName + "]: " + msg );
		
		// Notify the sender
		sender.notify( "\nMessage sent!" );
	}
		
	/**
	 * Delivers a message to players when an event occurs in the MUD (broadcasting)
	 */
	@Override
	public void broadcast( String mudName, String msg, String excludeClient )
		throws RemoteException {
		
		// Holds all client name -> client instance mappings of the MUD
		// To be put into the MUD servers hashmap
		HashMap<String, MUDClientInterface> nameToClient = mudClients.get( mudName );
		
		// Loop through the client hash map and broadcast the message
		nameToClient
			.forEach( ( clientName, client ) -> {
				if ( !clientName.equals( excludeClient ) ) { // exclude sender client
					try {
						client.notify( msg );	
					}
					catch ( RemoteException re ) {
						System.out.println("Could not broadcast message! Reason: " + re );
					}
						
				}
			}
		);
	}	
		

}
