<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getProducts } from '@/api/productApi'
import type { Product } from '@/types'
import ProductCard from '@/components/ProductCard.vue'

const products = ref<Product[]>([])
const loading = ref(true)
const error = ref<string | null>(null)

onMounted(async () => {
  try {
    products.value = await getProducts()
  } catch {
    error.value = 'Failed to load products'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page">
    <h1>Products</h1>
    <p v-if="loading">Loading products...</p>
    <p v-else-if="error" class="text-error">{{ error }}</p>
    <div v-else class="product-grid">
      <ProductCard v-for="product in products" :key="product.id" :product="product" />
    </div>
  </div>
</template>
