package nic;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
public class ProtocolProcessor {
	volatile Queue<Integer> messageBuffer ;
    volatile int sizeOfQueue ;
    volatile int dataInQueue ; //Sum Of Data In CurrentBuffer 
    long waitedForPP = 0 ;
    long waitedForTR = 0 ;
    int completeInsertionCycles = 0 ;
    ProtocolProcessor( int sizeOfQueue )
    {
    	this.sizeOfQueue = sizeOfQueue ;
    	messageBuffer = new LinkedList <Integer>();
    }
    void addMessages(int numberOfMessages , int [] sizeOfMessages )
    {
    	System.out.println("Generated Message By Lamdba : " + numberOfMessages);
    	long date = 0 ;
    	for (int i = 0 ; i < numberOfMessages ; i++ )
    	{
    		Boolean printMessage = true ; 
    		while ((sizeOfMessages[i]+dataInQueue > sizeOfQueue ))
    		{
    			if ( printMessage)
    			{
    				System.out.println("Added "+i+" messages to PP and now buffer is full so waiting...");
    				printMessage = false ;
    				date = new Date().getTime() ;
    			}
    			
    		}
    		if( date != 0 )
    		{
    			long millisecondPassed = new Date().getTime() - date ;
    			waitedForPP+=millisecondPassed;
    			System.out.println("Waited for PP Buffer Full for " + millisecondPassed + " milliseconds.");
    			date = 0 ;
    		}
    		messageBuffer.add(sizeOfMessages[i]);
    		dataInQueue += sizeOfMessages[i] ;
    	}
    	System.out.println("Added "+numberOfMessages +" to PP") ;
    	completeInsertionCycles++ ;
    }
    long processMessage (TransmitterReceiver tr )
    {
    	if(messageBuffer.isEmpty())
    	{
    		return 0;
    	}
    	double msg = messageBuffer.peek() ;
    	double numberOfPackets = Math.ceil( msg / 1526.0) ;
    	long timeInNS = 0 ;
    	for(int i = 0 ; i < numberOfPackets ; i++ )
    	{
    		timeInNS+=SimulationRunner.PP;
    	}
    	for(int i = 0 ; i < numberOfPackets ; i++ )
    	{
    		long date = 0 ;
    		Boolean printMessage = true ; 
    		while(!tr.hasFreeBuffer())
    		{
    			if ( printMessage)
    			{
    				System.out.println("Added "+i+" packets to TR and now buffer is full so waiting...");
    				printMessage = false ;
    				date = new Date().getTime() ;
    			}
    		}
    		if( date != 0 )
    		{
    			long millisecondPassed = new Date().getTime() - date ;
    			waitedForTR+=millisecondPassed;
    			System.out.println("Waited for TR Buffer for " + ( millisecondPassed ) + " milliseconds.");
    			timeInNS += millisecondPassed * 1000000 ;
    			
    		}
    		tr.putPacket();
    	}
    	System.out.println("PUT to TR : "+(int)numberOfPackets) ;
    	dataInQueue-=messageBuffer.peek();
    	messageBuffer.poll();
    	return timeInNS;
    }
    void printWaitedStatus ()
    {
    	System.out.println("Waited for PP Queue for "+ waitedForPP+ " milliseconds.");
    	System.out.println("Waited for TR Queue for "+ waitedForTR+ " milliseconds.");
    	System.out.println("Complete Insertion Cycles : "+ completeInsertionCycles);
    	System.out.println("Messages in PP : "+messageBuffer.size());
    }
    
}
