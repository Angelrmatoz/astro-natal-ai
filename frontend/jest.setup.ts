import { TextDecoder, TextEncoder } from 'node:util'

import '@testing-library/jest-dom'

if (!globalThis.TextEncoder) {
  globalThis.TextEncoder = TextEncoder as typeof globalThis.TextEncoder
}
if (!globalThis.TextDecoder) {
  globalThis.TextDecoder = TextDecoder as typeof globalThis.TextDecoder
}
