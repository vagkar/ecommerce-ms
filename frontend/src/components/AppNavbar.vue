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

<style scoped>
.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 2rem;
  background: #1e293b;
  color: white;
}

.navbar a {
  color: #94a3b8;
  text-decoration: none;
  margin-right: 1.5rem;
}

.navbar a:hover,
.navbar a.router-link-active {
  color: white;
}

.nav-brand {
  font-weight: bold;
  font-size: 1.25rem;
  color: white !important;
}

.navbar-left,
.navbar-right {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.cart-link {
  background: #3b82f6;
  color: white !important;
  padding: 0.4rem 1rem;
  border-radius: 6px;
}

.logout-btn {
  background: transparent;
  color: #94a3b8;
  border: 1px solid #94a3b8;
  padding: 0.4rem 1rem;
  border-radius: 6px;
  cursor: pointer;
}

.logout-btn:hover {
  color: white;
  border-color: white;
}
</style>
