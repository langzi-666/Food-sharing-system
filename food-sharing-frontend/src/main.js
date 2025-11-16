import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './store'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'

// 错误处理
window.addEventListener('error', (event) => {
  console.error('全局错误:', event.error)
})

window.addEventListener('unhandledrejection', (event) => {
  console.error('未处理的Promise拒绝:', event.reason)
})

const app = createApp(App)
app.use(pinia)
app.use(router)
app.use(ElementPlus)

// 确保 DOM 元素存在
const appElement = document.getElementById('app')
if (!appElement) {
  console.error('找不到 #app 元素！')
} else {
  app.mount('#app')
  console.log('Vue 应用已成功挂载')
}
