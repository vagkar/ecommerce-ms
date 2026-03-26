import { orderHttp } from './axios'
import type { CreateOrderRequest, Order } from '@/types'

export async function createOrder(data: CreateOrderRequest): Promise<Order> {
  const response = await orderHttp.post<Order>('/orders', data)
  return response.data
}

export async function getOrders(): Promise<Order[]> {
  const response = await orderHttp.get<{ content: Order[] }>('/orders')
  return response.data.content
}

export async function getOrder(id: string): Promise<Order> {
  const response = await orderHttp.get<Order>(`/orders/${id}`)
  return response.data
}
