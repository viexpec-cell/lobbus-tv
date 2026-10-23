"""Sons do primeiro acesso, um arquivo por momento, prontos para res/raw (48 kHz estéreo, OGG).
Mesma família da abertura (trilha A, Núcleo), sintetizados do zero com as peças do synth.py.
Uso: python3 sons.py <pasta de saída>"""
import sys, subprocess
import numpy as np
from pathlib import Path
from scipy.io import wavfile

PAC = Path(__file__).resolve().parents[2] / 'movimento'   # docs/lobbus/movimento (pacote da abertura)
sys.path.insert(0, str(PAC / 'ferramentas'))
OUTDIR = Path(sys.argv[1] if len(sys.argv) > 1 else Path(__file__).resolve().parent.parent / 'sons'); OUTDIR.mkdir(parents=True, exist_ok=True)
sys.argv = sys.argv[:1]
import synth as S
from synth import Mix, tick, blip, fm_bell, boom, noise_riser, whoosh, chord_pad, grains, midi, ts, saw, sweep_filter, hp, SR

def qr(m):        # t0 = o ruído começa no QR; varredura em 0,50 a 0,80; PIN em 0,84
    grains(m, 0.0, 0.55, 18, 40, 3000, 7000, 0.03, send=0.2, dec=0.006)
    m.add(tick(4200, 0.03, 0.5), 0.50, 0.10, send2=0.4)
    m.add(blip(midi(86), 0.3, dec=0.07), 0.80, 0.10, pan=0.3, send=0.5)
    for i in range(4): m.add(tick(3000 + 220 * i, 0.03, 0.3), 0.84 + i * 0.08, 0.06, pan=0.4, send2=0.4)
def recebido(m):  # t0 = chega o envio do celular; o voo começa em 0,15
    m.add(blip(midi(81), 0.4, dec=0.09), 0.0, 0.18, pan=0.3, send=0.6)
    m.add(blip(midi(88), 0.5, dec=0.12), 0.075, 0.16, pan=0.35, send=0.6)
    m.add(whoosh(0.9, 900, 3400, peak=0.5, q=0.8), 0.18, 0.20, pan=-0.2, send=0.4)
    grains(m, 0.2, 0.9, 40, 10, 4000, 9500, 0.03, send=0.3, dec=0.004)
def campo(m):     # cada campo que recebe os dados
    m.add(tick(4000, 0.03, 0.25), 0.0, 0.06, pan=-0.3, send2=0.4)
def etapa(m):     # cada etapa do log que vira OK
    m.add(tick(2800, 0.03, 0.5), 0.0, 0.14, send2=0.4)
    m.add(fm_bell(midi(81), 0.5, ratio=2.0, index=1.0, dec=0.08), 0.0, 0.05, send2=0.5)
def concluido(m): # 100%: os toques da pílula TV
    m.add(blip(midi(88), 0.4, dec=0.09), 0.0, 0.16, pan=0.25, send=0.6)
    m.add(blip(midi(93), 0.5, dec=0.12), 0.075, 0.14, pan=0.35, send=0.6)
def pronto(m):    # t0 = o log se desfaz; impacto em 1,41
    hit = 1.41
    m.add(noise_riser(hit, 300, 5200, curve=3.4), 0.0, 0.22, send=0.3)
    tt = ts(hit); p = tt / hit
    rise = sweep_filter(saw(midi(38) * (1 + p ** 2), tt, maxf=4000), 200 + 2400 * p ** 2.2, 'low', q=1.1) * (p ** 2.4)
    m.add(rise, 0.0, 0.12, send=0.3)
    grains(m, 0.05, hit - 0.04, 8, 90, 3000, 9000, 0.035, send=0.35, curve=2.0)
    m.add(boom(120, 41, 2.2, 0.7), hit, 0.55, send=0.2)
    m.add(hp(S.rng.standard_normal(int(0.03 * SR)), 1800) * np.exp(-ts(0.03) / 0.006), hit, 0.3, send=0.5)
    m.add(chord_pad([50, 57, 62, 66, 76], 3.4, dec=1.8, cutoff=(6200, 900)), hit, 0.36, send=0.55)
    for n, pn in ((74, -0.3), (81, 0.3), (86, 0.0)):
        m.add(fm_bell(midi(n), 3.0, ratio=3.01, index=1.4, dec=1.1), hit + 0.01, 0.09, pan=pn, send=0.7)
    grains(m, hit + 0.44, hit + 1.54, 30, 8, 3500, 7000, 0.03, send=0.25)
def chegada(m):   # foco pousa em Começar a assistir
    m.add(blip(midi(93), 0.4, dec=0.09), 0.0, 0.12, pan=0.2, send=0.6)
def inicio(m):    # t0 = o lobo vira brasas; linhas do Início a partir de 0,45
    grains(m, 0.0, 0.5, 70, 10, 4000, 9500, 0.03, send=0.3, dec=0.004)
    m.add(whoosh(0.75, 900, 3400, peak=0.55, q=0.8), 0.03, 0.22, pan=-0.3, send=0.4)
    for i in range(8): m.add(tick(3800 + 260 * i, 0.03, 0.25), 0.45 + i * 0.06, 0.035, pan=-0.4 + 0.1 * i, send2=0.4)
    m.add(blip(midi(93), 0.35, dec=0.07), 1.2, 0.10, pan=-0.5, send=0.6)

SONS = [('qr', qr, 1.6), ('recebido', recebido, 1.6), ('campo', campo, 0.5), ('etapa', etapa, 0.8), ('concluido', concluido, 1.0),
        ('pronto', pronto, 5.2), ('chegada', chegada, 0.9), ('inicio', inicio, 2.0)]
for name, fn, dur in SONS:
    S.rng = np.random.default_rng(2310)
    m = Mix(dur); fn(m)
    peak = -2.0 if name == 'pronto' else -6.0
    out = m.render(peak_db=peak, fade_in=0.0005, tail_fade=(dur - 0.25, dur - 0.01))
    w = OUTDIR / f'lobbus_pa_{name}.wav'
    wavfile.write(w, SR, (out.T * 32767).astype(np.int16))
    subprocess.run(['ffmpeg', '-y', '-loglevel', 'error', '-i', str(w), '-c:a', 'libvorbis', '-q:a', '6', '-ar', '48000', str(w.with_suffix('.ogg'))], check=True)
    w.unlink(); print('ok', w.with_suffix('.ogg').name)
