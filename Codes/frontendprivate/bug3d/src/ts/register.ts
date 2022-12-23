import {sender, SenderRequest, SenderResponse} from "@/ts/defaultAxios";

export class RegisterRequest extends SenderRequest {
    username = ""
    password = ""
    email = ""
    password_repeat = ""
}

export class RegisterResponse extends SenderResponse {

}

export function usernameValidate(username: string): boolean {
    return /^[0-9a-zA-Z]{6,20}$/.test(username)
}

export function passwordValidate(password: string): boolean {
    // eslint-disable-next-line
    return /^[0-9a-zA-Z\!\@\#\$\%\^\&\*\(\)\_\-\+\=\/\.\`\~]{6,20}$/.test(password)
}

export function emailValidate(email: string): boolean {
    // eslint-disable-next-line
    return /^([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x22([^\x0d\x22\x5c\x80-\xff]|\x5c[\x00-\x7f])*\x22)(\x2e([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x22([^\x0d\x22\x5c\x80-\xff]|\x5c[\x00-\x7f])*\x22))*\x40([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x5b([^\x0d\x5b-\x5d\x80-\xff]|\x5c[\x00-\x7f])*\x5d)(\x2e([^\x00-\x20\x22\x28\x29\x2c\x2e\x3a-\x3c\x3e\x40\x5b-\x5d\x7f-\xff]+|\x5b([^\x0d\x5b-\x5d\x80-\xff]|\x5c[\x00-\x7f])*\x5d))*(\.\w{2,})+$/.test(email)
}

//注册信息合法性检查
function validate(req: RegisterRequest): RegisterResponse {
    const rsp: RegisterResponse = new RegisterResponse()
    if (!usernameValidate(req.username))
        rsp.message.information = "用户名必须由6至20位的大小写字母和数字组成"
    else if (!passwordValidate(req.password))
        rsp.message.information = "密码必须由6至20位的大小写字母、数字和特殊字符组成"
    else if (req.password != req.password_repeat)
        rsp.message.information = "两次输入密码不一致"
    else if (!emailValidate(req.email))
        rsp.message.information = "请输入正确的邮箱"
    return rsp
}

//注册
export async function register(req: RegisterRequest): Promise<RegisterResponse> {
    let rsp: RegisterResponse = new RegisterResponse()
    await sender.post("/register", req, RegisterResponse, validate).then(response => {
        rsp = response
    })
    return new Promise<RegisterResponse>(resolve => {
        resolve(rsp)
    })
}
