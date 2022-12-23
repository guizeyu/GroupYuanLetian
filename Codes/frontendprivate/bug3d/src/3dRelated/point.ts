//数学相关，表示一个点或向量
export class Point {
    x = 0
    y = 0
    z = 0

    public constructor(X: number, Y: number, Z: number) {
        this.x = X;
        this.y = Y;
        this.z = Z;
    }

    public equals(p: Point) {
        return this.x.toFixed(1) == p.x.toFixed(1) &&
            this.y.toFixed(1) == p.y.toFixed(1) &&
            this.z.toFixed(1) == p.z.toFixed(1)
    }

    public clone(): Point {
        return new Point(this.x, this.y, this.z)
    }

    public squareLength(): number {
        return this.x * this.x + this.y * this.y + this.z * this.z
    }

    public length(): number {
        return Math.sqrt(this.squareLength())
    }

    public norm(): Point {
        const len = this.length()
        return new Point(this.x / len, this.y / len, this.z / len)
    }

    public add(p: Point): Point {
        return new Point(this.x + p.x, this.y + p.y, this.z + p.z)
    }

    public minus(p: Point): Point {
        return new Point(this.x - p.x, this.y - p.y, this.z - p.z)
    }

    //乘以标量
    public times(c: number): Point {
        return new Point(this.x * c, this.y * c, this.z * c)
    }

    //叉乘
    public cross(p: Point): Point {
        return new Point(this.y * p.z - this.z * p.y, this.z * p.x - this.x * p.z, this.x * p.y - this.y * p.x)
    }

    //点积
    public dot(p: Point): number {
        return this.x * p.x + this.y * p.y + this.z * p.z
    }
}
