package com.mygdx.game;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Hexlist implements Serializable{
	private static final long serialVersionUID = -4053194975249923091L;
	private HashMap<Integer, HashMap<Integer, HashMap<Integer, hex>>> map;
	
	public Hexlist(){
		map = new HashMap<Integer, HashMap<Integer, HashMap<Integer, hex>>>();
	}
	
	public void put(int iin, int jin, int kin, hex hexin){
		if (map.get(iin)==null){
			map.put(iin,new HashMap<Integer,HashMap<Integer, hex>>());
		}
		if (map.get(iin).get(jin)==null){
			map.get(iin).put(jin, new HashMap<Integer, hex>());
		}
		map.get(iin).get(jin).put(kin, hexin);
	}
	
	public hex nullget(int iin, int jin, int kin){
		if (map.get(iin)==null || map.get(iin).get(jin)==null || map.get(iin).get(jin).get(kin)==null){
			return null;
		}
		return map.get(iin).get(jin).get(kin);
	}
	
	public ArrayList<hex> retlist(){
		ArrayList<hex> templist = new ArrayList<hex>();
		for(int k1:map.keySet()){
			for(int k2:map.get(k1).keySet()){
				for(int k3:map.get(k1).get(k2).keySet()){
					templist.add(map.get(k1).get(k2).get(k3));
				}
			}
		}
		return templist;
	}
	
	public ArrayList<hex> rangelist(hex hexin){//for indiscriminant range
		int curi=hexin.getI(),curj=hexin.getJ(),curk=hexin.getK();
		int rangein=hexin.getunit().getrange();
		ArrayList<hex> templist = new ArrayList<hex>();
		int k=0;
		for(int i=-rangein;i<=rangein;i++){
			for(int j=Math.max(-rangein, -i-rangein);j<=Math.min(rangein,-i+rangein);j++){
				k=-i-j;
				
				if (map.get(curi+i)!=null && map.get(curi+i).get(curj+j)!=null && map.get(curi+i).get(curj+j).get(curk+k)!=null){
					templist.add(map.get(curi+i).get(curj+j).get(curk+k));
				}
			}
		}
		return templist;
	}
	
	public ArrayList<hex> movelist(hex hexin){
		return this.recursiveneighbors(hexin,1,hexin.getunit().getrange());
	}
	
	private ArrayList<hex> recursiveneighbors(hex hexin, int mode, int level){//mode 1 is movement
		int curi=hexin.getI(),curj=hexin.getJ(),curk=hexin.getK();
		ArrayList<hex> templist = new ArrayList<hex>();
		hex temphex=null;
		for(int i=curi-1;i<=curi+1;i++){
			for(int j=curj-1;j<=curj+1;j++){
				for(int k=curk-1;k<=curk+1;k++){
					temphex=this.nullget(i, j, k);
					if (temphex!=null && (mode!=1 || (temphex.type==1 || temphex.type==2))){
						templist.add(temphex);
					}
				}
			}
		}
		if (level!=1){
			ArrayList<hex> retlist = new ArrayList<hex>();
			ArrayList<hex> temptemplist = new ArrayList<hex>();
			retlist.addAll(templist);
			for(hex curhex:templist){
				temptemplist=this.recursiveneighbors(curhex, 1, level-1);
				for (hex curcurhex:temptemplist){
					if(!retlist.contains(curcurhex)){retlist.add(curcurhex);}
				}
			}
			return retlist;
		}
		return templist;
	}
	
	public ArrayList<hex> sightlist(hex hexin){
		int curi=hexin.getI(),curj=hexin.getJ(),curk=hexin.getK();
		int rangein=hexin.getunit().getsight();
		ArrayList<hex> templist = new ArrayList<hex>();
		ArrayList<hex> blocklist = new ArrayList<hex>();
		if (hexin.gettype()==2){return templist;}
		hex temphex;
		int k=0;
		for(int i=-rangein;i<=rangein;i++){
			for(int j=Math.max(-rangein, -i-rangein);j<=Math.min(rangein,-i+rangein);j++){
				k=-i-j;
				temphex=nullget(curi+i,curj+j,curk+k);
				if(temphex!=null){
					templist.add(temphex);
					if (temphex.type==2 || temphex.type==3){
						blocklist.add(temphex);
					}
				}
			}
		}
		
		ArrayList<hex> removelist = new ArrayList<hex>();
		for(hex checkhex:templist){
			for(hex rothex:blocklist){
				if(checkhex.gettype()==2){removelist.add(checkhex);}
				if (checkhex==rothex){break;}
				if (blocked(hexin, checkhex, rothex)){removelist.add(checkhex);}
			}
		}
		for(hex remhex:removelist){templist.remove(remhex);}
		return templist;
	}
	
	
	public ArrayList<hex> fullsightlist(int sidein){
		//get list of unithexes
		//compile within range hexes (check for dups)
		//perform LOS check to cull list
		//return final list
		
		ArrayList<hex> totalist = this.retlist();
		ArrayList<hex> hexunitlist = new ArrayList<hex>();
		for(hex temphex:totalist){
			if (temphex.occupied() && temphex.getunit().sameside(sidein)){hexunitlist.add(temphex);}
		}
		ArrayList<hex> retlist = new ArrayList<hex>();
		for(hex unithex:hexunitlist){
			ArrayList<hex> perunitsightlist = this.sightlist(unithex);
			retlist=addlists(retlist,perunitsightlist);
		}
		//now we have to cull nonunitlist based on hexunitlist and 
		
		return retlist;
	}
	
	public ArrayList<hex> addlists(ArrayList<hex> orilist, ArrayList<hex> addlist){
		for(hex temphex:addlist){
			if(!orilist.contains(temphex)){orilist.add(temphex);}
		}
		return orilist;
	}
	
	public ArrayList<hex> foglist(int sidein){
		//inverse of sight
		return this.invertlist(this.fullsightlist(sidein));
	}
	private ArrayList<hex> invertlist(ArrayList<hex> listin){
		ArrayList<hex> tempretlist = new ArrayList<hex>();
		for(hex temphex:this.retlist()){
			if(!listin.contains(temphex)){tempretlist.add(temphex);}
		}
		return tempretlist;
	}
	
	private Boolean blocked(hex starthex, hex endhex, hex midhex){
		double xs=starthex.getX();
		double ys=starthex.getY();
		double xe=endhex.getX();
		double ye=endhex.getY();
		double xm=midhex.getX();
		double ym=midhex.getY();
		if(((xs<=xm && xm<=xe)||(xe<=xm && xm<=xs) || (xs==xe)) && ((ys<=ym && ym<=ye)||(ye<=ym && ym<=ys)|| (ys==ye))){
			return LOS(starthex, endhex, midhex)<40;
		}
		return false;
	}
	
	private double LOS(hex starthex, hex endhex, hex midhex){
		//might be easier way to calcaulate 2d vector projection
		//double theta = Math.acos((Math.pow(hexd(starthex,endhex), 2)+Math.pow(hexd(midhex,starthex), 2)-Math.pow(hexd(midhex,endhex), 2))/(2*hexd(starthex,endhex)*hexd(midhex,starthex)));
		//return Math.sin(theta)*hexd(starthex,midhex);
		//https://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
		double dse=hexd(starthex,endhex);
		double dem=hexd(endhex,midhex);
		double dms=hexd(midhex,starthex);
		double s=(dse+dem+dms)/2;
		double A=Math.sqrt(s*(s-dse)*(s-dem)*(s-dms));
		return 2*A/dse;
		
	}
	private double hexd(hex hex1, hex hex2){
		return Math.sqrt(Math.pow(hex2.getY()-hex1.getY(),2)+Math.pow(hex2.getX()-hex1.getX(),2));
	}
	
}