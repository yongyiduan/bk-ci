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
        },
        messageNum: 0
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
        },
        updateMessageNum (state, num) {
            state.messageNum = num
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
        },
        setMessageNum ({ commit }, num) {
            commit('updateMessageNum', num)
        }
    }
})

window.addEventListener('resize', () => {
    store.state.appHeight = window.innerHeight
})

export default store
