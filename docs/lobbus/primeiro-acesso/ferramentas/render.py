"""Renderiza prototipo/primeiro.html quadro a quadro (Playwright + Chromium) e monta o MP4 sem som.
Uso: python3 render.py stills <pasta> 4.6,5.2,...
     python3 render.py video <saida.mp4> [fps]"""
import sys, subprocess, threading, http.server, functools, time
from pathlib import Path
from playwright.sync_api import sync_playwright

SITE = str(Path(__file__).resolve().parent.parent / 'prototipo')
mode, out = sys.argv[1], sys.argv[2]
class Q(http.server.SimpleHTTPRequestHandler):
    def log_message(self, *a): pass
handler = functools.partial(Q, directory=SITE)
srv = http.server.ThreadingHTTPServer(('127.0.0.1', 8766), handler)
threading.Thread(target=srv.serve_forever, daemon=True).start()

with sync_playwright() as p:
    b = p.chromium.launch(args=['--force-color-profile=srgb'])
    pg = b.new_page(viewport={'width': 1920, 'height': 1080}, device_scale_factor=1)
    errs = []
    pg.on('console', lambda m: errs.append(m.text) if m.type in ('error', 'warning') else None)
    pg.on('pageerror', lambda e: errs.append(str(e)))
    pg.goto('http://127.0.0.1:8766/primeiro.html')
    pg.wait_for_function('window.READY === true', timeout=30000)
    if errs: print('console:', errs)
    clip = {'x': 0, 'y': 0, 'width': 1920, 'height': 1080}
    if mode == 'stills':
        Path(out).mkdir(parents=True, exist_ok=True)
        for t in [float(x) for x in sys.argv[3].split(',')]:
            pg.evaluate(f'renderAt({t})')
            pg.screenshot(path=f'{out}/f_{t:05.2f}.png', clip=clip)
    else:
        fps = int(sys.argv[3]) if len(sys.argv) > 3 else 30
        end = pg.evaluate('TL.videoEnd')
        n = int(round(end * fps))
        ff = subprocess.Popen(['ffmpeg', '-y', '-loglevel', 'error', '-f', 'image2pipe', '-framerate', str(fps), '-c:v', 'png', '-i', '-',
                               '-c:v', 'libx264', '-pix_fmt', 'yuv420p', '-preset', 'slow', '-crf', '14', '-r', str(fps), out], stdin=subprocess.PIPE)
        t0 = time.time()
        for i in range(n):
            pg.evaluate(f'renderAt({i / fps})')
            ff.stdin.write(pg.screenshot(clip=clip, type='png'))
            if i % 150 == 0: print('frame', i, '/', n, round(time.time() - t0, 1), flush=True)
        ff.stdin.close(); ff.wait()
        print('frames', n, 'secs', round(time.time() - t0, 1))
    if errs: print('console:', errs[:10])
    b.close()
srv.shutdown()
