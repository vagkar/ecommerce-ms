<script setup lang="ts">
import type { Order } from '@/types'
import OrderStatusBadge from '@/components/OrderStatusBadge.vue'

defineProps<{
  order: Order
}>()
</script>

<template>
  <RouterLink :to="`/orders/${order.id}`" class="card-link">
    <div class="order-header">
      <span class="text-mono text-muted">{{ order.id.slice(0, 8) }}...</span>
      <OrderStatusBadge :status="order.status" />
    </div>
    <p class="order-total">&euro;{{ order.total.toFixed(2) }}</p>
    <p class="text-muted order-items-count">{{ order.items.length }} item(s)</p>
  </RouterLink>
</template>
