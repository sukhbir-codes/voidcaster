public final class Camera {
    public double posX;
    public double posY;

    public double dirX;
    public double dirY;

    public double planeX;
    public double planeY;

    public double moveSpeed = 4.2;
    public final double walkSpeed = 4.2;
    public final double sprintSpeed = 6.8;
    public final double rotSpeed = 2.6;

    private static final double RADIUS = 0.22;

    public Camera(double startX, double startY, double dirX, double dirY, double fovScale) {
        this.posX = startX;
        this.posY = startY;
        this.dirX = dirX;
        this.dirY = dirY;
        this.planeX = -dirY * fovScale;
        this.planeY = dirX * fovScale;
    }

    public void setSprinting(boolean sprinting) {
        moveSpeed = sprinting ? sprintSpeed : walkSpeed;
    }

    public void moveForward(double dt, Map map) {
        double moveStepX = dirX * moveSpeed * dt;
        double moveStepY = dirY * moveSpeed * dt;
        tryMove(moveStepX, moveStepY, map);
    }

    public void moveBackward(double dt, Map map) {
        double moveStepX = -dirX * moveSpeed * dt;
        double moveStepY = -dirY * moveSpeed * dt;
        tryMove(moveStepX, moveStepY, map);
    }

    public void strafeLeft(double dt, Map map) {
        double moveStepX = -(-dirY) * moveSpeed * dt;
        double moveStepY = -(dirX) * moveSpeed * dt;
        tryMove(moveStepX, moveStepY, map);
    }

    public void strafeRight(double dt, Map map) {
        double moveStepX = (-dirY) * moveSpeed * dt;
        double moveStepY = (dirX) * moveSpeed * dt;
        tryMove(moveStepX, moveStepY, map);
    }

    private void tryMove(double dx, double dy, Map map) {
        double checkX = posX + dx + Math.signum(dx) * RADIUS;
        if (!map.isSolid(checkX, posY)) {
            posX += dx;
        }
        double checkY = posY + dy + Math.signum(dy) * RADIUS;
        if (!map.isSolid(posX, checkY)) {
            posY += dy;
        }
    }

    public void rotate(double angle) {
        double cosA = Math.cos(angle);
        double sinA = Math.sin(angle);

        double oldDirX = dirX;
        dirX = dirX * cosA - dirY * sinA;
        dirY = oldDirX * sinA + dirY * cosA;

        double oldPlaneX = planeX;
        planeX = planeX * cosA - planeY * sinA;
        planeY = oldPlaneX * sinA + planeY * cosA;
    }
}