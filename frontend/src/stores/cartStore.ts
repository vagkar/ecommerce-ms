import { ref, computed, watch } from 'vue'
import { defineStore } from 'pinia'
import type { Product } from '@/types'

// Cart item = product + desired quantity
export interface CartItem {
  product: Product
  quantity: number
}

function loadCart(): CartItem[] {
  const saved = localStorage.getItem('cart')
  return saved ? JSON.parse(saved) : []
}

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>(loadCart())

  const totalItems = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))

  const totalPrice = computed(() =>
    items.value.reduce((sum, item) => sum + item.product.price * item.quantity, 0),
  )

  // Persist to localStorage whenever cart changes
  watch(items, (newItems) => localStorage.setItem('cart', JSON.stringify(newItems)), { deep: true })

  function addItem(product: Product) {
    const existing = items.value.find((item) => item.product.id === product.id)
    if (existing) {
      existing.quantity++
    } else {
      items.value.push({ product, quantity: 1 })
    }
  }

  function removeItem(productId: string) {
    items.value = items.value.filter((item) => item.product.id !== productId)
  }

  function clear() {
    items.value = []
  }

  return { items, totalItems, totalPrice, addItem, removeItem, clear }
})