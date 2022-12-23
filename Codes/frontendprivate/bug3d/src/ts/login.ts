import {sender, SenderRequest, SenderResponse} from "@/ts/defaultAxios";

export class LoginRequest extends SenderRequest {
    username = ""
    password = ""
}

export class LoginResponse extends SenderResponse {
    token = ""
}

//登陆
export async function login(req: LoginRequest): Promise<LoginResponse> {
    let rsp: LoginResponse = new LoginResponse();
    await sender.post<LoginRequest, LoginResponse>("/login", req, LoginResponse).then(response => {
        rsp = response;
    })
    return new Promise<LoginResponse>(resolve => {
        resolve(rsp)
    })
}
