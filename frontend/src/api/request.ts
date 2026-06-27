import axios, { type AxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

import type { Result } from '@/types/common'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 10000,
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
  (error) => {
    const message = error?.response?.data?.message || error?.message || '网络请求失败'
    ElMessage.error(message)
    return Promise.reject(error)
  },
)

export function requestData<T>(config: AxiosRequestConfig): Promise<T> {
  return request.request<Result<T>>(config).then((response) => response.data.data)
}

export default request
