package cn.shanhai.game;
import java.io.*;
import java.util.*;

/** Pure Java deterministic rules. No Android/UI dependencies. */
public final class Engine implements Serializable {
 public static final long serialVersionUID=2;
 public static final String[] HERO={"青丘","应龙","精卫","烛阴"}, TITLE={"九尾 · 惑心","翼龙 · 司雨","誓海 · 不息","钟山 · 昼夜"}, ART={"qingqiu","yinglong","jingwei","zhuyin"};
 public static final String[] SKILL={"摄心","龙威","衔石","昼夜"};
 public static final String[] SKILL_DESC={"每回合一次，夺取一名敌人的一张手牌。","每回合一次，本回合下一张破阵伤害 +1。","每回合一次，回复 1 点体力；满体力则摸 1 张牌。","每回合一次，摸 2 张牌，然后失去 1 点体力（体力须大于 1）。"};
 public static final String[] NAME={"破阵","流影","甘露","天启","夺魄","问鼎","风雷","玄甲","龙牙","归元","破障","援契","封潮","共生"};
 public static final String[] DESC={"对一名敌人造成 1 点伤害；每回合限一次，可用流影响应。","响应破阵或风雷，抵消这次攻击。","回复 1 点体力；濒危时自动使用自救。","立即摸 2 张牌。","夺取一名敌人的一张手牌。","与敌人交替打出破阵；先放弃者受到 1 点伤害。","依次对所有存活敌人造成 1 点伤害；可用流影响应。","装备：下次受到伤害时减免 1 点，随后消失。","装备：破阵伤害 +1，使用破阵后消失。","回复 2 点体力。","移除敌人的装备，并弃置其一张随机手牌。","令一名其他存活同伴摸 2 张牌。","令一名敌人的下次摸牌阶段少摸 2 张牌，不可叠加。","令一名其他存活同伴回复 2 点体力。"};
 public static final int ATTACK=0,DODGE=1,HEAL=2,DRAW=3,STEAL=4,DUEL=5,STORM=6,SHIELD=7,WEAPON=8,RESTORE=9,PURGE=10,GIFT=11,FROST=12,BOND=13;
 public static class Player implements Serializable {public int hero,hp=4,max=4,shield=0,weapon=0; public boolean frozen=false; public ArrayList<Integer> hand=new ArrayList<>();public boolean alive(){return hp>0;}}
 public static class Beat implements Serializable {public String kind,text;public int from,to,card,value;Beat(String k,int a,int b,int c,int v,String t){kind=k;from=a;to=b;card=c;value=v;text=t;}}
 public ArrayList<Beat> beats=new ArrayList<>();
 public void beat(String k,int a,int b,int c,int v,String t){beats.add(new Beat(k,a,b,c,v,t));if(beats.size()>80)beats.remove(0);}
 public Player[] p=new Player[4]; public ArrayList<Integer> deck=new ArrayList<>(),discard=new ArrayList<>(); public ArrayList<String> log=new ArrayList<>();
 public Random rng; public int turn=0,round=1,phase=0; // 0 action, 1 response, 2 discard, 3 finished
 public boolean attacked=false,skillUsed=false,fury=false; public int pendingSource=-1,pendingTarget=-1,pendingDamage=1,pendingCard=-1,duelResponder=-1;
 public ArrayList<Integer> stormTargets=new ArrayList<>(); public int winner=-1;public String event="山海开卷",notice="选择手牌，指定目标，再点出牌";public long revision=0;
 public Engine(int hero,long seed){rng=new Random(seed);for(int i=0;i<4;i++){p[i]=new Player();p[i].hero=(hero+i)%4;} for(int i=0;i<24;i++)deck.add(0);for(int i=0;i<18;i++)deck.add(1);for(int i=0;i<10;i++)deck.add(2);for(int i=3;i<10;i++)for(int j=0;j<6;j++)deck.add(i);for(int i=10;i<14;i++)for(int j=0;j<5;j++)deck.add(i);Collections.shuffle(deck,rng);for(int i=0;i<4;i++)draw(i,4);draw(0,2);beats.clear();beat("turn",0,0,-1,0,"你的回合");say("山海开卷 · 你与"+name(2)+"结盟");}
 public String name(int i){return HERO[p[i].hero];} public boolean enemy(int a,int b){return a%2!=b%2;} public void say(String s){event=s;log.add(s);if(log.size()>60)log.remove(0);revision++;}
 public void draw(int who,int n){beat("draw",who,who,-1,n,name(who)+"摸 "+n+" 张牌");for(int i=0;i<n;i++){if(deck.isEmpty()){deck.addAll(discard);discard.clear();Collections.shuffle(deck,rng);}if(!deck.isEmpty())p[who].hand.add(deck.remove(deck.size()-1));}}
 public int count(int who,int card){return Collections.frequency(p[who].hand,card);} public void consume(int who,int card){p[who].hand.remove((Integer)card);discard.add(card);}
 public boolean needsTarget(int card){return card==ATTACK||card==STEAL||card==DUEL||card>=10;}
 public String reason(int idx,int target){
  if(phase!=0)return "当前不在出牌阶段";if(idx<0||idx>=p[turn].hand.size())return "请选择一张手牌";
  int k=p[turn].hand.get(idx);if(k==DODGE)return "流影用于来袭时响应";if(k==ATTACK&&attacked)return "本回合破阵次数已用完";
  if((k==HEAL||k==RESTORE)&&p[turn].hp>=p[turn].max)return "你已满体力";
  if(k==SHIELD&&p[turn].shield>0||k==WEAPON&&p[turn].weapon>0)return "同类装备已生效";
  if(needsTarget(k)){if(target<0||target>3||!p[target].alive())return "请选择存活目标";
   if(k==GIFT||k==BOND){if(target==turn||enemy(turn,target))return "请选择你的同伴";}else if(!enemy(turn,target))return "请选择敌方目标";
   if(k==STEAL&&p[target].hand.isEmpty())return "目标没有手牌";
   if(k==PURGE&&p[target].hand.isEmpty()&&p[target].shield==0&&p[target].weapon==0)return "目标没有可破除的牌";
   if(k==FROST&&p[target].frozen)return "目标已被封潮";
   if(k==BOND&&p[target].hp==p[target].max)return "同伴已满体力";
  }return "";
 }
 public boolean legal(int idx,int target){return reason(idx,target).isEmpty();}
 public boolean canSelect(int idx){for(int t=0;t<4;t++)if(legal(idx,t))return true;return false;}

