/// <reference types="vite/client" />
interface ImportMetaEnv {
  VITE_API_URL: string
}

declare module '@opentiny/vue-theme/theme-tool.js'
declare module '@opentiny/vue-theme/theme'
declare module 'vue3-json-viewer'
declare module 'markdown-it-abbr'
declare module 'markdown-it-footnote'
declare module 'markdown-it-sub'
declare module 'markdown-it-sup'
declare module 'markdown-it-task-lists'
declare module 'markdown-it-table'
declare module 'markdown-it-code-copy'
declare module '*.vue'
declare module '*.md' {
  import type { ComponentOptions } from 'vue'
  const Component: ComponentOptions
  export default Component
}
