<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import { useOrderStore } from '@/stores/orderStore'
import OrderCard from '@/components/OrderCard.vue'
import { Client } from '@stomp/stompjs'
import type { OrderStatusUpdate } from '@/types'

const orderStore = useOrderStore()

let client: Client | null = null

function connectWebSocket() {
  client = new Client({
    brokerURL: 'ws://localhost:8082/ws',
    reconnectDelay: 5000,
    onConnect: () => {
      for (const order of orderStore.orders) {
        if (order.status === 'CREATED') {
          client!.subscribe(`/topic/orders/${order.id}`, (message) => {
            const update: OrderStatusUpdate = JSON.parse(message.body)
            orderStore.updateOrderStatus(update.orderId, update.status)
          })
        }
      }
    },
  })
  client.activate()
}

async function goToPage(page: number) {
  await orderStore.fetchOrders(page)
  if (client) {
    client.deactivate()
  }
  connectWebSocket()
}

onMounted(async () => {
  await orderStore.fetchOrders()
  connectWebSocket()
})

onUnmounted(() => {
  if (client) {
    client.deactivate()
  }
})
</script>

<template>
  <div class="page">
    <h1>My Orders</h1>
    <p v-if="orderStore.loading">Loading orders...</p>
    <p v-else-if="orderStore.error" class="text-error">{{ orderStore.error }}</p>
    <p v-else-if="orderStore.orders.length === 0">No orders yet.</p>
    <div v-else class="order-list">
      <OrderCard v-for="order in orderStore.orders" :key="order.id" :order="order" />

      <div v-if="orderStore.totalPages > 1" class="pagination">
        <button class="btn btn-outline" :disabled="orderStore.isFirstPage" @click="goToPage(orderStore.currentPage - 1)">
          &larr; Previous
        </button>
        <span class="page-info">
          Page {{ orderStore.currentPage + 1 }} of {{ orderStore.totalPages }}
        </span>
        <button class="btn btn-outline" :disabled="orderStore.isLastPage" @click="goToPage(orderStore.currentPage + 1)">
          Next &rarr;
        </button>
      </div>
    </div>
  </div>
</template>
