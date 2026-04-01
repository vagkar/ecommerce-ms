import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import './assets/main.css'
import { setupResponseInterceptors } from '@/api/axios'

const app = createApp(App)

app.use(createPinia())
app.use(router)

// Wire up 401 → logout + redirect, now that the router instance is ready
setupResponseInterceptors(router)

app.mount('#app')
