import axios from 'axios'
import { setCookie } from '@/utils'
import { errorHandler, successHandler, requestHandler } from './handler'

// 设置cookie，路由到git-ci独立集群
setCookie('X-DEVOPS-PROJECT-ID', 'gitciproject', document.domain.split('.').slice(-2).join('.'))

const request = axios.create({
    baseURL: AJAX_URL_PREFIX,
    validateStatus: status => {
        if (status > 400) {
            console.warn(`HTTP 请求出错 status: ${status}`)
        }
        return status >= 200 && status <= 503
    },
    withCredentials: true,
    xsrfCookieName: 'paas_perm_csrftoken',
    xsrfHeaderName: 'X-CSRFToken'
})

request.interceptors.request.use(requestHandler, error => Promise.reject(error))

request.interceptors.response.use(successHandler, errorHandler)

export default request

export function modifyRequestCommonHead (options) {
    request.defaults.headers.common = {
        ...request.defaults.headers.common,
        ...options
    }
}
