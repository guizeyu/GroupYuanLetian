import {Point} from "@/3dRelated/point";

export const HEARTBEAT = "heartbeat"
export const CHOOSE_SCENE = "choose_scene"
export const CHAT_TO_ALL = "chat_to_all"
export const PRIVATE_CHAT = "chat_private"
export const GET_ALL_USERS = "get_all_users"
export const CHARACTER_MOVE_TO = "character_move_to"

export interface SocketMessage {
    command: string
}

//心跳
export class HeartBeatRequest implements SocketMessage {
    command = HEARTBEAT
}

//选择场景
export class ChooseSceneRequest implements SocketMessage {
    command = CHOOSE_SCENE
    public scene: number

    public constructor(scene: number) {
        this.scene = scene
    }
}

//与所有人聊天
export class ChatToAllRequest implements SocketMessage {
    command = CHAT_TO_ALL
    public scene: number
    public chat_message: string

    public constructor(scene: number, chat_message: string) {
        this.scene = scene
        this.chat_message = chat_message
    }
}

export interface ChatToAllResponse extends SocketMessage {
    scene: number
    from_who: string
    chat_message: string
}

//私聊
export class PrivateChatRequest implements SocketMessage {
    command = PRIVATE_CHAT
    public scene: number
    public to_who: string
    public chat_message: string

    public constructor(scene: number, to_who: string, chat_message: string) {
        this.scene = scene
        this.to_who = to_who
        this.chat_message = chat_message
    }
}

export interface PrivateChatResponse extends SocketMessage {
    from_who: string
    chat_message: string
}

//获取用户列表
export interface GetAllUsersResponse extends SocketMessage {
    users: Array<string>
}

//广播
export abstract class BroadCastRequest implements SocketMessage {
    command = CHARACTER_MOVE_TO
    public abstract operation: string
}

export interface BroadCastResponse extends SocketMessage {
    operation: string
    username: string
}

//移动
export class MoveRequest extends BroadCastRequest {
    public operation = "move"
    public coordinate: Point
    public direction: Point
    public character: number

    constructor(coordinate: Point, direction: Point, character: number) {
        super();
        this.coordinate = coordinate;
        this.direction = direction;
        this.character = character;
    }
}

export interface MoveResponse extends BroadCastResponse {
    username: string
    coordinate: Point
    direction: Point
    character: number
    additionalCommand: string
}

//开关门
export class DoorRequest extends BroadCastRequest {
    public operation = "door"
    public open: boolean

    constructor(open: boolean) {
        super();
        this.open = open;
    }
}

export interface DoorResponse extends BroadCastResponse {
    open: boolean
}

//打破记录
export class BreakPersonalBestScoreRequest extends BroadCastRequest {
    public operation = "break"
    public score: number

    constructor(score: number) {
        super();
        this.score = score;
    }
}

export interface BreakPersonalBestScoreResponse extends BroadCastResponse {
    score: number
}

//冰壶位置
export class CurlingStoneRequest extends BroadCastRequest {
    public operation = "curling"
    public position: Point

    constructor(position: Point) {
        super();
        this.position = position;
    }
}

export interface CurlingStoneResponse extends BroadCastResponse {
    position: Point
}
