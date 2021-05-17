/**
 * @file main store
 * @author Blueking
 */

import Vue from 'vue'
import Vuex from 'vuex'

Vue.use(Vuex)

const store = new Vuex.Store({
    state: {
        user: {},
        appHeight: window.innerHeight,
        projectId: '',
        curPipeline: {}
    },
    getters: {},
    mutations: {
        updateUser (state, user) {
            state.user = Object.assign({}, user)
        },
        setProjectId (state, projectId) {
            state.projectId = projectId
        },
        setCurPipeline (state, pipeline) {
            state.curPipeline = pipeline
        }
    },
    actions: {
        setProjectId ({ commit }, projectId) {
            commit('setProjectId', projectId)
        },
        setCurPipeline ({ commit }, pipeline) {
            commit('setCurPipeline', pipeline)
        }
    }
})

window.addEventListener('resize', () => {
    store.state.appHeight = window.innerHeight
})

export default store
