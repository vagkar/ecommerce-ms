import { productHttp } from './axios'
import type { Product } from '@/types'

export async function getProducts(): Promise<Product[]> {
  const response = await productHttp.get<Product[]>('/products')
  return response.data
}

export async function getProduct(id: string): Promise<Product> {
  const response = await productHttp.get<Product>(`/products/${id}`)
  return response.data
}
