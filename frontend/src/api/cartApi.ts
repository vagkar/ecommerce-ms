import { orderHttp } from './axios'
import type { CartResponse } from '@/types'

export async function getCart(): Promise<CartResponse> {
  const response = await orderHttp.get<CartResponse>('/cart')
  return response.data
}

export async function addOrUpdateCartItem(productId: string, quantity: number): Promise<CartResponse> {
  const response = await orderHttp.put<CartResponse>('/cart/items', { productId, quantity })
  return response.data
}

export async function removeCartItem(productId: string): Promise<void> {
  await orderHttp.delete(`/cart/items/${productId}`)
}

export async function clearCart(): Promise<void> {
  await orderHttp.delete('/cart')
}