/*
 * Tencent is pleased to support the open source community by making BK-CI 蓝鲸持续集成平台 available.
 *
 * Copyright (C) 2019 THL A29 Limited, a Tencent company.  All rights reserved.
 *
 * BK-CI 蓝鲸持续集成平台 is licensed under the MIT license.
 *
 * A copy of the MIT License is included in this file.
 *
 *
 * Terms of the MIT License:
 * ---------------------------------------------------
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/**
 * @file main store
 */

import Vue from 'vue'
import Vuex from 'vuex'
import ajax from '../utils/ajax'
import pipelines from './modules/pipelines/'
import soda from './modules/soda/'
import atom from './modules/atom'

import {
    FETCH_ERROR,
    SET_EXTENSION
} from './constants'
import { ARTIFACTORY_OPERATION } from '../utils/extensionHooks'
Vue.use(Vuex)

export default new Vuex.Store({
    // 模块
    modules: {
        atom,
        pipelines,
        soda
    },
    // 公共 store
    state: {
        // 当前选中的project
        curProject: {},
        // 是否允许路由跳转
        allowRouterChange: true,
        // fetch error
        fetchError: null,

        cancelTokenMap: {},

        extensions: []
    },
    // 公共 mutations
    mutations: {
        [SET_EXTENSION]: (state, extensions) => {
            Object.assign(state, {
                extensions
            })
        },
        /**
         * 更新当前project
         *
         * @param {Object} state store state
         * @param {boolean} val 值
         */
        updateCurProject (state, project) {
            state.curProject = project || {}
        },
        /**
         * 修改 错误信息
         *
         * @param {Object} state store state
         * @param {boolean} val 值
         */
        [FETCH_ERROR]: (state, fetchError) => {
            console.log(fetchError)
            return Object.assign(state, {
                fetchError
            })
        }
    },
    // 公共 actions
    actions: {
        setExtensions: ({ commit }, extensions) => {
            console.log(extensions)
            commit(SET_EXTENSION, extensions)
        },
        requestProjectDetail: async ({ commit }, { projectId }) => {
            return ajax.get(AJAX_URL_PIRFIX + `/project/api/user/projects/${projectId}/`).then(response => {
                let data = {}
                if (typeof response.data === 'object' && typeof response.data.data === 'object') {
                    data = response.data.data
                    if (data.ccAppId) {
                        Object.assign(data, { 'ccAppId': data.ccAppId.toString() })
                    }
                }
                commit('updateCurProject', data)
            })
        }
    },
    // 公共 getters
    getters: {
        extensionKeyMap (state) {
            if (Array.isArray(state.extensions)) {
                return state.extensions.reduce((acc, extension) => {
                    acc[extension.key] = extension
                    return acc
                }, {})
            }
            return null
        },
        artifactExtensions (state) {
            const { extensions } = state
            return extensions.reduce((acc, extension) => {
                if (Array.isArray(extension.modules[ARTIFACTORY_OPERATION])) {
                    acc = [
                        ...acc,
                        ...extension.modules[ARTIFACTORY_OPERATION].map(mod => ({
                            ...mod,
                            appKey: extension.key
                        }))
                    ]
                }
                return acc
            }, [])
            // ARTIFACTORY_OPERATION
        }
    }
})
