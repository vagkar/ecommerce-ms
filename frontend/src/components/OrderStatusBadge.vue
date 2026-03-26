<script setup lang="ts">
import type { OrderStatus } from '@/types'

defineProps<{
  status: OrderStatus
}>()

const statusColors: Record<OrderStatus, string> = {
  CREATED: '#f59e0b',
  PAID: '#22c55e',
  PAYMENT_FAILED: '#ef4444',
}
</script>

<template>
  <span class="badge" :style="{ background: statusColors[status] }">
    {{ status }}
  </span>
</template>

<style scoped>
.badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 20px;
  color: white;
  font-size: 0.8rem;
  font-weight: bold;
}
</style>
