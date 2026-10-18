"""Gera os assets de marca da Lobbus TV a partir da logo oficial (PNG branco, fundo transparente).

Uso:  python3 tools/brand/make_lobbus_brand.py <logo_completa.png> <montserrat-800.ttf> <geist-mono-500.ttf>

Saídas em app/src/main/res (sobrescrevem os recursos de marca do OwnTV que vêm do :core):
  mipmap-*/lobbus_icon_foreground.png   primeiro plano do ícone adaptativo (lobo branco na zona segura)
  drawable-xhdpi/owntv_banner_<cor>.png banner 320x180 do launcher da TV (todas as cores de ícone)
  drawable-nodpi/lobbus_tv_banner_1280.png banner em alta para materiais
"""
import sys
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

INK = (13, 9, 6, 255)          # #0D0906
ORANGE = (242, 96, 12, 255)    # #F2600C
ON_ORANGE = (20, 12, 6, 255)   # #140C06
TEXT = (246, 239, 233, 255)    # #F6EFE9

COLOURS = ["eggshell", "petrol", "sunflower", "cobalt", "tomato", "board", "olive", "olive_cream", "pixel"]
DENSITIES = {"mdpi": 108, "hdpi": 162, "xhdpi": 216, "xxhdpi": 324, "xxxhdpi": 432}

root = Path(__file__).resolve().parents[2]
res = root / "app" / "src" / "main" / "res"


def wolf_from(logo: Image.Image) -> Image.Image:
    """O lobo ocupa as primeiras ~600 colunas da logo completa (2430x697)."""
    wolf = logo.crop((0, 0, 600, logo.height))
    return wolf.crop(wolf.getchannel("A").getbbox())


