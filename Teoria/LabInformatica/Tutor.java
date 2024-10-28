package LabInformatica;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

/**
 * Tutor modella il tutor di un laboratorio. Si occupa delle gestioni degli accessi ad esso.
 */
public class Tutor {
    private final Lab_zon1 lab;
    final private ReentrantLock lockLab;
    /**
     * variabile di condizione per l'attesa dei professori
     */
    final private Condition profWait;
    /**
     * array di variabili di condizione per l'attesa dei tesisti
     */
    final private Condition[] texWait;
    /**
     * variabile di condizione per l'attesa degli studenti
     */
    final private Condition studentWait;

    /**
     * @param l laboratorio la cui gestione si vuole assegnare il tutor
     */
    public Tutor(Lab_zon1 l) {
        this.lab = l;
        this.lockLab = new ReentrantLock();
        this.profWait = lockLab.newCondition();

        int n_comp = lab.getNcomputers();
        this.texWait = new Condition[n_comp];
        for (int i = 0; i < n_comp; i++) {
            this.texWait[i] = lockLab.newCondition();
        }
        this.studentWait = lockLab.newCondition();
    }

    /**
     * effettua una richiesta di accesso al laboratorio da parte del professore p
     *
     * @param p professore
     */
    public void profAceessReq(Professor p) {
        lockLab.lock();
        ;
        try {
            while (!lab.isFree()) {
                System.out.printf("Professor %d waiting for lab\n", p.getMatricola());
                profWait.wait();
            }
            lab.setOccupyAll();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lockLab.unlock();
        }
    }

    /**
     * avvisa il tutor che il professore hav liberato il laboratorio
     *
     * @param p professor che libera il lab
     */
    public void profReleaseLab(Professor p) {
        lockLab.lock();

        //rilascia tutti i pc del lab
        lab.setFreeAll();

        //controlla se esiste qualche professore in attesa
        if (lockLab.hasWaiters(profWait)) {
            profWait.signal();
        } else {
            //controlla se esiste qualche tesista in attesa
            for (Condition t : texWait) {
                t.signal();
            }
            //sveglia tutti gli studenti in attesa;
            studentWait.signalAll();
        }
        lockLab.unlock();
    }

    /**
     * effettua una richiesta d'accesso al  computer pc_id del laboratorio da parte del tesista t
     *
     * @param t     tesista
     * @param id_pc pc del lab con identificativo id
     */
    public void texAccessRequest(Tesista t, int id_pc) {
        lockLab.lock();
        try {
            while (lockLab.hasWaiters(profWait) || !lab.isFree(id_pc)) {
                System.out.printf("Tesista is Waiting for pc with id_pc %d\n", t.getMatricola(), id_pc);
                texWait[id_pc].await();
            }
            lab.setOccupyPC(id_pc);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lockLab.unlock();
        }
    }

    /**
     * avvisa il tutore che il tesista t ha terminato il lavoro al compuiter id_pc
     *
     * @param t     teststa
     * @param id_pc identificativo computer liberato
     */
    public void texReleasePc(Tesista t, int id_pc) {
        lockLab.lock();
        //rilascia tutti i computer del laboratorio
        lab.setFreePC(id_pc);

        //controlla se esiste qualche professore in attesa
        if (lockLab.hasWaiters(profWait)) {
            profWait.signal();
        } else {
            if (lockLab.hasWaiters(texWait[id_pc])) {
                texWait[id_pc].signal();
            }
        }
        lockLab.unlock();
    }

    /**
     * @return l'id del primo computer disponibile (se esiste e non ci sono tesisti in attesa su quel computer)
     * -1 se nessun computer è disponibile
     */
    public int getFreePC_forStudent() {
        for (int i = 0; i < lab.getNcomputers(); i++) {
            if (lab.isFree(i) && !lockLab.hasWaiters(texWait[i])) {
                return i;
            }
        }
        return -1;
    }

    /**
     * effettua una richiesta di accesso al primo computer disponibile da parte dello studente s
     *
     * @param s studente
     * @return id del pc ascquisito dallostudente
     */
    public int studentAccessRequest(Student s) {
        lockLab.lock();
        int id_pc = -1;
        try {
            while (lockLab.hasWaiters(profWait) || this.getFreePC_forStudent() == -1) {
                System.out.printf("Student %d waiting for free pc\n", s.getMatricola());
                studentWait.await();
            }
            id_pc = this.getFreePC_forStudent();
            lab.setOccupyPC(id_pc);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            lockLab.unlock();
        }
        return id_pc;
    }

    /**
     * avvisa il tutore che lo studente s ha terminato il lavoro sul pc id id_pc
     *
     * @param s     student
     * @param id_pc identificativo computer
     */
    public void studentReleasePc(Student s, int id_pc) {
        lockLab.lock();
        lab.setFreePC(id_pc);

        //controlla prof in attesa
        if (lockLab.hasWaiters(profWait)) {
            profWait.signal();
        } else {
            //controlla se tesista in attesaù
            if (lockLab.hasWaiters(texWait[id_pc])) {
                texWait[id_pc].signal();
            } else {
                //sveglia uno studente in wait()
                studentWait.signal();
            }
        }
        lockLab.unlock();
    }
}