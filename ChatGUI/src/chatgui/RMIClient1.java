package chatgui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.Math.sqrt;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import lib1.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
public class RMIClient1 extends UnicastRemoteObject implements ClientInterface {
    private RMIMainInterface stubMain;
    private RMIInterface stub = null;
    private String myName;
    private String username , ServerName;
    private Home home = new Home(this);
    private static final float SAMPLE_RATE = 8000.0f; //8kHz
    private static final int SAMPLE_SIZE_IN_BITS = 16;
    private static final int CHANNELS = 1;
    private static final boolean SIGNED = true;
    private static final boolean BIG_ENDIAN = false;
    private ByteArrayOutputStream out;
    private AudioRecorderTask audioRecorderTask;
    private AudioFormat format = new AudioFormat( SAMPLE_RATE, SAMPLE_SIZE_IN_BITS, CHANNELS, SIGNED, BIG_ENDIAN);
    
    public RMIClient1() throws RemoteException, NotBoundException, MalformedURLException {
        
        super();
        System.out.println("fff");
        stubMain = (RMIMainInterface) Naming.lookup("//127.0.0.1:19999/hellton");
        
    }
    
    private class AudioRecorderTask extends SwingWorker<Void,byte[]> {

        @Override
        protected Void doInBackground() {
            TargetDataLine microphone;
            DataLine.Info info = new DataLine.Info(TargetDataLine.class, format);
            if (!AudioSystem.isLineSupported(info)) {
                System.out.println("Error! Audio System line not supported.");
            } else {
                //TODO open mixer's getLine()?
                try {
                    microphone = AudioSystem.getTargetDataLine(format);
                    microphone.open(format);
                    out = new ByteArrayOutputStream();
                    int numBytesRead;
                    byte[] data = new byte[microphone.getBufferSize()/5];
                    microphone.start();
                    while (!isCancelled()) {
                        numBytesRead = microphone.read(data, 0, data.length);
                        out.write(data, 0, numBytesRead);
                    }
                    out.close();
                } catch (Exception excp) {
                    System.out.println("Error! Could not open Audio System line!");
                    excp.printStackTrace();
                }
            }
            return null;
        }
    }
    
    public synchronized void stop() throws RemoteException{
        audioRecorderTask.cancel(true);
        audioRecorderTask = null;
    }
    
    public synchronized void record() throws RemoteException{
        (audioRecorderTask = new AudioRecorderTask()).execute();
    }
    
    public synchronized void sendVoice(String name) throws RemoteException, InterruptedException{
        byte[] audio = out.toByteArray();
        System.out.println(Array.getLength(audio));
        stub.sendVoice(username, name, audio, Long.MAX_VALUE);
    }
    
    public synchronized void sendGroupVoice(String groupName) throws RemoteException {
        ArrayList<String> res = stub.display(groupName);
        byte[] audio = out.toByteArray();
        System.out.println(Array.getLength(audio));
        stub.sendGroupVoice(username, groupName, res, audio, Long.MAX_VALUE);
    }
    
    
    public synchronized void play(byte[] audio, long timeStamp) throws RemoteException {
        //System.out.println("received ->  " + audio1.length());
        System.out.println("received ->  " + Array.getLength(audio));
        //byte[] audio = audio1.getBytes();
        //byte[] audio = out2.toByteArray();
        InputStream in = new ByteArrayInputStream(audio);
        AudioInputStream ais = new AudioInputStream(in, format, audio.length / format.getFrameSize());
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
        int token = -1;

        try {

            SourceDataLine speaker = (SourceDataLine) AudioSystem.getLine(info);
            speaker.open(format);

            speaker.start();
            int bufferSize = (int) format.getSampleRate() * format.getFrameSize();
            byte[] buffer = new byte[bufferSize];
            int count;
            while ((count = ais.read(buffer, 0, buffer.length)) != -1) {
                if (count > 0) {
                    speaker.write(buffer, 0, count);
                }
            }
            speaker.drain();
            speaker.close();
        } catch (Exception excp) {
            excp.printStackTrace();
        }
    }
    
    @Override
    public synchronized void doNothing() throws RemoteException{
        System.exit(0);
        return;
    }
    
    public String makeIt(String curr,String temp)
    {
        String ret = "";
        for(int i=0;i<curr.length()-7;i++){
            ret += curr.charAt(i);
        }
        
        ret += "<br/>"+temp;
        ret += "</html>";
        return ret;
    }
    
    public void load() throws RemoteException {
        this.home.glist.setText("<html></html>");
        ArrayList<String> groups = stub.fetchGroups(username);
        System.out.println("ttttt");
        this.home.it = 0;
        for(int i=0;i<groups.size();i++){
            System.out.println(groups.get(i)+" yyuu "+this.home.it);
            String temp = groups.get(i);
            this.home.already[this.home.it] = temp;
            String currText = home.glist.getText();
            System.out.println(currText);
            String toadd = makeIt(currText,temp);
            this.home.glist.setText(toadd);
            this.home.bx[this.home.it].differ.setText(this.username);
            this.home.bx[this.home.it].groupName.setText(temp);
            this.home.it++;
        }
        
//        if(this.username.equals("sid")){
//            String temp = "utpal";
//            this.home.already[this.home.it] = temp;
//            String currText = home.glist.getText();
//            String toadd = makeIt(currText, temp);
//            this.home.glist.setText(toadd);
//            this.home.bx[this.home.it].groupName.setText(temp);
//            this.home.it++;
//        }
        System.out.println("hoi");
        return;
    }
    
    @Override
     public synchronized void refresh() throws RemoteException{
        this.load();
     }
    
