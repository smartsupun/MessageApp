package com.example.messageapp;

import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESEngine {
    private static AESEngine aesEngine;
    private String key = "test1234";


    public static AESEngine getInstance(){
        if(aesEngine==null){
            aesEngine = new AESEngine();
        }
        return aesEngine;
    }

    public String encryptText(String text){
        return encrypt(text,genKey(key));
    }

    public String decryptText(String cipher){
        return decrypt(cipher,genKey(key));
    }
    private SecretKeySpec genKey(String key) {
        byte[] sKey = key.getBytes();
        sKey = Arrays.copyOf(sKey, 32);
        SecretKeySpec secretKeySpec = new SecretKeySpec(sKey, "AES");
        return secretKeySpec;
    }

    private String encrypt(String text, SecretKeySpec key) {
        byte[] msg = text.getBytes();
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byte [] arr = cipher.doFinal(msg);
            StringBuilder sb = new StringBuilder();
            for (byte val :arr) {
                sb.append((char)val);
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String decrypt(String text, SecretKeySpec key) {
        byte[] cipherText = new byte [text.length()];
        for(int i = 0;i<text.length();i++){
            cipherText[i] = (byte) text.charAt(i);
        }

        try {

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] textbyte = cipher.doFinal(cipherText);
            String finalText = "";
            for (byte i : textbyte) {
                byte[] arr = {i};
                finalText += new String(arr);
            }
            return finalText;

        } catch (Exception e) {
            e.printStackTrace();
            return text;
        }

    }
}
