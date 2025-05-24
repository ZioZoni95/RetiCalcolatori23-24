import java.util.Calendar;
public class DataPrinterRunnable implements Runnable{
    @Override
    public void run(){
        while(true){
            System.out.println(Calendar.getInstance().getTime());
            System.out.println(Thread.currentThread().getName());
            try{
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                System.out.println("Thread Interrotto");
                return;
            }
        }
    }

    public static void main(String[] args){
        DataPrinterRunnable dt_Run = new DataPrinterRunnable();
        Thread dt_Thread = new Thread(dt_Run);
        dt_Thread.start();
        System.out.println(Thread.currentThread().getName());
    }
}
