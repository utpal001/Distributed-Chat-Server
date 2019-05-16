1. Requirements:

You need to have netbeans installed in the system.
Since we are using mysql as database, so mysql need to be installed in the system and changes has to be made in Javaconnect.java file present in JavaLibrary1->src->lib1

Open the send folder in netbeans and you are ready to run the project


2. Running the program:


	To start the server
	a. Run the program RMIServer1.java in RMIServer1 folder thrice with values of serverId = {0,1,2} (for other servers).
	b. Run the program RMIMainServer.java in RMIMainServer folder (starts the main server)

	To start the client
	a. Run the program ChatGui.java in Chatgui folder.


	User ID passwords:
	a. userID: sid; passwd: sid
	b. userID: utpal; passwd: utpal
	c. userID: user2; passwd: 123


**Note 
1. the only hardcoded part of the code is line 97 of the RMIMainServer.java to set server IP string.
2. To run a single project multiple times use (shift + F6) command.

