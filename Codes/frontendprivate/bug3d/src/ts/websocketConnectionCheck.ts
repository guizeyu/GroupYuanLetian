import {sender, SenderRequest, SenderResponse} from "@/ts/defaultAxios";

export class WsConnCheckRequest extends SenderRequest {

}

export class WsConnCheckResponse extends SenderResponse {
    connected = false
}

//检查重复登录
export async function checkWsConnection(req: WsConnCheckRequest): Promise<WsConnCheckResponse> {
    let rsp: WsConnCheckResponse = new WsConnCheckResponse();
    await sender.post<WsConnCheckRequest, WsConnCheckResponse>("/websocketConnectionCheck", req, WsConnCheckResponse).then(response => {
        rsp = response;
    })
    return new Promise<WsConnCheckResponse>(resolve => {
        resolve(rsp)
    })
}
