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
import javax.swing.JOptionPane;

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
    private QuestionHandler question;
    private String gChar;
    private String gKey;
    
    public PlayerHandler(String name, Socket playerSocket, BufferedReader in, PrintWriter out) throws IOException {
        this.name = name;
        this.player = playerSocket;
        this.in = in;
        this.out = out;
        this.score = 0;
        this.listPlayer = null;
        this.question = null;
        this.gChar = "";
        this.gKey = "";
    }
    
    public void setQuestion(QuestionHandler question) {
        this.question = question;
    }
    
    public QuestionHandler getQuestion() {
        return question;
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
    
    public void sendKeyword() {
        out.println(question.getBlurKeyword());
    }
    
    public void sendDescription() {
        out.println(question.getDescription());
    }
    
    public void sendLengthOfKeyword() {
        out.println(question.getLengthOfKeyword());
    }
    
    private boolean guessChar() {
        int coef = question.guessChar(gChar);
        if (coef != 0) {
            sendNotice("There are " + coef + " letters in the keyword, you can keep guessing");
            //TODO: trường hợp đoán đúng ký tự cuối cùng

            score = score + coef;
            return true;
        }
        return false;
    }
    
    private boolean guessKey() {
        boolean ans = question.guessKey(gKey);
        if (ans) {
            score = score + 5;
            return true;
        }
        return false;
    }
    
    public void sendNotice(String msg) {
        out.println("NOTICE");
        out.println(msg);
        out.println("END_NOTICE");
    }
    
    public void sendScoreToAll() {
        String info = name + "\n" + score;
        
        for (PlayerHandler player : listPlayer) {
            player.out.println("SCORE");
            player.out.println(info);
            player.out.println("END_SCORE");
            
        }
    }
    
    public void sendNoticeToAll(String msg) {
        for (PlayerHandler player : listPlayer) {
            if (player.getName() != this.name) {
                player.sendNotice(msg);
            }
        }
    }
    
    public void sendBlurKeyToAll() {
        for (PlayerHandler player : listPlayer) {
            player.out.println("BLUR");
            player.out.println(question.getBlurKeyword());
            player.out.println("END_BLUR");
            
        }
    }
    
    private void listenRequest() {
        try {
            String request;
            while (true) {
                request = in.readLine();
                if (request.equals("CHAR")) {
                    this.gChar = in.readLine();
                    //gọi hàm guessChar đúng trả về true, sai: false
                    if (this.guessChar()) {
                        sendNoticeToAll("There are \"" + gChar + "\" letters in the keyword, " + this.name + " will keep guessing");
                        sendBlurKeyToAll();
                        //TODO: trường hợp đoán đúng ký tự cuối cùng
                        request = in.readLine();
                        if (request.equals("KEY")) {
                            this.gKey = in.readLine();
                            if (this.gKey.equals("")) {
                                if (this.guessKey()) {
                                    //TODO: thông báo người chiến thắng
                                    sendNoticeToAll("The keyword is "+ this.gKey+", " +this.name+" IS THE WINNER!!");
                                    //TODO: kết thúc game, chuẩn bị game kế tiếp
                                } else {
                                    sendNotice("WRONG KEYWORD, you are disqualified!!");
                                    //TODO: cho player này mất quyền chơi trong game này
                                    sendScoreToAll();
                                }
                            }
                        }
                        
                    } else {
                        sendNotice(this.gChar + "is wrong character, you lose your turn!!");
                        sendNoticeToAll(this.gChar + "is wrong character, " + this.name + " lose his/her turn!!");
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(PlayerHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    public void run() {
        //listen to client
        listenRequest();
    }
    
}
