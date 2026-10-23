"""Trilha da prévia do primeiro acesso: a abertura aprovada + os sons novos, com as peças do synth.py.
Uso: python3 audio.py saida.wav"""
import sys, json, subprocess
import numpy as np
from pathlib import Path
from scipy.io import wavfile

AQUI = Path(__file__).resolve().parent
PAC = AQUI.parents[1] / 'movimento'   # docs/lobbus/movimento (pacote da abertura)
sys.path.insert(0, str(PAC / 'ferramentas'))
sys.argv = [sys.argv[0]] + sys.argv[1:]
OUT = sys.argv[1] if len(sys.argv) > 1 else 'trilha.wav'
import synth as S
from synth import Mix, tick, blip, fm_bell, boom, noise_riser, whoosh, chord_pad, grains, midi, ts, saw, sweep_filter, hp, SR

T = json.load(open(AQUI.parent / 'prototipo' / 'primeiro.json'))
FIELD_IN = json.load(open(AQUI / 'field_in.json'))   # quando cada campo recebe os dados (window.FIELD_IN no protótipo)
S.rng = np.random.default_rng(2310)
D = T['videoEnd']
m = Mix(D)

# 1 · abertura aprovada (trilha A + Encaixe), inteira
wav = AQUI / 'abertura.wav'
subprocess.run(['ffmpeg', '-y', '-loglevel', 'error', '-i', str(PAC / 'sons' / 'lobbus_abertura.ogg'), '-ar', str(SR), '-ac', '2', str(wav)], check=True)
sr, ab = wavfile.read(wav); ab = ab.astype(np.float64) / 32768.0
m.dry[:, :len(ab)] += ab.T * 1.0

def ok(t): m.add(S.ui_ok(), t, 0.22, send2=0.15)

# 2 · Começar
ok(T['s1']['ok'])
# 3 · QR: ruído vira código, varredura, PIN
n0, n1 = T['s2']['qrNoise']
grains(m, n0, n1, 18, 40, 3000, 7000, 0.03, send=0.2, dec=0.006)
m.add(tick(4200, 0.03, 0.5), T['s2']['qrScan'][0], 0.10, send2=0.4)
m.add(blip(midi(86), 0.3, dec=0.07), T['s2']['qrScan'][1], 0.10, pan=0.3, send=0.5)
for i in range(4): m.add(tick(3000 + 220 * i, 0.03, 0.3), T['s2']['pin'] + 0.14 + i * 0.08, 0.06, pan=0.4, send2=0.4)
# 4 · recebido do celular: dois toques, os dados voam
rc = T['s2']['recebido']
m.add(blip(midi(81), 0.4, dec=0.09), rc, 0.18, pan=0.3, send=0.6)
m.add(blip(midi(88), 0.5, dec=0.12), rc + 0.075, 0.16, pan=0.35, send=0.6)
m.add(whoosh(0.9, 900, 3400, peak=0.5, q=0.8), T['flight'][0] + 0.03, 0.20, pan=-0.2, send=0.4)
grains(m, T['flight'][0] + 0.05, T['flight'][0] + 0.75, 40, 10, 4000, 9500, 0.03, send=0.3, dec=0.004)
for i, tf in enumerate(FIELD_IN): m.add(tick(3800 + 260 * i, 0.03, 0.25), tf, 0.06, pan=-0.5 + 0.1 * i, send2=0.4)
for i in range(4): m.add(tick(4600, 0.02, 0.2), 12.85 + i * 0.05, 0.035, pan=-0.4, send2=0.3)
# 5 · Importar lista: dados contando, um OK por etapa
ok(T['s3']['ok'])
sg = T['s4']['stages']
grains(m, sg[0], sg[5], 6, 14, 2500, 6000, 0.022, send=0.2)
for i, tk in enumerate(sg[1:5]):
    m.add(tick(2600 + 180 * i, 0.03, 0.5), tk, 0.14, pan=0.35 if i % 2 else -0.35, send2=0.4)
    m.add(fm_bell(midi(79 + 2 * i), 0.5, ratio=2.0, index=1.0, dec=0.08), tk, 0.05, send2=0.5)
m.add(blip(midi(88), 0.4, dec=0.09), sg[5], 0.16, pan=0.25, send=0.6)
m.add(blip(midi(93), 0.5, dec=0.12), sg[5] + 0.075, 0.14, pan=0.35, send=0.6)
# 6 · os dados viram o lobo: subida, impacto, acorde aberto
S5 = T['s5']; r0, hit = S5['dissolveLog'], S5['hit']
m.add(noise_riser(hit - r0, 300, 5200, curve=3.4), r0, 0.22, send=0.3)
tt = ts(hit - r0); p = tt / (hit - r0)
rise = saw(midi(38) * (1 + p ** 2), tt, maxf=4000)
rise = sweep_filter(rise, 200 + 2400 * p ** 2.2, 'low', q=1.1) * (p ** 2.4)
m.add(rise, r0, 0.12, send=0.3)
grains(m, S5['morph'][0], hit - 0.04, 8, 90, 3000, 9000, 0.035, send=0.35, curve=2.0)
m.add(boom(120, 41, 2.2, 0.7), hit, 0.55, send=0.2)
m.add(hp(S.rng.standard_normal(int(0.03 * SR)), 1800) * np.exp(-ts(0.03) / 0.006), hit, 0.3, send=0.5)
m.add(chord_pad([50, 57, 62, 66, 76], 3.4, dec=1.8, cutoff=(6200, 900)), hit, 0.36, send=0.55)
for n, pn in ((74, -0.3), (81, 0.3), (86, 0.0)):
    m.add(fm_bell(midi(n), 3.0, ratio=3.01, index=1.4, dec=1.1), hit + 0.01, 0.09, pan=pn, send=0.7)
grains(m, 22.3, 23.4, 30, 8, 3500, 7000, 0.03, send=0.25)
m.add(blip(midi(93), 0.4, dec=0.09), S5['focus'], 0.12, pan=0.2, send=0.6)
# 7 · Começar a assistir: brasas e a entrada do Início (a mesma da abertura)
ok(S5['ok'])
I = T['inicio']; p0 = I['embers'][0]
grains(m, p0, p0 + 0.5, 70, 10, 4000, 9500, 0.03, send=0.3, dec=0.004)
m.add(whoosh(0.75, 900, 3400, peak=0.55, q=0.8), p0 + 0.03, 0.22, pan=-0.3, send=0.4)
for i in range(8): m.add(tick(3800 + 260 * i, 0.03, 0.25), I['rows'] + i * 0.06, 0.035, pan=-0.4 + 0.1 * i, send2=0.4)
m.add(blip(midi(93), 0.35, dec=0.07), I['rows'] + 0.75, 0.10, pan=-0.5, send=0.6)

out = m.render(peak_db=-2.0, fade_in=0.005, tail_fade=(D - 0.4, D - 0.02))
wavfile.write(OUT, SR, (out.T * 32767).astype(np.int16))
print('ok', OUT, round(len(out[0]) / SR, 2), 's')
