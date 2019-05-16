package lib1;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author sheldon
 */
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RMIInterface extends Remote {

    public ArrayList<String> display(String name) throws RemoteException;
    public void insert(String name) throws RemoteException;
    public String topology(String username, String myName) throws RemoteException, NotBoundException;
    public void communicate(int port , String name) throws NotBoundException, MalformedURLException, RemoteException;
    public void broadcast(String name , String message) throws RemoteException;
    public void receive(String groupName, String mess) throws RemoteException;
    public void generator(String message, int servNumber) throws RemoteException;
    public void send(String username, String name, String message) throws RemoteException;
    public String sendGroup(String username,String groupName , ArrayList<String> name, String message, int mark) throws RemoteException;
    public void remove(String name) throws RemoteException;
    public String groupRequest(String username, String groupName) throws RemoteException;
    public void logout(String myName , String name) throws RemoteException;
    public void sendVoice(String name, String username, byte[] out, long timeStamp) throws RemoteException;
    public void sendGroupVoice(String username, String groupName, ArrayList<String> name, byte[] out2, long timeStamp) throws RemoteException;
    public void requestDecide(int decide, String username, String groupName) throws RemoteException;
    public ArrayList<String> fetchGroups(String username) throws RemoteException;
    public ArrayList<String> displayRequests(String username) throws RemoteException;
    public void sendAgain(String username, String message) throws RemoteException;
    public void groupCreateRequest(String username , String groupName) throws RemoteException;
    public void leaveGroup(String username, String groupName) throws RemoteException;
}