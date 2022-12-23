import {sender, SenderRequest, SenderResponse} from "@/ts/defaultAxios";

export class BackstageRequest extends SenderRequest {

}

export class SingleBackstageInfo {
    type = ""
    info = ""
}

export class BackstageResponse extends SenderResponse {
    information = Array<SingleBackstageInfo>()
}

//查询用户后台信息
export async function queryBackstage(req: BackstageRequest): Promise<BackstageResponse> {
    let rsp: BackstageResponse = new BackstageResponse();
    await sender.post<BackstageRequest, BackstageResponse>("/backstage", req, BackstageResponse).then(response => {
        rsp = response;
    })
    return new Promise<BackstageResponse>(resolve => {
        resolve(rsp)
    })
}
