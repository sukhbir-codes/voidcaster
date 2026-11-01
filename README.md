# VoidCaster — Production Software Raycasting Engine

A software-rendered pseudo-3D raycasting engine written from scratch in pure Java. The project bypasses third-party game frameworks and graphics libraries (such as OpenGL, DirectX, or LibGDX), implementing direct memory rasterization, linear algebra transformations, and ray traversal entirely on the CPU.

---

## Technical & Architectural Highlights

### 1. Direct Framebuffer Rasterization
- Uses a memory-mapped 1D integer array (`pixels[]`) directly extracted from an off-screen `BufferedImage` via `DataBufferInt`.
- Bypasses standard `Graphics2D` drawing primitives for world geometry, converting 2D raster coordinates $(x, y)$ to flat memory addresses via $y \times \text{WIDTH} + x$ for direct 32-bit ARGB writes (`0xAARRGGBB`).

### 2. Hardware Triple-Buffering & OS Synchronization
- Integrates `java.awt.Canvas` backed by an active `BufferStrategy(3)` to eliminate screen tearing and rendering artifacts.
- Enforces frame presentation timing with `Toolkit.getDefaultToolkit().sync()` to flush display commands directly to the platform window server.

### 3. Decoupled Fixed-Timestep Loop
- Implements an accumulator game loop running physics and camera updates at an exact 60 Hz tick rate ($dt = \frac{1}{60}\text{s}$).
- Decouples collision detection and movement kinematics from monitor refresh rates and frame draw times, preventing clipping and variable-speed bugs under load.

### 4. DDA (Digital Differential Analysis) Grid Traversal
- Casts rays per vertical screen column using 2D direction and camera plane vectors ($\vec{D}$ and $\vec{P}$) with zero trigonometric function calls inside the scanline loop.
- Calculates step increments and grid crossings via `deltaDist` and `sideDist` vectors.
- Uses perpendicular wall distance ($perpWallDist$) projection to eliminate fish-eye optical distortion.

### 5. Depth-Sorted Billboard Sprite Pipeline
- Projects floating energy cores into camera space using the inverse of the 2D camera matrix.
- Renders sprites with correct occlusion against walls using a dedicated 1D horizontal $Z$-buffer (`zBuffer[x]`).
- Features real-time sinusoidal vertical hovering animations and distance-based depth fogging.

### 6. Dynamic Tactical Navigation System
- Real-time radar/mini-map rendered in the HUD displaying facility layout, active player orientation, camera FOV cone projection, and live uncollected core coordinates.
- Dynamic distance tracker calculating Euclidean ranges to the nearest active objective and evacuation airlock.

---

## Controls

| Key / Input | Action |
| :--- | :--- |
| `W` / `Up Arrow` | Move Forward |
| `S` / `Down Arrow` | Move Backward |
| `A` | Strafe Left |
| `D` | Strafe Right |
| `Left Arrow` | Rotate View Left |
| `Right Arrow` | Rotate View Right |
| `Shift` (Hold) | Sprint / Overdrive Mode |

---

## Project Structure

```text
VoidCaster/
├── Camera.java       # 2D vector kinematics, rotation matrix & sliding collision
├── Map.java          # Facility grid definition, tile logic & core entities
├── RayCaster.java    # DDA traversal, floor/ceiling rendering & sprite billboard pipeline
├── Texture.java      # Procedural 64x64 32-bit ARGB texture generators & sampling
├── VoidEngine.java   # Engine entry point, triple-buffer loop, HUD & key listeners
└── README.md         # Technical documentation
```
Build & Run Instructions
Ensure JDK 17 or higher is installed and configured on your system path.

1. Compile Source Files
```Bash
javac Texture.java Map.java Camera.java RayCaster.java VoidEngine.java
```
2. Run the Engine
```Bash
java VoidEngine
```
