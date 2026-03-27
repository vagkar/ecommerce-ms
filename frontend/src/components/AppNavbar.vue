<script setup lang="ts">
import { useAuthStore } from '@/stores/authStore'
import { useCartStore } from '@/stores/cartStore'
import { useRouter } from 'vue-router'

const authStore = useAuthStore()
const cartStore = useCartStore()
const router = useRouter()

function logout() {
  authStore.logout()
  cartStore.clear()
  router.push('/login')
}
</script>

<template>
  <nav class="navbar">
    <div class="navbar-left">
      <RouterLink to="/products" class="nav-brand">E-Shop</RouterLink>
      <RouterLink to="/products">Products</RouterLink>
      <RouterLink v-if="authStore.isLoggedIn" to="/orders">Orders</RouterLink>
    </div>
    <div class="navbar-right">
      <RouterLink v-if="authStore.isLoggedIn" to="/cart" class="cart-link">
        Cart ({{ cartStore.totalItems }})
      </RouterLink>
      <button v-if="authStore.isLoggedIn" @click="logout" class="logout-btn">Logout</button>
      <template v-else>
        <RouterLink to="/login">Login</RouterLink>
        <RouterLink to="/register">Register</RouterLink>
      </template>
    </div>
  </nav>
</template>
