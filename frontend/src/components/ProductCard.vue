<script setup lang="ts">
import type { Product } from '@/types'
import { useCartStore } from '@/stores/cartStore'
import { useAuthStore } from '@/stores/authStore'

// Props — data passed from parent component
defineProps<{
  product: Product
}>()

const cartStore = useCartStore()
const authStore = useAuthStore()
</script>

<template>
  <div class="product-card">
    <h3>{{ product.name }}</h3>
    <p class="price">€{{ product.price.toFixed(2) }}</p>
    <span v-if="!product.active" class="inactive-badge">Inactive</span>
    <button
      v-if="product.active && authStore.isLoggedIn"
      @click="cartStore.addItem(product)"
      class="add-btn"
    >
      Add to Cart
    </button>
  </div>
</template>

<style scoped>
.product-card {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1.5rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.product-card h3 {
  margin: 0;
}

.price {
  font-size: 1.25rem;
  font-weight: bold;
  color: #3b82f6;
  margin: 0;
}

.inactive-badge {
  color: #ef4444;
  font-size: 0.85rem;
}

.add-btn {
  margin-top: auto;
  padding: 0.5rem 1rem;
  background: #3b82f6;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.add-btn:hover {
  background: #2563eb;
}
</style>
