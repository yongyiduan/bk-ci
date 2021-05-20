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
        curPipeline: {}
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
        }
    },
    actions: {
        setProjectInfo ({ commit }, projectInfo) {
            commit('setProjectInfo', projectInfo)
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
