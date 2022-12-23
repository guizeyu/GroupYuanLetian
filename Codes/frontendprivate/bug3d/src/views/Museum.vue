<template>
  <chat-component ref="chatboard" v-on:msgSend="lockControls()"/>
  <div id="debug-info" style="display: none">
    <div>{{ coordinate }}</div>
  </div>
  <div id="blocker">
    <div id="instructions" @click="lockControls()">
      <p class="instructions-title">
        单击任何位置回到博物馆
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
  <template v-if="withinAnything && !learnedThatThing">
    <div id="interaction" class="rounded-lg border border-secondary bg-white bg-light">
      <h1 class="font-weight-bold text-center">
        E
      </h1>
    </div>
  </template>
  <template v-if="learnedThatThing">
    <div id="knowledge" class="rounded-lg border border-secondary bg-white container p-5 mt-2 bg-light">
      <div class="row">
        <h3 class="font-weight-light font-weight-bold">
          {{ knowledgeTitle }}
        </h3>
        <template v-if="learnedThatThing">
          <div class="spinner-grow text-success ml-2" role="status"></div>
        </template>
        <template v-else>
          <div class="spinner-grow text-danger ml-2" role="status"></div>
        </template>
      </div>
      <template v-if="learnedThatThing">
        <div class="row mt-1">
          <p>
            {{ knowledgeContent }}
          </p>
        </div>
      </template>
      <div class="row mt-2">
        <template v-if="learnedThatThing">
          <div class="alert alert-success w-100">
            您已学会！
          </div>
        </template>
        <template v-else>
          <div class="alert alert-danger w-100">
            按E键交互学习！
          </div>
        </template>
      </div>
    </div>
  </template>
  <modal ref="modal">您已在别处登陆，现已将稍早之前的登陆强制下线！</modal>
</template>

<script lang="ts">
import {defineComponent} from "vue"
import ChatComponent from "@/components/Chat/Chat.vue";
import connection, {EventSubject} from "@/websocket";
import store, {UPDATE_MUSEUM_INTERACTION} from "@/store";
import {
  BroadCastResponse,
  CHARACTER_MOVE_TO, DoorRequest, DoorResponse,
  GET_ALL_USERS, GetAllUsersResponse,
  MoveRequest,
  MoveResponse
} from "@/websocket/socketMessages";
import {checkWsConnection, WsConnCheckRequest} from "@/ts/websocketConnectionCheck";
import {forceOffline, WsForceOffRequest} from "@/ts/websocketForceOffline";
import Modal from "@/components/Modal.vue";
import * as THREE from 'three';
import {PointerLockControls} from 'three/examples/jsm/controls/PointerLockControls.js';
import {CSS2DObject, CSS2DRenderer} from 'three/examples/jsm/renderers/CSS2DRenderer.js';
import {Player} from "@/3dRelated/players";
import {
  CHARACTER_HEIGHT,
  CHARACTER_WIDTH,
  GRAVITY_ACCELERATION,
  JUMP_VELOCITY,
  MOVE_ACCELERATION, NAME_HEIGHT,
  objectsUtils,
  STOP_ACCELERATION
} from "@/3dRelated/objectsUtils";
import {Point} from "@/3dRelated/point";
import {regionDealer, MUSEUM, UserEnterRegionEvent, CircleRegion, RectangleRegion} from "@/3dRelated/region";
import {mapMutations} from "vuex";

