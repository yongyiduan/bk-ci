import store from '@/store'

function setCookie (cname, cvalue, domain) {
    document.cookie = `${cname}=${cvalue};domain=${domain}; path=/;expires=Fri, 31 Dec 2030 23:59:59 GMT`
}

function requestHandler (config) {
    return config
}

function errorHandler (error) {
    console.log('error catch', error)
    return Promise.reject(Error(error.message || '网络出现问题，请检查你的网络是否正常'))
}

function successHandler (response) {
    const { data: { code, data, message, status }, status: httpStatus } = response
    const errorMsg = { httpStatus, message, code: code || status }
    if (httpStatus === 401) {
        location.href = getLoginUrl()
    } else if ([503, 403, 418, 419].includes(httpStatus)) {
        store.dispatch('setExceptionInfo', { type: httpStatus, message })
        return
    } else if ((typeof code !== 'undefined' && code !== 0) || (typeof status !== 'undefined' && status !== 0)) {
        return Promise.reject(errorMsg)
    }
    return data || response.data
}

function getLoginUrl () {
    const cUrl = location.href
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

export { errorHandler, successHandler, requestHandler, setCookie }
