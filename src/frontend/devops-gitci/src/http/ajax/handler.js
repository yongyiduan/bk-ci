import router from '@/router'

function errorHandler (error) {
    console.log('error catch', error)
    return Promise.reject(Error('网络出现问题，请检查你的网络是否正常'))
}

function successHandler (response) {
    const { data: { code, data, message, status }, status: httpStatus } = response
    if (httpStatus === 401) {
        location.href = getLoginUrl()
    } else if (httpStatus === 503) {
        return Promise.reject({ // eslint-disable-line
            status: httpStatus,
            message: '服务维护中，请稍候...'
        })
    } else if (httpStatus === 418) {
        router.push({ name: 'exception', params: { type: 418, ...(router.currentRoute.params || {}) } })
    } else if (httpStatus === 403) {
        router.push({ name: 'exception', params: { type: 403, ...(router.currentRoute.params || {}) } })
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

export { errorHandler, successHandler }
