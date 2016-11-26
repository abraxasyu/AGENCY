package com.mygdx.game;

import java.io.Serializable;

//import com.badlogic.gdx.graphics.Texture;
//import com.badlogic.gdx.graphics.g2d.Sprite;

public class hex implements Serializable{
	private static final long serialVersionUID = 3683760632841098717L;
	int i,j,k;
	double x,y;
	unit hexunit;
	int type;//0(Random), 1(White), 2(Striped), 3(Black)
	boolean fog;
	
	public hex(int iin, int jin, int kin, int din,int typein)
	{
		i=iin;
		j=jin;
		k=kin;
		x=i*din*3+1280/2;
		y=(j-k)*din*Math.sqrt(3)+800/2;
		type=typein;
		if (type==0){
			int randtype=(int) (Math.random()*100);
			//System.out.println(randtype);
			if (0<=randtype && randtype<10){type=2;}
			else if(10<=randtype && randtype<20){type=3;}
			else{type=1;}
		}
//		if(fog){sprite = new Sprite(new Texture("verthex_fog.png"),din*4,din*4);}
//		else if (type==3){sprite = new Sprite(new Texture("verthex_solid.png"),din*4,din*4);}
//		else if (type==2){sprite = new Sprite(new Texture("verthex_striped.png"),din*4,din*4);}
//		else if (type==1){sprite = new Sprite(new Texture("verthex.png"),din*4,din*4);}
//		else{System.out.println("improper hex type");}
//
//		sprite.setCenter((float)x,(float)y);
	}
	public int gettype(){return type;}
	public double getX(){return x;}
	public double getY(){return y;}
	public int getI(){return i;}
	public int getJ(){return j;}
	public int getK(){return k;}
	//public Sprite getsprite(){return sprite;}
	public void setunit(unit unitin){
		hexunit=unitin;
	}
	public unit getunit(){return hexunit;}
	public boolean occupied(){
		return (hexunit!=null);
	}
	public String toString(){
		return "("+i+","+j+","+k+")";
	}
	
}
