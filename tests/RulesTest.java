package cn.shanhai.game;
import java.io.*;import java.util.*;
public class RulesTest {
 static int checks=0;static void ok(boolean v,String s){checks++;if(!v)throw new AssertionError(s);}
 static Engine blank(){Engine e=new Engine(0,42);for(Engine.Player p:e.p){e.discard.addAll(p.hand);p.hand.clear();}return e;}
 static void give(Engine e,int who,int k){int at=e.deck.indexOf(k);if(at>=0)e.deck.remove(at);else e.discard.remove((Integer)k);e.p[who].hand.add(k);}
 public static void main(String[] args)throws Exception{
 Engine e=blank();give(e,0,0);give(e,1,1);ok(!e.play(0,2),"cannot attack ally");ok(e.play(0,1),"attack legal");ok(e.p[1].hp==4,"dodge negates");give(e,0,0);ok(!e.play(0,1),"one attack per turn");
 e=blank();e.p[0].hero=1;give(e,0,0);give(e,0,8);ok(e.skill(1),"fury skill");ok(!e.skill(1),"one skill");e.play(1,1);e.play(0,1);ok(e.p[1].hp==1,"weapon plus fury damage");ok(!e.fury&&e.p[0].weapon==0,"one shot consumed");
 e=blank();e.turn=1;give(e,1,0);give(e,0,1);e.play(0,0);ok(e.phase==1&&e.responder()==0,"human response pauses");e.response(true);ok(e.p[0].hp==4&&e.phase==0,"human dodge resumes");
 e=blank();e.turn=1;give(e,1,5);give(e,0,0);e.play(0,0);ok(e.phase==1&&e.required()==0,"duel requires attack");e.response(true);ok(e.p[1].hp==3,"duel loser damaged");
 e=blank();e.p[1].shield=1;give(e,0,0);e.play(0,1);ok(e.p[1].hp==4&&e.p[1].shield==0,"shield absorbed");
 e=blank();e.p[1].hp=1;give(e,1,2);give(e,0,0);e.play(0,1);ok(e.p[1].hp==1&&e.count(1,2)==0,"automatic rescue");
 e=blank();give(e,0,6);e.play(0,1);ok(e.p[1].hp==3&&e.p[3].hp==3&&e.p[2].hp==4,"storm hits enemies only");
 e=blank();for(int i=0;i<6;i++)give(e,0,1);e.end();ok(e.phase==2,"discard entered");e.drop(0);ok(e.phase==2,"still over limit");e.drop(0);ok(e.turn==1&&e.phase==0,"discard ends turn");
 e=blank();e.p[1].hp=e.p[3].hp=1;give(e,0,6);e.play(0,1);ok(e.phase==3&&e.winner==0,"victory terminal");
 e=new Engine(2,123);ByteArrayOutputStream out=new ByteArrayOutputStream();ObjectOutputStream os=new ObjectOutputStream(out);os.writeObject(e);os.close();Engine copy=(Engine)new ObjectInputStream(new ByteArrayInputStream(out.toByteArray())).readObject();ok(copy.p[0].hand.equals(e.p[0].hand)&&copy.totalCards()==114,"save round trip");

 e=blank();give(e,0,11);ok(!e.play(0,1),"gift rejects enemy");e.play(0,2);ok(e.p[2].hand.size()==2,"gift draws for ally");
 e=blank();e.p[2].hp=1;give(e,0,13);e.play(0,2);ok(e.p[2].hp==3,"bond heals ally");
 e=blank();e.p[1].shield=e.p[1].weapon=1;give(e,1,1);give(e,0,10);e.play(0,1);ok(e.p[1].shield==0&&e.p[1].weapon==0&&e.p[1].hand.isEmpty(),"purge removes equipment and card");
 e=blank();give(e,0,12);e.play(0,1);ok(e.p[1].frozen,"frost mark");int before=e.p[1].hand.size();e.end();ok(e.turn==1&&!e.p[1].frozen&&e.p[1].hand.size()==before,"frost suppresses next draw only");
 int max=0,wins=0;for(int seed=0;seed<1000;seed++){e=new Engine(seed%4,seed);int steps=0;while(e.phase!=3&&steps++<15000){if(e.phase==1&&e.responder()==0)e.response(e.count(0,e.required())>0);else e.ai();ok(e.totalCards()==114,"card conservation "+seed);for(Engine.Player p:e.p)ok(p.hp>=0&&p.hp<=p.max,"hp bounds");}ok(e.phase==3,"simulation terminates "+seed);max=Math.max(max,steps);if(e.winner==0)wins++;}System.out.println("PASS "+checks+" assertions; 1000 complete games; max steps="+max+"; team0 wins="+wins);
 }
}
