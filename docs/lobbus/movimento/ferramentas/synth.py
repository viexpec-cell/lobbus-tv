"""Sons da Lobbus TV, sintetizados do zero (sem amostras de terceiros), 48 kHz estéreo.
Aprovado em 22/10/2026: trilha A (Núcleo) com a transição Encaixe, e o kit OK, Voltar, Canal, Erro.
Uso: python3 synth.py  ->  gera WAVs em ../sons/wav e converte para ../sons/*.ogg (precisa de ffmpeg).
As trilhas B e C e as outras transições ficam aqui só como histórico."""
import json, sys, numpy as np
from scipy import signal
from scipy.io import wavfile

SR = 48000
from pathlib import Path
AQUI = Path(__file__).resolve().parent
TL = json.load(open(AQUI.parent / 'abertura' / 'prototipo' / 'timeline.json'))
OUT = sys.argv[1] if len(sys.argv) > 1 else str(AQUI.parent / 'sons' / 'wav')
rng = np.random.default_rng(1022)

def ts(d): return np.arange(int(d * SR)) / SR
def midi(n): return 440.0 * 2 ** ((n - 69) / 12)

def env_ad(n, a, d, curve=6.0):
    t = np.arange(n) / SR
    e = np.where(t < a, (t / max(a, 1e-6)), np.exp(-curve * (t - a) / max(d, 1e-6)))
    return e

def lp(x, fc, order=2):
    sos = signal.butter(order, min(fc, SR * 0.45), 'low', fs=SR, output='sos'); return signal.sosfilt(sos, x)
def hp(x, fc, order=2):
    sos = signal.butter(order, fc, 'high', fs=SR, output='sos'); return signal.sosfilt(sos, x)
def bp(x, lo, hi, order=2):
    sos = signal.butter(order, [lo, min(hi, SR * 0.45)], 'band', fs=SR, output='sos'); return signal.sosfilt(sos, x)

