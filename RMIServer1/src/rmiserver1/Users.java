/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rmiserver1;

import lib1.*;
/**
 *
 * @author sheldon
 */
public class Users {
    public String username, myName;
    public ClientInterface client;
    
    public Users(String username , ClientInterface client, String myName){
        this.username = username;
        this.client = client;
        this.myName = myName;
    }
    
    public String getname(){
        return username;
    }
}
