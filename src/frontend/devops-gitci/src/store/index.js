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
        projectSetting: {},
        curPipeline: {},
        exceptionInfo: {
            type: 200
        },
        showStageReviewPanel: {
            isShow: false,
            stage: {},
            type: ''
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
        setProjectSetting (state, projectSetting) {
            state.projectSetting = projectSetting || {}
        },
        setCurPipeline (state, pipeline) {
            state.curPipeline = pipeline
        },
        setExceptionInfo (state, exceptionInfo) {
            state.exceptionInfo = exceptionInfo
        },
        setPermission (state, permission) {
            state.permission = permission
        },
        toggleStageReviewPanel (state, showStageReviewPanel) {
            Object.assign(state.showStageReviewPanel, showStageReviewPanel)
        }
    },
    actions: {
        setProjectInfo ({ commit }, projectInfo) {
            commit('setProjectInfo', projectInfo)
        },
        setProjectSetting ({ commit }, projectSetting) {
            commit('setProjectSetting', projectSetting)
        },
        setCurPipeline ({ commit }, pipeline) {
            commit('setCurPipeline', pipeline)
        },
        setExceptionInfo ({ commit }, exceptionInfo) {
            commit('setExceptionInfo', exceptionInfo)
        },
        setPermission ({ commit }, permission) {
            commit('setPermission', permission)
        },
        setUser ({ commit }, user) {
            commit('updateUser', user)
        },
        toggleStageReviewPanel ({ commit }, showStageReviewPanel) {
            commit('toggleStageReviewPanel', showStageReviewPanel)
        }
    }
})

window.addEventListener('resize', () => {
    store.state.appHeight = window.innerHeight
})

export default store
