package MirrorWorld.content;

import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.util.Time;
import mindustry.entities.Effect;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;

import static arc.math.Angles.randLenVectors;
import static arc.graphics.g2d.Lines.lineAngle;

/**
 * 镜潮（Mirror Tide）特效表。
 *
 * 统一模仿原版「行星际发射终端」的几何风格：
 *   - 多边形 / 菱形等几何形体
 *   - 暗部偏黄、亮部纯白，暗亮两层分离
 *   - 暗层用软光晕近似高斯模糊，亮层降低不透明度
 *   - 所有形体随时间持续旋转 / 缩放，呈现不断变动感
 *
 * 配色：
 *   darkAmber  = #ffb02e（暗部琥珀黄）
 *   darkYellow = #ffd24a（暗部亮黄）
 *   亮层统一使用 Color.white（纯白，低不透明度）
 */
public class MrFx {

    public static final Color darkAmber = Color.valueOf("ffb02e");
    public static final Color darkYellow = Color.valueOf("ffd24a");

    /** 通用几何爆发：暗层黄晕 + 亮层纯白描边，随时间旋转缩放 */
    public static final Effect
        polyBurst = new Effect(30f, e -> geom(e, 4, 30f, 2.5f)),

        craftFlash = new Effect(18f, e -> geom(e, 4, 16f, 3.2f)),

        /** 多边形拖尾：用于自制弹药尾迹 */
        polyTrail = new Effect(20f, e -> {
            float r = 10f * e.fout();
            Drawf.light(e.x, e.y, r * 1.3f, darkYellow, 0.5f * e.fout());

            Draw.color(Color.white, 0.7f * e.fout());
            Lines.stroke(1.6f * e.fout() + 0.3f);
            Lines.poly(e.x, e.y, 4, r, e.rotation + Time.time * 3f);
            Draw.reset();
        }).layer(Layer.effect),

        /** 迁越·离场：内收菱形 + 黄晕 + 随机射线 */
        mirrorWarp = new Effect(40f, e -> {
            float rot = e.rotation + Time.time * 2.2f;
            float r = 46f * e.fout();

            Drawf.light(e.x, e.y, r * 1.5f, darkAmber, 0.6f * e.fout());

            Draw.color(Color.white, 0.9f * e.fout());
            Lines.stroke(2.6f * e.fout() + 0.4f);
            Lines.poly(e.x, e.y, 4, r, rot);

            Draw.color(Color.white, 0.5f * e.fout());
            Lines.stroke(1.2f * e.fout());
            Lines.poly(e.x, e.y, 4, r * 0.6f, rot + 45f);

            randLenVectors(e.id, 12, r * 0.9f, (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 4f));
            Draw.reset();
        }).layer(Layer.effect),

        /** 迁越·到场：外扩菱形 + 黄晕 */
        mirrorWarpIn = new Effect(40f, e -> {
            float p = e.finpow();
            float rot = e.rotation + Time.time * 2.2f;
            float r = 46f * p;

            Drawf.light(e.x, e.y, r * 1.5f, darkAmber, 0.6f * e.fout());
            Drawf.light(e.x, e.y, r * 0.8f, darkYellow, 0.7f * e.fout());

            Draw.color(Color.white, 0.9f * e.fout());
            Lines.stroke(2.6f * e.fout() + 0.4f);
            Lines.poly(e.x, e.y, 4, r, rot);

            Draw.color(Color.white, 0.5f * e.fout());
            Lines.stroke(1.2f * e.fout());
            Lines.poly(e.x, e.y, 6, r * 0.66f, -rot);

            Draw.reset();
        }).layer(Layer.effect),

        /** 核心召唤·迁越裂隙：召唤单位时使用，六边形 + 内菱形 + 射线 */
        summonRift = new Effect(60f, e -> {
            float p = e.finpow();
            float rot = e.rotation + Time.time * 3f + e.fin() * 90f;

            Drawf.light(e.x, e.y, 70f * p, darkAmber, 0.7f * e.fout());
            Drawf.light(e.x, e.y, 40f * p, darkYellow, 0.8f * e.fout());

            Draw.color(Color.white, 0.9f * e.fout());
            Lines.stroke(3f * e.fout() + 0.5f);
            Lines.poly(e.x, e.y, 6, 60f * p, rot);

            Draw.color(Color.white, 0.6f * e.fout());
            Lines.stroke(1.6f * e.fout());
            Lines.poly(e.x, e.y, 4, 34f * p, -rot);

            randLenVectors(e.id, 16, 70f * p, (x, y) -> lineAngle(e.x + x, e.y + y, Mathf.angle(x, y), e.fout() * 5f + 1f));
            Draw.reset();
        }).layer(Layer.effect),

        /** 发射终端回声：外扩圆环 + 内嵌菱形，用于多配方工厂 / 发射类建筑 */
        launchEcho = new Effect(26f, e -> {
            float r = 4f + e.finpow() * 130f;

            Drawf.light(e.x, e.y, r, darkYellow, 0.4f * e.fout());

            Draw.color(Color.white, 0.7f * e.fout());
            Lines.stroke(2f * e.fout() + 0.3f);
            Lines.poly(e.x, e.y, 6, r, e.rotation + Time.time * 1.5f);

            Draw.color(Color.white, 0.5f * e.fout());
            Lines.stroke(1.2f * e.fout());
            Lines.poly(e.x, e.y, 4, r * 0.7f, e.rotation + Time.time * 2f);

            Draw.reset();
        }).layer(Layer.effect),

