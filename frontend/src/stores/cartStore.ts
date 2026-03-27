import { ref, computed, watch } from 'vue'
import { defineStore } from 'pinia'
import type { Product } from '@/types'
import {
  getCart,
  addOrUpdateCartItem,
  removeCartItem,
  clearCart as apiClearCart,
} from '@/api/cartApi'
import { getProduct } from '@/api/productApi'

// Cart item = product + desired quantity
export interface CartItem {
  product: Product
  quantity: number
}

function loadLocalCart(): CartItem[] {
  const saved = localStorage.getItem('cart')
  return saved ? JSON.parse(saved) : []
}

function saveLocalCart(items: CartItem[]) {
  localStorage.setItem('cart', JSON.stringify(items))
}

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>(loadLocalCart())
  const syncing = ref(false)

  const totalItems = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))

  const totalPrice = computed(() =>
    items.value.reduce((sum, item) => sum + item.product.price * item.quantity, 0),
  )

  // Persist to localStorage whenever cart changes
  watch(items, (newItems) => saveLocalCart(newItems), { deep: true })

  function isLoggedIn(): boolean {
    return !!localStorage.getItem('token')
  }

  // Sync local cart with backend after login
  async function syncWithBackend() {
    if (!isLoggedIn()) return
    syncing.value = true
    try {
      // Push local items to backend (merge)
      const localItems = [...items.value]
      for (const item of localItems) {
        await addOrUpdateCartItem(item.product.id, item.quantity)
      }

      // Fetch full cart from backend (includes items from other devices)
      const backendCart = await getCart()

      // Rebuild local items with full product data
      const mergedItems: CartItem[] = []
      for (const backendItem of backendCart.items) {
        // Check if we already have this product locally
        const local = localItems.find((li) => li.product.id === backendItem.productId)
        if (local) {
          mergedItems.push({ product: local.product, quantity: backendItem.quantity })
        } else {
          // Product came from another device — fetch product details
          const product = await getProduct(backendItem.productId)
          mergedItems.push({ product, quantity: backendItem.quantity })
        }
      }
      items.value = mergedItems
    } finally {
      syncing.value = false
    }
  }

  async function addItem(product: Product) {
    const existing = items.value.find((item) => item.product.id === product.id)
    if (existing) {
      existing.quantity++
      if (isLoggedIn()) {
        await addOrUpdateCartItem(product.id, existing.quantity)
      }
    } else {
      items.value.push({ product, quantity: 1 })
      if (isLoggedIn()) {
        await addOrUpdateCartItem(product.id, 1)
      }
    }
  }

  async function removeItem(productId: string) {
    items.value = items.value.filter((item) => item.product.id !== productId)
    if (isLoggedIn()) {
      await removeCartItem(productId)
    }
  }

  async function clear() {
    items.value = []
    if (isLoggedIn()) {
      await apiClearCart()
    }
  }

  return { items, totalItems, totalPrice, syncing, addItem, removeItem, clear, syncWithBackend }
})