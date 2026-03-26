<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()

const email = ref('')
const password = ref('')

async function handleLogin() {
  try {
    await authStore.login({ email: email.value, password: password.value })
    router.push('/products')
  } catch {
    // error is already in authStore.error
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <h2>Login</h2>
      <p v-if="authStore.error" class="error">{{ authStore.error }}</p>
      <form @submit.prevent="handleLogin">
        <input v-model="email" type="email" placeholder="Email" required />
        <input v-model="password" type="password" placeholder="Password" required />
        <button type="submit" :disabled="authStore.loading">
          {{ authStore.loading ? 'Loading...' : 'Login' }}
        </button>
      </form>
      <p class="switch-link">
        Don't have an account? <RouterLink to="/register">Register</RouterLink>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 60px);
}

.auth-card {
  background: white;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.auth-card h2 {
  margin: 0 0 1.5rem;
  text-align: center;
}

form {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

input {
  padding: 0.75rem;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 1rem;
}

button {
  padding: 0.75rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  cursor: pointer;
}

button:hover {
  background: #2563eb;
}

button:disabled {
  background: #94a3b8;
  cursor: not-allowed;
}

.error {
  color: #ef4444;
  text-align: center;
}

.switch-link {
  text-align: center;
  margin-top: 1rem;
}
</style>
