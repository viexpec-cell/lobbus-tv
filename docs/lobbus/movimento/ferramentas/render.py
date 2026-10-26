"""Renderiza o protótipo quadro a quadro (Playwright + Chromium) e monta o MP4 com ffmpeg.
Uso: PAGE="intro.html?t=encaixe" FPS=60 python3 render.py video saida.mp4
     SITE=../interface/prototipo PAGE="motion.html?m=canal" FPS=30 python3 render.py video canal.mp4
     PAGE="intro.html?t=encaixe" python3 render.py stills pasta 2.84,4.2,5.0"""
import os, sys, subprocess, threading, http.server, functools, json, time
from playwright.sync_api import sync_playwright

from pathlib import Path
SITE = os.environ.get('SITE') or str(Path(__file__).resolve().parent.parent / 'abertura' / 'prototipo')
mode = sys.argv[1]            # "stills" or "video"
out = sys.argv[2]
times = [float(x) for x in sys.argv[3].split(',')] if mode == 'stills' else None
import os
fps = int(os.environ.get('FPS', '60'))
PAGE = os.environ.get('PAGE', 'intro.html')

handler = functools.partial(http.server.SimpleHTTPRequestHandler, directory=SITE)
handler.log_message = lambda *a, **k: None
srv = http.server.ThreadingHTTPServer(('127.0.0.1', 8765), handler)
threading.Thread(target=srv.serve_forever, daemon=True).start()

with sync_playwright() as p:
    b = p.chromium.launch(args=['--force-color-profile=srgb'])
    pg = b.new_page(viewport={'width': 1920, 'height': 1080}, device_scale_factor=1)
    errs = []
    pg.on('console', lambda m: errs.append(m.text) if m.type in ('error', 'warning') else None)
    pg.on('pageerror', lambda e: errs.append(str(e)))
    pg.goto('http://127.0.0.1:8765/' + PAGE)
    pg.wait_for_function('window.READY === true', timeout=20000)
    if errs: print('console:', errs)
    clip = {'x': 0, 'y': 0, 'width': 1920, 'height': 1080}
    if mode == 'stills':
        for t in times:
            pg.evaluate(f'renderAt({t})')
            pg.screenshot(path=f'{out}/f_{t:.2f}.png', clip=clip)
    else:
        end = pg.evaluate('TL.videoEnd')
        start = float(os.environ.get('START', '0'))
        n = int(round((end - start) * fps))
        ff = subprocess.Popen(['ffmpeg', '-y', '-loglevel', 'error', '-f', 'image2pipe', '-framerate', str(fps), '-c:v', 'png', '-i', '-',
                               '-c:v', 'libx264', '-pix_fmt', 'yuv420p', '-preset', 'slow', '-crf', '16', '-r', str(fps), out], stdin=subprocess.PIPE)
        t0 = time.time()
        for i in range(n):
            pg.evaluate(f'renderAt({start + i / fps})')
            ff.stdin.write(pg.screenshot(clip=clip, type='png'))
        ff.stdin.close(); ff.wait()
        print('frames', n, 'secs', round(time.time() - t0, 1))
    b.close()
srv.shutdown()
