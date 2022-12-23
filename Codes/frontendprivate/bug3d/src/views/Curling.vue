<template>
  <chat-component ref="chatboard" v-on:msgSend="lockControls()"/>
  <div id="debug-info" style="display: none">
    <div>{{ coordinate }}</div>
    <div>{{ stoneCoordinate }}</div>
  </div>
  <div id="blocker">
    <div id="instructions" @click="lockControls()">
      <p class="instructions-title">
        单击任何位置回到冰壶馆
      </p>
      <p>
        移动：W、A、S、D<br/>
        跳跃：SPACE<br/>
        加速：SHIFT<br/>
        交互：E<br/>
        移动镜头：鼠标<br/>
        <span class="text-warning">长按Q返回上一页面</span>
      </p>
    </div>
  </div>
  <template v-if="pickCondition() || throwCondition()">
    <div id="interaction" class="rounded-lg border border-secondary bg-white bg-light">
      <h1 class="font-weight-bold text-center">
        E {{ curlingStoneOperation() }}
      </h1>
    </div>
  </template>
  <template v-else-if="!isStoneStop">
    <div class="interaction-hint rounded-lg border border-secondary bg-white bg-light">
      <h1 class="font-weight-bold text-center">
        请等待冰壶静止
      </h1>
    </div>
  </template>
  <template v-else-if="pickedCurlingStone && overBound">
    <div class="interaction-hint rounded-lg border border-secondary bg-white bg-light">
      <h1 class="font-weight-bold text-center">
        不能超越栏线掷球
      </h1>
    </div>
  </template>
  <div id="records" class="text-primary rounded-lg border border-secondary bg-white p-1 bg-light">
    <h4 class="font-weight-bold">
      最佳得分：{{ bestScore }}分
    </h4>
    <h4 class="font-weight-bold">
      当前得分：{{ currentScore }}分
    </h4>
  </div>
  <modal ref="modal">您已在别处登陆，现已将稍早之前的登陆强制下线！</modal>
</template>

