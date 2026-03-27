import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as apiLogin, register as apiRegister } from '@/api/authApi'
import type { LoginRequest, RegisterRequest } from '@/types'
import { useCartStore } from '@/stores/cartStore'
import axios from 'axios'

function getErrorMessage(e: unknown, fallback: string): string {
  if (axios.isAxiosError(e)) {
    return e.response?.data?.message || fallback
  }
  return fallback
}

export const useAuthStore = defineStore('auth', () => {
  // State
  const token = ref<string | null>(localStorage.getItem('token'))
  const error = ref<string | null>(null)
  const loading = ref(false)

  // Computed — derived from state
  const isLoggedIn = computed(() => !!token.value)

  // Actions
  async function login(data: LoginRequest) {
    loading.value = true
    error.value = null
    try {
      const response = await apiLogin(data)
      token.value = response.token
      localStorage.setItem('token', response.token)
      await useCartStore().syncWithBackend()
    } catch (e: unknown) {
      error.value = getErrorMessage(e, 'Login failed')
      throw e
    } finally {
      loading.value = false
    }
  }

  async function register(data: RegisterRequest) {
    loading.value = true
    error.value = null
    try {
      const response = await apiRegister(data)
      token.value = response.token
      localStorage.setItem('token', response.token)
      await useCartStore().syncWithBackend()
    } catch (e: unknown) {
      error.value = getErrorMessage(e, 'Registration failed')
      throw e
    } finally {
      loading.value = false
    }
  }

  function logout() {
    token.value = null
    localStorage.removeItem('token')
  }

  return { token, error, loading, isLoggedIn, login, register, logout }
})