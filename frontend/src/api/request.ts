import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

import type { Result } from '@/types/common'

/**
 * 访问令牌请求头，需与后端 WriteAccessInterceptor.TOKEN_HEADER 保持一致。
 */
const TOKEN_HEADER = 'X-ViaTrial-Token'

const SAFE_METHODS = new Set(['get', 'head', 'options', 'trace'])

interface SessionInfo {
  writeTokenRequired?: boolean
  writeToken?: string | null
}

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
})

let writeToken: string | null = null
let sessionPromise: Promise<string | null> | null = null

const loadWriteToken = async (): Promise<string | null> => {
  if (writeToken) {
    return writeToken
  }

  sessionPromise ??= axios
    .get<SessionInfo>('/api/v1/system/session', { timeout: 5000 })
    .then((response) => {
      writeToken = response.data?.writeToken || null
      return writeToken
    })
    .catch(() => {
      // 后端未启用令牌校验或接口不可用时，静默降级，避免影响本地读写。
      return null
    })
    .finally(() => {
      sessionPromise = null
    })

  return sessionPromise
}

const isWriteMethod = (method?: string) => !SAFE_METHODS.has((method || 'get').toLowerCase())

request.interceptors.request.use(async (config) => {
  if (!isWriteMethod(config.method) || config.headers?.[TOKEN_HEADER]) {
    return config
  }

  const token = await loadWriteToken()
  if (token) {
    config.headers.set(TOKEN_HEADER, token)
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const result = response.data as Result<unknown>

    if (result.code !== 200) {
      ElMessage.error(result.message || '请求失败')
      return Promise.reject(result)
    }

    return response
  },
  async (error) => {
    const config = error?.config as (AxiosRequestConfig & { _retriedWithToken?: boolean }) | undefined

    // 令牌过期或后端重启后重新生成时，重新取一次令牌并重试一次（审计项 A-1）。
    if (error?.response?.status === 401 && config && !config._retriedWithToken) {
      config._retriedWithToken = true
      writeToken = null
      const token = await loadWriteToken()
      if (token) {
        config.headers = config.headers ?? {}
        ;(config.headers as Record<string, string>)[TOKEN_HEADER] = token
        return request.request(config)
      }
    }

    const message = error?.response?.data?.message || error?.message || '网络请求失败'
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export function requestData<T>(config: AxiosRequestConfig): Promise<T> {
  return request.request<Result<T>>(config).then((response) => response.data.data)
}

export { TOKEN_HEADER }

export default request
