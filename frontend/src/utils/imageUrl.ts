/**
 * 图片 URL 卫生检查（审计项 A-6）。
 *
 * 题目图片地址完全来自用户输入，直接进 el-image 的 :src。虽然 `javascript:` 在 `img`
 * 上不会执行，但 `data:`/`file:`/相对路径等仍是不必要的攻击面，这里只放行绝对 http(s) URL。
 */
const ALLOWED_PROTOCOLS = new Set(['http:', 'https:'])

export function safeImageUrl(value?: string | null): string {
  const url = (value || '').trim()
  if (!url) {
    return ''
  }

  try {
    // 不传 base：相对路径与协议相对 URL 会解析失败，从而被拒绝。
    const parsed = new URL(url)
    const protocol = parsed.protocol.toLowerCase()
    return ALLOWED_PROTOCOLS.has(protocol) ? parsed.toString() : ''
  } catch {
    return ''
  }
}
