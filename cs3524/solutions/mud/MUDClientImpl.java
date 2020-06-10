/*******************************************************************
 * cs3524.solutions.mud.MUDClientImpl                              *
 *******************************************************************/
package cs3524.solutions.mud;

import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.rmi.RemoteException;

/**
 * MUDClientInterface implementation class
 */
public class MUDClientImpl 
	implements MUDClientInterface {

	private String clientName; // Holds the client name
	private String currMud = null; // Holds the name of the MUD the player is currently in
	private MUDServerInterface server; // The server stub variable
	private MUDClientInterface stub; // The client stub variable
	
	/**
	 * Constructor method
	 */
	public MUDClientImpl( String userName, MUDServerInterface serverStub ) {

    		clientName = userName;
    		server = serverStub;
	}
	
	/**
	 * Returns the client (player) name
	 */
	@Override
  	public String getClientName() {

	    	return clientName;
  	}
  	
  	/**
	 * Client stub setter method
	 */
  	@Override
  	public void setStub( MUDClientInterface clientStub ) 
  		throws RemoteException {
  		
  		stub = clientStub;
  	}
	
	/**
	 * Prints a message to the client (player) 
	 * (a callback method used for notifications and broadcasting)
	 */
	@Override
  	public void notify( String msg ) {
  	
    		System.out.println( msg );
  	}
  	
  	/**
	 * Shows a menu to pick from already running MUDs or create your own
	 */	
	@Override
  	public void choice( Scanner userIn ) 
  		throws RemoteException {
  		
		//Print the menu to the client (player) and get the available MUDs list
  		server.printMenu( stub );
		List<String> muds = server.showMuds();
		
  		boolean response; // Used to get response from the server (method return values)
  		Integer mudNum = null; // Integer to act as a player menu choice number
  		
  		while ( mudNum == null ) {
	  		
	  		System.out.print( "\n>> Select an option: " );
					
			try {	
				// Get player input
				mudNum = Integer.parseInt( userIn.nextLine().trim() );
			}
			catch (NumberFormatException e) { // If a non-number is typed in
				
				System.out.println( "Please type in a number!" );
				continue;
			}
			
			if ( mudNum <= muds.size() && mudNum > 0 ) { // When existing server is chosen

				currMud = muds.get( --mudNum ); // Match the index
				
				// Get the server response
				response = server.joinMUD( currMud, stub );
				if ( !response ) mudNum = null; // If negative, prompt for a new choice
			}
			else if ( mudNum == muds.size() + 1 ) { // When the player chooses to create new (own) server
				// Create and join the new MUD
				response = server.joinMUD( "new", stub );
				
				if ( !response ) { // If negative response from the server then prompt for a new option
					mudNum = null;
				}
				else { // Change the current mud
					muds = server.showMuds();
					currMud = muds.get( --mudNum );
				}
					
			}
			else { // on invalid input prompt for new choice

				mudNum = null;
				System.out.println("Invalid input! Please try again...");
			}
		}
	}
	
	/**
	 * Runs the switch statements handling player input commands in the MUD
	 */
	@Override
	public void commandLoop( Scanner userIn ) 
		throws RemoteException {
		
		String playerCmd; // Holds player command input
		System.out.print( "\nType in a 'help' for commands list.\n" );
			
		while ( true ) {
		
			System.out.print( "\n>> Type in a command: " );

			// Get the command to run from console
			playerCmd = (userIn.nextLine().trim()).toLowerCase();
			
			switch ( playerCmd ) {

				case "help": // Print out all the available commands
					System.out.println( "\n=== Commands List ===");
					System.out.println( "\n> 'help' - print commands list"
							+ "\n> 'move' - move player"
							+ "\n> 'take' - pick an item"
							+ "\n> 'drop' - drop an item"
							+ "\n> 'inv' - print out items in inventory"
							+ "\n> 'players' - print out current online players"
							+ "\n> 'msg' - send a message to a player"
							+ "\n> 'join' - join a MUD"
							+ "\n> 'leave' - leave a MUD"
							+ "\n> 'switch' - switch to another MUD (if joined)"
							+ "\n> 'quit' - leave all MUDs and quit server"
							+ "\n> press 'Enter' for current MUD and location details" );
					break;
					
				case "": // Print current MUD world view
					server.view( currMud, clientName );
					break;
				
				case "leave": // Leave the current MUD
					server.leaveMUD( currMud, clientName );
					// Intentional fallthrough
					
				case "switch": // Switch to another already joined MUD
					// Get the list of MUDs the player has already joined
					List<String> joinedMuds = server.showJoined( clientName );
					
					if ( joinedMuds != null ) { // If the player has not left all the previously joined MUDs
					
						Integer indx = null; // Variable to retrieve the correct MUD name to switch to
						int i; // Looping purposes
						
						while ( indx == null ) {
						
							i = 0;
							
							for ( String m : joinedMuds ) { // Iterate over the joined MUDs
								
								System.out.println("(" + (++i) + ") " + m);
							}
							
							System.out.print( "\nPick a MUD to switch to: " );
							
							try {
								// Get player input
								indx = Integer.parseInt( userIn.nextLine().trim() );
							}
							catch (NumberFormatException e) { // If a non-number is typed in
								
								System.out.println( "Please type in a number!" );
								continue;
							}
							
							if ( indx <= joinedMuds.size() && indx > 0 ) { // If the player input number 
							// is within the range of joined MUDs 
								String toMud = joinedMuds.get( --indx ); // Get the name of the MUD 
								// the player wants to switch to
								currMud = toMud;
								System.out.println( "\nYou switched to " + currMud );
							}
							else { // Loop again on incorrect number
							
								indx = null;
								System.out.println( "\nIncorrect number! Try again!" );
							}
						}
						break;		
					}
					// Intentional fallthrough
					
				case "join": // Join a MUD
					choice( userIn );
					break;
					
				case "move": // Move the player to the given location
					System.out.print( "\nCurrent view:\n");
					server.view( currMud, clientName );
					System.out.print( ">> Enter a direction: ");
					playerCmd = (userIn.nextLine().trim()).toLowerCase();
					
					// List of possible directions
					List<String> dirList = new ArrayList<>( List.of("west","north", "south", "east") );

					if ( dirList.contains( playerCmd ) ) { // If the direction given is in the directions list

						server.movePlayer( currMud, clientName, playerCmd );
					}
					else {

						System.out.println( "Invalid direction!" );
					}

					break;
					
				case "take": // Take an item next to you
					server.view( currMud, clientName );					
					System.out.print( "Pick item: ");
					playerCmd = (userIn.nextLine().trim()).toLowerCase();
					server.takeThing( currMud, clientName, playerCmd );
					break;	
				
				case "drop": // Drop an item at the current location
					server.showInventory( currMud, clientName );
					System.out.print( "Drop item: ");
					playerCmd = (userIn.nextLine().trim()).toLowerCase();					
					server.dropThing( currMud, clientName, playerCmd );
					break;
					
				case "inv": // Print out current items in inventory
					server.showInventory( currMud, clientName );
					break;
					
				case "players": // Show all online MUD players
					server.showPlayers( currMud, clientName );
					break;
					
				case "msg": // Send a personal message to a user
					if ( server.showPlayers( currMud, clientName ) ) {
						System.out.print( "\nTo: ");
						String to = userIn.nextLine().trim();
						if ( clientName.equals( to ) ) { // If the player tries to text him/herself
							
							System.out.print( "\nWhy would you text yourself? Go make some friends! :)\n");
							break;
						}
						
						System.out.print( "\nMessage: ");
						String msg = userIn.nextLine().trim(); // Get the player message
						
						if ( msg.isEmpty() ) { // If the message is blank
						
							System.out.print( "\nMessage hint: Try pressing the keyboard buttons next time!\n");
							break;
						}
						
						server.message( currMud , clientName, to, msg ); // Call the server method
					}
					break;
					
				case "quit": // exit command (close resources and exit server)
					System.out.println("\nLeaving all MUDs...\n"); 
					server.leaveAll( clientName );
					userIn.close();
					System.out.println("\nScanner Closed.\n"); 
					System.exit(0);
					break;
					
				default: // If the player input is not a command from the commands list
					System.out.println( "Invalid command!");
					break;
					
			}
		}
	}	
}
