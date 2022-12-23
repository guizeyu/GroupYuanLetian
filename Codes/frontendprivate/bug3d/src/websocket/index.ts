import {ChooseSceneRequest, HeartBeatRequest, SocketMessage} from "@/websocket/socketMessages";
import router from "@/router";
import store from "@/store";

const DEBUG: boolean = (process.env.NODE_ENV === 'development')

export abstract class EventSubject {
    public command: string

    public constructor(command: string) {
        this.command = command
    }

    public abstract doEvent(message: SocketMessage): void;
}

class Connection {
    private socket: WebSocket | null
    private url: string
    private heartBeating: boolean
    private events: EventSubject[]//Observer
    private cache: SocketMessage[]

    constructor() {
        this.socket = null
        this.url = ""
        this.heartBeating = false
        this.events = []
        this.cache = []
    }

    public connect(url: string) {
        this.url = url
        this.socket = new WebSocket(this.url)
        this.socket!.onopen = () => {
            this.heartBeat()
            this.send(new ChooseSceneRequest(store.getters.scene))
        }
        this.socket!.onmessage = ev => {
            this.onReceive(ev)
        }
        this.socket!.onclose = () => {
            this.heartBeating = false
            console.log("socket closed.")
        }
        this.socket!.onerror = () => {
            this.heartBeating = false
            alert("你的网络发生了一些问题，请尝试重新连接！")
            router.replace({name: 'login'})
        }
    }

    public disconnect(): void {
        this.events = []
        this.heartBeating = false
        this.socket!.close()
        console.log("socket disconnect.")
    }

    public registerEvent(e: EventSubject): void {
        this.events.push(e)
    }

    /*
    notify observers
    */
    public onReceive(message: MessageEvent): void {
        // if (DEBUG) console.log("socket receive:" + message.data)
        const data = <SocketMessage><unknown>(JSON.parse(message.data))
        let e: EventSubject
        for (e of this.events) {
            if (e.command == data.command) {
                e.doEvent(data)
            }
        }
    }

    /*
    发送心跳，直至断开连接
     */
    private heartBeat(): void {
        this.heartBeating = true
        const interval = setInterval(() => {
            if (!this.heartBeating) {
                clearInterval(interval)
                return
            }
            this.send(new HeartBeatRequest())
        }, 2500)
    }

    /*
    清空缓存
     */
    private sendAllCache(): void {
        let m: SocketMessage
        for (m of this.cache) {
            // if (DEBUG) console.log("socket send cache:" + JSON.stringify(m))
            this.socket!.send(JSON.stringify(m))
        }
        this.cache = []
    }

    /*
    惰性载入。在socket建立成功之前调用send函数会将数据存入缓存，
    在socket建立成功之后调用send函数会直接发送数据
     */
    public send(message: SocketMessage): void {
        this.cache.push(message)
        if (this.heartBeating) {
            this.sendAllCache()
            this.send = (message: SocketMessage) => {
                // if (DEBUG) console.log("socket send:" + JSON.stringify(message))
                this.socket!.send(JSON.stringify(message))
            }
        }
    }
}

const connection = new Connection()
export default connection
