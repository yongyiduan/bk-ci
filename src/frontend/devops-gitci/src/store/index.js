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
        projectInfo: {},
        curPipeline: {},
        exceptionInfo: {
            type: 200
        }
    },
    getters: {},
    mutations: {
        updateUser (state, user) {
            state.user = Object.assign({}, user)
        },
        setProjectInfo (state, projectInfo) {
            state.projectId = `git_${projectInfo.id}`
            state.projectInfo = projectInfo
        },
        setCurPipeline (state, pipeline) {
            state.curPipeline = pipeline
        },
        setExceptionInfo (state, exceptionInfo) {
            state.exceptionInfo = exceptionInfo
        }
    },
    actions: {
        setProjectInfo ({ commit }, projectInfo) {
            commit('setProjectInfo', projectInfo)
        },
        setCurPipeline ({ commit }, pipeline) {
            commit('setCurPipeline', pipeline)
        },
        setExceptionInfo ({ commit }, exceptionInfo) {
            commit('setExceptionInfo', exceptionInfo)
        }
    }
})

window.addEventListener('resize', () => {
    store.state.appHeight = window.innerHeight
})

export default store
