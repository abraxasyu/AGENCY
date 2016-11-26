package com.mygdx.game;

import java.io.Serializable;

//import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.graphics.g2d.Sprite;

public class unit implements Serializable{
	private static final long serialVersionUID = 3709460046319381630L;
	int sight;//sight
	int range;//range
	int side;
	hex unithex;
	
	public unit(hex spawnloc, int sightin, int rangein, int sidein){
		unithex=spawnloc;
		sight=sightin;range=rangein;side=sidein;
		//sprite = new Sprite(new Texture("unit.png"),40,40);
		//sprite.setCenter((float)unithex.getX(),(float)unithex.getY());
		unithex.setunit(this);
	}
	//public Sprite getsprite(){return sprite;}
	public int getsight(){return sight;}
	public int getrange(){return range;}
	public void setloc(hex hexin){
		unithex.setunit(null);
		unithex=hexin;
		unithex.setunit(this);
		//sprite.setCenter((float)unithex.getX(),(float)unithex.getY());
	}
	public void kill(){
		unithex.setunit(null);
		unithex=null;
	}
	public boolean sameside(int sidein){
		return side==sidein;
	}
}
