/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package rmiserver1;

import java.io.ByteArrayOutputStream;
import static java.lang.Math.min;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import lib1.ClientInterface;
import lib1.Javaconnect;
import lib1.RMIInterface;
import lib1.RMIMainInterface;

/**
 *
 * @author sheldon
 */
public class RMIServer1 extends UnicastRemoteObject implements RMIInterface {
    private Connection conn;
    private ResultSet rs;
    private PreparedStatement pst;
    private static final int servCount = 3;
    ArrayList<Users>vec1 = new ArrayList<Users>();
    ArrayList <String> vec2 = new ArrayList<String>();
    private static final int serverId = 2;
    static int port = 20000 + serverId;
    String [] serverNames = new String[3];
    RMIMainInterface mainStub = null;
    private static final String servName = "rmi://127.0.0.1:" + port + "/hellton" + port;
    private TreeMap<String, Integer> serverMap; 
    
    public RMIServer1() throws RemoteException, NotBoundException, MalformedURLException {
        super();
        this.serverMap = new TreeMap<>();
//        serverNames[0] = "rmi://127.0.0.1:20000/hellton20000";
//        serverNames[1] = "rmi://127.0.0.1:20001/hellton20001";
//        serverNames[2] = "rmi://127.0.0.1:20002/hellton20002";
        
        serverNames[0] = "rmi://127.0.0.1:20000/hellton20000";
        serverNames[1] = "rmi://127.0.0.1:20001/hellton20001";
        serverNames[2] = "rmi://127.0.0.1:20002/hellton20002";
    }
    
