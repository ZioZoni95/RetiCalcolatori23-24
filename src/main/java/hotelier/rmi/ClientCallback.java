package hotelier.rmi;

import hotelier.model.CityRanking;

import java.rmi.Remote;
import java.rmi.RemoteException;

/** Callback invocata dal server quando cambia la classifica locale di una città di interesse. */
public interface ClientCallback extends Remote {

    void rankingChanged(CityRanking ranking) throws RemoteException;
}
