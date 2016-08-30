package nic;

import java.util.Queue;

public class TransmitterReceiver {
	int packetsCount ; 
    int sizeOfQueue ; // Number of packets that can be stored in queue i.e. (BufferSize/1526)
    int numberOfPacketsInQueue ;
    int Tg ;
    int bandWidth ; //bitpersecond
    Boolean hasEntry = false ;
    public TransmitterReceiver(int sizeOfQueue,int Tg , int bandWidth) {
		// TODO Auto-generated constructor stub
    	this.sizeOfQueue = sizeOfQueue ;
    	this.Tg = Tg ;
    	this.bandWidth = bandWidth ;
	} 
    boolean hasFreeBuffer()
    {
    	return packetsCount*1506<sizeOfQueue ;
    }
    void putPacket ()
    {
    	updateCount(1 , false , 0 ) ;
    }
    synchronized void updateCount(int value , Boolean condition , int numberOfPacketsToTransfer )
    {
    	if(!condition)
    	{
    		if(!hasEntry)
    		{
    			hasEntry = true ;
    		}
    		packetsCount += value ;
    		return ;
    	}
    	if(numberOfPacketsToTransfer < packetsCount )
		{
    		System.out.println("Poped " + numberOfPacketsToTransfer + " Out of " + packetsCount) ;
			packetsCount -= numberOfPacketsToTransfer ;
			
		}
		else
		{
			System.out.println("Poped all Out of " + packetsCount) ;
			packetsCount = 0 ;
			hasEntry = false ;
			
		}
    }
    void popPackets()
    {
    	double numberOfPacketsToTransfer = 0 ;
    	double lastQuotent = 0 ;
    	while(true)
    	{
    		if(hasEntry)
    		{
	    		numberOfPacketsToTransfer =  ( ( bandWidth/(1526*8.0)) + (lastQuotent));
	    		lastQuotent = numberOfPacketsToTransfer%1;
	    		updateCount( (int)numberOfPacketsToTransfer , true ,(int)numberOfPacketsToTransfer)  ;
    		}
    		try {
				Thread.sleep(1000+((int)(numberOfPacketsToTransfer * SimulationRunner.TG)/1000));
				numberOfPacketsToTransfer = 0 ;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    
    }
    void printStatus()
    {
    	System.out.println("Messages in TR : "+packetsCount);
    }
}