def foreground(wolf: Image.Image, size: int) -> Image.Image:
    # Zona segura do ícone adaptativo: círculo de 66dp num quadro de 108dp. Lobo com ~56dp de altura.
    canvas = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    target_h = round(size * 56 / 108)
    w = round(wolf.width * target_h / wolf.height)
    mark = wolf.resize((w, target_h), Image.LANCZOS)
    canvas.alpha_composite(mark, ((size - w) // 2, (size - target_h) // 2))
    return canvas


def banner(logo: Image.Image, width: int, font_path: str, mono_path: str) -> Image.Image:
    h = round(width * 9 / 16)
    im = Image.new("RGBA", (width, h), INK)
    # brilho laranja suave no canto superior direito, como no site
    glow = Image.new("RGBA", (width, h), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    for i in range(60, 0, -1):
        a = int(70 * (1 - i / 60) ** 2)
        r = int(width * 0.55 * i / 60)
        gd.ellipse((width - r, -r, width + r, r), fill=(242, 96, 12, a))
    im.alpha_composite(glow)
    # Lobo + "Lobbus" sem a assinatura (ilegível no tamanho do banner). Na logo 2430x697 a
    # palavra ocupa as linhas 177 a 411 a partir da coluna 690; a assinatura fica abaixo.
    lobo = wolf_from(logo)
    palavra = logo.crop((690, 177, logo.width, 411))
    palavra = palavra.crop(palavra.getchannel("A").getbbox())
    wolf_h = round(h * 0.50)
    wolf_w = round(lobo.width * wolf_h / lobo.height)
    lobo = lobo.resize((wolf_w, wolf_h), Image.LANCZOS)
    word_h = round(wolf_h * 0.40)
    word_w = round(palavra.width * word_h / palavra.height)
    palavra = palavra.resize((word_w, word_h), Image.LANCZOS)
    d = ImageDraw.Draw(im)
    fs = round(word_h * 0.50)
    font = ImageFont.truetype(mono_path, fs)
    label = "TV"
    tw = d.textlength(label, font=font)
    pad_x, pill_h = fs * 0.62, word_h * 0.86
    pill_w = tw + pad_x * 2
    gap1, gap2 = round(width * 0.035), round(width * 0.028)
    total = wolf_w + gap1 + word_w + gap2 + pill_w
    x = round((width - total) / 2)
    y = (h - wolf_h) // 2
    im.alpha_composite(lobo, (x, y))
    wx = x + wolf_w + gap1
    wy = y + (wolf_h - word_h) // 2
    im.alpha_composite(palavra, (wx, wy))
    px = wx + word_w + gap2
    py = wy + (word_h - pill_h) / 2
    d.rounded_rectangle((px, py, px + pill_w, py + pill_h), radius=pill_h / 2, fill=ORANGE)
    d.text((px + pill_w / 2, py + pill_h / 2), label, font=font, fill=ON_ORANGE, anchor="mm")
    return im.convert("RGB")


def main() -> None:
    logo_path, font_path, mono_path = sys.argv[1:4]
    logo = Image.open(logo_path).convert("RGBA")
    wolf = wolf_from(logo)

    for d, px in DENSITIES.items():
        out = res / f"mipmap-{d}"
        out.mkdir(parents=True, exist_ok=True)
        foreground(wolf, px).save(out / "lobbus_icon_foreground.png", optimize=True)

    # Marca e palavra soltas para a interface (BrandMark / Wordmark), brancas e recortadas.
    nodpi = res / "drawable-nodpi"
    nodpi.mkdir(parents=True, exist_ok=True)
    mark = wolf.resize((round(wolf.width * 512 / wolf.height), 512), Image.LANCZOS)
    mark.save(nodpi / "lobbus_mark.png", optimize=True)
    palavra = logo.crop((690, 177, logo.width, 411))
    palavra = palavra.crop(palavra.getchannel("A").getbbox())
    palavra.save(nodpi / "lobbus_wordmark.png", optimize=True)

    # Assinatura da tela de abertura (Android 12+): 200x80dp, lobo + Lobbus + TV, fundo transparente.
    for d, scale in {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}.items():
        w, h = round(200 * scale), round(80 * scale)
        im = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        wh = round(h * 0.62)
        lobo = wolf.resize((round(wolf.width * wh / wolf.height), wh), Image.LANCZOS)
        ph = round(wh * 0.42)
        pw = round(palavra.width * ph / palavra.height)
        word = palavra.resize((pw, ph), Image.LANCZOS)
        dd = ImageDraw.Draw(im)
        fs = round(ph * 0.5)
        font = ImageFont.truetype(mono_path, fs)
        tw = dd.textlength("TV", font=font)
        pill_h, pill_w = ph * 0.86, tw + fs * 1.24
        gap = round(h * 0.09)
        total = lobo.width + gap + pw + gap * 0.8 + pill_w
        x = round((w - total) / 2)
        im.alpha_composite(lobo, (x, (h - wh) // 2))
        wx = x + lobo.width + gap
        wy = (h - ph) // 2
        im.alpha_composite(word, (wx, wy))
        px = wx + pw + gap * 0.8
        py = wy + (ph - pill_h) / 2
        dd.rounded_rectangle((px, py, px + pill_w, py + pill_h), radius=pill_h / 2, fill=ORANGE)
        dd.text((px + pill_w / 2, py + pill_h / 2), "TV", font=font, fill=ON_ORANGE, anchor="mm")
        out = res / f"drawable-{d}"
        out.mkdir(parents=True, exist_ok=True)
        for c in COLOURS:
            im.save(out / f"owntv_splash_name_{c}.png", optimize=True)

    big = banner(logo, 1280, font_path, mono_path)
    (res / "drawable-nodpi").mkdir(parents=True, exist_ok=True)
    big.save(res / "drawable-nodpi" / "lobbus_tv_banner_1280.png", optimize=True)
    small = big.resize((320, 180), Image.LANCZOS)
    (res / "drawable-xhdpi").mkdir(parents=True, exist_ok=True)
    for c in COLOURS:
        small.save(res / "drawable-xhdpi" / f"owntv_banner_{c}.png", optimize=True)
    print("ok")


if __name__ == "__main__":
    main()
