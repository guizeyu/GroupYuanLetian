<template>
  <chat-component ref="chatboard" v-on:msgSend="lockControls()"/>
  <div id="debug-info" style="display: none">
    <div>{{ coordinate }}</div>
  </div>
  <div id="blocker">
    <div id="instructions" @click="lockControls()">
      <p class="instructions-title">
        单击任何位置回到滑冰场
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
  <div id="records" class="text-primary rounded-lg border border-secondary bg-white p-1 bg-light">
    <h4 class="font-weight-bold">
      最佳用时：{{ bestTime }}秒
    </h4>
    <h4 class="font-weight-bold">
      上次用时：{{ lastTime.toFixed(3) }}秒
    </h4>
    <h4 class="font-weight-bold">
      当前用时：{{ currentTime.toFixed(3) }}秒
    </h4>
    <h4 class="font-weight-bold">
      检&nbsp;&nbsp;查&nbsp;&nbsp;点：{{ passCount }}/{{ passed.length }}
    </h4>
  </div>
  <modal ref="modal">您已在别处登陆，现已将稍早之前的登陆强制下线！</modal>
</template>

<script lang="ts">
import {defineComponent} from "vue"
import ChatComponent from "@/components/Chat/Chat.vue";
import Modal from "@/components/Modal.vue";
import connection, {EventSubject} from "@/websocket";
import {
  BreakPersonalBestScoreRequest,
  BreakPersonalBestScoreResponse,
  BroadCastResponse,
  CHARACTER_MOVE_TO,
  GET_ALL_USERS, GetAllUsersResponse, MoveRequest,
  MoveResponse
} from "@/websocket/socketMessages";
import {RectangleRegion, regionDealer, SLOPE, UserEnterRegionEvent} from "@/3dRelated/region";
import {Point} from "@/3dRelated/point";
import {
  CHARACTER_HEIGHT, CHARACTER_WIDTH,
  GRAVITY_ACCELERATION, ICE_MOVE_ACCELERATION, ICE_STOP_ACCELERATION,
  JUMP_VELOCITY, objectsUtils
} from "@/3dRelated/objectsUtils";
import * as THREE from "three";
import {Player} from "@/3dRelated/players";
import store, {UPDATE_BEST_SKI_TIME} from "@/store";
import {checkWsConnection, WsConnCheckRequest} from "@/ts/websocketConnectionCheck";
import {forceOffline, WsForceOffRequest} from "@/ts/websocketForceOffline";
import {PointerLockControls} from "three/examples/jsm/controls/PointerLockControls.js";
import {mapMutations} from "vuex";
import {CSS2DRenderer} from "three/examples/jsm/renderers/CSS2DRenderer.js";

