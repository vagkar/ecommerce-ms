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
