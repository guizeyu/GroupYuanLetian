//左补齐
function leftPad(text: string, pad: string, length: number): string {
    while (text.length < length) {
        text = pad + text
    }
    return text
}

//聊天消息基类
export class ChatMessage {
    public content = ""
    public time = ""

    constructor() {
        const now = new Date()
        this.time = leftPad(now.getHours().toString(), '0', 2) + ":"
            + leftPad(now.getMinutes().toString(), '0', 2) + ":"
            + leftPad(now.getSeconds().toString(), '0', 2)
    }
}

//系统消息
export class SystemMessage extends ChatMessage {

    constructor(content: string) {
        super();
        this.content = content
    }
}

//对所有人说消息
export class ChatToAllMessage extends ChatMessage {
    public from: string

    constructor(from: string, content: string) {
        super();
        this.from = from
        this.content = content
    }
}

//别人的私聊消息
export class HisPrivateMessage extends ChatMessage {
    public from: string

    constructor(from: string, content: string) {
        super();
        this.from = from
        this.content = content
    }
}

//我的私聊消息
export class MyPrivateMessage extends ChatMessage {
    public to: string

    constructor(to: string, content: string) {
        super();
        this.to = to
        this.content = content
    }
}
