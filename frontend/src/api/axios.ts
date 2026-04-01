import axios from 'axios'

// Base instance — no token needed (for auth calls)
export const authHttp = axios.create({
  // Local dev: VITE_USER_SERVICE_URL is set in .env → direct call e.g. http://localhost:8084
  // Docker: .env excluded via .dockerignore → falls back to '/api' → nginx proxies /api/auth/...
  baseURL: import.meta.env.VITE_USER_SERVICE_URL || '/api',
})

// Product service — GET is public, POST/PUT require auth
export const productHttp = axios.create({
  baseURL: import.meta.env.VITE_PRODUCT_SERVICE_URL || '/api',
})

// Order service — all endpoints require auth
export const orderHttp = axios.create({
  baseURL: import.meta.env.VITE_ORDER_SERVICE_URL || '/api',
})

// Attaches JWT token to every request.
// Not applied to authHttp — login/register don't need a token.
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

// Called from main.ts after the router is ready.
// Handles 401 responses by clearing the token and navigating to /login via the router
// (client-side navigation, no full page reload).
// Not applied to authHttp — a failed login also returns 401 and must not cause a redirect.
export function setupResponseInterceptors(router: { push: (to: string) => void }) {
  for (const instance of [productHttp, orderHttp]) {
    instance.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          localStorage.removeItem('token')
          router.push('/login')
        }
        return Promise.reject(error)
      },
    )
  }
}
