"""Gera os recursos do protótipo da abertura a partir das logos da Lobbus.
Uso: python3 prep.py <lobbus-simbolo-lobo-branco.png> <lobbus-logo-completa-branca.png> [print-do-inicio.png]
Saída: ../abertura/prototipo/assets (pontos do lobo, lobo branco, palavra Lobbus) e ../marca/lobbus-lobo.svg
Requer: numpy, pillow, potracer (pip install potracer)."""
import sys, json
from pathlib import Path
import numpy as np
from PIL import Image

AQUI = Path(__file__).resolve().parent
ASSETS = AQUI.parent / 'abertura' / 'prototipo' / 'assets'
lobo_png, logo_png = sys.argv[1], sys.argv[2]
ASSETS.mkdir(parents=True, exist_ok=True)

# 1 · nuvem de pontos do lobo (9000 pontos, mesma semente da versão aprovada)
wolf = Image.open(lobo_png).convert('RGBA'); a = np.array(wolf)[:, :, 3]
ys, xs = np.nonzero(a > 140); H, W = a.shape
rng = np.random.default_rng(7); N = 9000; idx = rng.integers(0, len(xs), N)
px = (xs[idx] + rng.random(N)) / W - 0.5; py = (ys[idx] + rng.random(N)) / H - 0.5
json.dump({'aspect': W / H, 'pts': np.round(np.stack([px * W / H, py], 1), 4).flatten().tolist()}, open(ASSETS / 'wolf_pts.json', 'w'))
wolf.save(ASSETS / 'wolf_white.png')

# 2 · palavra "Lobbus" recortada da logo completa (sem o slogan)
lg = Image.open(logo_png).convert('RGBA'); A = np.array(lg)[:, :, 3]
col = (A.max(0) > 0).astype(int); x_texto = next(x for x in range(1, len(col)) if col[x - 1] == 0 and col[x] == 1 and x > 10)
linhas = (A[:, x_texto:].max(1) > 0).astype(int); y0 = int(np.argmax(linhas)); y1 = y0 + int(np.argmin(linhas[y0:]))
sub = A[y0:y1, x_texto:]; c = np.nonzero(sub.max(0) > 0)[0]
lg.crop((x_texto, y0, x_texto + c.max() + 1, y1)).save(ASSETS / 'wordmark_white.png')

# 3 · lobo vetorizado (SVG)
import potrace
w2 = np.array(wolf.resize((W * 2, H * 2), Image.LANCZOS))[:, :, 3] <= 127
paths = potrace.Bitmap(w2).trace(turdsize=20, alphamax=0.75, opticurve=True, opttolerance=1.2)
d = ''
for cv in paths:
    s = cv.start_point; d += f'M{s.x/2:.2f},{s.y/2:.2f}'
    for sg in cv.segments:
        d += (f'L{sg.c.x/2:.2f},{sg.c.y/2:.2f}L{sg.end_point.x/2:.2f},{sg.end_point.y/2:.2f}' if sg.is_corner
              else f'C{sg.c1.x/2:.2f},{sg.c1.y/2:.2f} {sg.c2.x/2:.2f},{sg.c2.y/2:.2f} {sg.end_point.x/2:.2f},{sg.end_point.y/2:.2f}')
    d += 'Z'
svg = f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 {W} {H}" width="{W}" height="{H}"><path fill="#FFFFFF" fill-rule="evenodd" d="{d}"/></svg>'
(AQUI.parent / 'marca' / 'lobbus-lobo.svg').write_text(svg); (ASSETS / 'lobo.svg').write_text(svg)

# 4 · (opcional) print do Início aprovado, recortado para 1920x1080
if len(sys.argv) > 3:
    Image.open(sys.argv[3]).convert('RGB').resize((1920, 1080), Image.LANCZOS).save(ASSETS / 'home.jpg', quality=92)
print('ok', ASSETS)
