package MirrorWorld.content;

import arc.graphics.Blending;
import arc.graphics.Color;
import arc.graphics.Gl;
import arc.graphics.Mesh;
import arc.graphics.VertexAttribute;
import arc.graphics.gl.Shader;
import arc.math.Mathf;
import arc.math.geom.Mat3D;
import mindustry.graphics.Shaders;
import mindustry.graphics.g3d.GenericMesh;
import mindustry.graphics.g3d.PlanetParams;
import mindustry.type.Planet;

/**
 * 人造星环：一个位于赤道平面（XZ 平面）的半透明环带，
 * 通过 MultiMesh 与行星本体一起渲染。内缘明亮、外缘渐隐，并带轻微
 * 分段明暗，营造“人工建造”的科技感。
 */
public class RingMesh implements GenericMesh {
    /** 环带圆周分段数 */
    public static final int segments = 180;
    /** 明暗分段的“面板”数量 */
    public static final int panels = 28;

    public final Planet planet;
    protected Mesh mesh;
    protected Shader shader;

    public RingMesh(Planet planet) {
        this(planet, Color.valueOf("ffe08a"), Color.valueOf("8fd8e8"));
    }

    public RingMesh(Planet planet, Color inner, Color outer) {
        this.planet = planet;
        this.shader = new Shaders.LoadShader("ring", "ring");
        this.mesh = build(inner, outer);
    }

    private Mesh build(Color inner, Color outer) {
        float rIn = planet.radius * 1.32f;
        float rOut = planet.radius * 1.85f;

        // 每段两个三角形（6 顶点），每顶点 4 个 float：xyz + 颜色位
        float[] buf = new float[segments * 6 * 4];
        Color c = new Color();

        int p = 0;
        for (int i = 0; i < segments; i++) {
            float a0 = (float)i / segments * 360f;
            float a1 = (float)(i + 1) / segments * 360f;

            float cos0 = Mathf.cosDeg(a0), sin0 = Mathf.sinDeg(a0);
            float cos1 = Mathf.cosDeg(a1), sin1 = Mathf.sinDeg(a1);

            // 面板明暗：沿圆周做周期性明暗，形成分段感
            float band = 0.72f + 0.28f * Math.abs(Mathf.sinDeg(a0 * panels));

            color(c, inner, outer, 0f, band);
            float ci0 = c.toFloatBits();
            color(c, inner, outer, 0f, band);
            float ci1 = c.toFloatBits();
            color(c, inner, outer, 1f, band);
            float co0 = c.toFloatBits();
            color(c, inner, outer, 1f, band);
            float co1 = c.toFloatBits();

            float ix0 = cos0 * rIn, iz0 = sin0 * rIn;
            float ix1 = cos1 * rIn, iz1 = sin1 * rIn;
            float ox0 = cos0 * rOut, oz0 = sin0 * rOut;
            float ox1 = cos1 * rOut, oz1 = sin1 * rOut;

            // 三角形 1：内0 -> 内1 -> 外0
            p = put(buf, p, ix0, iz0, ci0);
            p = put(buf, p, ix1, iz1, ci1);
            p = put(buf, p, ox0, oz0, co0);
            // 三角形 2：内1 -> 外1 -> 外0
            p = put(buf, p, ix1, iz1, ci1);
            p = put(buf, p, ox1, oz1, co1);
            p = put(buf, p, ox0, oz0, co0);
        }

        Mesh m = new Mesh(true, segments * 6, 0, VertexAttribute.position3, VertexAttribute.color);
        m.setVertices(buf);
        return m;
    }

    private void color(Color out, Color inner, Color outer, float f, float band) {
        out.set(inner).lerp(outer, f);
        out.a = Mathf.clamp(Mathf.lerp(0.9f, 0.12f, f) * band, 0f, 1f);
    }

    private int put(float[] buf, int p, float x, float z, float colorBits) {
        buf[p] = x;
        buf[p + 1] = 0f;
        buf[p + 2] = z;
        buf[p + 3] = colorBits;
        return p + 4;
    }

    @Override
    public void render(PlanetParams params, Mat3D projection, Mat3D transform) {
        if (mesh.isDisposed()) {
            return;
        }
        // 半透明环带：只读深度（被行星遮挡的部分自然消隐），不写深度；
        // 关闭背面剔除，让环带正反两面都可见。
        Gl.depthMask(false);
        Gl.disable(Gl.cullFace);
        Blending.normal.apply();

        shader.bind();
        shader.setUniformMatrix4("u_proj", projection.val);
        shader.setUniformMatrix4("u_trans", transform.val);
        shader.apply();
        mesh.render(shader, Gl.triangles);

        Gl.enable(Gl.cullFace);
        Gl.depthMask(true);
        Blending.normal.apply();
    }

    @Override
    public void dispose() {
        mesh.dispose();
    }
}
