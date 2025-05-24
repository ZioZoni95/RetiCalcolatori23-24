package LabInformatica;
import java.util.Random;

/**
 * Il laboratorio di Informatica del Polo Marzotto è utilizzato da tre tipi di utenti, studenti, tesisti e professori ed ogni utente deve fare una richiesta al tutor per accedere al laboratorio.
 * I computers del laboratorio sono numerati da 1 a 20. Le richieste di accesso sono diverse a seconda del tipo dell'utente:
 *
 * - i professori accedono in modo esclusivo a tutto il laboratorio, poichè hanno necessità di utilizzare tutti i computers per effettuare prove in rete.
 * - i tesisti richiedono l'uso esclusivo di un solo computer, identificato dall'indice i, poichè su quel computer è installato un particolare software necessario per lo sviluppo della tesi.
 * - gli studenti richiedono l'uso esclusivo di un qualsiasi computer.
 * I professori hanno priorità su tutti nell'accesso al laboratorio, i tesisti hanno priorità sugli studenti.
 *
 * Nessuno può essere interrotto mentre sta usando un computer. Scrivere un programma JAVA che simuli il comportamento degli utenti e del tutor.
 * Il programma riceve in ingresso il numero di studenti, tesisti e professori che utilizzano il laboratorio ed attiva un thread per ogni utente.
 * Ogni utente accede k volte al laboratorio, con k generato casualmente. Simulare l'intervallo di tempo che intercorre tra un accesso ed il successivo e l'intervallo di permanenza in laboratorio mediante il metodo sleep.
 * Il tutor deve coordinare gli accessi al laboratorio. Il programma deve terminare quando tutti gli utenti hanno completato i loro accessi al laboratorio.
 */


public class LabInfMain {
    private static final int n_Computer_LabZon1 = 20;

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: MainLaboratorio numStudenti numTesisti numProfessori \\n\"\n" +
                    "                    + \"\\tnumstudenti \\t numero di studenti che accedono al laboratorio \\n \"\n" +
                    "                    + \"\\tnumTesisti \\t numero di tisti che accedono al laboratorio\\n\"\n" +
                    "                    + \"\\tnumProfessori \\tnumero di professori che accedono al laboratorio\\n\"\n" +
                    "                    + \"\\n\\nExample: MainLaboratorio 10 5 2.\"");
            System.exit(1);
        }
        int nStudent = 0;
        int nProfessor = 0;
        int nTex = 0;

        nStudent = Integer.parseInt(args[0]);
        nTex = Integer.parseInt(args[1]);
        nProfessor = Integer.parseInt(args[2]);

        Lab_zon1 lab_z1 = new Lab_zon1(n_Computer_LabZon1);
        Tutor t = new Tutor(lab_z1);

        //crea e avvia gli studenti
        for (int i = 0; i < nStudent; i++) {
            new Thread(new Student(i, t)).start();
        }
        //crea e avvia i professori
        for (int i = 0; i < nProfessor; i++) {
            new Thread(new Professor(i, t)).start();
        }
        //crea e avvia i tesisti
        for (int i = 0; i < nTex; i++) {
            Random rand = new Random();
            int randPc = rand.nextInt(n_Computer_LabZon1);
            new Thread(new Tesista(i,t,randPc)).start();
        }
    }
}
