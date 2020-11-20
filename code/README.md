# Instructions for testing

Collaborative Intrusion Detection System: Inter-Client Communication Testing with Centralized Server

Testing Instructions:

When running the CIDS, open four terminals on the Linux desktops.  These could be done on different VMs.
1. In the first terminal, execute the command: 

	```java Server```

This command creates the centralized server whereby each Client IDS connects.

2. On each of the other three terminals, run the command:

	```java IDS```

This commands creates an instance of a Client IDS (Intrusion Detection System).  Each client automatically establishes a connection with the centralized server.  IDS opens a GUI with three different panes:	
	- The right-most pane is populated with instances of alert-data that is read in and pre-processed by the respective client.  The data between each of the three clients will be the same.
	- The  left-most pane can be ignored as it will later be used to filter the data in the right-most display pane.
	- The top(center) panel has three buttons: "Create Group", "Join Group", and "Run intrusion detection"

3. Test the functionality of the buttons in GUI
	a. Click on the "Run intrustion detection" button.  Nothing should happen as this functionality has yet to be implemented.
	b. In one of the GUIs, click on "Create Group".  
		+ The user (client1) should be prompted to enter the name of the Group.  Upon which, the user clicks "Ok".
		+ In the terminal from which that instance of IDS is run, a message should be printed in the format "01:XXXX:YYYY" where XXXX is the assigned ID to that client and YYYY is the name of the group.
		+ If, when entering the name of a group, client 1 clicks "Cancel" rather than "Ok", client1 should continuously be prompted until a name is provided.
    c. From a different terminal, i.e. using a different client, click on the "Join Group" button.  This should prompt the user for the name of the group that the client wants to join.  Enter the name that was entered by client1 here.
		+ If client2 types in the name of the group that client1 created, a message will be outputted into client2's terminal under the format "02:XXXX:YYYY" where XXXX is the assigned ID of client2 and YYYY is the name of the group.
	d. After this message is output to the terminal, client1 should receive a popup in his GUI asking to approve client2 into the group.	
		+ From client1's GUI, click "accept"
		+ A message will be outputted in client1's and client2's terminal under the format "03:XXXX:accept" where XXXX is client1's ID.  Client1 will then send a message to the Server telling it to add client2 to the group.  Client1's terminal should print a message using the format "04:XXXX:YYYY:ZZZZ" where XXXX is client1's ID, YYYY is the group name, and ZZZZ is client2's ID.
    e. From a new terminal, i.e. client 3, click the "Join Group" button.
		+ Once again, enter the name of the group that client1 created.  Client1 will be prompted to either accept or deny client3 into the group.
		+ From client1's GUI, click on "deny"
		+ A message will be outputted in client1's and client3's terminal under the format "03:XXXX:deny" where XXXX is client1's ID.  
    f. From a terminal that has yet to be used, i.e. client4, click the "Join Group" button.
		+ When prompted for a group name, input a name that does not match the name of the group that client 1 created.
		+ Since this group does not exist, the Server simply ignores this request.  That is, nothing happens.