<script lang="ts">
import {defineComponent} from "vue"
import * as THREE from "three";
import {CSS2DRenderer} from "three/examples/jsm/renderers/CSS2DRenderer.js";
import {PointerLockControls} from "three/examples/jsm/controls/PointerLockControls.js";
import {Player} from "@/3dRelated/players";
import {Point} from "@/3dRelated/point";
import {
  CHARACTER_HEIGHT,
  CHARACTER_WIDTH,
  CURLING_STONE_HEIGHT,
  CURLING_STONE_RADIUS,
  CURLING_STONE_STOP_ACCELERATION,
  GRAVITY_ACCELERATION,
  ICE_MOVE_ACCELERATION,
  ICE_STOP_ACCELERATION,
  JUMP_VELOCITY,
  objectsUtils
} from "@/3dRelated/objectsUtils";
import ChatComponent from "@/components/Chat/Chat.vue";
import connection, {EventSubject} from "@/websocket";
import {
  BreakPersonalBestScoreRequest, BreakPersonalBestScoreResponse,
  BroadCastResponse,
  CHARACTER_MOVE_TO, CurlingStoneRequest, CurlingStoneResponse, GET_ALL_USERS, GetAllUsersResponse, MoveRequest,
  MoveResponse
} from "@/websocket/socketMessages";
import {CURLING, RectangleRegion, regionDealer, UserEnterRegionEvent} from "@/3dRelated/region";
import store, {UPDATE_BEST_CURLING_SCORE} from "@/store";
import {checkWsConnection, WsConnCheckRequest} from "@/ts/websocketConnectionCheck";
import {forceOffline, WsForceOffRequest} from "@/ts/websocketForceOffline";
import Modal from "@/components/Modal.vue";
import {mapMutations} from "vuex";
//退出游戏
let q = 0
//Three.js对象
let scene: typeof THREE.Scene
let stone: typeof THREE.Group
let camera: typeof THREE.PerspectiveCamera
let labelRenderer: typeof CSS2DRenderer
let renderer: typeof THREE.WebGLRenderer
let controls: typeof PointerLockControls
let clock: typeof THREE.Clock
let objects: Array<typeof THREE.Mesh>
//控制变量
let moveForward: boolean // 是否向前移动
let moveBackward: boolean // 是否向后移动
let moveLeft: boolean // 是否向左移动
let moveRight: boolean // 是否向右移动
let canJump: boolean // 是否能够跳跃
let speedUp: boolean //是否加速
let leftCollide: boolean // 是否左边碰撞
let rightCollide: boolean // 是否右边移动
let forwardCollide: boolean // 是否前面移动
let backCollide: boolean // 是否后面移动
let velocity: typeof THREE.Vector3 //人物移动速度
let direction: typeof THREE.Vector3 //人物移动方向
let rayCaster: typeof THREE.Raycaster //碰撞检测
let downsideDirection: typeof THREE.Vector3 //向下常量
let downsideRayCaster: typeof THREE.Raycaster //向下碰撞检测
let upsideDirection: typeof THREE.Vector3 //向上常量
let upsideRayCaster: typeof THREE.Raycaster //向上碰撞检测
//socket通信
let players: Map<string, Player>
let socketPosition = new Point(0, CHARACTER_HEIGHT, 0)
let socketDirection = new Point(10000, CHARACTER_HEIGHT, 0)
let socketStonePosition = new Point(0, CURLING_STONE_HEIGHT, -700)
//冰壶
let stoneVelocity: typeof THREE.Vector3
export default defineComponent({
  name: "CurlingView",
  components: {ChatComponent, Modal},
  data() {
    return {
      coordinate: "(0, 0, 0)",
      stoneCoordinate: "(0, 0, 0)",
      withinCurlingStone: false,
      pickedCurlingStone: false,
      overBound: false,
      isStoneStop: true,
      bestScore: store.getters.bestCurlingScore,
      currentScore: 0
    }
  },
  methods: {
    //是否达到了捡起冰壶的条件
    pickCondition() {
      return !this.pickedCurlingStone && this.withinCurlingStone && ((!this.overBound) || (this.isStoneStop))
    },
    //是否达到了掷球的条件
    throwCondition() {
      return this.pickedCurlingStone && !this.overBound && this.isStoneStop
    },
    //操作冰壶
    curlingStoneOperation() {
      if (this.pickCondition()) return "握住"
      else return "掷球"
    },
    showSystemMessage(message: string) {
      (this.$refs.chatboard as typeof ChatComponent).showSystemMessage(message)
    },
    beginRender() {
      this.init()
      this.registerRegionEvents()
      this.adjustCamera()
      this.renderGrass()
      this.renderCurling()
      this.initScene()
      this.initRenderer()
      this.initControls()
      this.registerSocketEvents()
      this.animate()
    },
    //初始化变量
    init() {
      q = 0
      scene = new THREE.Scene()
      camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1600)
      labelRenderer = new CSS2DRenderer()
      renderer = new THREE.WebGLRenderer({
        antialias: true,
        logarithmicDepthBuffer: true
      })
      controls = new PointerLockControls(camera, labelRenderer.domElement)
      clock = new THREE.Clock()
      objects = Array<typeof THREE.Mesh>()
      moveForward = false
      moveBackward = false
      moveLeft = false
      moveRight = false
      canJump = false
      speedUp = false
      leftCollide = false
      rightCollide = false
      forwardCollide = false
      backCollide = false
      velocity = new THREE.Vector3()
      direction = new THREE.Vector3()
      rayCaster = new THREE.Raycaster(controls.getObject().position, new THREE.Vector3(), 0, CHARACTER_WIDTH * 2)
      downsideDirection = new THREE.Vector3(0, -1, 0).normalize()
      downsideRayCaster = new THREE.Raycaster(controls.getObject().position, downsideDirection, 0, CHARACTER_HEIGHT + 1)
      upsideDirection = new THREE.Vector3(0, 1, 0).normalize()
      upsideRayCaster = new THREE.Raycaster(controls.getObject().position, upsideDirection, 0, CHARACTER_HEIGHT + 1)
      players = new Map<string, Player>()
      stoneVelocity = new THREE.Vector3(0, 0, 0)
    },
    //区域检测
    registerRegionEvents() {
      const that = this
      regionDealer.registerEvent(CURLING, new class extends UserEnterRegionEvent {
        doEvent(): void {
          that.overBound = true
        }
      }(new RectangleRegion(new Point(-75, CHARACTER_HEIGHT, -550), new Point(75, CHARACTER_HEIGHT, 750))))
    },
    //摄像机
    adjustCamera() {
      camera.position.set(0, CHARACTER_HEIGHT, -650)
      camera.lookAt(0, CHARACTER_HEIGHT, 10000)
    },
    //地面
    renderGrass() {
      let floorGeometry = new THREE.PlaneGeometry(10000, 10000, 100, 100)
      floorGeometry.rotateX(-0.5 * Math.PI)
      const texture = new THREE.TextureLoader().load('texture/grass.jpg')
      texture.wrapS = THREE.RepeatWrapping
      texture.wrapT = THREE.RepeatWrapping
      texture.repeat.set(10, 10)
      const grassMaterial = new THREE.MeshBasicMaterial({map: texture})
      const grass = new THREE.Mesh(floorGeometry, grassMaterial)
      scene.add(grass)
    },
    //冰壶馆初始化
    renderCurling() {
      objectsUtils.autoLoadObject("obj/curling.glb").then(object => {
        object.position.set(0, 0, 0)
        scene.add(object)
        objects = objects.concat(object.children)
      })
      objectsUtils.autoLoadObject("obj/stone.glb").then(object => {
        stone = object
        stone.children[0].material.transparent = "false"
        stone.children[0].material.precision = "lowp"
        stone.position.set(0, CURLING_STONE_HEIGHT, -700)
        stone.rotation.x = -Math.PI / 2
        scene.add(stone)
      })
    },
    //场景初始化
    initScene() {
      scene.add(new THREE.AmbientLight(0x303030))
      scene.fog = new THREE.Fog(0xffffff, 100, 5000)
      scene.add(controls.getObject())
    },
    //渲染器初始化
    initRenderer() {
      labelRenderer.setSize(window.innerWidth, window.innerHeight)
      labelRenderer.domElement.style.position = 'absolute'
      labelRenderer.domElement.style.top = '0px'
      document.body.appendChild(labelRenderer.domElement)
      renderer.setSize(window.innerWidth, window.innerHeight)
      renderer.setClearColor(0x87CEEB)
      renderer.outputEncoding = THREE.sRGBEncoding
      document.body.appendChild(renderer.domElement)
      window.addEventListener('resize', () => {
        camera.aspect = window.innerWidth / window.innerHeight
        camera.updateProjectionMatrix()
        renderer.setSize(window.innerWidth, window.innerHeight)
        labelRenderer.setSize(window.innerWidth, window.innerHeight)
      })
    },
    //键盘操作初始化
    initControls() {
      clock.stop()
      const blocker = document.getElementById('blocker')!
      const debugInfo = document.getElementById('debug-info')!
      const instructions = document.getElementById('instructions')!
      const chatBoard = this.$refs.chatboard as typeof ChatComponent
      labelRenderer.domElement.addEventListener('click', () => {
        controls.lock()
      })
      controls.addEventListener('lock', () => {
        instructions.style.display = 'none'
        blocker.style.display = 'none'
        clock.start()
      })
      controls.addEventListener('unlock', () => {
        blocker.style.display = 'block'
        instructions.style.display = ''
        moveForward = false
        moveBackward = false
        moveLeft = false
        moveRight = false
        canJump = false
        speedUp = false
        clock.stop()
      })
      const onKeyDown = (event: KeyboardEvent) => {
        switch (event.code) {
          case 'ArrowUp':
          case 'KeyW':
            moveForward = true
            break
          case 'ArrowLeft':
          case 'KeyA':
            moveLeft = true
            break
          case 'ArrowDown':
          case 'KeyS':
            moveBackward = true
            break
          case 'ArrowRight':
          case 'KeyD':
            moveRight = true
            break
          case 'Space':
            if (canJump && !this.pickedCurlingStone) velocity.y = Math.max(JUMP_VELOCITY, velocity.y)
            canJump = false
            break
          case 'ShiftLeft':
          case 'ShiftRight':
            speedUp = true
            break
          case 'KeyQ':
            q += 1
            if (q == 50) {
              this.exit()
            }
            else if (q > 50) {
              q = 0
            }
            break
        }
      }
      const onKeyUp = (event: KeyboardEvent) => {
        switch (event.code) {
          case 'ArrowUp':
          case 'KeyW':
            moveForward = false
            break
          case 'ArrowLeft':
          case 'KeyA':
            moveLeft = false
            break
          case 'ArrowDown':
          case 'KeyS':
            moveBackward = false
            break
          case 'ArrowRight':
          case 'KeyD':
            moveRight = false
            break
          case 'Enter':
            controls.unlock()
            chatBoard.focusInput()
            break
          case 'KeyP':
            if (debugInfo.style.display == 'block') debugInfo.style.display = 'none'
            else debugInfo.style.display = 'block'
            break
          case 'ShiftLeft':
          case 'ShiftRight':
            speedUp = false
            break
          case 'KeyQ':
            q = 0
            break
          case 'KeyE':
            if (this.pickCondition()) {
              this.pickCurlingStone()
            }
            else if (this.throwCondition()) {
              this.throwCurlingStone()
            }
            break
        }
      }
      document.body.addEventListener('keydown', onKeyDown)
      document.body.addEventListener('keyup', onKeyUp)
    },
    //websocket事件注册
    registerSocketEvents() {
      const that = this
      connection.registerEvent(new class extends EventSubject {
        public doEvent(rsp: BroadCastResponse): void {
          switch (rsp.operation) {
            //用户移动
            case "move": {
              const message = rsp as MoveResponse
              let player: Player
              let renderPerson = () => {
                player = players.get(message.username)!
                player.object.position.set(message.coordinate.x + player.objectPositionDelta[0],
                    message.coordinate.y + player.objectPositionDelta[1] - CHARACTER_HEIGHT + 3,
                    message.coordinate.z + player.objectPositionDelta[2])
                player.object.lookAt(message.coordinate.x + message.direction.x, CHARACTER_HEIGHT, message.coordinate.z + message.direction.z)
              }
              //如果用户的模型还没创建好
              if (!players.has(message.username)) {
                players.set(message.username, new Player(message.username))
                player = players.get(message.username)!
                if (player.loading) return
                player.loading = true
                objectsUtils.autoLoadCharacter(message.character).then(value => {
                  player.setObject(value[1])
                  player.objectPositionDelta = value[0]
                  player.loading = false
                  scene.add(player.object)
                  return objectsUtils.autoLoadObject("obj/simplifiedStone.glb")
                }).then(value => {
                  player.stoneObject = value
                  player.stoneObject.position.set(0, -100, 0)
                  scene.add(player.stoneObject)
                }).then(() => {
                  renderPerson()
                })
              }
              else {
                renderPerson()
              }
              break
            }
            //用户打破了最佳记录
            case "break": {
              const message = rsp as BreakPersonalBestScoreResponse
              that.showSystemMessage(message.username + " 创造了个人最佳记录：" + message.score + "分")
              break
            }
            //其他用户的冰壶位置更新
            case "curling": {
              const message = rsp as CurlingStoneResponse
              const player = players.get(message.username)!
              player.stoneObject.position.set(message.position.x, message.position.y + CURLING_STONE_HEIGHT, message.position.z)
              break
            }
          }
        }
      }(CHARACTER_MOVE_TO))
      connection.registerEvent(new class extends EventSubject {
        public doEvent(message: GetAllUsersResponse): void {
          //如果有玩家退出
          for (let username of players.keys()) {
            if (!message.users.includes(username)) {
              players.get(username)!.object.traverse((mesh: any) => {
                objectsUtils.disposeIt(mesh)
              })
              players.get(username)!.stoneObject.traverse((mesh: any) => {
                objectsUtils.disposeIt(mesh)
              })
              players.delete(username)
            }
          }
        }
      }(GET_ALL_USERS))
    },
    //帧动画
    animate() {
      requestAnimationFrame(this.animate)
      /*
                  ↑ y
                  |
                  |
                 ╱——————→
                ╱       x
               ╱
            z↙
       */
      if (controls.isLocked) {
        //碰撞检测、重力系统、人物移动
        const delta = clock.getDelta()
        this.calcThrownCurlingStonePosition(delta)
        const cameraPosition = controls.getObject().position
        const cameraDirection = controls.getDirection(new THREE.Vector3(0, 0, 0)).clone()
        velocity.x += velocity.x * ICE_STOP_ACCELERATION * delta
        velocity.z += velocity.z * ICE_STOP_ACCELERATION * delta
        velocity.y += GRAVITY_ACCELERATION * delta
        direction.z = Number(moveBackward) - Number(moveForward)
        direction.x = Number(moveLeft) - Number(moveRight)
        direction.normalize()
        forwardCollide = this.collideCheck(0)
        backCollide = this.collideCheck(180)
        leftCollide = this.collideCheck(90)
        rightCollide = this.collideCheck(270)
        if (moveForward || moveBackward) {
          if ((moveForward && forwardCollide) || (moveBackward && backCollide)) {
            velocity.z = 0
          }
          else {
            velocity.z += direction.z * ICE_MOVE_ACCELERATION * (speedUp ? 2.2 : 1) * delta
          }
        }
        else {
          if (forwardCollide || backCollide) {
            velocity.z = 0
          }
        }
        if (moveLeft || moveRight) {
          if ((moveRight && rightCollide) || (moveLeft && leftCollide)) {
            velocity.x = 0
          }
          else {
            velocity.x += direction.x * ICE_MOVE_ACCELERATION * (speedUp ? 2.2 : 1) * delta
          }
        }
        else {
          if (rightCollide || leftCollide) {
            velocity.x = 0
          }
        }
        downsideRayCaster.set(cameraPosition, downsideDirection)
        let downsideIntersections = downsideRayCaster.intersectObjects(objects, false)
        let isAboveObject = cameraPosition.y <= CHARACTER_HEIGHT //保底
        for (let d of downsideIntersections) {
          if (d.distance <= CHARACTER_HEIGHT) {
            isAboveObject = true
            break
          }
        }
        upsideRayCaster.set(cameraPosition, upsideDirection)
        let upsideIntersections = upsideRayCaster.intersectObjects(objects, false)
        let isBelowObject = false
        for (let d of upsideIntersections) {
          if (d.distance <= CHARACTER_HEIGHT) {
            isBelowObject = true
            break
          }
        }
        if (isBelowObject) {
          velocity.y = Math.min(0, velocity.y)
        }
        if (isAboveObject) {
          canJump = true
          velocity.y = Math.max(0, velocity.y)
        }
        cameraPosition.y += (velocity.y * delta)
        controls.moveRight(-velocity.x * delta)
        controls.moveForward(-velocity.z * delta)
        this.coordinate = `(${cameraPosition.x.toFixed(2)}, ${cameraPosition.y.toFixed(2)}, ${cameraPosition.z.toFixed(2)})`
        const newPosition = new Point(cameraPosition.x, cameraPosition.y, cameraPosition.z)
        const newDirection = new Point(cameraDirection.x, 0, cameraDirection.z).norm().times(5)
        const pointChanged = !newPosition.equals(socketPosition)
        const directionChanged = !newDirection.equals(socketDirection)
        //如果位置发生改变，则发送websocket消息
        if (pointChanged) {
          this.overBound = false
          regionDealer.deal(CURLING, newPosition)
          socketPosition = newPosition
          if (!this.pickedCurlingStone) {
            this.checkWithinCurlingStone()
          }
          else {
            this.updatePickedCurlingStonePosition()
          }
        }
        if (directionChanged) {
          socketDirection = newDirection
        }
        if (pointChanged || directionChanged) {
          connection.send(new MoveRequest(socketPosition, socketDirection.times(2000), store.getters.character))
        }
      }
      renderer.render(scene, camera)
      labelRenderer.render(scene, camera)
    },
    lockControls() {
      controls.lock()
    },
    //碰撞检测核心代码
    collideCheck(angle: number): boolean {
      let rotationMatrix = new THREE.Matrix4()
      rotationMatrix.makeRotationY(angle * Math.PI / 180)
      const cameraDirection = controls.getDirection(new THREE.Vector3(0, 0, 0)).clone()
      cameraDirection.y = 0
      cameraDirection.applyMatrix4(rotationMatrix)
      rayCaster.set(controls.getObject().position, cameraDirection)
      const intersections = rayCaster.intersectObjects(objects, false)
      for (let d of intersections) {
        if (d.distance <= CHARACTER_WIDTH * 1.5) {
          return true
        }
      }
      return false
    },
    //检测用户是否靠近冰壶
    checkWithinCurlingStone() {
      const stonePosition = new Point(stone.position.x, CHARACTER_HEIGHT, stone.position.z)
      this.withinCurlingStone = (stonePosition.minus(socketPosition).squareLength() <= (1.5 * CHARACTER_WIDTH) * (1.5 * CHARACTER_WIDTH))
    },
    //更新用户冰壶位置
    updatePickedCurlingStonePosition() {
      stone.position.set(socketPosition.x + socketDirection.x, CURLING_STONE_HEIGHT, socketPosition.z + socketDirection.z)
      stone.rotation.z = Math.atan(socketDirection.x / socketDirection.z)
    },
    //捡起冰壶
    pickCurlingStone() {
      this.pickedCurlingStone = true
      this.isStoneStop = true
      stoneVelocity.set(0, 0, 0)
    },
    //扔出冰壶
    throwCurlingStone() {
      this.pickedCurlingStone = false
      this.isStoneStop = false
      const angle = Math.atan(velocity.x / velocity.z) + Math.atan(socketDirection.x / socketDirection.z)
      if (!isNaN(angle)) {
        const velocityLength = Math.sqrt(velocity.x * velocity.x + velocity.z * velocity.z)
        stoneVelocity.x = velocityLength * Math.sin(angle)
        stoneVelocity.z = velocityLength * Math.cos(angle)
        stoneVelocity.y = 0
      }
    },
    //计算冰壶位置
    calcThrownCurlingStonePosition(delta: number) {
      stoneVelocity.x += stoneVelocity.x * CURLING_STONE_STOP_ACCELERATION * delta
      stoneVelocity.z += stoneVelocity.z * CURLING_STONE_STOP_ACCELERATION * delta
      stone.position.x += stoneVelocity.x * delta
      stone.position.z += stoneVelocity.z * delta
      //冰壶反弹
      if ((stone.position.x <= -75 + CURLING_STONE_RADIUS) || ((stone.position.x >= 75 - CURLING_STONE_RADIUS))) {
        stoneVelocity.x = -0.8 * stoneVelocity.x
      }
      if ((stone.position.z <= -750 + CURLING_STONE_RADIUS) || ((stone.position.z >= 750 - CURLING_STONE_RADIUS))) {
        stoneVelocity.z = -0.8 * stoneVelocity.z
      }
      this.stoneCoordinate = `(${stone.position.x.toFixed(2)}, ${stone.position.y.toFixed(2)}, ${stone.position.z.toFixed(2)})`
      const isStoneStopTmp = (stoneVelocity.x * stoneVelocity.x + stoneVelocity.z * stoneVelocity.z <= 5)
      if (this.isStoneStop != isStoneStopTmp) {
        this.isStoneStop = isStoneStopTmp
        this.stoneHasStopped()
      }
      //如果冰壶位置变化，则发送websocket消息
      const stoneNewPosition = new Point(stone.position.x, CURLING_STONE_HEIGHT, stone.position.z)
      if (!stoneNewPosition.equals(socketStonePosition)) {
        socketStonePosition = stoneNewPosition
        connection.send(new CurlingStoneRequest(socketStonePosition))
      }
    },
    //计算冰壶得分
    calcScore(): number {
      const p = new Point(stone.position.x, 0, stone.position.z)
      const t = new Point(0, 0, 600)
      const distance = p.minus(t).length() - CURLING_STONE_RADIUS
      if (distance <= 15) {
        return 4
      }
      else if (distance <= 30) {
        return 3
      }
      else if (distance <= 45) {
        return 2
      }
      else if (distance <= 60) {
        return 1
      }
      else {
        return 0
      }
    },
    //冰壶停止
    stoneHasStopped() {
      stoneVelocity.set(0, 0, 0)
      this.currentScore = this.calcScore()
      if (this.currentScore > this.bestScore) {
        this.bestScore = this.currentScore
        this.updateBestCurlingScore(this.bestScore)
        this.showSystemMessage("你创造了个人最佳记录：" + this.bestScore + "分")
        connection.send(new BreakPersonalBestScoreRequest(this.bestScore))
      }
      this.showSystemMessage(`你获得了${this.currentScore}分！`)
    },
    exit() {
      this.$router.push({name: "chooseCharacter"})
    },
    ...mapMutations([
      UPDATE_BEST_CURLING_SCORE
    ])
  },
  mounted() {
    //首先查询重复连接
    checkWsConnection(new WsConnCheckRequest()).then(resp => {
      //如果有重复连接
      if (resp.connected) {
        //那么强制下线
        return forceOffline(new WsForceOffRequest()).then(resp => {
          if (resp.message.success) {
            (this.$refs.modal as typeof Modal).show()
          }
        })
      }
    }).then(() => {
      connection.connect(store.getters.websocketURL + "/" + store.getters.username)
      this.showSystemMessage("欢迎来到冬奥冰壶场地，开始你的体验吧！")
    }).then(() => {
      this.beginRender()
    })
  },
  //释放所有内存
  unmounted() {
    regionDealer.unregisterAll(CURLING)
    connection.disconnect()
    controls.unlock()
    scene.traverse((mesh: any) => {
      objectsUtils.disposeIt(mesh)
    })
    scene.clear()
    renderer.dispose()
    document.body.removeChild(renderer.domElement)
    document.body.removeChild(labelRenderer.domElement)
  },
  created() {
    window.addEventListener('beforeunload', e => {
      connection.disconnect()
    })
  }
})
</script>

<style scoped lang="scss">
@import "../scss/curling";
</style>
