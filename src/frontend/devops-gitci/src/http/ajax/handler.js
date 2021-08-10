import store from '@/store'

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
    return data === undefined ? response.data : data
}

function getLoginUrl () {
    const cUrl = location.href
    const loginUrl = new URL(LOGIN_SERVICE_URL)
    const url = new URL('http://login.o.woa.com/call_back_oa/')
    url.searchParams.append('bk_appid', 1)
    url.searchParams.append('bk_login', 1)
    url.searchParams.append('c_url', cUrl)
    
    loginUrl.searchParams.append('url', url)
    loginUrl.searchParams.append('appkey', '909a565c052c4379b90d3d82152d2da1')
    loginUrl.searchParams.append('title', 'bklogin')
    
    return loginUrl.href
}

export { errorHandler, successHandler, requestHandler, getLoginUrl }
