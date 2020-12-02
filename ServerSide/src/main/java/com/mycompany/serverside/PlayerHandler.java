/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.serverside;

import java.io.BufferedReader;
import java.io.IOException;
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
    private QuestionHandler question;
    private String gChar;
    private String gKey;
    private ServerConnection serverConn;
    private boolean isDisqualified;
    private boolean isTurn;
    private boolean isSubmit;

    public PlayerHandler(String name, Socket playerSocket, BufferedReader in, PrintWriter out, ServerConnection server) throws IOException {
        this.name = name;
        this.player = playerSocket;
        this.in = in;
        this.out = out;
        this.score = 0;
        this.listPlayer = null;
        this.question = null;
        this.gChar = "";
        this.gKey = "";
        this.serverConn = server;
        this.isDisqualified = false;
        this.isTurn = false;
        this.isSubmit = false;
    }

    public boolean getIsTurn() {
        return this.isTurn;
    }

    public boolean getIsSubmit() {
        return isSubmit;
    }

    public void setIsSubmit(boolean isSubmit) {
        this.isSubmit = isSubmit;
    }

    public void setIsTurn(boolean turn) {
        this.isTurn = turn;
    }

    public void setQuestion(QuestionHandler question) {
        this.question = question;
    }

    public void setDisqualified(boolean dis) {
        this.isDisqualified = dis;
    }

    public boolean getDisqualified() {
        return isDisqualified;
    }

    public QuestionHandler getQuestion() {
        return question;
    }

    public String getName() {
        return name;
    }

    public void setScore(int score) {
        this.score = score;
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

    public void sendTurnToAll(String name) {
        for (PlayerHandler player : listPlayer) {
            player.out.println("TURN");
            player.out.println(name);
            //Khong co EndName vi no se lam block cac req va res khac
        }
    }

    public void sendNoticeToAll(String msg) {
        for (PlayerHandler player : listPlayer) {
            if (player.getName() != this.name) {
                player.sendNotice(msg);
            }
        }
    }

    public void sendBlurKeyToAll(boolean isFinish) {
        for (PlayerHandler player : listPlayer) {
            player.out.println("BLUR");
            if (isFinish) {
                player.out.println(question.getKeyword());
            } else {
                player.out.println(question.getBlurKeyword());
            }
            player.out.println("END_BLUR");

        }
    }

    public void sendDialogToAll(String msg) {
        for (PlayerHandler p : listPlayer) {
            p.out.println("DIALOG");
            p.out.println(msg);
            p.out.println("END_DIALOG");
        }
    }

    private void addGuessCharSeq(String ch) {
        if (!ch.equals("")) {
            for (PlayerHandler player : listPlayer) {
                player.getQuestion().addGuessCharSeq(ch.charAt(0));
            }
        }

    }

    private void listenRequest() {
        try {
            String request;
            while (true) {
                if (in.ready()) {
                    request = in.readLine();
                    if (request.equals("CHAR")) {
                        this.gChar = in.readLine();

                        System.out.println("gChar: " + gChar);

                        //gọi hàm guessChar đúng trả về true, sai: false
                        if (this.guessChar() && !this.gChar.equals("")) {
                            //tiếp tục trả lời

                            System.out.println("guessChar(): true");

                            this.isTurn = true;

                            sendNoticeToAll("There are \"" + gChar + "\" letters in the keyword, " + this.name + " will keep guessing");
                            sendBlurKeyToAll(false);
                            //TODO: trường hợp đoán đúng ký tự cuối cùng
                            request = in.readLine();
                            if (request.equals("KEY")) {
                                this.gKey = in.readLine();
                                if (!this.gKey.equals("")) {
                                    if (this.guessKey()) {
                                        //thông báo người chiến thắng
                                        //TODO: kết thúc game, chuẩn bị game kế tiếp
                                        sendScoreToAll();
                                        serverConn.printScoreBoard();
                                        sendNotice("GREAT, YOU WIN!!");
                                        sendNoticeToAll("The keyword is " + this.gKey + ", " + this.name.toUpperCase() + " IS THE WINNER!!");
                                        sendDialogToAll(this.name.toUpperCase() + " IS THE WINNER!!");
                                        sendBlurKeyToAll(true);
                                        serverConn.setIsFinish(true);
                                    } else {
                                        sendNotice("WRONG KEYWORD, you are disqualified!!");
                                        //TODO: cho player mất quyền chơi trong game này
                                        this.isDisqualified = true;
                                    }
                                }
                                sendScoreToAll();
                                serverConn.printScoreBoard();
                            }

                        } else if (this.gChar.equals("")) {
                            this.gChar = "Blank character";
                            request = in.readLine();
                            if (request.equals("KEY")) {
                                this.gKey = in.readLine();
                                if (!this.gKey.equals("")) {
                                    if (this.guessKey()) {
                                        //thông báo người chiến thắng
                                        //TODO: kết thúc game, chuẩn bị game kế tiếp
                                        sendScoreToAll();
                                        serverConn.printScoreBoard();
                                        sendNoticeToAll("The keyword is " + this.gKey + ", " + this.name.toUpperCase() + " IS THE WINNER!!");
                                        sendNotice("GREAT, YOU WIN!!");
                                        sendDialogToAll(this.name.toUpperCase() + " IS THE WINNER!!");

                                        sendBlurKeyToAll(true);
                                        serverConn.setIsFinish(true);
                                    } else {
                                        sendNotice("WRONG KEYWORD, you are disqualified!!");
                                        //TODO: cho player mất quyền chơi trong game này
                                        this.isDisqualified = true;
                                    }
                                }
                                sendScoreToAll();
                                serverConn.printScoreBoard();
                            }
                            if (!serverConn.getIsFinish()) {
                                this.isSubmit = true; //bấm submit -> break, reset time
                                sendNotice(this.gChar + " is wrong character, you lose your turn!!");
                                sendNoticeToAll(this.gChar + " is wrong character, " + this.name.toUpperCase() + " lose his/her turn!!");
                            }
                        } else {
                            //sai mất lượt
                            this.isSubmit = true; //bấm submit -> break, reset time
                            sendNotice(this.gChar + " is wrong character, you lose your turn!!");
                            sendNoticeToAll(this.gChar + " is wrong character, " + this.name.toUpperCase() + " lose his/her turn!!");
                        }
                        addGuessCharSeq(this.gChar);
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
