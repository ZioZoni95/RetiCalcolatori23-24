package esercizio1_2;
import java.util.Calendar;

public class DataPrinterThread extends Thread{
    public static void main(String[] args){
        DataPrinterThread th = new DataPrinterThread();
        th.start();
        System.out.println(Thread.currentThread().getName());
    }

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
}
