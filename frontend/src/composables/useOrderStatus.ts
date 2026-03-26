import { Client } from '@stomp/stompjs'
import { ref, onUnmounted } from 'vue'
import type { OrderStatusUpdate } from '@/types'

// Composable: provides a WebSocket subscription for live order status updates
export function useOrderStatus(orderId: string) {
  const status = ref<string | null>(null)
  const connected = ref(false)
  let client: Client | null = null

  function connect() {
    client = new Client({
      brokerURL: 'ws://localhost:8082/ws',
      // Auto-reconnect: retry every 5 seconds if connection drops
      reconnectDelay: 5000,
      onConnect: () => {
        connected.value = true
        // Subscribe to this specific order's topic
        client!.subscribe(`/topic/orders/${orderId}`, (message) => {
          const update: OrderStatusUpdate = JSON.parse(message.body)
          status.value = update.status
        })
      },
      onDisconnect: () => {
        connected.value = false
      },
      onStompError: (frame) => {
        console.error('WebSocket error:', frame.headers['message'])
      },
    })
    client.activate()
  }

  function disconnect() {
    if (client) {
      client.deactivate()
      client = null
    }
  }

  // Cleanup when component is unmounted
  onUnmounted(() => disconnect())

  connect()

  return { status, connected, disconnect }
}
