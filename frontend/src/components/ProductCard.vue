<script setup lang="ts">
import type { Product } from '@/types'
import { useCartStore } from '@/stores/cartStore'
import { useAuthStore } from '@/stores/authStore'

defineProps<{
  product: Product
}>()

const cartStore = useCartStore()
const authStore = useAuthStore()
</script>

<template>
  <div class="product-card">
    <h3>{{ product.name }}</h3>
    <p class="product-price">&euro;{{ product.price.toFixed(2) }}</p>
    <span v-if="!product.active" class="inactive-badge">Inactive</span>
    <button
      v-if="product.active && authStore.isLoggedIn"
      @click="cartStore.addItem(product)"
      class="btn btn-primary add-btn"
    >
      Add to Cart
    </button>
  </div>
</template>