    public static void main(String[] args) throws MalformedURLException, NotBoundException {
        Connection conn = Javaconnect.dbConnector();
        try {
                Registry reg = LocateRegistry.createRegistry(port);
                //reg.rebind(name1, new RMIServer1());
                Naming.rebind(servName, new RMIServer1());
                //Naming.rebind(servName,this);
                System.err.println("Server ready " + servName);
            
        } catch (RemoteException e) {
            //System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
    
    @Override
    public synchronized ArrayList<String> display(String name) throws RemoteException {
        ArrayList<String> ret = new ArrayList<String>();
        ret = mainStub.display(name);
        return ret;
    }

    @Override
    public synchronized void insert(String name) throws RemoteException {
        conn = Javaconnect.dbConnector();
        try {
            String name1 = name + "\n";
            pst = conn.prepareStatement("insert into messages values('" + name1 + "')");
            pst.execute();
            conn.close();
        } 
        catch (SQLException ex) {
            Logger.getLogger(RMIServer1.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public synchronized String topology(String username, String myName) throws RemoteException, NotBoundException {
        try {
            System.out.println("verifying -> " + RemoteServer.getClientHost());
                    } catch (ServerNotActiveException ex) {
            Logger.getLogger(RMIServer1.class.getName()).log(Level.SEVERE, null, ex);
        }
        String ret = "";
        System.out.println("ret ->" + myName);
        try {
            mainStub = (RMIMainInterface) Naming.lookup("rmi://127.0.0.1:19999/hellton");
            System.out.println(myName);
//            String [] arr = Naming.list("rmi://127.0.0.1:20000/");
//            for(String it : arr){
//                System.out.println(it);
//            }

            ClientInterface nc = (ClientInterface) Naming.lookup(myName);
            Users u1 = new Users(username , nc, myName);
            System.out.println(u1 + " wow");
            vec1.add(u1);
            String mess = "Welcome " + username + " to the group" + "$" + System.currentTimeMillis();
            
            
            for(int i=0;i<vec1.size();i++){
                if(vec1.get(i).username.equals(username)) continue;
                System.out.println(vec1.get(i).myName + " " + mess);
                vec1.get(i).client.reciveMessage("admin","Welcome " + username + " to the group");
            }
            
            for(int i=0;i<servCount;i++){
                if(servName.equals(serverNames[i])) continue;
                RMIInterface stub = (RMIInterface) Naming.lookup(serverNames[i]);
                stub.receive("admin", mess);
            }
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(RMIServer1.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return ret;
    }
    
    @Override
    public synchronized void communicate(int port  , String name) throws NotBoundException, RemoteException, MalformedURLException{
        RMIInterface stub = (RMIInterface) Naming.lookup(name);
        //stub.topology("sheldon");
    }
    
    @Override
    public synchronized void broadcast(String username,String message) throws RemoteException{
        for (int i = 0; i < vec1.size(); i++) {
            vec1.get(i).client.reciveMessage("broadcast", username + ": " + message);
        }
        RMIInterface stub = null;
        for (int i = 0; i < servCount; i++) {
            if (servName.equals(serverNames[i])) {
                continue;
            }
            try {
                stub = (RMIInterface) Naming.lookup(serverNames[i]);
            } catch (NotBoundException ex) {
                Logger.getLogger(RMIServer1.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MalformedURLException ex) {
                Logger.getLogger(RMIServer1.class.getName()).log(Level.SEVERE, null, ex);
            }
            stub.receive("broadcast",username + ": " + message);
        }
    }
    
    @Override
    public synchronized void generator(String name, int servNumber) throws RemoteException{
        vec2.add(name);
        serverMap.put(name, servNumber);
    }
    
    public synchronized void remove(String name) throws RemoteException{
        //this is for logging out, so we delete the user from our messaging list
        serverMap.remove(name);
        for(int i=0;i<vec2.size();i++){
            System.out.println(vec2.get(i)  + " " + name + " " + vec2.get(i).equals(name));
            if(vec2.get(i).equals(name)){
                vec2.remove(i);
                break;
            }
        }
        for(int i=0;i<vec1.size();i++){
            System.out.println(vec1.get(i).getname()  + " " + name + " " + vec1.get(i).getname().equals(name));
            if(vec1.get(i).getname().equals(name)){
                vec1.remove(i);
            }
        }
        System.out.println(vec1.size() + " " + vec2.size());
    }
    
    @Override
    public synchronized void sendAgain(String username, String message) throws RemoteException{
        System.out.println("blblblb " + username + " " + message);
        for(int i=0;i<vec1.size();i++){
            System.out.println("In this: " + username + " " + vec1.get(i).username);
            if(vec1.get(i).getname().equals(username)){
                vec1.get(i).client.reciveMessage("$$", message);
                return;
            }
        }
        
        RMIInterface stub = null;
        for (int i = 0; i < servCount; i++) {
            if (servName.equals(serverNames[i])) {
                continue;
            }

            try {
                for (Map.Entry<String, Integer> entry : serverMap.entrySet()) {
                    if (entry.getValue() == i) {
                        stub = (RMIInterface) Naming.lookup(serverNames[i]);
                        stub.sendAgain(username, message);
                    }
                }
            } catch (NotBoundException ex) {
                Logger.getLogger(RMIServer1.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MalformedURLException ex) {
                Logger.getLogger(RMIServer1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @Override
    public synchronized void receive(String groupName, String mess) throws RemoteException{
        for (int i = 0; i < vec1.size(); i++) {
            System.out.println(vec1.get(i).myName);
            vec1.get(i).client.reciveMessage(groupName,mess);
        }
    }
    @Override
    public synchronized void send(String username, String name, String message) throws RemoteException{
        message = message + "," + System.currentTimeMillis();
        for(int i=0;i<vec1.size();i++){
            System.out.println("In this: " + name + " " + vec1.get(i).username);
            if(vec1.get(i).getname().equals(name) && !vec1.get(i).equals(username)){
                vec1.get(i).client.reciveMessage("",username + ": " + message);
                return;
            }
        }
        RMIInterface stub = null;
        for (int i = 0; i < servCount; i++) {
            if (servName.equals(serverNames[i])) {
                continue;
            }

            try {
                for (Map.Entry<String, Integer> entry : serverMap.entrySet()) {
                    if (entry.getValue() == i) {
                        stub = (RMIInterface) Naming.lookup(serverNames[i]);
                        stub.send(username, name, message);
                    }
                }
            } catch (NotBoundException ex) {
                Logger.getLogger(RMIServer1.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MalformedURLException ex) {
                Logger.getLogger(RMIServer1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    
    @Override
    public synchronized String sendGroup(String username, String groupName, ArrayList<String> name, String message, int mark) throws RemoteException {
        if(mark == 0){
            message = message + "$" + System.currentTimeMillis();
        }
        for (int i = 0; i < vec1.size(); i++) {
            System.out.println("In this: " + name + " " + vec1.get(i).username);
            if (name.contains(vec1.get(i).username)) {
                System.out.println("hi " + vec1.get(i).username + vec1.get(i));
                if(vec1.get(i).username.equals(username)){
                    continue;
                }
                vec1.get(i).client.reciveMessage(groupName ,username + ": " + message);
                //return;
            }
        }
        RMIInterface stub = null;
        System.out.println("list -> " + serverMap);
        for (int i = 0; i < servCount; i++) {
            if (servName.equals(serverNames[i])) {
                continue;
            }

            try {
                ArrayList <String> ret = new ArrayList <String>();
                Collections.sort(name);
                int ct = 0;
                for (Map.Entry<String, Integer> entry : serverMap.entrySet()) {
                    if (entry.getValue() == i) {
                        String tm = entry.getKey();
                        if(ct == name.size()){
                            break;
                        }
                        System.out.println(tm.compareTo(name.get(ct)) + " " + name.get(ct));
                        if(tm.compareTo(name.get(ct)) < 0){
                            continue;
                        }
                        while(tm.compareTo(name.get(ct)) > 0){
                            ct += 1;
                            if(ct == name.size()){
                                break;
                            }
                        }
                        if(ct < name.size() && tm.equals(name.get(ct))){
                            ret.add(tm);                            
                        }
                    }
                }
                System.out.println(ret);
                if(ret.size() == 0){
                    return username + ": " + message;
                }
                stub = (RMIInterface) Naming.lookup(serverNames[i]);
                stub.sendGroup(username, groupName, ret , message, 1);
            } catch (NotBoundException ex) {
                Logger.getLogger(RMIServer1.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MalformedURLException ex) {
                Logger.getLogger(RMIServer1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return username + ": " + message;
    }

    @Override
    public synchronized String groupRequest(String username, String groupName) throws RemoteException {
        return mainStub.groupRequest(username, groupName);
    }
    @Override
    public synchronized void groupCreateRequest(String username , String groupName) throws RemoteException{
        mainStub.groupCreateRequest(username, groupName);
    }
    public synchronized void leaveGroup(String username, String groupName) throws RemoteException{
        mainStub.leaveGroup(username, groupName);
    }
    @Override
    public synchronized void requestDecide(int decide, String username, String groupName) throws RemoteException {
        mainStub.requestDecide(decide, username, groupName);
        
    }
    public synchronized ArrayList<String> displayRequests(String username) throws RemoteException{
        ArrayList<String> ret = mainStub.displayRequests(username);
        return ret;
    }
    @Override
    public synchronized ArrayList<String> fetchGroups(String username) throws RemoteException {
        return mainStub.fetchGroups(username);
    }

    @Override
    public void logout(String myName, String name) throws RemoteException {
        mainStub.logout(myName, name);
    }
   
    @Override
    public void sendVoice(String username, String name, byte[] out2, long timeStamp) throws RemoteException{
        timeStamp = min(System.currentTimeMillis(),timeStamp);
        for (int i = 0; i < vec1.size(); i++) {
            System.out.println("In this: " + name + " " + vec1.get(i).username);
            if (vec1.get(i).getname().equals(name)) {
                vec1.get(i).client.play(out2, timeStamp);
                return;
            }
        }
        
        RMIInterface stub = null;
        for (int i = 0; i < servCount; i++) {
            if (servName.equals(serverNames[i])) {
                continue;
            }

            try {
                for (Map.Entry<String, Integer> entry : serverMap.entrySet()) {
                    if (entry.getValue() == i) {
                        stub = (RMIInterface) Naming.lookup(serverNames[i]);
                        stub.sendVoice(username, name, out2, timeStamp);
                    }
                }
            } catch (NotBoundException ex) {
                Logger.getLogger(RMIServer1.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MalformedURLException ex) {
                Logger.getLogger(RMIServer1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @Override
    public void sendGroupVoice(String username, String groupName, ArrayList<String> name, byte[] out2, long timeStamp) throws RemoteException{
        timeStamp = min(System.currentTimeMillis(),timeStamp);
        for (int i = 0; i < vec1.size(); i++) {
            System.out.println("In this: " + name + " " + vec1.get(i).username);
            if (name.contains(vec1.get(i).username)) {
                if(vec1.get(i).username.equals(username)){
                    continue;
                }
                vec1.get(i).client.play(out2, timeStamp);
                return;
            }
        }
        RMIInterface stub = null;
        for (int i = 0; i < servCount; i++) {
            if (servName.equals(serverNames[i])) {
                continue;
            }

            try {
                ArrayList <String> ret = new ArrayList <String>();
                Collections.sort(name);
                int ct = 0;
                for (Map.Entry<String, Integer> entry : serverMap.entrySet()) {
                    if (entry.getValue() == i) {
                        String tm = entry.getKey();
                        if(ct == name.size()){
                            break;
                        }
                        System.out.println(tm.compareTo(name.get(ct)) + " " + name.get(ct));
                        if(tm.compareTo(name.get(ct)) < 0){
                            continue;
                        }
                        while(tm.compareTo(name.get(ct)) > 0){
                            ct += 1;
                            if(ct == name.size()){
                                break;
                            }
                        }
                        if(ct < name.size() && tm.equals(name.get(ct))){
                            ret.add(tm);
                            
                        }
                    }
                }
                System.out.println(ret);
                if(ret.size() == 0){
                    return;
                }
                stub = (RMIInterface) Naming.lookup(serverNames[i]);
                stub.sendGroupVoice(username, groupName, ret, out2, timeStamp);
            } catch (NotBoundException ex) {
                Logger.getLogger(RMIServer1.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MalformedURLException ex) {
                Logger.getLogger(RMIServer1.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
