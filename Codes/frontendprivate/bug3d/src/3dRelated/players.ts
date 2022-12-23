import * as THREE from "three";
import {Point} from "@/3dRelated/point";
import {CSS2DObject} from 'three/examples/jsm/renderers/CSS2DRenderer.js';
import {NAME_HEIGHT} from "@/3dRelated/objectsUtils";

//存储了一个用户的信息
export class Player {
    username = "" //用户名
    object: typeof THREE.Group //用户人物
    objectPositionDelta = [0, 0, 0] //人物模型偏移
    loading = false
    readonly nameObject: typeof THREE.CSS2DObject  //人物头上的名称
    stoneObject: typeof THREE.Group

    constructor(username: string) {
        this.username = username
        const nameDiv = document.createElement('div')
        nameDiv.textContent = username
        nameDiv.style.color = "#FFF"
        nameDiv.style.textShadow = "1px 1px 2px #000"
        nameDiv.style.backgroundColor = "rgba(0, 0, 0, 0)"
        nameDiv.style.fontSize = "20px"
        this.nameObject = new CSS2DObject(nameDiv)
    }

    //设置人物模型
    public setObject(object: typeof THREE.Group) {
        this.object = object
        this.nameObject.position.set(0, NAME_HEIGHT, 0)
        this.object.add(this.nameObject)
    }
}
