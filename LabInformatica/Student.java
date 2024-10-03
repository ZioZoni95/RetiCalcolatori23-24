package LabInformatica;

public class Student extends Utente {
    /**
     * id del pc attualmente assegnato, - altrimenti
     */
    private int assignedPc;

    /**
     *
     * @param idM matricola
     * @param t tutor
     */
    public Student(int idM,Tutor t){
        super(idM,t);
        assignedPc=-1;
    }

    @Override
    void richiestaAccesso(){
        System.out.printf("Student %d request access for a pc\n",this.getMatricola());
        assignedPc=tutor.studentAccessRequest(this);
        System.out.printf("Student assigned to Pc : %d\n",this.getMatricola(),this.assignedPc);
    }

    @Override
    void release(){
        System.out.printf("student released Pc : %d\n",this.getMatricola(),this.assignedPc);
        tutor.studentReleasePc(this,this.assignedPc);
        this.assignedPc = -1;
    }
}
