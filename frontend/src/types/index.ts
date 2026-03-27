// Maps to Product entity in product-service
export interface Product {
  id: string
  name: string
  price: number
  active: boolean
}

// Maps to OrderItemResponse DTO
export interface OrderItem {
  productId: string
  productName: string
  quantity: number
  unitPrice: number
  lineTotal: number
}

// Maps to OrderResponse DTO in order-service
export interface Order {
  id: string
  userId: string
  status: OrderStatus
  total: number
  items: OrderItem[]
}

export type OrderStatus = 'CREATED' | 'PAID' | 'PAYMENT_FAILED'

// Auth DTOs — maps to user-service
export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  password: string
}

export interface AuthResponse {
  token: string
}

// POST /orders request body
export interface CreateOrderItemRequest {
  productId: string
  quantity: number
}

export interface CreateOrderRequest {
  items: CreateOrderItemRequest[]
}

// Maps to CartResponse / CartItemResponse DTOs in order-service
export interface CartItemResponse {
  productId: string
  quantity: number
}

export interface CartResponse {
  items: CartItemResponse[]
}

// Maps to Spring Page<T> response
export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  number: number // current page (0-indexed)
  first: boolean
  last: boolean
}

// WebSocket message — sent by OrderStatusBroadcaster
export interface OrderStatusUpdate {
  orderId: string
  status: OrderStatus
}
