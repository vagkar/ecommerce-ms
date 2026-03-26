<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getOrder } from '@/api/orderApi'
import { useOrderStatus } from '@/composables/useOrderStatus'
import OrderStatusBadge from '@/components/OrderStatusBadge.vue'
import type { Order, OrderStatus } from '@/types'
import { watch } from 'vue'

const route = useRoute()
const orderId = route.params.id as string

const order = ref<Order | null>(null)
const loading = ref(true)
const error = ref<string | null>(null)

// WebSocket subscription — listens for live status updates
const { status: liveStatus, connected } = useOrderStatus(orderId)

// When a WebSocket update arrives, update the order status
watch(liveStatus, (newStatus) => {
  if (newStatus && order.value) {
    order.value.status = newStatus as OrderStatus
  }
})

onMounted(async () => {
  try {
    order.value = await getOrder(orderId)
  } catch {
    error.value = 'Failed to load order'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="order-detail-page">
    <h1>Order Detail</h1>
    <p v-if="loading">Loading...</p>
    <p v-else-if="error" class="error">{{ error }}</p>
    <template v-else-if="order">
      <div class="order-info">
        <div class="info-row">
          <span class="label">Order ID:</span>
          <span class="mono">{{ order.id }}</span>
        </div>
        <div class="info-row">
          <span class="label">Status:</span>
          <OrderStatusBadge :status="order.status" />
        </div>
        <div class="info-row">
          <span class="label">Total:</span>
          <span class="total">€{{ order.total.toFixed(2) }}</span>
        </div>
      </div>

      <h2>Items</h2>
      <div class="items-list">
        <div v-for="item in order.items" :key="item.productId" class="item-row">
          <span>{{ item.productName }}</span>
          <span>€{{ item.unitPrice.toFixed(2) }} × {{ item.quantity }}</span>
          <span class="item-price">€{{ item.lineTotal.toFixed(2) }}</span>
        </div>
      </div>

      <p v-if="order.status === 'CREATED'" class="waiting-msg">
        Waiting for payment confirmation...
        <span class="connection-dot" :class="connected ? 'online' : 'offline'" />
      </p>
    </template>
  </div>
</template>

<style scoped>
.order-detail-page {
  padding: 2rem;
  max-width: 700px;
  margin: 0 auto;
}

.order-info {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 1.5rem;
  margin-bottom: 2rem;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.5rem 0;
}

.label {
  color: #64748b;
}

.mono {
  font-family: monospace;
  font-size: 0.9rem;
}

.total {
  font-size: 1.25rem;
  font-weight: bold;
}

.items-list {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.item-row {
  display: flex;
  justify-content: space-between;
  padding: 0.75rem 1.5rem;
  border-bottom: 1px solid #e2e8f0;
}

.item-row:last-child {
  border-bottom: none;
}

.item-price {
  font-weight: bold;
}

.waiting-msg {
  margin-top: 1.5rem;
  color: #f59e0b;
  font-style: italic;
}

.connection-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-left: 0.5rem;
  vertical-align: middle;
}

.connection-dot.online {
  background: #22c55e;
}

.connection-dot.offline {
  background: #ef4444;
}

.error {
  color: #ef4444;
}
</style>
