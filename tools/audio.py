"""Original layered instrument-like score and event foley. No sampled third-party audio."""
import numpy as np, wave, pathlib
from scipy.signal import butter,sosfilt
out=pathlib.Path(__file__).resolve().parents[1]/'app/src/main/assets/audio';out.mkdir(exist_ok=True)
sr=32000;rng=np.random.default_rng(14)
def save(name,a):
 if a.ndim==1:a=np.column_stack([a,a])
 peak=np.max(np.abs(a));a=a/max(1,peak/.85)
 with wave.open(str(out/(name+'.wav')),'w') as f:
  f.setparams((2,2,sr,len(a),'NONE','not compressed'));f.writeframes((np.clip(a,-.95,.95)*32767).astype('<i2').tobytes())
def tone(f,d=1.5,kind='pluck'):
 t=np.arange(int(sr*d))/sr
 if kind=='bell':return sum(np.sin(2*np.pi*f*m*t)*np.exp(-t*(1.6+i))*.45/(i+1) for i,m in enumerate([1,2.76,5.4]))*np.minimum(t*200,1)
 if kind=='flute':return (np.sin(2*np.pi*f*t+.025*np.sin(2*np.pi*5*t))+.13*np.sin(2*np.pi*f*2*t))*.35*np.sin(np.pi*t/d)**1.4
 return sum(np.sin(2*np.pi*f*(i+1)*t)*np.exp(-t*(2+i*.8))*.45/(i+1)**1.6 for i in range(6))*np.minimum(t*250,1)
def noise(d,lo,hi):
 a=rng.normal(0,1,int(sr*d));return sosfilt(butter(2,[lo,hi],btype='bandpass',fs=sr,output='sos'),a)
def reverb(a):
 b=a.copy()
 for sec,gain in [(.057,.18),(.113,.12),(.179,.08)]:
  n=int(sr*sec)
  if n<len(a):b[n:]+=a[:-n]*gain
 return b
length=72;a=np.zeros((length*sr,2));notes=[0,7,12,14,7,4,2,7,0,4,9,12,7,4,2,0,7,9,14,12,9,7,4,2]
def add(at,s,pan=.5,vol=1):
 st=int(at*sr);end=min(len(a),st+len(s));s=s[:end-st]*vol;a[st:end,0]+=s*np.sqrt(1-pan);a[st:end,1]+=s*np.sqrt(pan)
for phrase in range(3):
 for i,n in enumerate(notes):
  at=phrase*24+i;f=146.83*2**(n/12);add(at,reverb(tone(f,2.8)),.25 if i%2 else .7,.28)
  if i%4==0:add(at+.15,tone(f*2,3.5,'flute'),.52,.19)
 for j in range(4):
  root=146.83*2**([0,-3,-5,-2][j]/12);add(phrase*24+j*6,tone(root/2,5,'flute'),.5,.20)
a[:sr]*=np.linspace(0,1,sr)[:,None];a[-2*sr:]*=np.linspace(1,0,2*sr)[:,None];save('mountains',a)
for name,d,f in [('tap',.12,340),('select',.19,580),('error',.23,150)]:
 t=np.arange(int(sr*d))/sr;s=tone(f,d)*.25+noise(d,300,2600)*np.exp(-t*60)*.08;save(name,s)
t=np.arange(int(sr*.48))/sr;save('slash',noise(.48,700,7000)*np.sin(np.pi*t/.48)**3*.35+np.sin(2*np.pi*(130*t-50*t*t))*np.exp(-t*12)*.12)
t=np.arange(int(sr*.6))/sr;save('hit',np.sin(2*np.pi*(110*t-65*t*t))*np.exp(-t*10)*.5+noise(.6,80,1800)*np.exp(-t*25)*.13)
for name,frequencies in [('guard',[730,1095]),('heal',[440,550,660]),('cast',[294,440]),('equip',[330,830]),('skill',[220,330,440]),('turn',[440,660])]:
 s=sum(tone(f,1.3,'bell')*.19 for f in frequencies);save(name,reverb(s))
t=np.arange(int(sr*.32))/sr;save('draw',noise(.32,500,4500)*np.sin(np.pi*t/.32)**3*.13)
t=np.arange(int(sr*1.5))/sr;save('storm',noise(1.5,45,1000)*np.exp(-t*2)*.65+np.sin(2*np.pi*52*t)*np.exp(-t*4)*.16)
s=np.zeros(sr*3)
for i,n in enumerate([0,4,7,12]):
 w=tone(294*2**(n/12),1.5,'bell')*.26;st=int(i*.34*sr);s[st:st+len(w)]+=w
save('win',reverb(s));save('fall',tone(146.83,2,'bell')*.33)

# Ogg Vorbis keeps the long score compact; short SoundPool effects stay PCM.
import subprocess
subprocess.run(['ffmpeg','-hide_banner','-loglevel','error','-y','-i',str(out/'mountains.wav'),'-c:a','libvorbis','-q:a','6',str(out/'mountains.ogg')],check=True)
(out/'mountains.wav').unlink()
