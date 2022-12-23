import * as THREE from "three";
import {GLTFLoader} from "three/examples/jsm/loaders/GLTFLoader.js";
import {FBXLoader} from "three/examples/jsm/loaders/FBXLoader.js";

//负责处理一些3D物体的常见操作
class ObjectsUtils {
    private readonly gltfLoader = new GLTFLoader()
    private readonly fbxLoader = new FBXLoader()

    //销毁3D物体，释放内存
    public disposeIt(mesh: any) {
        if (mesh instanceof THREE.Mesh) {
            if (mesh.geometry.dispose)
                mesh.geometry.dispose()
            if (mesh.material.texture.dispose)
                mesh.material.texture.dispose()
            if (mesh.material.dispose)
                mesh.material.dispose()
        }
        if (mesh instanceof THREE.Group)
            mesh.clear()
        if (mesh instanceof THREE.Object3D)
            mesh.clear()
    }

    //加载fbx模型文件
    public loadFbx(url: string): Promise<typeof THREE.Group> {
        return new Promise<typeof THREE.Group>((resolve, reject) => {
            this.fbxLoader.load(url,
                (fbx: any) => {
                    resolve(fbx)
                },
                (xhr: ProgressEvent) => {
                },
                (error: any) => {
                    console.log(error)
                    reject(error)
                }
            )
        })
    }

    //加载glb、gltf模型文件
    public loadGltf(url: string): Promise<typeof THREE.Group> {
        return new Promise<typeof THREE.Group>((resolve, reject) => {
            this.gltfLoader.load(url,
                (gltf: any) => {
                    resolve(gltf.scene)
                },
                (xhr: ProgressEvent) => {

                },
                (error: any) => {
                    console.log(error)
                    reject(error)
                }
            )
        })
    }

    //自动判断模型文件的类型并加载
    public autoLoadObject(url: string): Promise<typeof THREE.Group> {
        return new Promise<typeof THREE.Group>((resolve, reject) => {
            if (url.endsWith(".glb") || url.endsWith(".gltf")) {
                this.loadGltf(url).then(value => {
                    resolve(value)
                })
            }
            else if (url.endsWith(".fbx")) {
                this.loadFbx(url).then(value => {
                    resolve(value)
                })
            }
            else {
                reject("File encoding not supported.")
            }
        })
    }

    //六个人物的位置偏移参数
    public readonly positionDelta = [[], [0, 13.59, 0], [0, 0, 0], [0, 12.09, 0], [0, 0.84, 0], [0, 0.84, 0], [0, 16.59, 0]]
    public readonly scale = [[], 5.25, 3.75, 0.33, 0.75, 4.2, 0.375]

    //加载人物并使用偏移参数
    public autoLoadCharacter(index: number): Promise<Array<any>> {
        return new Promise<Array<any>>((resolve => {
            const url = "obj/" + ["", "character1.glb", "character2.glb", "character3.fbx",
                "character4.fbx", "character5.glb", "character6.fbx"][index]
            this.autoLoadObject(url).then(object => {
                object.scale.set(this.scale[index], this.scale[index], this.scale[index])
                resolve([this.positionDelta[index], object])
            })
        }))
    }
}

//控制参数
export const CHARACTER_HEIGHT = 30//人物身高
export const CHARACTER_WIDTH = 20//人物长宽
export const JUMP_VELOCITY = 300//跳跃力量
export const GRAVITY_ACCELERATION = -1000//重力加速度
export const STOP_ACCELERATION = -10//失去键盘指令后，人物停止的加速度
export const MOVE_ACCELERATION = 500//人物的移动加速度
export const GENTLE_SLOPE_VELOCITY = 30 //人物在缓坡上向上移动的速度
export const ICE_STOP_ACCELERATION = -0.1 //失去键盘指令后，人物在冰面上停止的加速度
export const ICE_MOVE_ACCELERATION = 100//人物在冰面上的移动加速度
export const NAME_HEIGHT = 3//人物名字的相对高度
export const CURLING_STONE_HEIGHT = 3//冰壶的相对高度
export const CURLING_STONE_RADIUS = 15//冰壶的半径
export const CURLING_STONE_STOP_ACCELERATION = -0.1 //冰壶停止的加速度
export const objectsUtils = new ObjectsUtils()
