from PIL import Image, ImageDraw, ImageFont
import os

# Modern flat color palette
BG = (0, 0, 0, 0)  # transparent
LOCK_BODY = (41, 98, 255)       # vibrant blue
LOCK_BODY_DARK = (25, 70, 200)  # darker blue accent
SHACKLE_ENCRYPT = (41, 98, 255) # closed shackle - same blue
SHACKLE_DECRYPT = (76, 175, 80) # open shackle - green (unlocked)
KEY_HOLE = (255, 255, 255)      # white keyhole
PGP_COLOR = (255, 255, 255)     # white text
ARROW_UP = (76, 175, 80)        # green for encrypt
ARROW_DOWN = (255, 152, 0)      # orange for decrypt
BADGE_ENCRYPT = (76, 175, 80)   # green badge
BADGE_DECRYPT = (255, 152, 0)   # orange badge

BASE = r"D:\GIT\MyOpenTech-PGP-SupportPac\src\ACEv13\v2.0.1.0\PGPSupportPac\icons\full"

def draw_lock_16(draw, is_encrypt):
    """Draw a modern 16x16 padlock icon"""
    w, h = 16, 16
    
    # Shackle (arc on top)
    if is_encrypt:
        # Closed shackle
        draw.arc([4, 1, 11, 9], 0, 360, fill=LOCK_BODY, width=2)
        draw.rectangle([4, 5, 5, 7], fill=BG)  # clear bottom left
        draw.rectangle([10, 5, 11, 7], fill=BG) # clear bottom right
        draw.line([4, 5, 4, 7], fill=LOCK_BODY, width=1)
        draw.line([11, 5, 11, 7], fill=LOCK_BODY, width=1)
        # Simpler: just draw the arc part
    else:
        # Open shackle - shifted up-right
        draw.arc([5, 0, 12, 8], 0, 360, fill=SHACKLE_DECRYPT, width=2)
    
    # Lock body - rounded rectangle
    draw.rectangle([2, 7, 13, 14], fill=LOCK_BODY)
    draw.rectangle([3, 7, 12, 14], fill=LOCK_BODY)
    
    # Keyhole
    draw.rectangle([7, 9, 8, 12], fill=KEY_HOLE)
    draw.point((7, 9), fill=KEY_HOLE)
    draw.point((8, 9), fill=KEY_HOLE)
    
    # Small badge indicator in corner
    if is_encrypt:
        draw.rectangle([11, 0, 15, 4], fill=BADGE_ENCRYPT)
        # tiny up arrow
        draw.point((13, 1), fill=(255, 255, 255))
        draw.line([(12, 2), (14, 2)], fill=(255, 255, 255))
        draw.point((13, 3), fill=(255, 255, 255))
    else:
        draw.rectangle([0, 0, 4, 4], fill=BADGE_DECRYPT)
        # tiny down arrow  
        draw.point((2, 3), fill=(255, 255, 255))
        draw.line([(1, 2), (3, 2)], fill=(255, 255, 255))
        draw.point((2, 1), fill=(255, 255, 255))


