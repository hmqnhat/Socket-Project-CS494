/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.serverside;

/**
 *
 * @author NhatQuoc
 */
public class QuestionHandler {

    private String keyword;
    private String description;
    private String blurKeyword;
    private String guessCharSeq;

    public QuestionHandler(String keyword, String description) {
        this.keyword = keyword.trim();
        this.description = description.trim();
        this.blurKeyword = "";
        makeBlurKeyword('@');
        guessCharSeq = "";
    }

    public String getKeyword() {
        return keyword;
    }

    public String getDescription() {
        return description;
    }

    public int getLengthOfKeyword() {
        return keyword.length();
    }

    public String getBlurKeyword() {
        return blurKeyword;
    }

    public void makeBlurKeyword(char guessChar) {
        String blurKey = this.blurKeyword;
        char[] ch = this.keyword.toCharArray();
        if (blurKey == "") {

            for (int i = 0; i < ch.length; i++) {
                if (ch[i] == guessChar) {
                    blurKey += ch[i];
                } else {
                    blurKey += '#';
                }
            }
        } else {
            char[] temp = blurKey.toCharArray();
            blurKey = "";

            for (int i = 0; i < temp.length; i++) {
                if (temp[i] != '#') {
                    blurKey += temp[i];
                } else if (ch[i] == guessChar) {
                    blurKey += ch[i];

                } else {
                    blurKey += '#';
                }
            }
        }

        this.blurKeyword = blurKey;
    }

    private boolean checkGuessCharSeq(char ch) {
        char[] tmp = guessCharSeq.toCharArray();
        for (int i = 0; i < tmp.length; i++) {
            if (ch == tmp[i]) {
                return true;
            }
        }

        return false;
    }

    public void addGuessCharSeq(char ch) {
        this.guessCharSeq = this.guessCharSeq + ch;
    }

    public int guessChar(String guessChar) {
        guessChar = guessChar.toLowerCase();
        int count = 0;
        if (guessChar.equals("")) {
            return count;
        }
        //kiểm tra xem ký tự này đã được đoán hay chưa
        if (checkGuessCharSeq(guessChar.charAt(0))) {
            return count;
        }

        char[] temp = keyword.toCharArray();
        for (int i = 0; i < temp.length; i++) {
            if (temp[i] == guessChar.charAt(0)) {
                count++;
            }
        }

        if (count != 0) {
            makeBlurKeyword(guessChar.charAt(0));
        }

        return count;
    }

    public boolean guessKey(String key) {
        key = key.toLowerCase();
        if (keyword.equals(key)) {
            return true;
        }
        return false;
    }

}
