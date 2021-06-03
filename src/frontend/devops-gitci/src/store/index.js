/**
 * @file main store
 * @author Blueking
 */

import Vue from 'vue'
import Vuex from 'vuex'
import { modifyRequestCommonHead } from '@/http/ajax'

Vue.use(Vuex)

const store = new Vuex.Store({
    state: {
        user: {},
        appHeight: window.innerHeight,
        permission: false,
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
            const projectId = `git_${projectInfo.id}`
            state.projectId = projectId
            state.projectInfo = projectInfo
            modifyRequestCommonHead({ 'X-DEVOPS-PROJECT-ID': projectId })
        },
        setCurPipeline (state, pipeline) {
            state.curPipeline = pipeline
        },
        setExceptionInfo (state, exceptionInfo) {
            state.exceptionInfo = exceptionInfo
        },
        setPermission (state, permission) {
            state.permission = permission
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
        },
        setPermission ({ commit }, permission) {
            commit('setPermission', permission)
        }
    }
})

window.addEventListener('resize', () => {
    store.state.appHeight = window.innerHeight
})

export default store