def draw_lock_30(draw, is_encrypt):
    """Draw a modern 30x30 padlock icon"""
    w, h = 30, 30
    
    # Lock body - main rounded rect
    body_left, body_top, body_right, body_bottom = 4, 13, 25, 27
    draw.rounded_rectangle([body_left, body_top, body_right, body_bottom], radius=3, fill=LOCK_BODY)
    
    # Shackle
    if is_encrypt:
        # Closed shackle
        for i in range(2):
            draw.arc([8-i//2, 3, 21+i//2, 16], 180, 0, fill=LOCK_BODY_DARK, width=1)
        draw.arc([8, 3, 21, 16], 180, 0, fill=LOCK_BODY, width=3)
    else:
        # Open shackle - shifted right and up
        draw.arc([10, 1, 23, 14], 180, 0, fill=SHACKLE_DECRYPT, width=3)
        # Erase left leg partially to look "open"
        draw.rectangle([10, 8, 13, 14], fill=BG)
    
    # Keyhole - circle + line
    draw.ellipse([12, 17, 17, 22], fill=KEY_HOLE)
    draw.rectangle([13, 21, 16, 25], fill=KEY_HOLE)
    
    # "PGP" text at bottom
    # Too small for font, draw pixel text
    # P
    for y in range(28, 30):
        draw.point((5, y), fill=PGP_COLOR)
    draw.point((6, 28), fill=PGP_COLOR)
    draw.point((5, 29), fill=PGP_COLOR)
    # G  
    draw.point((9, 28), fill=PGP_COLOR)
    draw.point((8, 29), fill=PGP_COLOR)
    draw.point((10, 29), fill=PGP_COLOR)
    # P
    for y in range(28, 30):
        draw.point((13, y), fill=PGP_COLOR)
    draw.point((14, 28), fill=PGP_COLOR)
    
    # Badge with arrow
    if is_encrypt:
        draw.rounded_rectangle([20, 0, 29, 9], radius=2, fill=BADGE_ENCRYPT)
        # Up arrow
        cx, cy = 24, 4
        draw.polygon([(cx, cy-3), (cx-2, cy), (cx+2, cy)], fill=(255,255,255))
        draw.rectangle([cx-1, cy, cx, cy+2], fill=(255,255,255))
    else:
        draw.rounded_rectangle([0, 0, 9, 9], radius=2, fill=BADGE_DECRYPT)
        # Down arrow
        cx, cy = 4, 4
        draw.polygon([(cx, cy+3), (cx-2, cy), (cx+2, cy)], fill=(255,255,255))
        draw.rectangle([cx-1, cy-2, cx, cy], fill=(255,255,255))


def draw_lock_32(draw, is_encrypt):
    """Draw a modern 32x32 padlock icon"""
    w, h = 32, 32
    
    # Lock body
    draw.rounded_rectangle([4, 14, 27, 29], radius=3, fill=LOCK_BODY)
    
    # Shackle
    if is_encrypt:
        draw.arc([9, 3, 22, 17], 180, 0, fill=LOCK_BODY, width=3)
    else:
        draw.arc([11, 1, 24, 15], 180, 0, fill=SHACKLE_DECRYPT, width=3)
        draw.rectangle([11, 9, 14, 15], fill=BG)
    
    # Keyhole
    draw.ellipse([13, 18, 18, 23], fill=KEY_HOLE)
    draw.rectangle([14, 22, 17, 27], fill=KEY_HOLE)
    
    # "PGP" - small pixel text
    y_base = 30
    # P: 3 pixels tall
    draw.rectangle([6, y_base, 6, y_base+1], fill=(200,200,255))
    draw.point((7, y_base), fill=(200,200,255))
    # G
    draw.point((10, y_base), fill=(200,200,255))
    draw.point((9, y_base+1), fill=(200,200,255))
    draw.point((11, y_base+1), fill=(200,200,255))
    # P
    draw.rectangle([14, y_base, 14, y_base+1], fill=(200,200,255))
    draw.point((15, y_base), fill=(200,200,255))
    
    # Badge with arrow
    if is_encrypt:
        draw.rounded_rectangle([22, 0, 31, 9], radius=2, fill=BADGE_ENCRYPT)
        cx, cy = 26, 4
        draw.polygon([(cx, cy-3), (cx-3, cy), (cx+3, cy)], fill=(255,255,255))
        draw.rectangle([cx-1, cy, cx, cy+3], fill=(255,255,255))
    else:
        draw.rounded_rectangle([0, 0, 9, 9], radius=2, fill=BADGE_DECRYPT)
        cx, cy = 4, 4
        draw.polygon([(cx, cy+3), (cx-3, cy), (cx+3, cy)], fill=(255,255,255))
        draw.rectangle([cx-1, cy-3, cx, cy], fill=(255,255,255))


def save_icon(size, draw_func, is_encrypt, folder_prefix):
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    draw_func(draw, is_encrypt)
    
    name = "PGPEncrypter.gif" if is_encrypt else "PGPDecrypter.gif"
    rel_path = os.path.join(folder_prefix, "com", "ibm", "broker", "supportpac", "pgp", name)
    full_path = os.path.join(BASE, rel_path)
    
    # Back up original
    backup = full_path + ".bak"
    if os.path.exists(full_path) and not os.path.exists(backup):
        os.rename(full_path, backup)
    
    # Save as GIF with transparency
    img.save(full_path, "GIF", transparency=0)
    print(f"  Saved: {rel_path}")


print("Generating modern PGP icons...")
print()

# clcl16 - 16x16 class icons
for enc in [True, False]:
    save_icon(16, draw_lock_16, enc, "clcl16")

# obj16 - 16x16 object icons
for enc in [True, False]:
    save_icon(16, draw_lock_16, enc, "obj16")

# obj30 - 30x30 object icons
for enc in [True, False]:
    save_icon(30, draw_lock_30, enc, "obj30")

# obj32 - 32x32 object icons
for enc in [True, False]:
    save_icon(32, draw_lock_32, enc, "obj32")

print()
print("Done! All icons generated.")