    @Override
    public synchronized void reciveMessage(String groupName, String wow) throws RemoteException {
        System.out.println(this.home.it+ " iiioo");
//        if(!groupName.equals("$$") && !groupName.equals("admin")){
//            System.out.println("blah " + wow);
//            String[] sep = wow.split("\\:");
//            try {
//                Thread.sleep(3999);
//                System.out.println("active");
//            } catch (InterruptedException ex) {
//                Logger.getLogger(RMIClient1.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            stub.sendAgain(sep[0] , sep[1]);
//            return;
//        }
        System.out.println(wow);
        String temp = groupName;
        if(groupName.equals("")){
            for(int i=0;i<wow.length();i++){
                if(wow.charAt(i) == ':'){
                    break;
                }
                temp += wow.charAt(i);
            }
        }
        
//        
//        System.out.println("huii ");
        System.out.println(this.home.it);
        int id = this.home.it;
        
        for(int i=0;i<this.home.it;i++){
            if(this.home.already[i].equals(temp)){
                id = i;
                break;
            }
        }
        
        if(id == this.home.it){
            this.home.it++;
            this.home.already[id] = temp;
            String currText = home.glist.getText();
            String toadd = makeIt(currText,temp);
            this.home.glist.setText(toadd);
            this.home.bx[this.home.it].differ.setText(this.username);
            this.home.bx[this.home.it].groupName.setText(temp);
            this.home.k[id] = 0;
        }
        
        this.home.msg[id][this.home.k[id]] = wow;
        this.home.k[id]++;
        this.order(id);
        //String currText = this.home.bx[id].mlist.getText();
        //String toadd = makeIt(currText, wow);
        //this.home.bx[id].mlist.setText(toadd);
    } 
    
    private void order(int id) {
        this.home.bx[id].mlist.setText("<html></html>");
        TreeMap <Long,String > mp = new TreeMap<> ();
        
        for(int i=0;i<this.home.k[id];i++){
            String[] temp = this.home.msg[id][i].split("\\$");
            String prepared = "";
            
            for(int j=0;j<temp.length - 1;j++){
                prepared += temp[j];
            }
            System.out.println("wowo " + temp[temp.length - 1] + temp.length);
            mp.put(Long.parseLong(temp[temp.length - 1]), prepared);
        }
        
        for (Map.Entry<Long,String> entry : mp.entrySet()){
            String currText = this.home.bx[id].mlist.getText();
            String toadd = makeIt(currText, entry.getValue());
            this.home.bx[id].mlist.setText(toadd);
        }
        
        return;
    }
        
    public synchronized void logout() throws RemoteException{
        home.setVisible(false);
        stub.logout(myName, username);
    }
    
    public synchronized void sendPersonal(String sendeeName, String mess) throws RemoteException{
        mess += "  <-" + new Date(System.currentTimeMillis());
        stub.send(username, sendeeName, mess);    
    }
    
    public synchronized void sendBroad(String mess) throws RemoteException{
        mess += "  <-" + new Date(System.currentTimeMillis());
        stub.broadcast(username, mess);    
    }
                                        
    public synchronized void sendGroup(String groupName, String mess) throws RemoteException{
        System.out.println(mess);
        ArrayList<String>res = stub.display(groupName);
        String ret = stub.sendGroup(username, groupName, res, mess, 0);    
        this.reciveMessage(groupName, ret);
    }
    public synchronized void groupRequest(String groupName) throws RemoteException{
        String owner = stub.groupRequest(username , groupName);
    }
    public synchronized void displayRequests() throws RemoteException{
        ArrayList<String> ret = stub.displayRequests(this.username);
        this.home.requests = ret;
        this.home.resuqestBox.setText("<html></html>");
        for(int i=0;i<ret.size();i++){
            String temp = ret.get(i);
            String currText = this.home.resuqestBox.getText();
            String toadd = makeIt(currText, temp);
            this.home.resuqestBox.setText(toadd);
        }
        
        if(this.home.resuqestBox.getText().equals("")){
            this.home.resuqestBox.setText("No Request Currently!");
        }
    }
    
    public synchronized void requestDecide(String groupName,String requser,int parity) throws RemoteException{
        stub.requestDecide(parity,requser,groupName);
        this.displayRequests();
        this.load();
    }
    
    public synchronized void groupCreateRequest(String groupName) throws RemoteException{
        stub.groupCreateRequest(this.username, groupName);
        
    }
    
    public synchronized void leaveGroup(String groupName) throws RemoteException{
        stub.leaveGroup(this.username, groupName);
        this.sendGroup(groupName, this.username + " has left the group");
    }
    
    public synchronized int login(String username , String passwd) throws RemoteException{
        this.username = username;
        this.myName = "rmi://127.0.0.1:20000/" + username; 
        String val = stubMain.check(username, passwd);
        System.out.println(username + " " + passwd);
        if(val.equals("0")){
           return 0;
        }
        this.ServerName = val;
        System.out.println(val + " " + myName);
        try {
            Naming.rebind(myName, this);
            stub = (RMIInterface) Naming.lookup(val);
            
            stub.topology(username, myName);
        } catch (NotBoundException ex) {
            Logger.getLogger(RMIClient1.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        } catch (MalformedURLException ex) {
            Logger.getLogger(RMIClient1.class.getName()).log(Level.SEVERE, null, ex);
            return 0;
        }
        return 1;
    }
                                
    public void shuru() throws RemoteException {
        this.home.user.setText("Welcome!, "+username);
        this.home.setVisible(true);
        this.home.process();
        for(int i=0;i<1000;i++){
            this.home.k[i] = 0;
        }
        this.load();
        this.displayRequests();
    }
}
