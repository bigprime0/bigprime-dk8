import { fileURLToPath, URL } from 'node:url'
import { defineConfig, loadEnv } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import VueDevTools from 'vite-plugin-vue-devtools'
import autoImportPlugin from '@opentiny/unplugin-tiny-vue'
import ViteCompression from 'vite-plugin-compression'
import { resolve } from 'path'
import Markdown from 'vite-plugin-md'
import highlightjs from 'markdown-it-highlightjs'
// import doc from 'markdown-it-toc-done-right'
// import anchor from 'markdown-it-anchor'

export default defineConfig(({ mode, command }) => {
  const isBuild = command === 'build'
  const env = loadEnv(mode, process.cwd())
  return {
    plugins: [
      vue({
        include: [/\.vue$/, /\.md$/] // 允许 Vue 处理 .md 文件
      }),
      autoImportPlugin('vite'),
      vueJsx(),
      VueDevTools(),
      ViteCompression({
        disable: !isBuild,
        threshold: 10240,
        algorithm: 'gzip',
        ext: '.gz'
      }),
      Markdown({
        markdownItOptions: {
          html: true,
          linkify: true,
          typographer: true
        },
        markdownItUses: [[highlightjs, { auto: true }]]
      })
    ],
    define: {
      'process.env': { ...process.env }
    },
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url))
      }
    },
    server: {
      port: 7006, // 端口号
      open: true, // 是否自动打开浏览器
      proxy: {
        '/api/': {
          target: env.VITE_API_URL, //目标地址,一般是服务器地址
          changeOrigin: true, //允许跨域
          ws: true //允许websocket代理
          //rewrite: path => path.replace(/^\/api\//, '') //重写路径
        }
      }
    },
    css: {
      preprocessorOptions: {
        less: {
          modifyVars: {
            hack: `true; @import (reference) "${resolve('src/assets/style/breakpoint.less')}";`
          },
          javascriptEnabled: true
        }
      }
    },
    optimizeDeps: {
      include: [
        `monaco-editor/esm/vs/language/json/json.worker`,
        `monaco-editor/esm/vs/language/css/css.worker`,
        `monaco-editor/esm/vs/language/html/html.worker`,
        `monaco-editor/esm/vs/language/typescript/ts.worker`,
        `monaco-editor/esm/vs/editor/editor.worker`
      ]
    }
  }
})
