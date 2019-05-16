/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmimainserver;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.remote.rmi.RMIIIOPServerImpl;
import lib1.*;
import lib1.Javaconnect;
/**
 *
 * @author sheldon
 */
public class RMIMainServer extends UnicastRemoteObject implements RMIMainInterface {
    private int servCount = 3;
    private Connection conn ;
    private ResultSet rs = null;
    private PreparedStatement pst = null;
    private int[] cnt = new int[3];
    private ArrayList<String> arr0 = new ArrayList <String>() , arr1 = new ArrayList <String>();
    private TreeMap<String, Integer> serverMap; 
    
    
    RMIInterface stub1 = null , stub2 = null;
    RMIInterface skel[] = new RMIInterface[3];
    
    
    public RMIMainServer() throws RemoteException, NotBoundException, MalformedURLException{
        super();
        this.serverMap = new TreeMap<>();
        skel[0] = (RMIInterface) Naming.lookup("//127.0.0.1:20000/hellton" + 20000);
        skel[1] = (RMIInterface) Naming.lookup("//127.0.0.1:20001/hellton" + 20001);
        skel[2] = (RMIInterface) Naming.lookup("//127.0.0.1:20002/hellton" + 20002);

//        skel[0] = (RMIInterface) Naming.lookup("//172.20.43.46:20000/hellton" + 20000);
//        skel[1] = (RMIInterface) Naming.lookup("//172.20.43.46:20001/hellton" + 20001);
//        skel[2] = (RMIInterface) Naming.lookup("//172.20.43.46:20002/hellton" + 20002);
    }
    
    public static void main(String[] args) throws RemoteException, MalformedURLException, NotBoundException {
        Connection conn = Javaconnect.dbConnector();
        Registry reg = LocateRegistry.createRegistry(19999);
        
        Naming.rebind("rmi://127.0.0.1:19999/hellton" , new RMIMainServer());
        System.err.println("Server ready " + 19999);
    }

