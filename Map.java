import java.util.ArrayList;
import java.util.List;

public final class Map {
    public static final class CoreEntity {
        public double x;
        public double y;
        public boolean collected;

        public CoreEntity(double x, double y) {
            this.x = x;
            this.y = y;
            this.collected = false;
        }
    }

    private final int width;
    private final int height;
    private final int[][] grid;
    private final List<CoreEntity> cores = new ArrayList<>();

    private static final int[][] FACILITY_MAP = {
        {1,1,1,1,1,1,1,1,1,1,2,2,2,2,2,2,2,2,2,2},
        {1,0,0,0,0,0,1,0,0,0,2,0,0,0,0,0,2,0,5,2},
        {1,0,1,1,0,0,1,0,1,0,2,0,2,2,2,0,2,0,0,2},
        {1,0,1,5,0,0,0,0,1,0,0,0,2,0,0,0,2,2,0,2},
        {1,0,1,1,0,1,1,0,1,1,2,0,2,0,2,0,0,0,0,2},
        {1,0,0,0,0,0,1,0,0,0,2,0,0,0,2,2,2,2,0,2},
        {1,1,1,0,1,0,1,1,1,0,2,2,2,0,0,0,0,2,0,2},
        {1,0,0,0,1,0,0,0,1,0,0,0,2,2,2,2,0,2,0,2},
        {1,0,1,1,1,1,1,0,1,1,1,0,0,0,0,2,0,0,0,2},
        {1,0,0,0,0,0,0,0,0,0,1,1,1,0,0,2,2,2,0,2},
        {3,3,3,0,3,3,3,1,0,0,0,0,1,0,0,0,0,0,0,2},
        {3,0,0,0,0,0,3,1,1,1,0,0,1,1,1,2,2,2,2,2},
        {3,0,3,3,3,0,3,0,0,1,0,0,0,0,1,0,0,0,0,4},
        {3,0,3,5,3,0,3,0,0,1,1,1,1,0,1,0,1,1,0,4},
        {3,0,3,0,0,0,0,0,0,0,0,0,1,0,1,0,1,0,0,1},
        {3,0,3,3,3,3,3,3,0,3,0,0,1,0,0,0,1,0,0,1},
        {3,0,0,0,0,0,0,3,0,3,3,0,1,1,1,0,1,1,0,1},
        {3,3,3,3,3,0,0,3,0,0,3,0,0,0,1,0,0,0,0,1},
        {3,0,0,0,0,0,0,0,0,0,3,0,0,0,1,1,1,1,0,1},
        {3,3,3,3,3,3,3,3,3,3,3,1,1,1,1,1,1,1,4,1}
    };

    public Map() {
        this.grid = FACILITY_MAP;
        this.height = FACILITY_MAP.length;
        this.width = FACILITY_MAP[0].length;

        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                if (grid[r][c] == 5) {
                    cores.add(new CoreEntity(c + 0.5, r + 0.5));
                    grid[r][c] = 0;
                }
            }
        }
    }

    public boolean isSolid(double x, double y) {
        int mapX = (int) x;
        int mapY = (int) y;
        if (mapX < 0 || mapX >= width || mapY < 0 || mapY >= height) {
            return true;
        }
        int tile = grid[mapY][mapX];
        return (tile >= 1 && tile <= 3);
    }

    public int getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return 1;
        return grid[y][x];
    }

    public void checkPickup(double playerX, double playerY) {
        for (CoreEntity core : cores) {
            if (!core.collected) {
                double distSq = (core.x - playerX) * (core.x - playerX) + (core.y - playerY) * (core.y - playerY);
                if (distSq < 0.45) {
                    core.collected = true;
                }
            }
        }
    }

    public int getRemainingCores() {
        int count = 0;
        for (CoreEntity core : cores) {
            if (!core.collected) count++;
        }
        return count;
    }

    public List<CoreEntity> getCores() { return cores; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}