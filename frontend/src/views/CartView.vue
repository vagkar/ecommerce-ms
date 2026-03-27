<script setup lang="ts">
import { useCartStore } from '@/stores/cartStore'
import { useOrderStore } from '@/stores/orderStore'
import { useRouter } from 'vue-router'
import { ref } from 'vue'

const cartStore = useCartStore()
const orderStore = useOrderStore()
const router = useRouter()
const placing = ref(false)

async function placeOrder() {
  placing.value = true
  try {
    const order = await orderStore.placeOrder({
      items: cartStore.items.map((item) => ({
        productId: item.product.id,
        quantity: item.quantity,
      })),
    })
    await cartStore.clear()
    router.push(`/orders/${order.id}`)
  } catch {
    // error is in orderStore.error
  } finally {
    placing.value = false
  }
}
</script>

<template>
  <div class="page-narrow">
    <h1>Cart</h1>
    <p v-if="cartStore.items.length === 0">Your cart is empty.</p>
    <template v-else>
      <div class="cart-items">
        <div v-for="item in cartStore.items" :key="item.product.id" class="cart-item">
          <div>
            <h3>{{ item.product.name }}</h3>
            <p class="text-muted">&euro;{{ item.product.price.toFixed(2) }} &times; {{ item.quantity }}</p>
          </div>
          <div class="item-actions">
            <span class="item-total">&euro;{{ (item.product.price * item.quantity).toFixed(2) }}</span>
            <button @click="cartStore.removeItem(item.product.id)" class="btn btn-sm btn-danger-outline">Remove</button>
          </div>
        </div>
      </div>
      <div class="cart-footer">
        <p class="total">Total: &euro;{{ cartStore.totalPrice.toFixed(2) }}</p>
        <p v-if="orderStore.error" class="text-error">{{ orderStore.error }}</p>
        <button @click="placeOrder" :disabled="placing" class="btn btn-success btn-lg">
          {{ placing ? 'Placing order...' : 'Place Order' }}
        </button>
      </div>
    </template>
  </div>
</template>