def sweep_filter(x, f_curve, kind='low', q=0.707, block=256):
    """Biquad variável em blocos (RBJ), f_curve com o mesmo tamanho de x."""
    y = np.zeros_like(x); z = np.zeros(2)
    for i in range(0, len(x), block):
        f = float(np.clip(f_curve[min(i + block // 2, len(x) - 1)], 20, SR * 0.45))
        w0 = 2 * np.pi * f / SR; al = np.sin(w0) / (2 * q); c = np.cos(w0)
        if kind == 'low': b = [(1 - c) / 2, 1 - c, (1 - c) / 2]
        elif kind == 'band': b = [al, 0, -al]
        else: b = [(1 + c) / 2, -(1 + c), (1 + c) / 2]
        a = [1 + al, -2 * c, 1 - al]
        seg, z = signal.lfilter(np.array(b) / a[0], np.array(a) / a[0], x[i:i + block], zi=z)
        y[i:i + block] = seg
    return y

def saw(f, t, maxf=12000):
    out = np.zeros_like(t); k = 1
    ph = 2 * np.pi * np.cumsum(np.broadcast_to(f, t.shape)) / SR if np.ndim(f) else 2 * np.pi * f * t
    f0 = np.max(f) if np.ndim(f) else f
    while k * f0 < maxf:
        out += np.sin(k * ph) / k; k += 1
    return out * 0.6
def sine_ph(fcurve):
    return np.sin(2 * np.pi * np.cumsum(fcurve) / SR)

def reverb_ir(rt=2.2, pre=0.018, bright=0.35, seed=5):
    r = np.random.default_rng(seed); n = int((rt + 0.3) * SR); t = np.arange(n) / SR
    irs = []
    for ch in range(2):
        nz = r.standard_normal(n)
        dark = lp(nz, 2600) * np.exp(-6.9 * t / rt)
        early = lp(nz, 9000) * np.exp(-6.9 * t / (rt * 0.35)) * bright
        ir = dark + early
        ir = np.concatenate([np.zeros(int(pre * SR)), ir]); ir /= np.sqrt(np.sum(ir ** 2))
        irs.append(ir)
    return irs
IR = reverb_ir()
IR_SHORT = reverb_ir(rt=0.9, pre=0.008, bright=0.5, seed=9)

class Mix:
    def __init__(self, dur):
        self.n = int(dur * SR); self.dry = np.zeros((2, self.n)); self.wet = np.zeros((2, self.n)); self.wet2 = np.zeros((2, self.n))
    def add(self, sig, t0, gain=1.0, pan=0.0, send=0.0, send2=0.0):
        i0 = int(round(t0 * SR));
        if i0 >= self.n: return
        sig = sig[: self.n - i0] * gain
        th = (pan + 1) * np.pi / 4; L, R = np.cos(th), np.sin(th)
        for ch, g in ((0, L), (1, R)):
            self.dry[ch, i0:i0 + len(sig)] += sig * g
            if send: self.wet[ch, i0:i0 + len(sig)] += sig * g * send
            if send2: self.wet2[ch, i0:i0 + len(sig)] += sig * g * send2
    def render(self, peak_db=-2.0, fade_in=0.04, tail_fade=(None, None)):
        out = self.dry.copy()
        for ch in range(2):
            out[ch] += signal.fftconvolve(self.wet[ch], IR[ch])[: self.n]
            out[ch] += signal.fftconvolve(self.wet2[ch], IR_SHORT[ch])[: self.n]
        out = hp(out, 28, 2) if out.ndim == 1 else np.stack([hp(c, 28, 2) for c in out])
        fi = int(fade_in * SR); out[:, :fi] *= np.linspace(0, 1, fi)
        if tail_fade[0] is not None:
            a, b = int(tail_fade[0] * SR), int(tail_fade[1] * SR)
            out[:, a:b] *= np.cos(np.linspace(0, np.pi / 2, b - a)) ** 2; out[:, b:] = 0
        pk = np.max(np.abs(out)); out = out / pk * (10 ** (peak_db / 20))
        out = np.tanh(out * 1.15) / np.tanh(1.15) * (10 ** (peak_db / 20)) / (10 ** (peak_db / 20))
        pk = np.max(np.abs(out)); out = out / pk * (10 ** (peak_db / 20))
        return out

def save(name, st):
    wavfile.write(f'{OUT}/{name}.wav', SR, (st.T * 32767).astype(np.int16))

# ---------------- peças reutilizáveis ----------------
def tick(freq=3200, dur=0.03, noise=0.5):
    t = ts(dur); e = np.exp(-t / 0.006)
    s = np.sin(2 * np.pi * freq * t) * e + noise * hp(rng.standard_normal(len(t)), 4000) * np.exp(-t / 0.0015)
    return s
def blip(freq, dur=0.18, att=0.003, dec=0.08, harm=0.25):
    t = ts(dur); e = np.minimum(t / att, 1) * np.exp(-(t) / dec)
    return (np.sin(2 * np.pi * freq * t) + harm * np.sin(4 * np.pi * freq * t)) * e
def fm_bell(freq, dur=2.5, ratio=3.5, index=2.2, dec=0.9):
    t = ts(dur); ie = index * np.exp(-t / 0.35)
    e = np.minimum(t / 0.004, 1) * np.exp(-t / dec)
    return np.sin(2 * np.pi * freq * t + ie * np.sin(2 * np.pi * freq * ratio * t)) * e
def boom(f0=110, f1=42, dur=1.6, dec=0.55):
    t = ts(dur); f = f1 + (f0 - f1) * np.exp(-t / 0.09)
    e = np.minimum(t / 0.002, 1) * np.exp(-t / dec)
    return sine_ph(f) * e
def noise_riser(dur, lo, hi, curve=3.0, q=1.2):
    t = ts(dur); x = rng.standard_normal(len(t)); p = t / dur
    fc = lo * (hi / lo) ** (p ** 1.4)
    e = (np.exp(curve * p) - 1) / (np.exp(curve) - 1)
    return sweep_filter(x, fc, 'band', q=q) * e
def whoosh(dur, f0, f1, peak=0.5, q=0.9):
    t = ts(dur); x = rng.standard_normal(len(t)); p = t / dur
    fc = f0 * (f1 / f0) ** p
    e = np.exp(-((p - peak) / 0.22) ** 2)
    return sweep_filter(x, fc, 'band', q=q) * e
def chord_pad(notes, dur, att=0.01, dec=2.2, cutoff=(5200, 700), detune=0.004):
    t = ts(dur); s = np.zeros_like(t)
    for n in notes:
        f = midi(n)
        for d in (-detune, detune):
            s += saw(f * (1 + d), t, maxf=9000)
    fc = cutoff[1] + (cutoff[0] - cutoff[1]) * np.exp(-t / 0.45)
    s = sweep_filter(s, fc, 'low', q=0.8)
    e = np.minimum(t / att, 1) * np.exp(-t / dec)
    return s * e / len(notes)
def grains(mix, t0, t1, rate0, rate1, f_lo, f_hi, gain, pan_w=0.8, send=0.3, dec=0.012, curve=1.0):
    t = t0
    while t < t1:
        p = (t - t0) / (t1 - t0); rate = rate0 + (rate1 - rate0) * p ** curve
        f = f_lo * (f_hi / f_lo) ** rng.random()
        g = blip(f, dur=0.06, att=0.001, dec=dec, harm=0.0)
        mix.add(g, t, gain * (0.4 + 0.6 * rng.random()) * (0.3 + 0.7 * p ** 0.5), pan=(rng.random() * 2 - 1) * pan_w, send=send)
        t += rng.exponential(1 / rate)

D = TL['videoEnd']
HIT = TL['hit']

# ---------------- A · Núcleo (cinematográfico, quente) ----------------
def variant_a(trans=None):
    global rng
    rng = np.random.default_rng(1022)
    m = Mix(D)
    t = ts(HIT)
    drone = (np.sin(2 * np.pi * midi(38) * t) + 0.5 * np.sin(2 * np.pi * midi(45) * t) + 0.35 * np.sin(2 * np.pi * midi(50) * t))
    de = (np.minimum(t / 1.4, 1) ** 2) * (0.35 + 0.65 * (t / HIT) ** 2.5)
    m.add(drone * de, 0, 0.32, send=0.25)
    for i, lt in enumerate(TL['logLines']):
        m.add(tick(2600 + 180 * i), lt, 0.16, pan=-0.35 if i % 2 else 0.35, send2=0.4)
    grains(m, TL['counter'][0], TL['counter'][1], 6, 26, 2500, 6000, 0.05, send=0.2)
    lo = TL['loaderOut'][0]
    gl = 620 * np.exp(-ts(0.3) / 0.12) + 180
    m.add(sine_ph(gl) * np.exp(-ts(0.3) / 0.1), lo, 0.08, send=0.4)
    r0 = TL['noiseIn'][0]
    m.add(noise_riser(HIT - r0, 300, 5200, curve=3.4), r0, 0.26, send=0.3)
    tt = ts(HIT - r0); p = tt / (HIT - r0)
    rise = saw(midi(38) * (1 + p ** 2), tt, maxf=4000)
    rise = sweep_filter(rise, 200 + 2400 * p ** 2.2, 'low', q=1.1) * (p ** 2.4)
    m.add(rise, r0, 0.15, send=0.3)
    grains(m, TL['morph'][0], HIT - 0.04, 8, 90, 3000, 9000, 0.04, send=0.35, curve=2.0)
    # impacto
    m.add(boom(120, 41, 2.2, 0.7), HIT, 1.0, send=0.2)
    tr = hp(rng.standard_normal(int(0.03 * SR)), 1800) * np.exp(-ts(0.03) / 0.006)
    m.add(tr, HIT, 0.45, send=0.5)
    m.add(chord_pad([50, 57, 62, 66, 76], D - HIT, dec=1.8, cutoff=(6200, 900)), HIT, 0.62, send=0.55)
    for n, pn in ((74, -0.3), (81, 0.3), (86, 0.0)):
        m.add(fm_bell(midi(n), D - HIT, ratio=3.01, index=1.4, dec=1.1), HIT + 0.01, 0.10, pan=pn, send=0.7)
    # TV
    pt = TL['pill'][0]
    m.add(blip(midi(88), 0.4, dec=0.09), pt, 0.20, pan=0.25, send=0.6)
    m.add(blip(midi(93), 0.5, dec=0.12), pt + 0.075, 0.18, pan=0.35, send=0.6)
    grains(m, TL['tagline'][0], TL['tagline'][1], 30, 8, 3500, 7000, 0.035, send=0.25)
    if trans is None:
        w0, w1 = TL['wipe']
        m.add(whoosh(w1 - w0 + 0.3, 2600, 450, peak=0.5), w0, 0.30, pan=0, send=0.4)
    else:
        TRANS_FX[trans](m)
    return m.render(tail_fade=(D - 0.35, D - 0.02))

# ---------------- sons das transições finais ----------------
def fx_encaixe(m):
    p0 = TL['trans'][0]
    grains(m, p0, p0 + 0.5, 70, 10, 4000, 9500, 0.03, send=0.3, dec=0.004)
    m.add(whoosh(0.75, 900, 3400, peak=0.55, q=0.8), p0 + 0.03, 0.22, pan=-0.3, send=0.4)
    for i in range(8):
        m.add(tick(3800 + 260 * i, 0.03, 0.25), p0 + 0.45 + i * 0.06, 0.035, pan=-0.4 + 0.1 * i, send2=0.4)
    m.add(blip(midi(93), 0.35, dec=0.07), TL['trans'][1] - 0.1, 0.10, pan=-0.5, send=0.6)
def fx_mergulho(m):
    p0 = TL['trans'][0]
    m.add(noise_riser(0.55, 300, 6500, curve=3.0, q=0.9), p0, 0.30, send=0.3)
    tt = ts(0.55); p = tt / 0.55
    m.add(sine_ph(midi(50) * (1 + 1.5 * p ** 3)) * p ** 2, p0, 0.05, send=0.4)
    m.add(boom(62, 34, 1.4, 0.55), p0 + 0.55, 0.38, send=0.3)
    t = ts(1.2); sw = (np.sin(2 * np.pi * midi(62) * t) + 0.6 * np.sin(2 * np.pi * midi(69) * t) + 0.4 * np.sin(2 * np.pi * midi(76) * t))
    m.add(sw * np.minimum(t / 0.3, 1) * np.exp(-t / 0.5), p0 + 0.5, 0.05, send=0.8)
def fx_persianas(m):
    p0 = TL['trans'][0]; cols = [0, 384, 672, 960, 1248, 1536, 1920]; order = [2, 1, 0, 0, 1, 2]
    for c in range(6):
        cxp = (cols[c] + cols[c + 1]) / 2
        m.add(tick(3000 + 300 * (2 - order[c]), 0.03, 0.4), p0 + order[c] * 0.07 + 0.02, 0.10, pan=(cxp / 960 - 1) * 0.8, send2=0.4)
    m.add(whoosh(0.7, 3000, 800, peak=0.45, q=0.9), p0, 0.20, send=0.4)
def fx_ligar(m):
    p0 = TL['trans'][0]
    f = 150 + 2250 * np.exp(-ts(0.22) / 0.06)
    m.add(sine_ph(f) * np.exp(-ts(0.22) / 0.12), p0, 0.10, send2=0.3)
    m.add(boom(90, 40, 0.5, 0.12), p0 + 0.2, 0.30, send2=0.3)
    m.add(hp(rng.standard_normal(int(0.08 * SR)), 2500) * np.exp(-ts(0.08) / 0.02), p0 + 0.2, 0.12, send2=0.3)
    t = ts(0.4); hum = sine_ph(50 + 50 * np.minimum(t / 0.25, 1)) + 0.4 * sine_ph(100 + 100 * np.minimum(t / 0.25, 1))
    m.add(hum * np.minimum(t / 0.05, 1) * np.exp(-t / 0.2), p0 + 0.27, 0.16, send=0.2)
    st = bp(rng.standard_normal(int(0.4 * SR)), 2000, 8000) * np.exp(-ts(0.4) / 0.09)
    m.add(st, p0 + 0.27, 0.10, send2=0.3)
TRANS_FX = {'encaixe': fx_encaixe, 'mergulho': fx_mergulho, 'persianas': fx_persianas, 'ligar': fx_ligar}

# ---------------- B · Sinal (digital, sistema) ----------------
def variant_b():
    m = Mix(D)
    t = ts(HIT)
    pulse = signal.square(2 * np.pi * midi(38) * t, 0.5)
    pulse = lp(pulse, 600) * (np.minimum(t / 1.0, 1) * (0.3 + 0.7 * (t / HIT) ** 2))
    gate = 0.65 + 0.35 * (np.sin(2 * np.pi * 8 * t) > 0)
    m.add(pulse * gate, 0, 0.12, send=0.15)
    scale = [74, 76, 79, 81, 83, 86]
    for i, lt in enumerate(TL['logLines']):
        m.add(fm_bell(midi(scale[i]), 0.35, ratio=2.0, index=1.2, dec=0.06), lt, 0.11, pan=-0.4 + 0.16 * i, send2=0.5)
    c0, c1 = TL['counter']; tc = c0
    while tc < c1:
        p = (tc - c0) / (c1 - c0); m.add(tick(4200, 0.02, 0.3), tc, 0.07, pan=0.1, send2=0.2)
        tc += 0.09 - 0.065 * np.sin(np.pi * p)
    lo = TL['loaderOut'][0]
    for k in range(3): m.add(blip(midi(86 - 5 * k), 0.12, dec=0.03, harm=0.6), lo + 0.05 * k, 0.12, send2=0.4)
    r0 = TL['noiseIn'][0]
    nr = noise_riser(HIT - r0, 500, 7000, curve=3.0, q=2.0)
    steps = 2 ** (4 + 6 * (ts(HIT - r0) / (HIT - r0)))
    crushed = np.round(nr / np.max(np.abs(nr)) * steps) / steps * np.max(np.abs(nr))
    m.add(crushed, r0, 0.22, send=0.25)
    tt = ts(HIT - r0); p = tt / (HIT - r0)
    arp_notes = [62, 69, 74, 78, 81, 86]; ta = r0
    k = 0
    while ta < HIT - 0.05:
        p = (ta - r0) / (HIT - r0)
        m.add(blip(midi(arp_notes[k % 6]), 0.1, att=0.001, dec=0.025, harm=0.5), ta, 0.035 + 0.05 * p, pan=(-0.5 if k % 2 else 0.5), send2=0.3)
        ta += 0.11 - 0.07 * p; k += 1
    for k in range(3):
        m.add(hp(rng.standard_normal(int(0.025 * SR)), 2500) * np.exp(-ts(0.025) / 0.008), HIT - 0.12 + 0.035 * k, 0.15, send2=0.3)
    m.add(boom(140, 46, 1.8, 0.45), HIT, 0.9, send=0.15)
    for n, pn in ((62, -0.4), (69, 0.4), (74, 0.0), (78, -0.2), (85, 0.2)):
        m.add(fm_bell(midi(n), D - HIT, ratio=3.5, index=2.6, dec=1.3), HIT, 0.16, pan=pn, send=0.55)
    m.add(chord_pad([50, 57, 62], D - HIT, dec=1.2, cutoff=(3000, 500)), HIT, 0.35, send=0.4)
    pt = TL['pill'][0]
    sq = lambda f, d: lp(signal.square(2 * np.pi * f * ts(d), 0.5), 5000) * np.exp(-ts(d) / 0.05)
    m.add(sq(midi(86), 0.15), pt, 0.11, send2=0.5)
    m.add(sq(midi(93), 0.25), pt + 0.07, 0.10, send2=0.5)
    grains(m, TL['tagline'][0], TL['tagline'][1], 40, 10, 2500, 8000, 0.04, send=0.15, dec=0.006)
    w0, w1 = TL['wipe']
    m.add(whoosh(w1 - w0 + 0.3, 5000, 700, peak=0.5, q=2.5), w0, 0.22, send=0.3)
    return m.render(tail_fade=(D - 0.35, D - 0.02))

# ---------------- C · Pulso (mínimo, premium) ----------------
def kick(f0=95, f1=48, dec=0.16):
    t = ts(0.5); f = f1 + (f0 - f1) * np.exp(-t / 0.03)
    return sine_ph(f) * np.minimum(t / 0.0015, 1) * np.exp(-t / dec)
def variant_c():
    m = Mix(D)
    tk = 0.18; k = 0
    while tk < HIT - 0.16:
        p = tk / HIT
        m.add(kick(90, 46, 0.14 + 0.05 * (1 - p)), tk, 0.30 * (0.45 + 0.55 * p ** 1.5), send=0.10)
        tk += 0.52 * (1 - p) ** 1.6 + 0.07; k += 1
    for i, lt in enumerate(TL['logLines']):
        x = rng.standard_normal(int(0.08 * SR))
        wood = bp(x, 1600 + 90 * i, 1900 + 90 * i, 2) * np.exp(-ts(0.08) / 0.012)
        m.add(wood, lt, 0.5, pan=-0.3 if i % 2 else 0.3, send2=0.35)
    r0 = TL['noiseIn'][0]
    tt = ts(HIT - 0.12 - r0); p = tt / tt[-1]
    whine = sine_ph(midi(86) * (1 + 0.5 * p ** 2)) * p ** 2 * 0.5 + sine_ph(midi(93) * (1 + 0.5 * p ** 2)) * p ** 3 * 0.25
    whine[-int(0.04 * SR):] *= np.linspace(1, 0, int(0.04 * SR))
    m.add(whine, r0, 0.05, send=0.2)
    nr = noise_riser(HIT - 0.12 - r0, 2000, 9000, curve=4.0, q=0.8); nr[-int(0.04 * SR):] *= np.linspace(1, 0, int(0.04 * SR))
    m.add(nr, r0, 0.10, send=0.2)
    # silêncio de 120 ms antes do impacto
    m.add(boom(70, 37, 2.6, 1.2), HIT, 0.95, send=0.15)
    t = ts(D - HIT)
    tone = (np.sin(2 * np.pi * midi(38) * t) * 0.6 + np.sin(2 * np.pi * midi(50) * t) * 0.4) * np.minimum(t / 0.005, 1) * np.exp(-t / 1.4)
    m.add(tone, HIT, 0.35, send=0.3)
    shimmer = np.zeros_like(t)
    for n in (74, 81, 88, 90):
        shimmer += np.sin(2 * np.pi * midi(n) * t + 0.4 * np.sin(2 * np.pi * 5.5 * t))
    shimmer *= np.minimum(t / 0.35, 1) * np.exp(-t / 1.0)
    m.add(shimmer, HIT + 0.02, 0.05, send=0.8)
    pt = TL['pill'][0]
    m.add(fm_bell(midi(93), 1.2, ratio=4.0, index=0.8, dec=0.35), pt, 0.17, pan=0.2, send=0.7)
    w0, w1 = TL['wipe']
    m.add(whoosh(w1 - w0 + 0.3, 1800, 600, peak=0.5, q=0.7), w0, 0.22, send=0.5)
    return m.render(tail_fade=(D - 0.35, D - 0.02))

# ---------------- Kit da interface ----------------
def ui_ok():
    t = ts(0.12); s = blip(1320, 0.12, att=0.0015, dec=0.028, harm=0.15) + 0.4 * tick(5200, 0.12, 0.4)
    return s
def ui_back():
    t = ts(0.14); f = 900 * np.exp(-t / 0.05) + 520
    return sine_ph(f) * np.minimum(t / 0.0015, 1) * np.exp(-t / 0.035) + 0.3 * tick(3800, 0.14, 0.3)
def ui_zap():
    s = whoosh(0.18, 1400, 5200, peak=0.55, q=1.4) * 1.4
    s[: len(tick())] += 0.0
    out = np.zeros(int(0.22 * SR)); out[: len(s)] += s
    tk = tick(3000, 0.03, 0.5); i = int(0.13 * SR); out[i:i + len(tk)] += 0.5 * tk
    return out
def ui_error():
    a = blip(midi(64), 0.2, att=0.004, dec=0.07, harm=0.3); b = blip(midi(60), 0.32, att=0.004, dec=0.11, harm=0.3)
    out = np.zeros(int(0.46 * SR)); out[: len(a)] += a; i = int(0.11 * SR); out[i:i + len(b)] += b
    return lp(out, 3500)
KIT = [('ok', ui_ok), ('voltar', ui_back), ('canal', ui_zap), ('erro', ui_error)]

if __name__ == '__main__':
    import os, subprocess
    os.makedirs(OUT, exist_ok=True)
    final = {'lobbus_abertura': lambda: variant_a('encaixe')}
    for name, fn in KIT:
        def mk(fn=fn):
            s = fn(); m = Mix(len(s) / SR + 0.3); m.add(s, 0.0, 1.0, send2=0.12)
            return m.render(peak_db=-6.0, fade_in=0.0005)
        final['lobbus_ui_' + name] = mk
    for name, fn in final.items():
        save(name, fn())
        ogg = Path(OUT).parent / f'{name}.ogg'
        subprocess.run(['ffmpeg', '-y', '-loglevel', 'error', '-i', f'{OUT}/{name}.wav', '-c:a', 'libvorbis', '-q:a', '6', '-ar', '48000', str(ogg)], check=True)
        print('ok', ogg)
