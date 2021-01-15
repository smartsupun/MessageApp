//package com.example.messageapp;
//
//import android.content.Intent;
//import android.os.Bundle;
//
//import androidx.appcompat.app.AppCompatActivity;
//
//public class splashScreen extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        setContentView(R.layout.activity_splash_screen);
//
//        Thread thread = new Thread(){
//
//            public void run(){
//                try{
//                    sleep(3000);
//
//                }
//                catch (Exception e) {
//                    e.printStackTrace();
//
//                }
//                finally {
//                    Intent intent = new Intent(com.example.Message.splashScreen.this ,  MainActivity.class);
//                    startActivity(intent);
//                    finish();
//                }
//
//            }
//        };thread.start();
//
//    }
//}
