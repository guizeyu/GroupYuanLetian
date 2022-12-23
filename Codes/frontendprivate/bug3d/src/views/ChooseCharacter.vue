<template>
  <div class="rounded-lg border border-secondary container p-4 mt-2 bg-light">
    <div class="row">
      <h3 class="font-weight-light">欢迎！<span class="font-weight-bold">{{ username }}</span></h3>
      <button class="btn-outline-danger btn ml-3" @click.prevent="logOut()">注销</button>
    </div>
    <div class="dropdown row mt-3">
      <h5 class="font-weight-light">选择您的人物形象：</h5>
      <button class="btn dropdown-toggle btn-outline-primary" type="button" id="dropdownMenuButton1"
              data-toggle="dropdown"
              aria-expanded="false">
        形象{{ character }}
      </button>
      <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
        <template v-for="(i, index) in [1, 2, 3, 4, 5, 6]" :key="index">
          <a class="dropdown-item" @click.prevent="() => {this.character = i}">形象{{ i }}</a>
        </template>
      </div>
    </div>
    <div class="row mt-3">
      <img :src="`/img/character${character}.jpg`" width="200" height="200" alt="形象"/>
    </div>
    <div class="dropdown row mt-3">
      <h5 class="font-weight-light align-middle">选择场景：</h5>
      <button class="btn btn-outline-primary dropdown-toggle" type="button" id="dropdownMenuButton2"
              data-toggle="dropdown"
              aria-expanded="false">
        场景{{ this.scene }}
      </button>
      <div class="dropdown-menu" aria-labelledby="dropdownMenuButton">
        <template v-for="(i, index) in [1, 2, 3]" :key="index">
          <a class="dropdown-item" @click.prevent="() => {this.scene = i}">场景{{ i }}</a>
        </template>
      </div>
      <p class="ml-2 font-weight-light">{{ ["博物馆", "滑冰场", "冰壶馆（注意：高GPU占用）"][this.scene - 1] }}</p>
    </div>
    <div class="row mt-3">
      <div class="progress w-100">
        <div class="progress-bar progress-bar-striped" role="progressbar"
             :style="`width: ${loaded / Math.max(1, total) * 100}%`">{{ loadingInformation }}
        </div>
      </div>
    </div>
    <div class="row mt-3">
      <button class="btn btn-primary" :disabled="loading" @click.prevent="enterScene()">
        确定进入
      </button>
    </div>
  </div>
  <div class="rounded-lg border border-secondary container p-4 mt-2 bg-light mb-2">
    <div class="row">
      <h3 class="font-weight-light">您的个人后台信息</h3>
      <table class="table table-striped table-hover">
        <thead class="thead-dark">
        <tr>
          <th scope="col">类型</th>
          <th scope="col">信息</th>
        </tr>
        </thead>
        <tbody>
        <template v-for="(singleInfo, index) in this.backstageInfo" :key="index">
          <tr>
            <td>{{ singleInfo.type }}</td>
            <td>{{ singleInfo.info }}</td>
          </tr>
        </template>
        <tr>
          <td class="align-middle">您上一次选择的形象</td>
          <td>形象{{ store.getters.character }}&nbsp;&nbsp;
            <img :src="`/img/character${store.getters.character}.jpg`" width="100" height="100" alt="形象"/></td>
        </tr>
        <tr>
          <td>您已经学习博物馆中的所有内容</td>
          <td>{{ store.getters.museumInteraction ? "是" : "否" }}</td>
        </tr>
        <tr>
          <td>滑冰场最佳个人记录</td>
          <td>{{ store.getters.bestSkiTime }}秒</td>
        </tr>
        <tr>
          <td>冰壶馆最佳个人记录</td>
          <td>{{ store.getters.bestCurlingScore }}分</td>
        </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue"
import store, {UPDATE_CHARACTER, UPDATE_SCENE, UPDATE_TOKEN, UPDATE_USERNAME} from "@/store";
import {mapMutations} from "vuex";
import {BackstageRequest, queryBackstage, SingleBackstageInfo} from "@/ts/backstage";
import {objectsUtils} from "@/3dRelated/objectsUtils";

export default defineComponent({
  name: "ChooseCharacterView",
  data() {
    return {
      character: store.getters.character,
      scene: 1,
      loading: true,
      loadingInformation: "正在加载3D资源...",
      loaded: 0,
      total: 0,
      backstageInfo: Array<SingleBackstageInfo>(),
      store
    }
  },
  methods: {
    enterScene() {
      this.updateCharacter(this.character)
      this.updateScene(this.scene)
      switch (this.scene) {
        case 1:
          this.$router.push({name: "museum"})
          break
        case 2:
          this.$router.push({name: "slope"})
          break
        case 3:
          this.$router.push({name: "curling"})
          break
      }
    },
    logOut() {
      this.updateUsername("")
      this.updateToken("")
      this.$router.push({name: "login"})
    },
    //预加载模型
    autoPreload(url: string) {
      this.total += 1
      objectsUtils.autoLoadObject(url).then((object: any) => {
        this.loaded += 1
        if (this.loaded == this.total) {
          this.loadingInformation = "加载完成！"
          this.loading = false
        }
        object.traverse((mesh: any) => {
          objectsUtils.disposeIt(mesh)
        })
      })
    },
    ...mapMutations([
      UPDATE_SCENE, UPDATE_CHARACTER, UPDATE_TOKEN, UPDATE_USERNAME
    ])
  },
  mounted() {
    queryBackstage(new BackstageRequest()).then(resp => {
      this.backstageInfo = resp.information
    })
    this.autoPreload("obj/character1.glb")
    this.autoPreload("obj/character2.glb")
    this.autoPreload("obj/character3.fbx")
    this.autoPreload("obj/character4.fbx")
    this.autoPreload("obj/character5.glb")
    this.autoPreload("obj/character6.fbx")
    this.autoPreload("obj/simplifiedStone.glb")
    this.autoPreload("obj/museum.glb")
    this.autoPreload("obj/door.glb")
    this.autoPreload("obj/slope.glb")
    this.autoPreload("obj/curling.glb")
    this.autoPreload("obj/stone.glb")
  },
  computed: {
    username() {
      return store.getters.username
    }
  }
})
</script>

<style scoped lang="scss">

</style>
