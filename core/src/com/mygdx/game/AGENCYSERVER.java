package com.mygdx.game;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class AGENCYSERVER {
	private static int port=33333;
	public static void main(String[] args) throws Exception{
		ServerSocket listener = new ServerSocket(port);
		System.out.println("Server up and running @ port: "+port);
		try {
            while (true) {
            	gameinstance newgame= new gameinstance();
            	newgame.create();
            	newgame.player0 = newgame.new Player(listener.accept(),0);
            	newgame.player1 = newgame.new Player(listener.accept(),1);
            	newgame.player0.setopponent(newgame.player1);
            	newgame.player1.setopponent(newgame.player0);
            	newgame.player0.start();
            	newgame.player1.start();
                //new AGENCYSERVERTHREAD(listener.accept(),'1').start();
            }
        } finally {
            listener.close();
        }
	}
}

class gameinstance{
	int turn=9999;
	//board
	Player player1;
	Player player0;
	Hexlist hexlist;
	int scr_width=1280;
	int scr_height=800;
	
	public void create(){
		hexlist = new Hexlist();
		if(Math.random() < 0.5){turn=0;}else{turn=1;};
		int d=20;
		int xr=10, yr=10, zr=10;
		for(int i=-xr;i<=xr;i++){
			for(int j=-yr;j<=yr;j++){
				for(int k=-zr;k<=zr;k++){
					if(i+j+k==0 & Math.abs(i*d*3)<=scr_width*3.4/8 & Math.abs((j-k)*d*Math.sqrt(3))<=scr_height*3.2/8){
						hex temphex = new hex(i,j,k,d,0);
						hexlist.put(i,j,k,temphex);
					}
				}
			}
		}
		createunits();
	}
	public void createunits(){
		new unit(hexlist.nullget(-2,-3, 5),3,2,0);
		new unit(hexlist.nullget( 0,-4, 4),3,2,0);
		new unit(hexlist.nullget( 2,-5, 3),3,2,0);
		new unit(hexlist.nullget(-2, 5,-3),3,2,1);
		new unit(hexlist.nullget( 0, 4,-4),3,2,1);
		new unit(hexlist.nullget( 2, 3,-5),3,2,1);
	}
	
	class Player extends Thread{
		int side;
		Socket socket;
		ObjectInputStream input;
		ObjectOutputStream output;
		Player opponent=null;
		
		public void setopponent(Player opponentin){
			opponent=opponentin;
		}
		
		public Player(Socket socketin, int sidein){
			socket=socketin;
			side=sidein;
			try {
				output = new ObjectOutputStream(socket.getOutputStream());
				output.flush();
				input = new ObjectInputStream(socket.getInputStream());
                log("Player " + socketin.toString() + "is player: "+side);
                output.writeObject("Welcome " + socketin.toString() + "you are player: "+side);
                output.writeObject(side);
                output.writeObject(1000+turn);
                output.writeObject(hexlist);
            } catch (IOException e) {
                log("Error 1: " + e);
            }
		}
		public void passturn(){
			try {
				output.writeObject(turn+1000);
			} catch (IOException e) {
				log("Error 3: "+e.toString());
			}
		}
		private void sendhexlist(){
			try {
				output.writeObject(hexlist);
			} catch (IOException e) {
				log("Error 3: "+e.toString());
			}
		}
		public void run(){
			while(true){
				Object fromclient=null;
				try {
					fromclient = input.readObject();
				} catch (ClassNotFoundException e) {
					log("Error 2: "+e.toString());
				} catch (IOException e){//client disconnected
					log("Error 2.5: "+e.toString());
					try {
						input.close();
						output.close();
						socket.close();
						opponent.output.writeObject(-1);
						opponent.input.close();
						opponent.output.close();
						opponent.socket.close();
						break;
					} catch (IOException e1) {log("Error 2.6: "+e1.toString());break;}
				}
				if (fromclient != null) {
					//DO STUFF WITH INPUT
					log(fromclient.getClass().getName());
					if (fromclient.getClass().getName()=="java.lang.String"){
						log((String)fromclient);
					}
					else if (fromclient.getClass().getName()=="java.lang.Integer"){
						if((Integer)fromclient==1001 || (Integer)fromclient==1000){
							turn = (Integer)fromclient-1000;
							log("turn received: "+turn);
							this.passturn();
							opponent.passturn();
						}
					}
					else if (fromclient.getClass().getName()=="com.mygdx.game.Hexlist"){
						hexlist=(Hexlist)fromclient;
						this.sendhexlist();
						opponent.sendhexlist();
					}
				}
			}
		}
	}
	
	private static void log(String msg){
		System.out.println("Server: "+ msg);
	}
}
