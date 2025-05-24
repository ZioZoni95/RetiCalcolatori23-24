package LabInformatica;
import java.util.ArrayList;

/**
 * Lab_zon1 rappresenta un laboratorio universitario in cui sono presenti ncomputers
 *
 */
public class Lab_zon1 {
    private final int ncomputers;
    /**
     * rappresenta l'insieme dei computer
     * Ogni item dell'array dell'array è un booleano e può essere
     *  --True -> computer libero
     *  --False ->computer occupato
     */
    private final ArrayList<Boolean> computer;

    /**
     * @param ncomputers num computer presenti nel laboratorio
     *
     */
    public Lab_zon1(int ncomputers){
        this.ncomputers = ncomputers;
        computer = new ArrayList<>(ncomputers);
        //inizializazione di tutti computers a liberi
        for (int i =0; i<ncomputers; i++){
            computer.add(Boolean.TRUE);
        }
    }

    /**
     * @param idx id di un computer
     * @return true se è disponibile, false altrimenti
     */
    public boolean isFree(int idx){
        return computer.get(idx);
    }

    /**
     * setta disponibile tutti i computer
     */
    public void setFreeAll(){
        for (int i = 0; i<ncomputers; i++){
            computer.set(i,Boolean.TRUE);
        }
    }

    /**
     * setta allo stato occupato tutti i computer
     */

    public void setOccupyAll(){
        for (int i = 0; i<ncomputers; i++){
            computer.set(i,Boolean.FALSE);
        }
    }

    /***
     * rende un pc identificato da idx libero
     * @param idx id pc
     */

    public void setFreePC(int idx){
        computer.set(idx,Boolean.TRUE);
    }

    /**
     * rende un pc identificato da idx come occupato
     * @param idx id pc
     */

    public void setOccupyPC(int idx){
        computer.set(idx,Boolean.FALSE);
    }

    /**
     *
     * @return true se tutti i computer sono free,false altrimenti
     */
    public boolean isFree(){
        for(Boolean computer : computer){
            if(!computer) // pc occupato
                return false;
        }
        return true;
    }

    /**
     *
     * @return Lab_zon1#ncomputer
     */
    public int getNcomputers(){
        return ncomputers;
    }

    /**
     *
     * @return l'id del primo computer disponibile (se esiste)
     *         -1 se nessun computer è disponibile
     */
    public int getFirstAvaiablePC(){
        for(int i=0; i<this.ncomputers;i++){
            if(computer.get(i))
                return i;
        }
        return -1;
    }
}
