import {Point} from "@/3dRelated/point";

//区域基类
export abstract class Region {
    public abstract within(p: Point): boolean
}

//圆型区域
export class CircleRegion extends Region {
    public readonly point: Point
    public readonly radius: number
    private readonly squareRadius: number

    constructor(point: Point, radius: number) {
        super();
        this.point = point;
        this.radius = radius;
        this.squareRadius = this.radius * this.radius;
    }

    public within(p: Point): boolean {
        return this.point.minus(p).squareLength() < this.squareRadius
    }
}

//矩形区域
export class RectangleRegion extends Region {
    public readonly vertex: Array<Point>

    constructor(vertex1: Point, vertex2: Point) {
        super();
        this.vertex = []
        this.vertex.push(vertex1)
        this.vertex.push(new Point(vertex1.x, vertex1.y, vertex2.z))
        this.vertex.push(vertex2)
        this.vertex.push(new Point(vertex2.x, vertex1.y, vertex1.z))
        for (const i of [1, 2, 3]) {
            this.vertex[i].y = vertex1.y
        }
    }

    public within(p: Point): boolean {
        const direction = this.vertex[1].minus(this.vertex[0]).cross(p.minus(this.vertex[0])).y >= 0
        for (const i of [1, 2, 3]) {
            if ((this.vertex[(i + 1) % 4].minus(this.vertex[i]).cross(p.minus(this.vertex[i])).y >= 0) != direction) return false
        }
        return true
    }

}

//用户进入区域的事件
export abstract class UserEnterRegionEvent {
    public region: Region

    public constructor(region: Region) {
        this.region = region
    }

    public abstract doEvent(): void;
}

//观察者模式
class RegionDealer {
    private readonly events: Array<UserEnterRegionEvent[]>

    constructor() {
        this.events = [[], [], [], []];
    }

    //观察者模式的notify
    public deal(scene: number, p: Point) {
        let e: UserEnterRegionEvent
        for (e of this.events[scene]) {
            if (e.region.within(p)) {
                e.doEvent()
            }
        }
    }

    public registerEvent(scene: number, e: UserEnterRegionEvent) {
        this.events[scene].push(e)
    }

    public unregisterAll(scene: number) {
        this.events[scene] = []
    }
}

//场景编号
export const MUSEUM = 1
export const SLOPE = 2
export const CURLING = 3
export const regionDealer = new RegionDealer()
