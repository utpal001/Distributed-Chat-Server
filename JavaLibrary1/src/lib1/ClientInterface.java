/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lib1;

import java.io.ByteArrayOutputStream;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 *
 * @author sheldon
 */
public interface ClientInterface extends Remote {
    public void doNothing() throws RemoteException;
    public void reciveMessage(String groupName, String wow) throws RemoteException;
    public void play(byte[] out2, long timeStamp) throws RemoteException;
    public void refresh() throws RemoteException;
}
