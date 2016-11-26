package com.mygdx.game;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class AGENCY extends ApplicationAdapter implements InputProcessor {
	SpriteBatch batch;
	Texture texture_blank,texture_green,texture_fog,texture_red,texture_striped,texture_solid,texture_unitgreen,texture_unitred,texture_select;
	Sprite sprite_blank,sprite_green,sprite_fog,sprite_red,sprite_striped,sprite_solid,sprite_unitgreen,sprite_unitred;
	OrthographicCamera camera;
	int scr_width=1280;
	int scr_height=800;
	int d=20;
	BitmapFont info_font;
	String info_str;
	Hexlist hexlist;
	ArrayList<Sprite> hexspritelist;
	ArrayList<Sprite> unitspritelist;
	ArrayList<Sprite> fogspritelist;
	Texture texture_turn;
	Sprite sprite_turn;
	hex cursor;
	hex select;
	ArrayList<Sprite> selectspritelist;
	int turn=9999;
	
	//networking
	int port;
	ObjectInputStream input;
	ObjectOutputStream output;
	Socket clientsocket;
	int side;
	boolean buildcomp=false;
	
	@Override
	public void create () {
		//networking
		hexspritelist = new ArrayList<Sprite>();
		unitspritelist = new ArrayList<Sprite>();
		fogspritelist = new ArrayList<Sprite>();
		selectspritelist = new ArrayList<Sprite>();
		batch = new SpriteBatch();
		texture_blank = new Texture("verthex.png");
		texture_striped = new Texture("verthex_striped.png");
		texture_solid = new Texture("verthex_solid.png");
		texture_red = new Texture("verthex_red.png");
		texture_green = new Texture("verthex_green.png");
		texture_fog = new Texture("verthex_fog_150.png");
		texture_unitgreen = new Texture("unit_green.png");
		texture_unitred = new Texture("unit_red.png");
		texture_select = new Texture("verthex_select.png");
		texture_turn = new Texture("point.png");
		sprite_turn = new Sprite(texture_turn,d*4,d*4);
		sprite_turn.setCenter(0, 0);
		info_font = new BitmapFont();
		info_font.setColor(Color.WHITE);
		info_str = "";
		camera = new OrthographicCamera();
		camera.setToOrtho(false,scr_width,scr_height);
		Gdx.input.setInputProcessor(this);
		port=33333;
		
		try {
			clientsocket = new Socket("192.168.1.223", port);
			output = new ObjectOutputStream(clientsocket.getOutputStream());
			output.flush();
			input = new ObjectInputStream(clientsocket.getInputStream());
		} catch (UnknownHostException e) {
			log("Error 1: "+e.toString());
		} catch (IOException e) {
			log("Error 2: "+e.toString());
		}
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Object fromserver=null;
						try {
							fromserver = input.readObject();
						} catch (ClassNotFoundException e) {
							log("Error 3: "+e.toString());
						}
						if (fromserver != null) {
							//DO STUFF WITH INPUT
							log(fromserver.getClass().getName());
							if (fromserver.getClass().getName()=="java.lang.String"){
								log((String)fromserver);
							}
							else if (fromserver.getClass().getName()=="java.lang.Integer"){
								if ((Integer)fromserver==1 || (Integer)fromserver==0){
									side=(Integer)fromserver;
									log("assigned to team :"+(Integer)fromserver);
								}
								else if((Integer)fromserver==1001 || (Integer)fromserver==1000){
									turn = (Integer)fromserver-1000;
									log("turn received: "+turn);
									sprite_turn.setCenter(0, (side==turn ? 1:0)*500);
								}
							}
							else if (fromserver.getClass().getName()=="com.mygdx.game.Hexlist"){
								hexlist=(Hexlist)fromserver;
								hexlistspritebuild();								
							}
	                    }
					}catch(EOFException e){//log("Error 3.5: "+e.toString());e.printStackTrace();
					}catch (IOException e) {log("Error 4: "+e.toString());e.printStackTrace();break;}
                }
			}			
		}).start();
		
	}

	
	
	@Override
	public void render () {
		
		
		camera.update();
		Gdx.gl.glClearColor(0.95f, 0.9f, 0.9f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		//hex
		for(Sprite tempsprite:hexspritelist){tempsprite.draw(batch);}
		//unit
		for(Sprite tempsprite:unitspritelist){tempsprite.draw(batch);}
		//fog
		for(Sprite tempsprite:fogspritelist){tempsprite.draw(batch);}
		//select
		for(Sprite tempsprite:selectspritelist){tempsprite.draw(batch);}
		//cursor
		if (cursor!=null){
			Sprite tempsprite = new Sprite(texture_select,d*4,d*4);
			tempsprite.setCenter((float)cursor.getX(), (float)cursor.getY());
			tempsprite.draw(batch);
		}
		info_font.draw(batch,info_str,20,100);
		sprite_turn.draw(batch);
		batch.end();
	}
	
	@Override
	public void dispose () {
		batch.dispose();
	}

	public void hexlistspritebuild(){
		ArrayList<hex> foglist=hexlist.foglist(side);
		ArrayList<Sprite> temphexspritelist = new ArrayList<Sprite>();
		ArrayList<Sprite> tempunitspritelist = new ArrayList<Sprite>();
		ArrayList<Sprite> tempfogspritelist = new ArrayList<Sprite>();
		for(hex temphex:hexlist.retlist()){
			//0(Random), 1(White), 2(Striped), 3(Black)
			if (temphex.gettype()==1){
				Sprite tempsprite = new Sprite(texture_blank,d*4,d*4);
				tempsprite.setCenter((float)temphex.getX(), (float)temphex.getY());
				temphexspritelist.add(tempsprite);
			}
			else if (temphex.gettype()==2){
				Sprite tempsprite = new Sprite(texture_striped,d*4,d*4);
				tempsprite.setCenter((float)temphex.getX(), (float)temphex.getY());
				temphexspritelist.add(tempsprite);
			}
			else if (temphex.gettype()==3){
				Sprite tempsprite = new Sprite(texture_solid,d*4,d*4);
				tempsprite.setCenter((float)temphex.getX(), (float)temphex.getY());
				temphexspritelist.add(tempsprite);
			}
			
			if (temphex.occupied()){
				if(temphex.getunit().sameside(side)){
					Sprite tempsprite = new Sprite(texture_unitgreen,d*2,d*2);
					tempsprite.setCenter((float)temphex.getX(), (float)temphex.getY());
					tempunitspritelist.add(tempsprite);
				}
				else if (!foglist.contains(temphex)){
					Sprite tempsprite = new Sprite(texture_unitred,d*2,d*2);
					tempsprite.setCenter((float)temphex.getX(), (float)temphex.getY());
					tempunitspritelist.add(tempsprite);
				}
			}
		}
		for (hex temphex:foglist){
			Sprite tempsprite = new Sprite(texture_fog,d*4,d*4);
			tempsprite.setCenter((float)temphex.getX(), (float)temphex.getY());
			tempfogspritelist.add(tempsprite);
		}
		hexspritelist = temphexspritelist;
		unitspritelist = tempunitspritelist;
		fogspritelist = tempfogspritelist;
		buildcomp=true;
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (pointer > 0) return false;
		if (button == Input.Buttons.RIGHT){
			select=null;
			selectspritelist = new ArrayList<Sprite>();
		}
		else if (button == Input.Buttons.LEFT){
			hex prevselect=select;
			select = hexselect(screenX,scr_height-screenY);
			selectspritelist = new ArrayList<Sprite>();
			if (select==null){selectspritelist = new ArrayList<Sprite>();}
			else if (select.occupied()){//tile is occupied
				if(select.getunit().sameside(side)){ //own unit selected
					if (prevselect==select){//double click same unit so deselect
						select=null;
					}
					else{//selected other own unit
						for(hex temphex:hexlist.movelist(select)){
							Sprite tempsprite = new Sprite(texture_green,d*4,d*4);
							tempsprite.setCenter((float)temphex.getX(), (float)temphex.getY());
							selectspritelist.add(tempsprite);
						}
					}
				}
				else{ //opponent unit selected;
					if(prevselect!=null && prevselect.occupied() && prevselect.getunit().sameside(side) && hexlist.movelist(prevselect).contains(select) && side==turn){
						select.getunit().kill();
						prevselect.getunit().setloc(select);
						select=null;
						passturn();
					}
					else{
						for(hex temphex:hexlist.movelist(select)){
							Sprite tempsprite = new Sprite(texture_red,d*4,d*4);
							tempsprite.setCenter((float)temphex.getX(), (float)temphex.getY());
							selectspritelist.add(tempsprite);
						}
					}
				}
			}
			else if(prevselect!=null && prevselect.occupied() && prevselect.getunit().sameside(side) && hexlist.movelist(prevselect).contains(select) && side==turn){
				//tile selected
				prevselect.getunit().setloc(select);
				select=null;
				passturn();
			}
			else{//other tile selected
				
			}
			//redraw
			hexlistspritebuild();
			return true;
		}
		return false;
	}
	
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		cursor=hexselect(screenX,scr_height-screenY);
		if(cursor!=null){info_str=cursor.toString();}
		else{info_str="";}
		return true;
	}
	
	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}
	@Override
	public boolean scrolled(int amount) {
		// TODO Auto-generated method stub
		return false;
	}
	public void log(String msg){
		System.out.println("Client: "+msg);
	}
	private hex hexselect(int xin, int yin){
		double mind=1600;
		double tempd=0;
		if (!buildcomp){return null;}
		hex rethex = null;
		for(hex temphex:hexlist.retlist()){
			tempd=Math.pow(temphex.getX()-xin,2)+Math.pow(temphex.getY()-yin,2);
			if(tempd<mind){
				rethex=temphex;
				mind=tempd;
			}
		}
		return rethex;
	}
	private void passturn(){
		log("turnpassed");
		try {
			output.writeObject(1-turn+1000);
			output.writeObject(hexlist);
		} catch (IOException e) {
			log("Error 5: "+e.toString());
		}
	}
}
