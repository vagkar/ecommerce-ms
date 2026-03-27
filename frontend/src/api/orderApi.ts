import { orderHttp } from './axios'
import type { CreateOrderRequest, Order, PageResponse } from '@/types'

export async function createOrder(data: CreateOrderRequest): Promise<Order> {
  const response = await orderHttp.post<Order>('/orders', data)
  return response.data
}

export async function getOrders(page = 0, size = 20): Promise<PageResponse<Order>> {
  const response = await orderHttp.get<PageResponse<Order>>('/orders', {
    params: { page, size },
  })
  return response.data
}

export async function getOrder(id: string): Promise<Order> {
  const response = await orderHttp.get<Order>(`/orders/${id}`)
  return response.data
}
