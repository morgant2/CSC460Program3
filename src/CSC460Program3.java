import java.util.concurrent.Semaphore;
/**
 * Created by Tommy on 4/16/2017.
 */
public class CSC460Program3 {
    static Semaphore wMutex = new Semaphore(1);
    static Semaphore rMutex = new Semaphore(1);
    static Semaphore wantIn = new Semaphore(1);

    static Semaphore writersWaiting = new Semaphore(0);

    static int readCount = 0;
    static int writeCount = 0;

    private static final int READERS = 5;
    private static final int WRITERS = 3;

    public static void main(String[] args)
    {
        Thread[] readerArray = new Thread[READERS];
        Thread[] writerArray = new Thread[WRITERS];

        for (int i = 0; i < READERS; i++) {
            readerArray[i] = new Thread(new Read("Thread " + i));
            readerArray[i].start();
        }

        for (int i = 0; i < WRITERS; i++) {
            writerArray[i] = new Thread(new Write("Thread " + i));
            writerArray[i].start();
        }

        System.out.println("Done");
    }

    static class Read implements Runnable
    {
        @Override
        public void run()
        {
            do {
                try{
                    rMutex.acquire();
                    readCount++;
                    rMutex.release();

                    wMutex.acquire();

                    if(writeCount > 0)
                    {
                        wMutex.release();
                        writersWaiting.acquire();
                    }
                    else
                    {
                        wMutex.release();
                    }

                    //Perform Read

                    rMutex.acquire();
                    wMutex.acquire();
                    readCount--;

                    if((readCount ==0) && (writeCount > 0))
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
            } while (true);


        }
    }

    static class Write implements Runnable
    {
        @Override
        public void run()
        {
            do {


                try {
                    wMutex.acquire();
                    writeCount++;
                    wMutex.release();

                    if (writeCount > 1 || readCount > 0) {
                        wantIn.acquire();
                    }

                    //Perform Write

                    wMutex.acquire();
                    writeCount--;

                    if (writeCount > 0) {
                        wantIn.release();
                    } else {
                        rMutex.acquire();
                        while (readCount > 0) {
                            writersWaiting.release();
                            readCount--;
                        }
                    }

                    wMutex.release();
                    rMutex.release();
                } catch (InterruptedException ex) {
                    System.out.println(ex.getMessage());
                }
            }while(true);
        }
    }



}
