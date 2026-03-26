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
    // Convert cart items to CreateOrderRequest format
    const order = await orderStore.placeOrder({
      items: cartStore.items.map((item) => ({
        productId: item.product.id,
        quantity: item.quantity,
      })),
    })
    cartStore.clear()
    // Navigate to order detail page for live status tracking
    router.push(`/orders/${order.id}`)
  } catch {
    // error is in orderStore.error
  } finally {
    placing.value = false
  }
}
</script>

<template>
  <div class="cart-page">
    <h1>Cart</h1>
    <p v-if="cartStore.items.length === 0">Your cart is empty.</p>
    <template v-else>
      <div class="cart-items">
        <div v-for="item in cartStore.items" :key="item.product.id" class="cart-item">
          <div class="item-info">
            <h3>{{ item.product.name }}</h3>
            <p>€{{ item.product.price.toFixed(2) }} × {{ item.quantity }}</p>
          </div>
          <div class="item-actions">
            <span class="item-total">€{{ (item.product.price * item.quantity).toFixed(2) }}</span>
            <button @click="cartStore.removeItem(item.product.id)" class="remove-btn">Remove</button>
          </div>
        </div>
      </div>
      <div class="cart-footer">
        <p class="total">Total: €{{ cartStore.totalPrice.toFixed(2) }}</p>
        <p v-if="orderStore.error" class="error">{{ orderStore.error }}</p>
        <button @click="placeOrder" :disabled="placing" class="place-btn">
          {{ placing ? 'Placing order...' : 'Place Order' }}
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.cart-page {
  padding: 2rem;
  max-width: 700px;
  margin: 0 auto;
}

.cart-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  margin-bottom: 1rem;
}

.item-info h3 {
  margin: 0;
}

.item-info p {
  margin: 0.25rem 0 0;
  color: #64748b;
}

.item-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.item-total {
  font-weight: bold;
  font-size: 1.1rem;
}

.remove-btn {
  background: transparent;
  color: #ef4444;
  border: 1px solid #ef4444;
  padding: 0.3rem 0.8rem;
  border-radius: 6px;
  cursor: pointer;
}

.cart-footer {
  margin-top: 2rem;
  text-align: right;
}

.total {
  font-size: 1.5rem;
  font-weight: bold;
}

.place-btn {
  padding: 0.75rem 2rem;
  background: #22c55e;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1.1rem;
  cursor: pointer;
}

.place-btn:hover {
  background: #16a34a;
}

.place-btn:disabled {
  background: #94a3b8;
  cursor: not-allowed;
}

.error {
  color: #ef4444;
}
</style>
