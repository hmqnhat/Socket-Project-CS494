/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.serverside;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author NhatQuoc
 */
public class PlayerHandler implements Runnable {

    private String name;
    private int score;
    private Socket player;
    private BufferedReader in;
    private PrintWriter out;
    private ArrayList<PlayerHandler> listPlayer;

    public PlayerHandler(String name, Socket playerSocket, BufferedReader in, PrintWriter out) throws IOException {
        this.name = name;
        this.player = playerSocket;
        this.in = in;
        this.out = out;
        this.score = 0;
        this.listPlayer = null;
    }

    public String getName() {
        return name;
    }

    public int getScore() {
        return score;
    }

    public PrintWriter getOut() {
        return out;
    }

    public void setListPlayer(ArrayList<PlayerHandler> listPlayer) {
        this.listPlayer = listPlayer;
    }

    public void sendInfoToAll() {
        String info = name + "\n" + score;
        for (PlayerHandler player : listPlayer) {
            player.out.println(info);
        }
    }

    public void close() {
        try {
            this.player.close();
            this.in.close();
        } catch (IOException ex) {
            Logger.getLogger(PlayerHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.out.close();
    }

    @Override
    public void run() {
        //game play
    }

}
