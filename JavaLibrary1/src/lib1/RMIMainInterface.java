/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lib1;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 *
 * @author sheldon
 */
public interface RMIMainInterface extends Remote {
    public String check(String name , String passwd) throws RemoteException;
    public ArrayList<String> display(String name) throws RemoteException;
    public void logout(String myName , String name) throws RemoteException;
    public String groupRequest(String username , String groupName) throws RemoteException;
    public void requestDecide(int decide , String groupName, String username) throws RemoteException;
    public ArrayList<String> fetchGroups(String username) throws RemoteException;
    public ArrayList<String> displayRequests(String username) throws RemoteException;
    public void groupCreateRequest(String username , String groupName) throws RemoteException;
    public void leaveGroup(String username, String groupName) throws RemoteException;
}
