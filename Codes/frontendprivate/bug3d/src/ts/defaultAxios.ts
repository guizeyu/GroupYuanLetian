import axios, {AxiosError, AxiosRequestConfig, AxiosResponse, AxiosStatic} from "axios"
import store from "@/store";

axios.defaults.baseURL = store.getters.httpURL

const DEBUG: boolean = (process.env.NODE_ENV === 'development')

if (DEBUG)
    // axios.defaults.baseURL = "http://localhost:3000"
    axios.defaults.baseURL = store.getters.httpURL

//默认response
function defaultResponse(response: AxiosResponse) {
    if (DEBUG)
        console.log(response.data)
    if ((response.status >= 200 && response.status <= 299) || response.status == 304)
        return response.data //将data返回，丢掉其余的元信息
    else {
        console.error(response)
        throw response.status
    }
}

//默认response出错
function defaultResponseError(error: AxiosError) {
    if (error) {
        return Promise.reject(error)
    }
}

//默认，检查用户登陆状态，如果登陆，会将token加到headers里
function defaultLoginCheck(config: AxiosRequestConfig) {
    if (DEBUG)
        console.log(config)
    //使用白名单机制，除了这几个url，其余都需要身份验证
    if ((<string>(config.url)).indexOf("/login") == -1 &&
        (<string>(config.url)).indexOf("/register") == -1) {
        if (!store.getters.isLogin || typeof (config.headers) == 'undefined')
            return Promise.reject("not authenticated")
        else {
            config.headers.token = store.getters.token
            return config
        }
    }
    else {
        return config
    }
}

//默认拦截器
axios.interceptors.request.use(defaultLoginCheck, error => Promise.reject(error))
axios.interceptors.response.use(defaultResponse, defaultResponseError)

export class Message {
    public success = true
    public information = ""
}

//请求参数基类
export class SenderRequest {

}

//响应参数基类
export class SenderResponse {
    public message = new Message()
}

//factory工厂
export function create<Type>(c: { new(): Type }): Type {
    return new c();
}

//检查请求参数是否合法的默认参数
function defaultValidate<Q extends SenderRequest, S extends SenderResponse>(req: Q, rsp: typeof SenderResponse): S {
    return <S>create(rsp);
}

class Sender {
    private axios: AxiosStatic

    public constructor(axios: AxiosStatic) {
        this.axios = axios
    }

    private async send<Q extends SenderRequest, S extends SenderResponse>
    (_url: string, req: Q, rspType: typeof SenderResponse,
     validate: (req: Q, rsp: typeof SenderResponse) => S = defaultValidate,
     method: "GET" | "POST"): Promise<S> {
        let rsp: S = validate(req, rspType)
        //如果request合法，那么发送请求
        if (rsp.message.information.length == 0) {
            const body: AxiosRequestConfig = {}
            body["method"] = method
            body["url"] = _url
            if (method == "GET") body["params"] = req
            else if (method == "POST") body["data"] = req
            await this.axios(body).then(response => {
                rsp = <S><unknown>response
            }).catch(error => {
                console.log(error)
                alert('network error')
            })
        }
        else {
            rsp.message.success = false
        }
        return new Promise<S>(resolve => {
            resolve(rsp)
        })
    }

    public async post<Q extends SenderRequest, S extends SenderResponse>
    (_url: string, req: Q, rspType: typeof SenderResponse, validate: (req: Q, rsp: typeof SenderResponse) => S = defaultValidate): Promise<S> {
        return this.send(_url, req, rspType, validate, "POST");
    }

    public async get<Q extends SenderRequest, S extends SenderResponse>
    (_url: string, req: Q, rspType: typeof SenderResponse, validate: (req: Q, rsp: typeof SenderResponse) => S = defaultValidate): Promise<S> {
        return this.send(_url, req, rspType, validate, "GET");
    }

    public async sendBlob<Q extends SenderRequest, S extends SenderResponse>
    (_url: string, req: Q, rspType: typeof SenderResponse, form: FormData,
     validate: (req: Q, rsp: typeof SenderResponse) => S = defaultValidate,
     config: AxiosRequestConfig = {headers: {'Content-Type': 'multipart/form-data'}}): Promise<S> {
        let rsp: S = validate(req, rspType)
        //如果request合法，那么发送请求
        if (rsp.message.information.length == 0) {
            if (DEBUG) {
                for (const e in req) {
                    console.log(e + " : " + form.get(e))
                }
            }
            await this.axios.post(_url, form, config).then(response => {
                rsp = <S><unknown>response
            }).catch(error => {
                console.log(error)
                alert('network error')
            })
        }
        else {
            rsp.message.success = false
        }
        return new Promise<S>(resolve => {
            resolve(rsp)
        })
    }
}

export const sender: Sender = new Sender(axios)
