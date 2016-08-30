package nic;

import java.util.Date;
import java.util.Random;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.random.RandomGenerator;

public class SimulationRunner {
	
	public static int lambdaLowerLimit = 100 ;
	public static int lambdaUperLimit = 7000 ;
	public static int L = 32000 ; //Bytes
	
	
	public static int bandwidth = 800000 ;//Bits Per Second
	public static int TG = 1 ; //microsecond
	public static int PP = 2 ; //nanosecond
	
	
	public static int LPP = 256000 ;//Bytes
	public static int LTR = 256000 ;//Bytes
	public static int runTime = 60 ; //seconds
	
	//LPP LTR bandwidth runTime
	public static void main ( String args [] )
	{
		if(args.length == 9)
		{
			LPP = Integer.parseInt(args[0]);
			LTR = Integer.parseInt(args[1]);
			bandwidth = Integer.parseInt(args[2]);
			runTime =  Integer.parseInt(args[3]); 
			lambdaLowerLimit = Integer.parseInt(args[4]); 
			lambdaUperLimit = Integer.parseInt(args[5]); 
			L = Integer.parseInt(args[6]); 
			TG = Integer.parseInt(args[7]); 
			PP = Integer.parseInt(args[8]); 
		}
		if(LPP < 64000)
		{
			System.out.println("The PP buffer size should atleast carry one full message") ;
			return ;
		}
		
		ProtocolProcessor pp = new ProtocolProcessor ( LPP );
		TransmitterReceiver tr = new TransmitterReceiver (LTR ,TG ,bandwidth);
		Thread ppr = new Thread (new PPRecieverThread(pp)); 
		Thread pps = new Thread (new PPSenderThread(pp,tr));
		Thread trs = new Thread (new TRSenderThread(tr));
		ppr.start();
		pps.start();
		trs.start();
		try {
			Thread.sleep(runTime * 1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		//Print Status
		pp.printWaitedStatus();
		tr.printStatus();
		//Stop Threads
		ppr.stop();
		pps.stop();
		trs.stop();
		
	}
	public static class PPRecieverThread implements Runnable
	{
		ProtocolProcessor pp = null ;
		Random random = null ;
		PPRecieverThread(ProtocolProcessor pp )
		{
			this.pp = pp ;
			random = new Random();
			random.setSeed(new Date().getTime());
		}
		public void run() {
			// TODO Auto-generated method stub
			while (true)
			{
				
				
				ExponentialDistribution size = new ExponentialDistribution(L) ;
				//Generate the count of messages between upper and lower limit
				int values = random.nextInt((lambdaUperLimit-lambdaLowerLimit+1)) + lambdaLowerLimit;
				int [] sizes = new int[values] ;
				for(int i = 0 ; i < values; i ++ )
				{
					sizes[i] = (int)size.sample() ;
					if(sizes[i] > 64000)
					{
						sizes[i] = 64000 ;
					}
				}
				
				//Add Messages to the PP 
				pp.addMessages(values, sizes);
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Thread.currentThread().interrupt();
				}
				
			}
		}
	}
	public static class PPSenderThread implements Runnable
	{
		ProtocolProcessor pp = null ;
		TransmitterReceiver tr = null ;
		PPSenderThread(ProtocolProcessor pp ,TransmitterReceiver tr )
		{
			this.pp = pp ;
			this.tr = tr ;
		}
		public void run() {
			// TODO Auto-generated method stub
			long time = 0 ;
			while(true)
			{
				
				time+=pp.processMessage(tr);
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if(time > 1000000000)
				{
					try {
						Thread.sleep(time/1000000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}
	
	public static class TRSenderThread implements Runnable
	{
		TransmitterReceiver tr = null ;
		TRSenderThread(TransmitterReceiver tr )
		{
			this.tr = tr ;
		}
		public void run() {
			// TODO Auto-generated method stub
			tr.popPackets();
		}
	}
	
}