        /** 打印单位：自下而上扫描的层叠菱形 + 底座方框，无任何圆形 */
        unitPrint = new Effect(42f, e -> {
            float p = e.finpow();
            float rot = e.rotation + Time.time * 2.5f;

            Drawf.light(e.x, e.y, 42f * p, darkAmber, 0.5f * e.fout());

            // 三层菱形自下而上扫描，像打印机逐层成形
            for(int i = 0; i < 3; i++){
                float h = (e.fin() + i / 3f) % 1f;
                float yy = e.y - 26f + h * 52f;
                float w = 16f * (1f - Math.abs(h - 0.5f) * 1.4f);

                Draw.color(Color.white, 0.85f * e.fout() * (1f - Math.abs(h - 0.5f)));
                Lines.stroke(1.8f);
                Lines.poly(e.x, yy, 4, Math.max(w, 0.5f), rot + i * 30f);
            }

            // 底座方框
            Draw.color(Color.white, 0.5f * e.fout());
            Lines.stroke(1.4f);
            Lines.square(e.x, e.y - 26f, 24f * p, 45f);

            Draw.reset();
        }).layer(Layer.effect),

        /** 电磁弹射导轨：两条平行亮线 + 前端 V 形箭头，沿 rotation 方向 */
        catapult = new Effect(22f, e -> {
            float len = 90f * e.finpow();
            float dx = Mathf.cosDeg(e.rotation), dy = Mathf.sinDeg(e.rotation);
            float px = -dy, py = dx;

            Drawf.light(e.x, e.y, 40f, darkYellow, 0.45f * e.fout());

            for(int s = -1; s <= 1; s += 2){
                float ox = px * 8f * s, oy = py * 8f * s;
                Draw.color(Color.white, 0.75f * e.fout());
                Lines.stroke(2.2f);
                Lines.line(e.x + ox, e.y + oy, e.x + ox + dx * len, e.y + oy + dy * len);
            }

            float hx = e.x + dx * len, hy = e.y + dy * len;
            Draw.color(Color.white, 0.85f * e.fout());
            Lines.stroke(2f);
            Lines.line(hx - dx * 10f + px * 8f, hy - dy * 10f + py * 8f, hx, hy);
            Lines.line(hx - dx * 10f - px * 8f, hy - dy * 10f - py * 8f, hx, hy);

            Draw.reset();
        }).layer(Layer.effect),

        /** 地形改造闪光：菱形 + 内接方形 */
        terraform = new Effect(30f, e -> {
            float p = e.finpow();
            float rot = e.rotation + Time.time * 1.5f;

            Drawf.light(e.x, e.y, 36f * p, darkYellow, 0.6f * e.fout());

            Draw.color(Color.white, 0.85f * e.fout());
            Lines.stroke(2.2f * e.fout() + 0.3f);
            Lines.poly(e.x, e.y, 4, 30f * p, rot);

            Draw.color(Color.white, 0.5f * e.fout());
            Lines.stroke(1f * e.fout());
            Lines.square(e.x, e.y, 24f * p, rot + 45f);

            Draw.reset();
        }).layer(Layer.effect);

    /** 六边形软光晕：替代 Drawf.circles 的圆形光晕，保持镜潮几何风格 */
    public static void polyGlow(float x, float y, float radius, Color color){
        Draw.color(color);
        for(int i = 0; i < 3; i++){
            Lines.stroke(Math.max(0.6f, 2.2f - i * 0.5f));
            Lines.poly(x, y, 6, radius * (1f - i * 0.10f), Time.time * 0.5f + i * 20f);
        }
        Draw.reset();
    }

    /** 六边形范围框：替代 Drawf.dashCircle / Lines.circle 的圆形范围显示 */
    public static void hexRing(float x, float y, float radius, Color color){
        Draw.color(color);
        Lines.stroke(2f);
        Lines.poly(x, y, 6, radius, Time.time * 0.35f);
        Draw.reset();
    }

    /** 直线进度条：几何风格，替代圆形进度环。位于 (x, y) 处，以中心点居中 */
    public static void bar(float x, float y, float width, float progress, Color color){
        float h = 4f;
        Draw.color(Color.black, 0.45f);
        Fill.rect(x, y, width + 2f, h + 2f);
        Draw.color(color);
        Fill.rect(x - width / 2f + width * progress / 2f, y, width * progress, h);
        Draw.reset();
    }


    /** 通用几何特效核心：暗层偏黄软光晕（高斯模糊近似）+ 亮层纯白低透明描边 */
    private static void geom(Effect.EffectContainer e, int sides, float baseRadius, float spin){
        float p = e.finpow();
        float rot = e.rotation + Time.time * spin + e.fin() * 60f;

        Drawf.light(e.x, e.y, baseRadius * 1.6f * p, darkAmber, 0.5f * e.fout());
        Drawf.light(e.x, e.y, baseRadius * 0.9f * p, darkYellow, 0.65f * e.fout());

        Draw.color(Color.white, 0.8f * e.fout());
        Lines.stroke(2.4f * e.fout() + 0.3f);
        Lines.poly(e.x, e.y, sides, baseRadius * p, rot);

        Draw.color(Color.white, 0.45f * e.fout());
        Lines.stroke(1.2f * e.fout());
        Lines.poly(e.x, e.y, sides, baseRadius * 0.72f * p, rot + 180f / sides);

        Draw.reset();
    }
}
