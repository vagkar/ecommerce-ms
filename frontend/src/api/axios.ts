import axios from 'axios'

// Base instance — no token needed (for auth calls)
export const authHttp = axios.create({
  baseURL: 'http://localhost:8084',
})

// Product service — GET is public, POST/PUT require auth
export const productHttp = axios.create({
  baseURL: 'http://localhost:8081',
})

// Order service — all endpoints require auth
export const orderHttp = axios.create({
  baseURL: 'http://localhost:8082',
})

// Interceptor: automatically attaches JWT token to every request
function addAuthInterceptor(instance: ReturnType<typeof axios.create>) {
  instance.interceptors.request.use((config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  })
}

addAuthInterceptor(productHttp)
addAuthInterceptor(orderHttp)
