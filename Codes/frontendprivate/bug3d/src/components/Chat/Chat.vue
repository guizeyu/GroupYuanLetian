<template>
  <div class="container-fluid col-sm-8 col-md-6 col-lg-4 p-0" id="chat_board">
    <div id="chat_messages">
      <template v-for="(e, index) in messages" :key="index">
        <template v-if="e instanceof SystemMessage">
          <system-message-component :time="e.time">
            {{ e.content }}
          </system-message-component>
        </template>
        <template v-else-if="e instanceof ChatToAllMessage">
          <user-message-component :time="e.time" :from="e.from">
            {{ e.content }}
          </user-message-component>
        </template>
        <template v-else-if="e instanceof HisPrivateMessage">
          <his-private-message-component :time="e.time" :from="e.from">
            {{ e.content }}
          </his-private-message-component>
        </template>
        <template v-else-if="e instanceof MyPrivateMessage">
          <my-private-message-component :time="e.time" :to="e.to">
            {{ e.content }}
          </my-private-message-component>
        </template>
      </template>
    </div>
    <div id="chat_input">
      <div class="input-group h-100">
        <div class="input-group-prepend h-100">
          <button class="btn btn-outline-secondary dropdown-toggle" type="button" data-toggle="dropdown">对<span
              :class="chatToWho==='所有人'?'text-primary':'text-private-message'">{{ chatToWho }}</span>说
          </button>
          <div class="dropdown-menu">
            <template v-for="(e, index) in allTheUsers" :key="index">
              <a class="dropdown-item" @click.prevent="chooseChatToWho(e)">{{ e }}</a>
            </template>
            <div role="separator" class="dropdown-divider"></div>
            <a class="dropdown-item" @click.prevent="chooseChatToWho('所有人')">所有人</a>
          </div>
        </div>
        <input type="text" @keyup.enter.stop.prevent="sendChatMessage" v-model="inputText" class="form-control h-100"
               placeholder="按下Enter发送" id="inputText">
      </div>
    </div>
  </div>
</template>

<script lang="ts">
import {defineComponent} from "vue"
import SystemMessageComponent from "@/components/Chat/SystemMessage.vue";
import UserMessageComponent from "@/components/Chat/UserMessage.vue";
import HisPrivateMessageComponent from "@/components/Chat/HisPrivateMessage.vue";
import MyPrivateMessageComponent from "@/components/Chat/MyPrivateMessage.vue";
import {ChatMessage, ChatToAllMessage, HisPrivateMessage, MyPrivateMessage, SystemMessage} from "@/ts/chat";
import connection, {EventSubject} from "@/websocket";
import {
  CHAT_TO_ALL,
  ChatToAllRequest,
  ChatToAllResponse, GET_ALL_USERS, GetAllUsersResponse, PRIVATE_CHAT, PrivateChatRequest, PrivateChatResponse
} from "@/websocket/socketMessages";

export default defineComponent({
  name: "ChatComponent",
  components: {HisPrivateMessageComponent, UserMessageComponent, SystemMessageComponent, MyPrivateMessageComponent},
  emits: ['msgSend'],
  data() {
    return {
      inputText: "",
      messages: Array<SystemMessage>(),
      SystemMessage,
      ChatToAllMessage,
      HisPrivateMessage,
      MyPrivateMessage,
      needScrollToBottom: false,
      allTheUsers: Array<string>(),
      chatToWho: "所有人"
    }
  },
  methods: {
    chooseChatToWho(who: string) {
      this.chatToWho = who
    },
    addMessage(message: ChatMessage) {
      this.messages.push(message)
      this.needScrollToBottom = true
    },
    //显示系统消息
    showSystemMessage(message: string) {
      this.addMessage(new SystemMessage(message))
    },
    //发送消息
    sendChatMessage() {
      if (this.inputText.length == 0)
        this.addMessage(new SystemMessage("请不要发送空白的消息。"))
      else if (this.chatToWho == "所有人")
        connection.send(new ChatToAllRequest(1, this.inputText))
      else {
        if (this.allTheUsers.includes(this.chatToWho)) {
          connection.send(new PrivateChatRequest(1, this.chatToWho, this.inputText))
          this.addMessage(new MyPrivateMessage(this.chatToWho, this.inputText))
        }
        else {
          this.showSystemMessage("您的私聊对象" + this.chatToWho + "已不在房间。")
        }
      }
      this.inputText = ""
      document.getElementById('inputText')!.blur()
      this.$emit('msgSend')
    },
    //设置窗口焦点
    focusInput() {
      document.getElementById('inputText')!.focus()
    },
    //判断窗口焦点是否为聊天框
    isFocusActive(): boolean {
      return document.getElementById('inputText')! == document.activeElement
    }
  },
  created() {
    const that = this
    //收到对所有人说的内容
    connection.registerEvent(new class extends EventSubject {
      public doEvent(message: ChatToAllResponse) {
        that.addMessage(new ChatToAllMessage(message.from_who, message.chat_message))
      }
    }(CHAT_TO_ALL))
    //房间内人员变化
    connection.registerEvent(new class extends EventSubject {
      public doEvent(message: GetAllUsersResponse): void {
        that.allTheUsers = message.users
      }
    }(GET_ALL_USERS))
    //收到私聊
    connection.registerEvent(new class extends EventSubject {
      public doEvent(message: PrivateChatResponse): void {
        that.addMessage(new HisPrivateMessage(message.from_who, message.chat_message))
      }
    }(PRIVATE_CHAT))
  },
  updated() {
    if (this.needScrollToBottom) {
      const console = document.getElementById("chat_messages")!
      console.scrollTop = console.scrollHeight
      this.needScrollToBottom = false
    }
  }
})
</script>

<style scoped lang="scss">
@import "../../scss/chat";
</style>
