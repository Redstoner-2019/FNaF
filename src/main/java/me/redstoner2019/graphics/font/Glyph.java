package me.redstoner2019.graphics.font;

public class Glyph {
    public float width;
    public float height;
    public float x;
    public float y;
    public float advance;

    public Glyph(float width, float height, float x, float y, float advance) {
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;
        this.advance = advance;
    }
}
