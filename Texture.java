public final class Texture {
    public static final int SIZE = 64;
    public final int[] pixels;

    public Texture(int type) {
        pixels = new int[SIZE * SIZE];
        generateTexture(type);
    }

    private void generateTexture(int type) {
        for (int y = 0; y < SIZE; y++) {
            for (int x = 0; x < SIZE; x++) {
                int idx = y * SIZE + x;
                switch (type) {
                    case 1 -> {
                        boolean frame = (x < 3 || x > SIZE - 4 || y < 3 || y > SIZE - 4);
                        boolean cyanStrip = (x >= 8 && x <= 10) || (x >= SIZE - 11 && x <= SIZE - 9);
                        boolean rivet = ((x == 5 || x == SIZE - 6) && (y == 5 || y == SIZE - 6 || y == 31));

                        if (cyanStrip && y >= 8 && y <= SIZE - 9) {
                            pixels[idx] = 0xFF00E5FF;
                        } else if (frame) {
                            pixels[idx] = 0xFF0F172A;
                        } else if (rivet) {
                            pixels[idx] = 0xFF94A3B8;
                        } else {
                            pixels[idx] = 0xFF1E293B;
                        }
                    }
                    case 2 -> {
                        boolean vent = (y % 8 >= 2 && y % 8 <= 5) && (x >= 12 && x <= SIZE - 13);
                        boolean border = (x == 0 || y == 0 || x == SIZE - 1 || y == SIZE - 1);
                        boolean heatPipe = (x == 31 || x == 32);

                        if (heatPipe) {
                            pixels[idx] = 0xFFFF5400;
                        } else if (vent) {
                            pixels[idx] = 0xFFFFB703;
                        } else if (border) {
                            pixels[idx] = 0xFF181008;
                        } else {
                            pixels[idx] = 0xFF2B221B;
                        }
                    }
                    case 3 -> {
                        boolean seam = (x == 0 || y == 0 || x == SIZE - 1 || y == SIZE - 1 || x == 32 || y == 32);
                        boolean indicator = (x >= 28 && x <= 35 && y >= 14 && y <= 18);
                        boolean hazardAccent = (y >= SIZE - 6 && ((x + y) / 4) % 2 == 0);

                        if (indicator) {
                            pixels[idx] = 0xFF38BDF8;
                        } else if (hazardAccent) {
                            pixels[idx] = 0xFFFFCC00;
                        } else if (seam) {
                            pixels[idx] = 0xFF64748B;
                        } else {
                            pixels[idx] = 0xFFCBD5E1;
                        }
                    }
                    case 4 -> {
                        boolean doorFrame = (x < 5 || x > SIZE - 6 || y < 5 || y > SIZE - 6);
                        boolean beacon = (x >= 27 && x <= 36 && y >= 12 && y <= 20);
                        boolean stripes = ((x + y) / 8) % 2 == 0 && (y >= 44 && y <= 54);

                        if (beacon) {
                            pixels[idx] = 0xFF00FF88;
                        } else if (stripes) {
                            pixels[idx] = 0xFFFFB703;
                        } else if (doorFrame) {
                            pixels[idx] = 0xFF0F172A;
                        } else {
                            pixels[idx] = 0xFF334155;
                        }
                    }
                    case 5 -> {
                        double dx = x - SIZE / 2.0;
                        double dy = y - SIZE / 2.0;
                        double dist = Math.sqrt(dx * dx + dy * dy);

                        boolean cap = (Math.abs(dx) <= 12 && (y >= 8 && y <= 14)) || 
                                      (Math.abs(dx) <= 12 && (y >= 50 && y <= 56));
                        boolean corePin = (Math.abs(dx) <= 2 && y >= 14 && y <= 50);

                        if (dist < 9) {
                            pixels[idx] = 0xFFFFFFFF;
                        } else if (dist < 15) {
                            pixels[idx] = 0xFF00F5D4;
                        } else if (dist < 18) {
                            pixels[idx] = 0xFF0077B6;
                        } else if (cap || corePin) {
                            pixels[idx] = 0xFF1E293B;
                        } else {
                            pixels[idx] = 0x00000000;
                        }
                    }
                    default -> pixels[idx] = 0xFF222222;
                }
            }
        }
    }

    public int getPixel(int textX, int textY) {
        if (textX < 0) textX = 0;
        if (textX >= SIZE) textX = SIZE - 1;
        if (textY < 0) textY = 0;
        if (textY >= SIZE) textY = SIZE - 1;
        return pixels[textY * SIZE + textX];
    }
}