//退出游戏
let q = 0
//Three.js对象
let scene: typeof THREE.Scene
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
export default defineComponent({
  name: "SlopeView",
  components: {ChatComponent, Modal},
  data() {
    return {
      coordinate: "(0, 0, 0)",
      passed: [false, false, false, false, false, false, false, false],
      currentTime: 0,
      startTime: performance.now(),
      lastTime: -1,
      forbidPass: 0
    }
  },
  computed: {
    //通过检查点的数量
    passCount() {
      let count = 0
      for (const p of this.passed) count += p ? 1 : 0
      return count
    },
    //最佳用时
    bestTime() {
      return store.getters.bestSkiTime
    }
  },
  methods: {
    //计算是否通过了所有检测点，如果是，更新记录
    checkPassAll() {
      if (this.passCount < this.passed.length) return
      this.passed = [false, false, false, false, false, false, false, false]
      if (this.currentTime < this.bestTime) {
        this.updateBestSkiTime(this.currentTime.toFixed(3))
        connection.send(new BreakPersonalBestScoreRequest(Number(this.currentTime.toFixed(3))))
        this.showSystemMessage("你创造了个人最佳记录：" + this.bestTime + "秒")
      }
      this.lastTime = this.currentTime
      this.currentTime = 0
      this.forbidPass = 10
      this.startTime = performance.now()
    },
    showSystemMessage(message: string) {
      (this.$refs.chatboard as typeof ChatComponent).showSystemMessage(message)
    },
    beginRender() {
      this.init()
      this.registerRegionEvents()
      this.adjustCamera()
      this.renderGrass()
      this.renderSlope()
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
      camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 2000)
      labelRenderer = new CSS2DRenderer()
      renderer = new THREE.WebGLRenderer()
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
    },
    //8个检查点
    registerRegionEvents() {
      const that = this
      regionDealer.registerEvent(SLOPE, new class extends UserEnterRegionEvent {
        public doEvent() {
          if (that.forbidPass) return
          that.passed[0] = true
          that.checkPassAll()
        }
      }(new RectangleRegion(new Point(100, CHARACTER_HEIGHT, -190), new Point(200, CHARACTER_HEIGHT, -210))))
      regionDealer.registerEvent(SLOPE, new class extends UserEnterRegionEvent {
        public doEvent() {
          if (that.forbidPass) return
          that.passed[1] = true
          that.checkPassAll()
        }
      }(new RectangleRegion(new Point(-100, CHARACTER_HEIGHT, -190), new Point(-200, CHARACTER_HEIGHT, -210))))
      regionDealer.registerEvent(SLOPE, new class extends UserEnterRegionEvent {
        public doEvent() {
          if (that.forbidPass) return
          that.passed[2] = true
          that.checkPassAll()
        }
      }(new RectangleRegion(new Point(100, CHARACTER_HEIGHT, 190), new Point(200, CHARACTER_HEIGHT, 210))))
      regionDealer.registerEvent(SLOPE, new class extends UserEnterRegionEvent {
        public doEvent() {
          if (that.forbidPass) return
          that.passed[3] = true
          that.checkPassAll()
        }
      }(new RectangleRegion(new Point(-100, CHARACTER_HEIGHT, 190), new Point(-200, CHARACTER_HEIGHT, 210))))
      regionDealer.registerEvent(SLOPE, new class extends UserEnterRegionEvent {
        public doEvent() {
          if (that.forbidPass) return
          that.passed[4] = true
          that.checkPassAll()
        }
      }(new RectangleRegion(new Point(100, CHARACTER_HEIGHT, -10), new Point(200, CHARACTER_HEIGHT, 10))))
      regionDealer.registerEvent(SLOPE, new class extends UserEnterRegionEvent {
        public doEvent() {
          if (that.forbidPass) return
          that.passed[5] = true
          that.checkPassAll()
        }
      }(new RectangleRegion(new Point(-100, CHARACTER_HEIGHT, -10), new Point(-200, CHARACTER_HEIGHT, 10))))
      regionDealer.registerEvent(SLOPE, new class extends UserEnterRegionEvent {
        public doEvent() {
          if (that.forbidPass) return
          that.passed[6] = true
          that.checkPassAll()
        }
      }(new RectangleRegion(new Point(-10, CHARACTER_HEIGHT, 300), new Point(10, CHARACTER_HEIGHT, 400))))
      regionDealer.registerEvent(SLOPE, new class extends UserEnterRegionEvent {
        public doEvent() {
          if (that.forbidPass) return
          that.passed[7] = true
          that.checkPassAll()
        }
      }(new RectangleRegion(new Point(-10, CHARACTER_HEIGHT, -300), new Point(10, CHARACTER_HEIGHT, -400))))
    },
    //摄像机
    adjustCamera() {
      camera.position.set(150, CHARACTER_HEIGHT, -190)
      camera.lookAt(0, CHARACTER_HEIGHT, -10000)
    },
    //初始化草地
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
    //初始化滑冰场
    renderSlope() {
      objectsUtils.autoLoadObject("obj/slope.glb").then(object => {
        object.position.set(0, 0, 0)
        scene.add(object)
        objects = objects.concat(object.children)
      })
    },
    //初始化场景
    initScene() {
      scene.add(new THREE.AmbientLight(0x303030))
      scene.fog = new THREE.Fog(0xffffff, 100, 1500)
      scene.add(controls.getObject())
    },
    //初始化渲染器
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
    //初始化键盘操作
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
            if (canJump) velocity.y = Math.max(JUMP_VELOCITY, velocity.y)
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
        }
      }
      document.body.addEventListener('keydown', onKeyDown)
      document.body.addEventListener('keyup', onKeyUp)
    },
    //注册websocket事件
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
              //如果用户的形象还没有被加载进来
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
                }).then(() => {
                  renderPerson()
                })
              }
              else {
                renderPerson()
              }
              break
            }
            //用户打破记录
            case "break": {
              const message = rsp as BreakPersonalBestScoreResponse
              that.showSystemMessage(message.username + " 创造了个人最佳记录：" + message.score + "秒")
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
        this.currentTime = (performance.now() - this.startTime) / 1000
        this.forbidPass = Math.max(0, this.forbidPass - 1)
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
          regionDealer.deal(SLOPE, newPosition)
          socketPosition = newPosition
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
    exit() {
      this.$router.push({name: "chooseCharacter"})
    },
    ...mapMutations([
      UPDATE_BEST_SKI_TIME
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
      this.showSystemMessage("欢迎来到冬奥滑雪场，开始你的体验吧！")
    }).then(() => {
      this.beginRender()
      this.startTime = performance.now()
    })
  },
  //释放所有内存
  unmounted() {
    regionDealer.unregisterAll(SLOPE)
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
@import "../scss/slope";
</style>
