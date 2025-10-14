/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.ltmproject.net;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author admin
 */
public class ClientSocket {
    private static ClientSocket instance;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    
    public static ClientSocket getInstance(){
        if(instance == null) instance = new ClientSocket();
        return instance;
    }
    
    public void connect(String host, int port) throws IOException{
        socket = new Socket(host, port);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void send(String message){
        out.println(message);
    }
    
    public String receive() throws IOException{
        return in.readLine();
    }
    
    public void close() throws IOException{
        if(socket != null) socket.close();
    }
    
}