    @Override
    public synchronized String check(String name , String passwd) throws RemoteException {
        try {
            
            conn = Javaconnect.dbConnector();
            System.out.println("Atmain: " + name + " " + passwd);
            pst = conn.prepareStatement("select * from info where id = ? and password = ?");
            pst.setString(1, name);
            pst.setString(2, passwd);
            
            rs = pst.executeQuery();
            if(rs.next()){
                System.out.println(cnt[0] + " " + cnt[1]);
                int min1 = (int)1e6, minindex = -1;
                for(int i=0;i<servCount;i++){
                    
                    if(min1 > cnt[i]){
                        min1 = cnt[i];
                        minindex = i;
                    }
                }
                cnt[minindex] += 1;
                serverMap.put(name , minindex);
                System.out.println("minindex -> " + minindex);
                for(int i=0;i<servCount;i++){
                    if(i == minindex){
                        continue;
                    }
                    skel[i].generator(name, minindex);
                }
                //finding the server from its server number
                int num = 20000 + minindex;
                String tm = "rmi://172.19.16.174:" + num + "/hellton" + num;
                return tm;
//                if(cnt[0] >= cnt[1]){
//                    stub1.generator(name);
//                    String tm = "rmi://172.19.17.215:20001/hellton" + 20001;
//                    arr0.add(name);
//                    cnt[1] += 1;
//                    return tm;
//                }
//                
//                else{
//                    stub2.generator(name);
//                    String tm = "rmi://172.19.17.215:20000/hellton" + 20000;
//                    cnt[0] += 1;
//                    arr1.add(name);
//                    return tm;
//                }
            }
        } catch (SQLException ex) {
            Logger.getLogger(RMIMainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "0";
    }

    @Override
    public ArrayList<String> display(String name) throws RemoteException {
        System.err.println(name + " is trying to contact!");
        conn = Javaconnect.dbConnector();
        //String ret = "";
        ArrayList<String> ret = new ArrayList<String>();
        try {

            pst = conn.prepareStatement("select * from persongroup where groupname = ?");
            pst.setString(1, name);
            rs = pst.executeQuery();
            while (rs.next()) {
                ret.add(rs.getString("person"));
            }
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(RMIMainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
    public synchronized void logout(String myName , String name) throws RemoteException{
        try {
            ClientInterface stub = (ClientInterface) Naming.lookup(myName);
//            if(arr0.contains(name)){
//                cnt[1] -= 1;
//            }
//            else{
//                cnt[0] -= 1;
//            }
            if(serverMap.containsKey(name)){
                int servNumber = serverMap.get(name);
                cnt[servNumber] -= 1;
                serverMap.remove(name, servNumber);
            }
            for(int i=0;i<servCount;i++){
                skel[i].remove(name);
            }
            stub.doNothing();
        } catch (NotBoundException ex) {
            Logger.getLogger(RMIMainServer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger(RMIMainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized String groupRequest(String username , String groupName) throws RemoteException{
        conn = Javaconnect.dbConnector();
        //String ret = "";
        try {
            pst = conn.prepareStatement("insert into requests values (? , ?)");
            pst.setString(1, username);
            pst.setString(2, groupName);
            pst.execute();
            conn.close();
            conn = Javaconnect.dbConnector();
            pst = conn.prepareStatement("select person from persongroup where groupName = ? and admin = 1");
            pst.setString(1, groupName);
            rs = pst.executeQuery();
            String ret = "";
            while(rs.next()){
                ret = rs.getString("person");
                break;
            }
            conn.close();
            return ret;
        } catch (SQLException ex) {
            
            Logger.getLogger(RMIMainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
    public synchronized void groupCreateRequest(String username , String groupName) throws RemoteException{
        conn = Javaconnect.dbConnector();
        //String ret = "";
        try {
            pst = conn.prepareStatement("insert into persongroup values (? , ?, 1)");
            pst.setString(2, username);
            pst.setString(1, groupName);
            pst.execute();
            conn.close();
            
            
        } catch (SQLException ex) {
            
            Logger.getLogger(RMIMainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized ArrayList<String> displayRequests(String username) throws RemoteException{
        System.err.println(username + " is trying to contact!");
        conn = Javaconnect.dbConnector();
        ArrayList<String> ret = new ArrayList<String>();
        try {
            // select request.id from requests inner join persongroup on requests.groupname = persongroup.groupname where person.admin = 1 and request.groupname = '';
            pst = conn.prepareStatement("select requests.id , requests.groupname from requests inner join persongroup on requests.groupname = persongroup.groupname where persongroup.admin = 1 and persongroup.person = ?");
            pst.setString(1, username);
            rs = pst.executeQuery();
            while (rs.next()) {
                ret.add(rs.getString("requests.id") + " - " + rs.getString("requests.groupname"));
                System.out.println(rs.getString("requests.id") + " - " + rs.getString("requests.groupname"));
            }
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(RMIMainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
    
    //(decide = 1) -> accept else reject
    public synchronized void requestDecide(int decide , String username, String groupName) throws RemoteException{
        conn = Javaconnect.dbConnector();
        //String ret = "";
        try {
            System.out.println(username + " " + groupName +" " + decide);
            pst = conn.prepareStatement("delete from requests where id = ? and groupname = ?");
            pst.setString(1, username);
            pst.setString(2, groupName);
            pst.execute();
            if(decide == 1){
                pst = conn.prepareStatement("insert into persongroup values (?, ?, 0)");
                pst.setString(2, username);
                pst.setString(1, groupName);
                pst.execute();
            }
            
            conn.close();
        } catch (SQLException ex) {

            Logger.getLogger(RMIMainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public synchronized void leaveGroup(String username, String groupName) throws RemoteException {
        conn = Javaconnect.dbConnector();
        //String ret = "";
        try {
            System.out.println("leave group -> " + username + " " + groupName);
            pst = conn.prepareStatement("delete from persongroup where person = ? and groupname = ? and admin = 0");
            pst.setString(1, username);
            pst.setString(2, groupName);
            pst.execute();

            conn.close();
        } catch (SQLException ex) {

            Logger.getLogger(RMIMainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public synchronized ArrayList<String> fetchGroups(String username) throws RemoteException{
        conn = Javaconnect.dbConnector();
        //String ret = "";
        ArrayList<String> ret = new ArrayList<String>();
        try {
            pst = conn.prepareStatement("select groupname from persongroup where person = ?");
            pst.setString(1, username);
            rs = pst.executeQuery();
            while(rs.next()){
                ret.add(rs.getString("groupname"));
            }
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(RMIMainServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }
}
