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

const { status: liveStatus, connected } = useOrderStatus(orderId)

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
  <div class="page-narrow">
    <h1>Order Detail</h1>
    <p v-if="loading">Loading...</p>
    <p v-else-if="error" class="text-error">{{ error }}</p>
    <template v-else-if="order">
      <div class="order-info">
        <div class="info-row">
          <span class="text-muted">Order ID:</span>
          <span class="text-mono">{{ order.id }}</span>
        </div>
        <div class="info-row">
          <span class="text-muted">Status:</span>
          <OrderStatusBadge :status="order.status" />
        </div>
        <div class="info-row">
          <span class="text-muted">Total:</span>
          <span class="total">&euro;{{ order.total.toFixed(2) }}</span>
        </div>
      </div>

      <h2>Items</h2>
      <div class="items-list">
        <div v-for="item in order.items" :key="item.productId" class="item-row">
          <span>{{ item.productName }}</span>
          <span>&euro;{{ item.unitPrice.toFixed(2) }} &times; {{ item.quantity }}</span>
          <span class="item-price">&euro;{{ item.lineTotal.toFixed(2) }}</span>
        </div>
      </div>

      <p v-if="order.status === 'CREATED'" class="waiting-msg">
        Waiting for payment confirmation...
        <span class="connection-dot" :class="connected ? 'online' : 'offline'" />
      </p>
    </template>
  </div>
</template>
