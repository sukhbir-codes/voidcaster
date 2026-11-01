import java.util.Comparator;
import java.util.List;

public final class RayCaster {
    private final int width;
    private final int height;
    private final Texture[] textures;
    private final double[] zBuffer;
    private double animTimer = 0;

    public RayCaster(int width, int height) {
        this.width = width;
        this.height = height;
        this.zBuffer = new double[width];
        this.textures = new Texture[] {
            new Texture(1),
            new Texture(2), 
            new Texture(3), 
            new Texture(4),
            new Texture(5) 
        };
    }

    public void update(double dt) {
        animTimer += dt * 4.0;
    }

    public void render(int[] pixels, Camera cam, Map map) {
        int halfH = height / 2;

        for (int y = halfH; y < height; y++) {
            double rayDirX0 = cam.dirX - cam.planeX;
            double rayDirY0 = cam.dirY - cam.planeY;
            double rayDirX1 = cam.dirX + cam.planeX;
            double rayDirY1 = cam.dirY + cam.planeY;

            int p = y - halfH;
            if (p == 0) continue;
            double rowDistance = (0.5 * height) / p;

            double floorStepX = rowDistance * (rayDirX1 - rayDirX0) / width;
            double floorStepY = rowDistance * (rayDirY1 - rayDirY0) / width;

            double floorX = cam.posX + rowDistance * rayDirX0;
            double floorY = cam.posY + rowDistance * rayDirY0;

            double fog = Math.min(1.0, rowDistance / 16.0);

            for (int x = 0; x < width; ++x) {
                int tx = (int) (Texture.SIZE * (floorX - (int) floorX)) & (Texture.SIZE - 1);
                int ty = (int) (Texture.SIZE * (floorY - (int) floorY)) & (Texture.SIZE - 1);

                floorX += floorStepX;
                floorY += floorStepY;

                boolean seam = (tx == 0 || ty == 0);
                int floorCol = seam ? 0xFF0F172A : 0xFF1E293B;
                pixels[y * width + x] = applyFog(floorCol, fog);

                boolean ceilRib = (ty == 16 || ty == 48);
                int ceilCol = ceilRib ? 0xFF1E293B : 0xFF090D16;
                pixels[(height - y - 1) * width + x] = applyFog(ceilCol, fog);
            }
        }

        for (int x = 0; x < width; x++) {
            double cameraX = 2.0 * x / (double) width - 1.0;
            double rayDirX = cam.dirX + cam.planeX * cameraX;
            double rayDirY = cam.dirY + cam.planeY * cameraX;

            int mapX = (int) cam.posX;
            int mapY = (int) cam.posY;

            double deltaDistX = (rayDirX == 0) ? 1e30 : Math.abs(1.0 / rayDirX);
            double deltaDistY = (rayDirY == 0) ? 1e30 : Math.abs(1.0 / rayDirY);

            double sideDistX;
            double sideDistY;
            int stepX;
            int stepY;

            if (rayDirX < 0) {
                stepX = -1;
                sideDistX = (cam.posX - mapX) * deltaDistX;
            } else {
                stepX = 1;
                sideDistX = (mapX + 1.0 - cam.posX) * deltaDistX;
            }

            if (rayDirY < 0) {
                stepY = -1;
                sideDistY = (cam.posY - mapY) * deltaDistY;
            } else {
                stepY = 1;
                sideDistY = (mapY + 1.0 - cam.posY) * deltaDistY;
            }

            boolean hit = false;
            int side = 0;

            while (!hit) {
                if (sideDistX < sideDistY) {
                    sideDistX += deltaDistX;
                    mapX += stepX;
                    side = 0;
                } else {
                    sideDistY += deltaDistY;
                    mapY += stepY;
                    side = 1;
                }
                if (map.isSolid(mapX, mapY) || map.getTile(mapX, mapY) == 4) {
                    hit = true;
                }
            }

            double perpWallDist = (side == 0) ? (sideDistX - deltaDistX) : (sideDistY - deltaDistY);
            if (perpWallDist < 0.001) perpWallDist = 0.001;

            zBuffer[x] = perpWallDist;

            int lineHeight = (int) (height / perpWallDist);
            int drawStart = -lineHeight / 2 + height / 2;
            if (drawStart < 0) drawStart = 0;
            int drawEnd = lineHeight / 2 + height / 2;
            if (drawEnd >= height) drawEnd = height - 1;

            int tile = map.getTile(mapX, mapY);
            int textureIndex = Math.max(0, Math.min(3, tile - 1));
            Texture tex = textures[textureIndex];

            double wallX = (side == 0) ? (cam.posY + perpWallDist * rayDirY) : (cam.posX + perpWallDist * rayDirX);
            wallX -= Math.floor(wallX);

            int texX = (int) (wallX * Texture.SIZE);
            if (side == 0 && rayDirX > 0) texX = Texture.SIZE - texX - 1;
            if (side == 1 && rayDirY < 0) texX = Texture.SIZE - texX - 1;

            double step = 1.0 * Texture.SIZE / lineHeight;
            double texPos = (drawStart - height / 2.0 + lineHeight / 2.0) * step;
            double fogFactor = Math.min(1.0, perpWallDist / 16.0);

            for (int y = drawStart; y <= drawEnd; y++) {
                int texY = (int) texPos & (Texture.SIZE - 1);
                texPos += step;
                int color = tex.getPixel(texX, texY);

                if (side == 1) {
                    color = (color >> 1) & 0x7F7F7F;
                }
                pixels[y * width + x] = applyFog(color, fogFactor);
            }
        }

        List<Map.CoreEntity> cores = map.getCores();
        cores.sort(Comparator.comparingDouble(c -> -((c.x - cam.posX) * (c.x - cam.posX) + (c.y - cam.posY) * (c.y - cam.posY))));

        Texture coreTex = textures[4];
        double bobOffset = Math.sin(animTimer) * 18.0;

        for (Map.CoreEntity core : cores) {
            if (core.collected) continue;

            double spriteX = core.x - cam.posX;
            double spriteY = core.y - cam.posY;

            double invDet = 1.0 / (cam.planeX * cam.dirY - cam.dirX * cam.planeY);
            double transformX = invDet * (cam.dirY * spriteX - cam.dirX * spriteY);
            double transformY = invDet * (-cam.planeY * spriteX + cam.planeX * spriteY);

            if (transformY <= 0.1) continue;

            int spriteScreenX = (int) ((width / 2.0) * (1 + transformX / transformY));
            int spriteHeight = Math.abs((int) (height / transformY));
            int drawStartY = (int) (-spriteHeight / 2 + height / 2 + (bobOffset / transformY));
            if (drawStartY < 0) drawStartY = 0;
            int drawEndY = (int) (spriteHeight / 2 + height / 2 + (bobOffset / transformY));
            if (drawEndY >= height) drawEndY = height - 1;

            int spriteWidth = Math.abs((int) (height / transformY));
            int drawStartX = -spriteWidth / 2 + spriteScreenX;
            if (drawStartX < 0) drawStartX = 0;
            int drawEndX = spriteWidth / 2 + spriteScreenX;
            if (drawEndX >= width) drawEndX = width - 1;

            double fog = Math.min(1.0, transformY / 16.0);

            for (int stripe = drawStartX; stripe < drawEndX; stripe++) {
                int texX = (int) (256 * (stripe - (-spriteWidth / 2 + spriteScreenX)) * Texture.SIZE / spriteWidth) / 256;
                if (stripe >= 0 && stripe < width && transformY < zBuffer[stripe]) {
                    for (int y = drawStartY; y < drawEndY; y++) {
                        int d = (int) ((y - (bobOffset / transformY)) * 256 - height * 128 + spriteHeight * 128);
                        int texY = ((d * Texture.SIZE) / spriteHeight) / 256;
                        int color = coreTex.getPixel(texX, texY);
                        if ((color & 0xFF000000) != 0) {
                            pixels[y * width + stripe] = applyFog(color, fog);
                        }
                    }
                }
            }
        }
    }

    private int applyFog(int color, double fogFactor) {
        if (fogFactor <= 0.0) return color;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;

        int fogR = 0x09;
        int fogG = 0x0D;
        int fogB = 0x16;

        r = (int) (r * (1.0 - fogFactor) + fogR * fogFactor);
        g = (int) (g * (1.0 - fogFactor) + fogG * fogFactor);
        b = (int) (b * (1.0 - fogFactor) + fogB * fogFactor);

        return (0xFF << 24) | (r << 16) | (g << 8) | b;
    }
}