//退出游戏
let q = 0
//Three.js对象
let door: typeof THREE.Group
let scene: typeof THREE.Scene
let camera: typeof THREE.PerspectiveCamera
let labelRenderer: typeof CSS2DRenderer
let renderer: typeof THREE.WebGLRenderer
let controls: typeof PointerLockControls
let clock: typeof THREE.Clock
let objects: Array<typeof THREE.Mesh>
let npc: typeof THREE.Group
let npcSays: HTMLDivElement
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
  name: "MuseumView",
  components: {ChatComponent, Modal},
  data() {
    return {
      coordinate: "(0, 0, 0)",
      within: [false, false, false, false, false, false],
      learned: [false, false, false, false, false, false],
      withinDoor: false,
      doorOpen: false,
      withinNpc: false
    }
  },
  computed: {
    withinAnything() {
      //@ts-ignore
      return this.within.indexOf(true) >= 0 || this.withinDoor || this.withinNpc
    },
    learnedThatThing() {
      //@ts-ignore
      const index = this.within.indexOf(true)
      //@ts-ignore
      return index >= 0 && this.learned[index]
    },
    knowledgeTitle() {
      let titles = [
        "高山滑雪",
        "自由式滑雪",
        "花样滑冰",
        "跳台滑雪",
        "冰球",
        "冰壶"
      ]
      //@ts-ignore
      const index = this.within.indexOf(true)
      return index >= 0 ? titles[index] : ""
    },
    knowledgeContent() {
      let contents = [
        "高山滑雪是一项于高山上进行的冬季运动项目，共设有10个小项，都为个人赛。比赛时，运动员需穿上有衬垫的紧身衣，并在心口的位置佩戴号码布、戴好保护盔、穿上滑雪板、滑雪杖以及使用“脱离式固定器”。由高山滑雪于1936年冬季奥运正式成为比赛项目之后，共有10个小项，当中包括了：男子滑降赛（Downhill）、男子回转 （Slalom）、男子大回转、男子超级大回转和男子混合式滑雪（Combined），以及女子滑降赛、女子回转、女子大回转、女子超级大回转和女子混合式滑雪10个小项。 运动员要由起点出发以最快速度到达终点，时间准确至百分之一秒，若出现成绩相同情况，名次可并排。",
        "自由式滑雪是一项冬季运动项目，运动员要借助滑雪板和雪杖于特定的雪场上做出指定和自选的动作。现时的冬季奥运中，设有男子个人雪上技巧（Moguls）、男子个人空中技巧（Aerials）、女子个人雪上技巧以及女子个人空中技巧4个小项。自由式滑雪是众多冬季运动之中历史最短暂的，它起源于1960年代的美国，当时的年青人爱好刺激，于是就创立出自由式滑雪，后来自由式滑雪普及全国，于1971年的新罕布夏州举办了世界上第一个正式的自由式滑雪比赛，当时的自由式滑雪以空中技巧赛为主流。及后于1979年，国际滑雪总会正式承认并纳入自由式滑雪为一项正式的冬季运动。后来，国际滑雪总会为制定有关自由式滑雪的飞行动作和保护装备等条文。1986年的世界锦标赛于法国举行，当时的比赛项目分为雪上技巧、空中技巧和雪上芭蕾三项。",
        "花样滑冰是一项由单人、双人或团体以冰刀在冰面上划出图形并表演技术动作的体育活动。这项运动在1908年成为奥林匹克运动会的项目 ，其四个现行奥运竞赛项目包括男子单人滑、女子单人滑、双人滑以及冰舞，非奥运竞赛项目包括团体滑与四人滑。在成人组赛事中，选手表演短节目与自由滑两种内容，其中包含旋转、跳跃、转体、托举、抛跳、螺旋线以及其他元素。花样滑冰大约于18世纪中时期开始以一种体育运动形式出现于英国，并于一个世纪后陆续流行于欧美各国。而国际滑冰联盟于1892年在瑞士正式成立，并制定出该项目的比赛规则，例如：比赛场地至少要长56米、宽26米，冰的厚度不可少于3厘米。",
        "跳台滑雪，是一项于跳台上的冬季运动项目。它要求运动员借助速度和弹跳力沿跳台下滑，使身体跃入空中，运动员会于空中飞行一段时间后，再落在地上滑行，成绩以跳台和落点间的距离计算。跳台滑雪与不少冬季运动都是源于北欧的挪威。它的起源之说有三。第一个说法是跳台滑雪本来是一种刑罚，古时的挪威会把犯人从雪山推下，使犯人从断崖中飞向半空，最后让犯人摔死。第二个说法是，有一位挪威运动员在1860年以同样的方法滑行，并成功着陆，这就是跳台滑雪的鼻祖。最后一个则是有两位挪威农民于奥斯陆中的全国滑雪比赛中表演了跳台飞跃的动作，后来在当时的政府加以推广之下，成为一项热门的冬季运动。跳台滑雪逐渐普及，于第一届冬季奥运中成为了正式的比赛项目，但只设男子个人和团体项目，直至2012年才正式加入女子个人项目。",
        "冰球（英语：ice hockey）又称冰上曲棍球，在北美（加拿大和美国）和部分欧洲国家（如拉脱维亚和瑞典）则直接称为“曲棍球”（hockey），是一项在滑冰场上进行的接触性团队运动，参赛选手穿着溜冰鞋在滑冰过程中用末端弯曲的球棍把一个硫化橡胶圆盘（冰球，英语：puck）打进对手球门作为得分方式。作为一种冬季运动，冰球主要盛行于有低温冰雪条件的寒带和亚寒带地区，比如北美北部（加拿大和美国北部）和较为寒冷的北欧、中欧和东欧地区（俄罗斯、芬兰、瑞典、捷克和斯洛伐克），但因为现在室内人工溜冰场和洗冰车的普及已经扩展到许多温带和亚热带地区。冰球是冬季奥林匹克运动会的重点比赛项目之一和北美四大职业运动之一，在加拿大与袋棍球分别是冬季国球和夏季国球。",
        "冰壶（英语：Curling）又称为“冰上溜石”。冰壶是一项包含男、女子与混双项目的队制冬季奥林匹克运动会项目，冰壶所有的“壶”直接称为冰壶亦可称为“石壶”或“壶”，冰壶比赛的两边运动员分别把冰壶投出，将自己队的冰壶尽量留在比赛场地的圆心中并且可将对方的冰壶撞走。冰壶起源于16世纪的苏格兰，当时人们会于冬天时在冰冻的池塘湿地上玩一种推石头游戏。最早的冰壶在1511年的苏格兰首次出现比赛，后来在17世纪之时，当时比赛所用的石头被加上一个手把，成了现代冰壶的最初外形。19世纪时期，冰壶游戏由在英国传至欧洲和美洲，并且渐渐的普及起来。1966年，国际冰壶联合会在英国成立。在1982年国际冰壶联合会成为世界认可的组织，自此冰壶运动成为了一个世界认可的体育运动。及后在长野冬季奥运中，男、女子的冰壶赛首度成为了正式的比赛项目，其后奥委会更在2006年开设混双项目与2008年开设轮椅项目。"
      ]
      //@ts-ignore
      const index = this.within.indexOf(true)
      return index >= 0 ? contents[index] : ""
    }
  },
  methods: {
    clearWithin() {
      this.within = [false, false, false, false, false, false]
      this.withinDoor = false
      this.withinNpc = false
    },
    //学习一个项目
    learnThatThing() {
      const index = this.within.indexOf(true)
      if (index >= 0) {
        this.learned[index] = true
        if (this.learned.indexOf(false) == -1) this.updateMuseumInteraction(true)
      }
    },
    //开门
    openDoor() {
      door.position.set(20, 0, -233)
      door.rotation.y = -Math.PI / 2
      this.doorOpen = true
    },
    //关门
    closeDoor() {
      door.position.set(0, 0, -213)
      door.rotation.y = 0
      this.doorOpen = false
    },
    //开/关门并发送websocket消息
    openOrCloseDoor() {
      if (this.doorOpen) this.closeDoor()
      else this.openDoor()
      connection.send(new DoorRequest(this.doorOpen))
    },
    //npc对话
    npcSays() {
      const sayings = ["冬季奥林匹克运动会，简称冬季奥运会、冬奥，为冬季运动的奥林匹克运动会，每隔4年举行一届，赛事的主要特征是在冰上和雪地举行的冬季运动，如滑冰、滑雪等适合在冬季举行的项目。",
        "第一届冬季奥林匹克运动会于1924年1月25日在法国的霞慕尼举行。",
        "1986年，国际奥委会全会决定将冬季奥运会和夏季奥运会从1994年起分开，每2年间隔交叉举行，1992年冬季奥运会是最后一届与夏季奥运会同年举行的冬奥会。",
        "美国举办过4次冬季奥运会，是举办奥运会次数最多的国家。",
        "法国、瑞士、挪威、意大利、奥地利、日本、加拿大都举办过两次以上冬季奥运会。",
        "亚洲第1次奥运会在日本札幌举办。中国北京是既举办过夏季奥运会，又举办过冬季奥运会的城市。",
        "冬奥会的比赛项目有冬季两项、冰球、冰壶、无舵雪橇、有舵雪橇和俯式冰橇、滑冰（速度滑冰、短道速滑、花样滑冰）、滑雪（越野滑雪、跳台滑雪、北欧两项、高山滑雪、自由式滑雪、单板滑雪）。",
        "第24届冬季奥林匹克运动会（XXIV Olympic Winter Games），简称2022年北京冬季奥运会，是由中国举办的国际性奥林匹克赛事，于2022年2月4日开幕，2月20日闭幕。",
        "2022年北京冬季奥运会共设7个大项，15个分项，109个小项。",
        "2013年11月3日，中国奥委会正式致函国际奥委。中国奥委会于2013年11月3日正式致函国际奥委会，提名北京市为2022年冬奥会的申办城市。",
        "2022年北京冬奥会会徽以汉字“冬”为灵感来源，运用中国书法的艺术形态,将厚重的东方文化底蕴与国际化的现代风格融为一体。"
      ]
      npcSays.textContent = sayings[Math.floor(Math.random() * sayings.length)]
    },
    //显示系统消息
    showSystemMessage(message: string) {
      (this.$refs.chatboard as typeof ChatComponent).showSystemMessage(message)
    },
    beginRender() {
      this.init()
      this.registerRegionEvents()
      this.adjustCamera()
      this.renderGrass()
      this.renderHouse()
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
      npcSays = document.createElement('div')
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
      rayCaster = new THREE.Raycaster(controls.getObject().position, new THREE.Vector3(), 0, CHARACTER_WIDTH + 1)
      downsideDirection = new THREE.Vector3(0, -1, 0).normalize()
      downsideRayCaster = new THREE.Raycaster(controls.getObject().position, downsideDirection, 0, CHARACTER_HEIGHT + 1)
      upsideDirection = new THREE.Vector3(0, 1, 0).normalize()
      upsideRayCaster = new THREE.Raycaster(controls.getObject().position, upsideDirection, 0, CHARACTER_HEIGHT + 1)
      players = new Map<string, Player>()
    },
    //区域检测
    registerRegionEvents() {
      const that = this
      regionDealer.registerEvent(MUSEUM, new class extends UserEnterRegionEvent {
        public doEvent() {
          that.within[0] = true
        }
      }(new CircleRegion(new Point(112, CHARACTER_HEIGHT, -133), 30)))
      regionDealer.registerEvent(MUSEUM, new class extends UserEnterRegionEvent {
        public doEvent() {
          that.within[1] = true
        }
      }(new CircleRegion(new Point(112, CHARACTER_HEIGHT, 0), 30)))
      regionDealer.registerEvent(MUSEUM, new class extends UserEnterRegionEvent {
        public doEvent() {
          that.within[2] = true
        }
      }(new CircleRegion(new Point(112, CHARACTER_HEIGHT, 133), 30)))
      regionDealer.registerEvent(MUSEUM, new class extends UserEnterRegionEvent {
        public doEvent() {
          that.within[3] = true
        }
      }(new CircleRegion(new Point(-112, CHARACTER_HEIGHT, 133), 30)))
      regionDealer.registerEvent(MUSEUM, new class extends UserEnterRegionEvent {
        public doEvent() {
          that.within[4] = true
        }
      }(new CircleRegion(new Point(-112, CHARACTER_HEIGHT, -133), 30)))
      regionDealer.registerEvent(MUSEUM, new class extends UserEnterRegionEvent {
        public doEvent() {
          that.within[5] = true
        }
      }(new CircleRegion(new Point(-112, CHARACTER_HEIGHT, 0), 30)))
      regionDealer.registerEvent(MUSEUM, new class extends UserEnterRegionEvent {
        public doEvent() {
          that.withinDoor = true
        }
      }(new RectangleRegion(new Point(-30, CHARACTER_HEIGHT, -175), new Point(35, CHARACTER_HEIGHT, -280))))
      regionDealer.registerEvent(MUSEUM, new class extends UserEnterRegionEvent {
        public doEvent(): void {
          that.withinNpc = true
          npcSays.style.color = "#0CF"
          npcSays.style.textShadow = "1px 1px 2px #000"
          npcSays.style.backgroundColor = "rgba(0, 0, 0, 0.5)"
          npcSays.style.border = 'solid'
          npcSays.style.borderRadius = '3px'
          npcSays.style.fontSize = "20px"
          npcSays.style.width = '400px'
          npcSays.textContent = "欢迎来到冬奥博物馆，让我来给你介绍冬奥会吧！"
        }
      }(new CircleRegion(new Point(0, CHARACTER_HEIGHT, 0), 30)))
    },
    //摄像机
    adjustCamera() {
      camera.position.set(0, CHARACTER_HEIGHT, -400)
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
    //博物馆初始化
    renderHouse() {
      objectsUtils.autoLoadObject("obj/museum.glb").then(object => {
        object.position.set(0, 0, 0)
        scene.add(object)
        objects = objects.concat(object.children)
      })
      objectsUtils.autoLoadObject("obj/door.glb").then(object => {
        door = object
        door.position.set(0, 0, -213)
        scene.add(door)
        objects = objects.concat(door.children)
      })
      objectsUtils.autoLoadObject("obj/npc.glb").then(object => {
        npc = object
        npc.position.set(0, 0, 0)
        npc.scale.set(3.75, 3.75, 3.75)
        const saysObject = new CSS2DObject(npcSays)
        saysObject.position.set(0, NAME_HEIGHT * 3, 0)
        npc.add(saysObject)
        scene.add(npc)
      })
    },
    //场景初始化
    initScene() {
      scene.add(new THREE.AmbientLight(0x303030))
      scene.fog = new THREE.Fog(0xffffff, 100, 1500)
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
          case 'KeyE':
            if (this.withinDoor) this.openOrCloseDoor()
            else if (this.withinNpc) this.npcSays()
            else this.learnThatThing()
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
                    message.coordinate.y + player.objectPositionDelta[1] - CHARACTER_HEIGHT,
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
                }).then(() => {
                  renderPerson()
                })
              }
              else {
                renderPerson()
              }
              break
            }
            //用户开门
            case "door": {
              const message = rsp as DoorResponse
              that.doorOpen = message.open
              if (that.doorOpen) that.openDoor()
              else that.closeDoor()
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
        const cameraPosition = controls.getObject().position
        velocity.x += velocity.x * STOP_ACCELERATION * delta
        velocity.z += velocity.z * STOP_ACCELERATION * delta
        velocity.y += GRAVITY_ACCELERATION * delta
        direction.z = Number(moveBackward) - Number(moveForward)
        direction.x = Number(moveLeft) - Number(moveRight)
        direction.normalize()
        if (moveForward) forwardCollide = this.collideCheck(0)
        if (moveBackward) backCollide = this.collideCheck(180)
        if (moveLeft) leftCollide = this.collideCheck(90)
        if (moveRight) rightCollide = this.collideCheck(270)
        if (moveForward || moveBackward) {
          if ((moveForward && forwardCollide) || (moveBackward && backCollide)) {
            velocity.z = 0
          }
          else {
            velocity.z += direction.z * MOVE_ACCELERATION * (speedUp ? 2.2 : 1) * delta
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
            velocity.x += direction.x * MOVE_ACCELERATION * (speedUp ? 2.2 : 1) * delta
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
        const cameraDirection = controls.getDirection(new THREE.Vector3(0, 0, 0))
        this.coordinate = `(${cameraPosition.x.toFixed(2)}, ${cameraPosition.y.toFixed(2)}, ${cameraPosition.z.toFixed(2)})`
        const newPosition = new Point(cameraPosition.x, cameraPosition.y, cameraPosition.z)
        const newDirection = new Point(cameraDirection.x, 0, cameraDirection.z).norm().times(5)
        const pointChanged = !newPosition.equals(socketPosition)
        const directionChanged = !newDirection.equals(socketDirection)
        //如果位置发生改变，则发送websocket消息
        if (pointChanged) {
          this.clearWithin()
          regionDealer.deal(MUSEUM, newPosition)
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
        if (d.distance <= CHARACTER_WIDTH) {
          return true
        }
      }
      return false
    },
    exit() {
      this.$router.push({name: "chooseCharacter"})
    },
    ...mapMutations([
      UPDATE_MUSEUM_INTERACTION
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
      this.showSystemMessage("欢迎来到冬奥博物馆，开始你的体验吧！")
    }).then(() => {
      this.beginRender()
    })
  },
  //释放所有内存
  unmounted() {
    regionDealer.unregisterAll(MUSEUM)
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
@import "../scss/museum";
</style>
