<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '@/stores/authStore'
import { useRoute, useRouter } from 'vue-router'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const email = ref('')
const password = ref('')

async function handleRegister() {
  try {
    await authStore.register({ email: email.value, password: password.value })
    const redirect = (route.query.redirect as string) || '/products'
    router.push(redirect)
  } catch {
    // error is already in authStore.error
  }
}
</script>

<template>
  <div class="page-center">
    <div class="card-shadow auth-card">
      <h2>Register</h2>
      <p v-if="authStore.error" class="text-error">{{ authStore.error }}</p>
      <form class="form" @submit.prevent="handleRegister">
        <input v-model="email" class="input" type="email" placeholder="Email" required />
        <input v-model="password" class="input" type="password" placeholder="Password" required />
        <button type="submit" class="btn btn-primary btn-lg" :disabled="authStore.loading">
          {{ authStore.loading ? 'Loading...' : 'Register' }}
        </button>
      </form>
      <p class="switch-link">
        Already have an account? <RouterLink to="/login">Login</RouterLink>
      </p>
    </div>
  </div>
</template>