 public boolean play(int idx,int target){if(!legal(idx,target))return false;int k=p[turn].hand.remove(idx);discard.add(k);beat("card",turn,needsTarget(k)?target:turn,k,0,name(turn)+" · "+NAME[k]);say(name(turn)+" · "+NAME[k]);switch(k){
 case ATTACK: attacked=true;int damage=1+p[turn].weapon+(fury?1:0);p[turn].weapon=0;fury=false;attack(turn,target,damage,k);break;
 case HEAL:p[turn].hp=Math.min(p[turn].max,p[turn].hp+1);beat("heal",turn,turn,k,1,"+1");break;
 case RESTORE:int restored=Math.min(2,p[turn].max-p[turn].hp);p[turn].hp+=restored;beat("heal",turn,turn,k,restored,"+"+restored);break;
 case DRAW:draw(turn,2);break;
 case STEAL:steal(turn,target);break;
 case DUEL:pendingSource=turn;pendingTarget=target;pendingCard=DUEL;duelResponder=target;phase=1;resolveAuto();break;
 case STORM:stormTargets.clear();for(int i=1;i<4;i++){int t=(turn+i)%4;if(p[t].alive()&&enemy(turn,t))stormTargets.add(t);}nextStorm();break;
 case PURGE:p[target].shield=p[target].weapon=0;if(!p[target].hand.isEmpty())discard.add(p[target].hand.remove(rng.nextInt(p[target].hand.size())));say(name(target)+"的装备被破除");break;
 case GIFT:draw(target,2);break;
 case FROST:p[target].frozen=true;say(name(target)+"被封潮 · 下次少摸两张");break;
 case BOND:p[target].hp=Math.min(p[target].max,p[target].hp+2);beat("heal",turn,target,k,2,name(target)+"恢复体力");break;
 case SHIELD:p[turn].shield=1;break;
 case WEAPON:p[turn].weapon=1;break;
 }checkOver();return true;}
 public void steal(int a,int b){if(!p[b].hand.isEmpty()){p[a].hand.add(p[b].hand.remove(rng.nextInt(p[b].hand.size())));say(name(a)+"夺取了"+name(b)+"的手牌");}}
 public boolean skill(int target){if(phase!=0||skillUsed)return false;int h=p[turn].hero;if(h==0&&(target<0||target>3||!enemy(turn,target)||!p[target].alive()||p[target].hand.isEmpty()))return false;if(h==3&&p[turn].hp<=1)return false;skillUsed=true;beat("skill",turn,h==0?target:turn,-1,0,SKILL[h]);say(name(turn)+" · "+SKILL[h]);if(h==0)steal(turn,target);if(h==1)fury=true;if(h==2){if(p[turn].hp<p[turn].max)p[turn].hp++;else draw(turn,1);}if(h==3){draw(turn,2);p[turn].hp--;}return true;}
 void attack(int src,int target,int dmg,int k){pendingSource=src;pendingTarget=target;pendingDamage=dmg;pendingCard=k;phase=1;resolveAuto();}
 public int responder(){return pendingCard==DUEL?duelResponder:pendingTarget;}
 public int required(){return pendingCard==DUEL?ATTACK:DODGE;}
 void resolveAuto(){while(phase==1&&responder()!=0){int r=responder();respond(count(r,required())>0);}}
 public boolean respond(boolean use){if(phase!=1)return false;int r=responder(),required=required();if(use&&count(r,required)==0)return false;if(use){consume(r,required);beat("guard",r,r,required,0,name(r)+" · "+NAME[required]);say(name(r)+"以"+NAME[required]+"响应");if(pendingCard==DUEL){duelResponder=(r==pendingTarget?pendingSource:pendingTarget);return true;}}else{int source=pendingCard==DUEL?(r==pendingTarget?pendingSource:pendingTarget):pendingSource;damage(r,pendingCard==DUEL?1:pendingDamage,source);}
 if(phase!=3){phase=0;if(pendingCard==STORM)nextStorm();else if(!p[turn].alive())advance();}return true;}
 public void response(boolean use){if(responder()==0&&respond(use))resolveAuto();}
 void nextStorm(){if(phase==3)return;if(stormTargets.isEmpty()){phase=0;if(!p[turn].alive())advance();return;}int t=stormTargets.remove(0);if(!p[t].alive()){nextStorm();return;}attack(turn,t,1,STORM);}
 void damage(int who,int value,int src){if(p[who].shield>0){value=Math.max(0,value-1);p[who].shield=0;say(name(who)+"的玄甲抵消 1 点伤害");}p[who].hp-=value;if(value>0)beat("hit",src,who,-1,value,"−"+value);if(value>0)say(name(who)+"受到 "+value+" 点伤害");while(p[who].hp<=0&&count(who,HEAL)>0){consume(who,HEAL);p[who].hp++;beat("heal",who,who,HEAL,1,"甘露自救");say(name(who)+"以甘露自救");}if(p[who].hp<=0){p[who].hp=0;beat("fall",who,who,-1,0,name(who)+"离场");discard.addAll(p[who].hand);p[who].hand.clear();say(name(who)+"离场");}checkOver();}
 void checkOver(){boolean a=p[0].alive()||p[2].alive(),b=p[1].alive()||p[3].alive();if(!a||!b){phase=3;winner=a?0:1;say(winner==0?"山海同契 · 我方获胜":"此战惜败 · 来日再会");}}
 public void end(){if(phase!=0)return;if(p[turn].hand.size()>p[turn].hp){phase=2;notice="弃置手牌至体力上限：还需 "+(p[turn].hand.size()-p[turn].hp)+" 张";}else advance();}
 public void drop(int idx){if(phase!=2||idx<0||idx>=p[turn].hand.size())return;discard.add(p[turn].hand.remove(idx));revision++;if(p[turn].hand.size()<=p[turn].hp)advance();else notice="还需弃置 "+(p[turn].hand.size()-p[turn].hp)+" 张";}
 void advance(){if(phase==3)return;do{turn=(turn+1)%4;if(turn==0)round++;}while(!p[turn].alive());phase=0;attacked=skillUsed=fury=false;beat("turn",turn,turn,-1,0,name(turn)+"的回合");if(p[turn].frozen){p[turn].frozen=false;say(name(turn)+"受封潮影响，本回合不摸牌");}else draw(turn,2);say(name(turn)+"的回合");notice=turn==0?"选择手牌，指定目标，再点出牌":"等待 "+name(turn)+" 行动";if(round>60){int a=p[0].hp+p[2].hp,b=p[1].hp+p[3].hp;winner=a>=b?0:1;phase=3;say("六十轮终局 · 按阵营剩余体力判定胜负");}}
 public int target(){return targetFor(ATTACK);}
 public int targetFor(int k){int best=-1;double score=-1e9;for(int i=0;i<4;i++)if(p[i].alive()&&i!=turn){boolean support=k==GIFT||k==BOND;if(support==enemy(turn,i))continue;double v=10-p[i].hp;
  if(k==STEAL)v=p[i].hand.size()*2;else if(k==PURGE)v=4*p[i].shield+4*p[i].weapon+p[i].hand.size();else if(k==FROST)v=p[i].frozen?-100:5+p[i].hp;else if(k==GIFT)v=10-p[i].hand.size();else if(k==BOND)v=(p[i].max-p[i].hp)*4;else v-=p[i].shield*1.5;
  if(v>score){score=v;best=i;}}
 return best;}
 public void ai(){if(phase==3)return;if(phase==1){resolveAuto();return;}if(phase==2){int worst=0;double score=1e9;for(int j=0;j<p[turn].hand.size();j++){int k=p[turn].hand.get(j);double v=k==DODGE?8:(k==HEAL?9:4);if(k==ATTACK&&count(turn,ATTACK)>1)v=1;if(k==BOND&&!p[(turn+2)%4].alive())v=0;if(v<score){score=v;worst=j;}}drop(worst);return;}
 if(!skillUsed&&skill(targetFor(STEAL)))return;
 int best=-1,bt=-1;double score=-1;
 for(int i=0;i<p[turn].hand.size();i++){int k=p[turn].hand.get(i),t=targetFor(k);if(!legal(i,t))continue;double v=5;
  if(k==HEAL||k==RESTORE)v=15+(p[turn].max-p[turn].hp);if(k==BOND)v=13+(p[t].max-p[t].hp);
  if(k==DRAW)v=12;if(k==SHIELD)v=11;if(k==WEAPON)v=attacked?1:10;if(k==PURGE)v=p[t].shield+p[t].weapon>0?14:4;
  if(k==ATTACK)v=9+(p[t].hp==1?8:0);if(k==DUEL){v=3+count(turn,ATTACK)*2-p[t].hand.size()*.5;if(p[turn].hp==1&&count(turn,ATTACK)==0)v=-2;}
  if(k==GIFT)v=7;if(k==FROST)v=8;if(v>score){score=v;best=i;bt=t;}}
 if(best>=0)play(best,bt);else end();}

 public int totalCards(){int n=deck.size()+discard.size();for(Player q:p)n+=q.hand.size();return n;}
}
