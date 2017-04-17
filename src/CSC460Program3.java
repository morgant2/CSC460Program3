import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * Created by Tommy on 4/16/2017.
 */
public class CSC460Program3 {
    static Semaphore wMutex = new Semaphore(1);
    static Semaphore rMutex = new Semaphore(1);
    static Semaphore wantIn = new Semaphore(1);

    static Semaphore writersWaiting = new Semaphore(0);

    static AtomicInteger readCount = new AtomicInteger(0);
    static AtomicInteger writeCount = new AtomicInteger(0);

    private static final int READERS = 50;
    private static final int WRITERS = 50;

    public static void main(String[] args)
    {
        Thread[] readerArray = new Thread[READERS];
        Thread[] writerArray = new Thread[WRITERS];

        for (int i = 0; i < READERS; i++) {
            readerArray[i] = new Thread(new Read());
            readerArray[i].setName("Reader Thread " + i);
            readerArray[i].start();
        }

        for (int i = 0; i < WRITERS; i++) {
            writerArray[i] = new Thread(new Write());
            writerArray[i].setName("Writer Thread " + i);
            writerArray[i].start();
        }

    }

    static class Read implements Runnable
    {
        @Override
        public void run()
        {
//            do {
                try{
                    rMutex.acquire();
                	readCount.getAndIncrement();
                    
                    rMutex.release();
                    wMutex.acquire();

                    if(writeCount.get() > 0)
                    {
                        wMutex.release();
                        writersWaiting.acquire();
                    }
                    else
                    {
                        wMutex.release();
                    }

                    //Perform Read
                    System.out.println(Thread.currentThread().getName() + " is reading");
                    Thread.sleep(1000);
                    System.out.println(Thread.currentThread().getName() + " is finished reading");
                    
                    rMutex.acquire();
                    wMutex.acquire();
                	readCount.getAndDecrement();

                    if((readCount.get() ==0) && (writeCount.get() > 0))
                    {
                        wantIn.release();
                        rMutex.release();
                        wMutex.release();
                    }
                    else
                    {
                        rMutex.release();
                        wMutex.release();
                    }
                }
                catch (InterruptedException ex)
                {
                    System.out.println(ex.getMessage());
                }
//            } while (true);
        }
    }

    static class Write implements Runnable
    {
        @Override
        public void run()
        {
//            do {
                try {
                    wMutex.acquire();
                    writeCount.getAndIncrement();
                    wMutex.release();

                    if (writeCount.get() > 1 || readCount.get() > 0) {
                        wantIn.acquire();
                    }

                    //Perform Write
                    System.out.println(Thread.currentThread().getName() + " is writing");
                    Thread.sleep(2000);
                    System.out.println(Thread.currentThread().getName() + " is finished writing");

                    wMutex.acquire();
                    writeCount.getAndDecrement();

                    if (writeCount.get() > 0) {
                        wantIn.release();
                    } else {
                        rMutex.acquire();
                        while (readCount.get() > 0) {
                            writersWaiting.release();
                        	readCount.getAndDecrement();
                        }
                    }

                    wMutex.release();
                    rMutex.release();
                } catch (InterruptedException ex) {
                    System.out.println(ex.getMessage());
                }
//            }while(true);
        }
    }



}
