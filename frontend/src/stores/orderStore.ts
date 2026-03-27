import { ref } from 'vue'
import { defineStore } from 'pinia'
import { createOrder as apiCreateOrder, getOrders as apiGetOrders } from '@/api/orderApi'
import type { CreateOrderRequest, Order } from '@/types'
import axios from 'axios'

function getErrorMessage(e: unknown, fallback: string): string {
  if (axios.isAxiosError(e)) {
    return e.response?.data?.message || fallback
  }
  return fallback
}

export const useOrderStore = defineStore('order', () => {
  const orders = ref<Order[]>([])
  const loading = ref(false)
  const error = ref<string | null>(null)

  // Pagination state
  const currentPage = ref(0)
  const totalPages = ref(0)
  const totalElements = ref(0)
  const isFirstPage = ref(true)
  const isLastPage = ref(true)

  async function fetchOrders(page = 0) {
    loading.value = true
    error.value = null
    try {
      const pageResponse = await apiGetOrders(page)
      orders.value = pageResponse.content
      currentPage.value = pageResponse.number
      totalPages.value = pageResponse.totalPages
      totalElements.value = pageResponse.totalElements
      isFirstPage.value = pageResponse.first
      isLastPage.value = pageResponse.last
    } catch (e: unknown) {
      error.value = getErrorMessage(e, 'Failed to fetch orders')
    } finally {
      loading.value = false
    }
  }

  async function placeOrder(data: CreateOrderRequest): Promise<Order> {
    loading.value = true
    error.value = null
    try {
      const order = await apiCreateOrder(data)
      orders.value.unshift(order) // add to the beginning of the list
      return order
    } catch (e: unknown) {
      error.value = getErrorMessage(e, 'Failed to place order')
      throw e
    } finally {
      loading.value = false
    }
  }

  // Called by WebSocket composable when status changes
  function updateOrderStatus(orderId: string, status: string) {
    const order = orders.value.find((o) => o.id === orderId)
    if (order) {
      order.status = status as Order['status']
    }
  }

  return {
    orders, loading, error,
    currentPage, totalPages, totalElements, isFirstPage, isLastPage,
    fetchOrders, placeOrder, updateOrderStatus,
  }
})