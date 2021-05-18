import axios from 'axios'

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

function errorHandler (error) {
    console.log('error catch', error)
    return Promise.reject(Error('网络出现问题，请检查你的网络是否正常'))
}

function getLoginUrl () {
    const cUrl = location.origin + location.pathname + encodeURIComponent(location.search)
    if (/=%s/.test(LOGIN_SERVICE_URL)) {
        return LOGIN_SERVICE_URL.replace(/%s/, cUrl)
    } else {
        const loginUrl = new URL(LOGIN_SERVICE_URL)
        if (/=$/.test(loginUrl.search)) {
            return LOGIN_SERVICE_URL + cUrl
        } else {
            loginUrl.searchParams.append('c_url', cUrl)
            return loginUrl.href
        }
    }
}

request.interceptors.request.use(config => {
    return config
}, function (error) {
    return Promise.reject(error)
})

request.interceptors.response.use(response => {
    const { data: { code, data, message, status }, status: httpStatus } = response
    if (httpStatus === 401) {
        location.href = getLoginUrl()
    } else if (httpStatus === 503) {
        return Promise.reject({ // eslint-disable-line
            status: httpStatus,
            message: '服务维护中，请稍候...'
        })
    } else if (httpStatus === 418) {
        console.log('no permission')
    } else if (httpStatus === 403) {
        const errorMsg = { httpStatus, code: httpStatus, message }
        return Promise.reject(errorMsg)
    } else if ((typeof code !== 'undefined' && code !== 0) || (typeof status !== 'undefined' && status !== 0)) {
        let msg = message
        if (Object.prototype.toString.call(message) === '[object Object]') {
            msg = Object.keys(message).map(key => message[key].join(';')).join(';')
        } else if (Object.prototype.toString.call(message) === '[object Array]') {
            msg = message.join(';')
        }
        const errorMsg = { httpStatus, message: msg, code: code || status }
        return Promise.reject(errorMsg)
    }
    return data
}, errorHandler)

export default request
