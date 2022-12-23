import {sender, SenderRequest, SenderResponse} from "@/ts/defaultAxios";

export class WsForceOffRequest extends SenderRequest {

}

export class WsForceOffResponse extends SenderResponse {

}

//强制下线
export async function forceOffline(req: WsForceOffRequest): Promise<WsForceOffResponse> {
    let rsp: WsForceOffResponse = new WsForceOffResponse();
    await sender.post<WsForceOffRequest, WsForceOffResponse>("/websocketForceOffline", req, WsForceOffResponse).then(response => {
        rsp = response;
    })
    return new Promise<WsForceOffResponse>(resolve => {
        resolve(rsp)
    })
}
