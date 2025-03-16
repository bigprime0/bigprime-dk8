<template>
  <div>
    <h1 v-if="showButton">
      <u>SSH Web Client</u>&nbsp;
      <tiny-button
        type="success"
        @click="handleCreateConnect"
        size="mini"
        ghost
        style="color: #5cb300"
      >
        新建连接
      </tiny-button>
      <tiny-button type="success" @click="handleReConnect" size="mini" ghost style="color: #5cb300">
        新建会话
      </tiny-button>
    </h1>
    <div ref="terminal" class="terminal"></div>
    <tiny-dialog-box
      :visible="boxVisibility"
      @update:visible="boxVisibility = $event"
      resize
      title="连接设置"
      width="30%"
      :is-form-reset="false"
    >
      <tiny-form :model="formData" label-width="100px" style="width: 300px">
        <tiny-form-item label="地址" prop="host">
          <tiny-ip-address v-model="formData.host"></tiny-ip-address>
        </tiny-form-item>
        <tiny-form-item label="端口" prop="port">
          <tiny-numeric v-model="formData.port"></tiny-numeric>
        </tiny-form-item>
      </tiny-form>
      <template #footer>
        <tiny-button @click="handleConnect">连接</tiny-button>
      </template>
    </tiny-dialog-box>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import {
  TinyButton,
  TinyDialogBox,
  TinyForm,
  TinyFormItem,
  TinyIpAddress,
  TinyNumeric
} from '@opentiny/vue'
import { Terminal } from 'xterm'
import { FitAddon } from 'xterm-addon-fit'
import 'xterm/css/xterm.css'

const terminal = ref()
const ws = ref(null)
const url = defineModel('url', { default: import.meta.env.VITE_WS_URL })
const showButton = defineModel('showButton', { default: true })
const namespace = defineModel('namespace', { default: '' })
const pod = defineModel('pod', { default: '' })
const boxVisibility = ref(false)
const formData = ref({
  host: '',
  port: 22
})

let term = null

onMounted(() => {
  if (!showButton.value) {
    handleReConnect()
  }
})

const initTerminal = () => {
  term = new Terminal({
    rows: 100,
    rendererType: 'canvas', //渲染类型
    convertEol: true, //启用时，光标将设置为下一行的开头
    letterSpacing: '1',
    disableStdin: false, //是否应禁用输入
    cursorStyle: 'underline', //光标样式
    cursorBlink: true, //光标闪烁
    theme: {
      foreground: '#1edc1e', //字体
      background: '#000000', //背景色
      cursor: 'help', //设置光标
      lineHeight: 20
    },
    fontFamily: 'Consolas, Courier, monospace',
    fontSize: 14
  })
  const fitAddon = new FitAddon()
  term.loadAddon(fitAddon)
  term.open(terminal.value)
  fitAddon.fit()
  initWebsocket()
}

const initWebsocket = () => {
  // 连接WebSocket
  ws.value = new WebSocket(url.value)

  let isPasswordMode = false

  let inputBuffer = ''
  // 处理用户输入
  term.onData((data) => {
    if (data === '\r') {
      // Enter键
      if (inputBuffer === 'clear') {
        term.clear()
        inputBuffer = ''
        ws.value.send('\n')
      } else {
        ws.value.send(inputBuffer)
        inputBuffer = ''
        if (!isPasswordMode) {
          term.write('\r\n')
        }
      }
    } else if (data === '\x7F') {
      // Backspace
      if (inputBuffer.length > 0) {
        inputBuffer = inputBuffer.slice(0, -1)
        term.write('\b \b')
      }
    } else {
      inputBuffer += data
      term.write(isPasswordMode ? '*' : data)
    }
  })

  // 处理服务器响应
  ws.value.onmessage = (event) => {
    const data = event.data
    if (data.includes('password:')) {
      isPasswordMode = true
      term.write('\x1B[?25l') // 隐藏光标
    } else if (data.includes('$ ')) {
      isPasswordMode = false
      term.write('\x1B[?25h') // 显示光标
      if (namespace.value && pod.value) {
        ws.value.send('kubectl exec -it ' + pod.value + ' -n ' + namespace.value + ' -- /bin/bash ')
      }
    }
    term.write(data)
  }

  ws.value.onerror = (error) => {
    term.write('\r\nWebSocket Error: ' + error.message)
  }

  ws.value.onopen = () => {
    ws.value.send(
      JSON.stringify({
        type: 'CONNECT',
        host: formData.value.host,
        port: formData.value.port
      })
    )
  }

  // 处理WebSocket关闭
  ws.value.onclose = () => {
    term.write('\r\nSSH连接已关闭。\r\n')
  }
}

const handleConnect = () => {
  handleReConnect()
  boxVisibility.value = false
}

const handleCreateConnect = () => {
  boxVisibility.value = true
}

const handleReConnect = () => {
  if (terminal.value) {
    terminal.value.innerHTML = ''
  }
  initTerminal()
}
</script>

<style>
.terminal {
  width: 100%;
  height: calc(100vh - 195px);
  padding: 5px;
}
</style>
