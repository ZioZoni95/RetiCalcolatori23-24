import java.time.LocalTime;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


public class GreenHousescheduler {

//classi degli events
    
    private class LightOn implements Runnable{
        public void run(){
            System.out.println("Accendi le luci ");
        }
    }

    private class LightOff implements Runnable{
        public void run(){
            System.out.println("Spegni le luci ");
        }
    }

    private class WaterOn implements Runnable{
        public void run(){
            System.out.println("Apri l'acqua della serra ");
        }
    }

    private class WaterOff implements Runnable{
        public void run(){
            System.out.println("Chiudi l'acqua della serra ");
        }
    }

    private class ThermNight implements Runnable{
        public void run(){
            System.out.println("Setta termostato in modalità notte ");
        }
    }

    private class ThermDay implements Runnable{
        public void run(){
            System.out.println("Setta termostato in modalità giorno ");
        }
    }

    private class Bell implements Runnable{
        public void run(){
            System.out.println("BING BING BING");
        }
    }

    //setting dello scheduler

    class Terminate implements Runnable{
        ScheduledThreadPoolExecutor scheduler;
        public Terminate(ScheduledThreadPoolExecutor sc){
            this.scheduler=sc;
        }
        public void run(){
            System.out.println("Arresto in corso ");
            scheduler.shutdownNow();
        }
    }

    public static void schedule(ScheduledThreadPoolExecutor scheduler, Runnable event, long delay, TimeUnit u){
        scheduler.schedule(event,delay,u);
    }

    public static void repeat(ScheduledThreadPoolExecutor scheduler, Runnable event, long initialDelay, long period, TimeUnit u){
        scheduler.scheduleAtFixedRate(event, initialDelay, period, u);
    }

    //main
    //Da fixare i vari delay initial & period...funziona tutto ma è troppo veloce
    public static void main(String[] args){
        GreenHousescheduler gh = new GreenHousescheduler();
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(15);
        schedule(scheduler,gh.new Terminate(scheduler),6000,TimeUnit.MILLISECONDS);
        repeat(scheduler,gh.new Bell(), 1000, 3000,TimeUnit.MILLISECONDS);
        repeat(scheduler,gh.new LightOn(),0,2000,TimeUnit.MILLISECONDS);
        repeat(scheduler,gh.new LightOff(),2000,2000,TimeUnit.MILLISECONDS);
        repeat(scheduler,gh.new WaterOn(),0,6000,TimeUnit.MILLISECONDS);
        repeat(scheduler,gh.new WaterOff(),300,6000,TimeUnit.MILLISECONDS);
        LocalTime now = LocalTime.now();
        LocalTime morning = LocalTime.of(7,0,0);
        LocalTime night = LocalTime.of(19,0,0);
        Boolean light = now.isAfter(morning) && now.isBefore(night);
        int interval = 86400;
        if(light){
            int seconds = night.toSecondOfDay() - now.toSecondOfDay();
            repeat(scheduler, gh.new ThermDay(), 0,interval,TimeUnit.SECONDS);
            repeat(scheduler, gh.new ThermNight(), seconds , interval, TimeUnit.SECONDS);
        }
        else{
            int seconds = morning.toSecondOfDay() - now.toSecondOfDay();
            repeat(scheduler, gh.new ThermDay(), 0,interval,TimeUnit.SECONDS);
            repeat(scheduler, gh.new ThermNight(), seconds , interval, TimeUnit.SECONDS);
        }
    }
}

