/**
 * @file main store
 * @author Blueking
 */

import Vue from 'vue'
import Vuex from 'vuex'
import http from '@/api'

Vue.use(Vuex)

const store = new Vuex.Store({
    state: {
        user: {},
        appHeight: window.innerHeight
    },
    getters: {
        user: state => state.user,
        appHeight: state => state.appHeight
    },
    mutations: {
        updateUser (state, user) {
            state.user = Object.assign({}, user)
        }
    },
    actions: {
        userInfo (context, config = {}) {
            const url = ''
            // mock 的地址，示例先使用 mock 地址
            const mockUrl = url + (url.indexOf('?') === -1 ? '?' : '&') + '=index&invoke=getUserInfo'
            return http.get(mockUrl, {}, config).then(response => {
                const userData = response.data || {}
                context.commit('updateUser', userData)
                return userData
            })
        }
    }
})

window.addEventListener('resize', () => {
    store.state.appHeight = window.innerHeight
})

export default store
