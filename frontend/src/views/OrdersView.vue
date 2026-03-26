<script setup lang="ts">
import { onMounted } from 'vue'
import { useOrderStore } from '@/stores/orderStore'
import OrderStatusBadge from '@/components/OrderStatusBadge.vue'

const orderStore = useOrderStore()

onMounted(() => {
  orderStore.fetchOrders()
})
</script>

<template>
  <div class="orders-page">
    <h1>My Orders</h1>
    <p v-if="orderStore.loading">Loading orders...</p>
    <p v-else-if="orderStore.error" class="error">{{ orderStore.error }}</p>
    <p v-else-if="orderStore.orders.length === 0">No orders yet.</p>
    <div v-else class="order-list">
      <RouterLink
        v-for="order in orderStore.orders"
        :key="order.id"
        :to="`/orders/${order.id}`"
        class="order-card"
      >
        <div class="order-header">
          <span class="order-id">{{ order.id.slice(0, 8) }}...</span>
          <OrderStatusBadge :status="order.status" />
        </div>
        <p class="order-total">€{{ order.total.toFixed(2) }}</p>
        <p class="order-items">{{ order.items.length }} item(s)</p>
      </RouterLink>
    </div>
  </div>
</template>

<style scoped>
.orders-page {
  padding: 2rem;
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 1rem;
  max-width: 700px;
}

.order-card {
  display: block;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1rem 1.5rem;
  text-decoration: none;
  color: inherit;
}

.order-card:hover {
  border-color: #3b82f6;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.order-id {
  font-family: monospace;
  color: #64748b;
}

.order-total {
  font-size: 1.25rem;
  font-weight: bold;
  margin: 0.5rem 0 0;
}

.order-items {
  color: #64748b;
  margin: 0.25rem 0 0;
}

.error {
  color: #ef4444;
}
</style>
