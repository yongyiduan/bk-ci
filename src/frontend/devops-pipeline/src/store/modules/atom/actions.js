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
import request from '@/utils/request'
import {
    FETCH_ERROR,
    PROCESS_API_URL_PREFIX,
    STORE_API_URL_PREFIX,
    LOG_API_URL_PREFIX,
    MACOS_API_URL_PREFIX
} from '@/store/constants'
import {
    SET_STAGE_TAG_LIST,
    SET_PIPELINE_STAGE,
    SET_COMMON_SETTING,
    SET_PIPELINE_CONTAINER,
    SET_TEMPLATE,
    SET_CONTAINER_DETAIL,
    SET_ATOM_MODAL,
    SET_ATOM_MODAL_FETCHING,
    UPDATE_ATOM_TYPE,
    UPDATE_ATOM,
    INSERT_ATOM,
    PROPERTY_PANEL_VISIBLE,
    SET_PIPELINE_EDITING,
    DELETE_CONTAINER,
    DELETE_STAGE,
    ADD_CONTAINER,
    DELETE_ATOM,
    UPDATE_CONTAINER,
    ADD_STAGE,
    UPDATE_STAGE,
    CONTAINER_TYPE_SELECTION_VISIBLE,
    SET_INSERT_STAGE_INDEX,
    SET_INSERT_STAGE_ISFINALLY,
    SET_PIPELINE,
    SET_BUILD_PARAM,
    DELETE_ATOM_PROP,
    SET_PIPELINE_EXEC_DETAIL,
    SET_REMOTE_TRIGGER_TOKEN,
    SET_GLOBAL_ENVS,
    TOGGLE_ATOM_SELECTOR_POPUP,
    UPDATE_ATOM_INPUT,
    UPDATE_WHOLE_ATOM_INPUT,
    UPDATE_ATOM_OUTPUT,
    UPDATE_ATOM_OUTPUT_NAMESPACE,
    FETCHING_ATOM_LIST,
    SET_STORE_LOADING,
    SET_STORE_SEARCH,
    FETCHING_ATOM_VERSION,
    SET_ATOM_VERSION_LIST,
    SET_EXECUTE_STATUS,
    SET_SAVE_STATUS,
    SET_DEFAULT_STAGE_TAG,
    TOGGLE_STAGE_REVIEW_PANEL,
    SET_IMPORTED_JSON,
    SET_EDIT_FROM,
    SET_CUR_JOBTYPE,
    IS_RECOMMEND_MORE_LOADING,
    IS_UNRECOMMEND_MORE_LOADING,
    IS_PROJECT_PAGE_OVER,
    IS_STORE_PAGE_OVER,
    IS_UNRECOMMEND_STORE_PAGE_OVER,
    IS_UNRECOMMEND_PROJECT_PAGE_OVER,
    SET_STORE_DATA,
    SET_UNRECOMMEND_STORE_DATA,
    SET_UNRECOMMEND_PROJECT_DATA,
    SET_UNRECOMMEND_ATOM_COUNT,
    SET_PROJECT_DATA,
    SET_PROJECT_ATOMS,
    SET_STORE_ATOMS,
    SET_PROJECT_UNRECOMMEN_ATOMS,
    SET_STORE_UNRECOMMEN_ATOMS,
    SET_CLASSIFY,
    SET_INNER_ACTIVE_NAME,
    SET_ATOM_CODE
} from './constants'
import { PipelineEditActionCreator, actionCreator } from './atomUtil'
import { hashID, randomString } from '@/utils/util'

function rootCommit (commit,
    ACTION_CONST, payload) {
    commit(ACTION_CONST, payload, { root: true })
}

function getMapByKey (list, key) {
    const keyList = []
    const objMap = list.reduce((objMap, item) => {
        objMap[item[key]] = item
        keyList.push(item[key])
        return objMap
    }, {})
    return [keyList, objMap]
}

export default {
    triggerStage ({ commit }, { projectId, pipelineId, buildNo, stageId, cancel, reviewParams, id, suggest }) {
        return request.post(`/${PROCESS_API_URL_PREFIX}/user/builds/projects/${projectId}/pipelines/${pipelineId}/builds/${buildNo}/stages/${stageId}/manualStart?cancel=${cancel}`, { reviewParams, id, suggest })
    },
    async fetchStageTagList ({ commit }) {
        try {
            const res = await request.get(`/${PROCESS_API_URL_PREFIX}/user/pipelines/stageTag`)
            const defaultStageTag = res.data.filter(item => item.defaultFlag).map(item => item.id)
            commit(SET_STAGE_TAG_LIST, res.data)
            commit(SET_DEFAULT_STAGE_TAG, defaultStageTag)
        } catch (error) {
            console.log(error)
        }
    },
    async fetchCommonSetting ({ commit }) {
        try {
            const res = await request.get(`/${PROCESS_API_URL_PREFIX}/user/setting/common/get`)
            commit(SET_COMMON_SETTING, res.data)
        } catch (error) {
            console.log(error)
        }
    },
    setExecuteStatus ({ commit }, status) {
        commit(SET_EXECUTE_STATUS, status)
    },
    setSaveStatus ({ commit }, status) {
        commit(SET_SAVE_STATUS, status)
    },
    toggleStageReviewPanel: actionCreator(TOGGLE_STAGE_REVIEW_PANEL),
    addStoreAtom ({ commit, state }) {
        const store = state.storeAtomData || {}
        let page = store.page || 1
        const pageSize = store.pageSize || 1500
        const keyword = store.keyword || undefined
        const loadEnd = store.loadEnd || false
        const loading = store.loading || false
        if (loadEnd || loading) return

        commit(SET_STORE_LOADING, true)
        return request.get(`${STORE_API_URL_PREFIX}/user/market/atom/list`, { params: { page, pageSize, keyword } }).then((res) => {
            const data = res.data || {}
            const records = data.records || []
            const atomList = store.data || []
            const storeData = {
                data: [...atomList, ...records],
                page: ++page,
                pageSize: 1500,
                loadEnd: records.length < pageSize,
                loading: false,
                keyword
            }
            commit(SET_STORE_DATA, storeData)
        }).catch((e) => {
            if (e.code === 403) e.message = ''
            rootCommit(commit, FETCH_ERROR, e)
        }).finally(() => {
            commit(SET_STORE_LOADING, false)
        })
    },

    setStoreSearch ({ commit }, str) {
        commit(SET_STORE_SEARCH, str)
    },

    clearStoreAtom ({ commit }) {
        commit(SET_STORE_DATA, {})
    },

    setPipelineStage ({ commit }, stages) {
        commit(SET_PIPELINE_STAGE, stages)
    },

    setPipelineContainer ({ commit }, { oldContainers, containers }) {
        commit(SET_PIPELINE_CONTAINER, { oldContainers, containers })
    },
    requestTemplate: async ({ commit, dispatch }, { projectId, templateId, version }) => {
        try {
            const url = version ? `/${PROCESS_API_URL_PREFIX}/user/templates/projects/${projectId}/templates/${templateId}?version=${version}` : `/${PROCESS_API_URL_PREFIX}/user/templates/projects/${projectId}/templates/${templateId}`
            const response = await request.get(url)
            dispatch('setPipeline', response.data.template)
            commit(SET_TEMPLATE, {
                template: response.data
            })
        } catch (e) {
            if (e.code === 403) {
                e.message = ''
            }
            rootCommit(commit, FETCH_ERROR, e)
        }
    },
    getCheckAtomInfo: ({ commit }, { projectId, pipelineId, buildId, elementId }) => {
        return request.get(`/${PROCESS_API_URL_PREFIX}/user/builds/${projectId}/${pipelineId}/${buildId}/${elementId}/toReview`).then(response => {
            return response.data
        })
    },
    handleCheckAtom: ({ commit }, { projectId, pipelineId, buildId, elementId, postData }) => {
        return request.post(`/${PROCESS_API_URL_PREFIX}/user/builds/${projectId}/${pipelineId}/${buildId}/${elementId}/review/`, postData).then(response => {
            return response.data
        })
    },
    requestPipeline: async ({ commit, dispatch }, { projectId, pipelineId }) => {
        try {
            const response = await request.get(`/${PROCESS_API_URL_PREFIX}/user/pipelines/${projectId}/${pipelineId}`)
            dispatch('setPipeline', response.data)
        } catch (e) {
            if (e.code === 403) {
                e.message = ''
            }
            rootCommit(commit, FETCH_ERROR, e)
        }
    },
    requestBuildParams: async ({ commit }, { projectId, pipelineId, buildId }) => {
        try {
            const response = await request.get(`/${PROCESS_API_URL_PREFIX}/user/builds/${projectId}/${pipelineId}/${buildId}/parameters`)
            commit(SET_BUILD_PARAM, {
                buildParams: response.data,
                buildId
            })
        } catch (e) {
            rootCommit(commit, FETCH_ERROR, e)
        }
    },
    setPipeline: actionCreator(SET_PIPELINE),
    setEditFrom: actionCreator(SET_EDIT_FROM),
    setPipelineEditing: actionCreator(SET_PIPELINE_EDITING),
    fetchContainers: async ({ commit }, { projectCode }) => {
        try {
            const { data: containers } = await request.get(`${STORE_API_URL_PREFIX}/user/pipeline/container/${projectCode}`)
            const containerList = containers.filter(container => container.type !== 'trigger')
            const triggerContainer = containers.find(container => container.type === 'trigger')
            const [containerTypeList, containerModalMap] = getMapByKey(containerList, 'baseOS')
            commit(SET_CONTAINER_DETAIL, {
                containerTypeList: ['TRIGGER', ...containerTypeList],
                containerModalMap: {
                    ...containerModalMap,
                    TRIGGER: triggerContainer
                }
            })
        } catch (e) {
            rootCommit(commit, FETCH_ERROR, e)
        }
    },
    fetchBuildResourceByType: ({ commit }, { projectCode, containerId, os, buildType }) => {
        return request.get(`${STORE_API_URL_PREFIX}/user/pipeline/container/projects/${projectCode}/containers/${containerId}/oss/${os}?buildType=${buildType}`)
    },
    setCurJobType: ({ commit }, str) => {
        commit(SET_CUR_JOBTYPE, str)
    },
    fetchClassify: async ({ commit }) => {
        const { data: atomClassifyList } = await request.get(`${STORE_API_URL_PREFIX}/user/pipeline/atom/classify`)
        const [atomClassifyCodeList, atomClassifyMap] = getMapByKey(atomClassifyList, 'classifyCode')
        Object.assign(atomClassifyMap, {
            'all': {
                classifyCode: 'all',
                classifyName: (window.pipelineVue.$i18n && window.pipelineVue.$i18n.t('storeMap.all')) || 'all'
            }
        })
        commit(SET_CLASSIFY, {
            atomClassifyCodeList,
            atomClassifyMap
        })
    },
    setInnerActiveName: async ({ commit }, innerActiveName) => {
        commit(SET_INNER_ACTIVE_NAME, innerActiveName)
    },
    setProjectData: async ({ commit }, projectData) => {
        commit(SET_PROJECT_DATA, projectData)
    },
    setStoreData: async ({ commit }, storeData) => {
        commit(SET_STORE_DATA, storeData)
    },
    setUnRecommendProjectData: async ({ commit }, unRecommendProjectData) => {
        commit(SET_UNRECOMMEND_PROJECT_DATA, unRecommendProjectData)
    },
    setUnRecommendStoreData: async ({ commit }, unRecommendStoreData) => {
        commit(SET_UNRECOMMEND_STORE_DATA, unRecommendStoreData)
    },
    setProjectPageOver: async ({ commit }, payload) => {
        commit(IS_PROJECT_PAGE_OVER, payload)
    },
    updateProjectAtoms: async ({ state, commit }, payload) => {
        const { atoms, recommend } = payload
        if (recommend) {
            commit(SET_PROJECT_ATOMS, {
                projectRecommendAtomMap: atoms
            })
            let page = 1
            const { projectRecommendAtomMap, projectData } = state
            if ((Object.keys(projectRecommendAtomMap).length / projectData.pageSize) !== (projectData.page - 1)) {
                page = projectData.page - 1 || 1
            }
            commit(SET_PROJECT_DATA, {
                page
            })
        } else {
            commit(SET_PROJECT_UNRECOMMEN_ATOMS, {
                projectUnRecommendAtomMap: atoms
            })
            commit(SET_UNRECOMMEND_PROJECT_DATA, {
                page: state.unRecommendProjectData.page - 1
            })
        }
    },
    updateStoreAtoms: async ({ commit, state }, payload) => {
        const { atoms, recommend } = payload
        const mutation = recommend ? SET_STORE_ATOMS : SET_STORE_UNRECOMMEN_ATOMS
        commit(mutation, {
            storeRecommendAtomMap: atoms
        })
    },

    setAtomCode: async ({ commit }, atomCode) => {
        commit(SET_ATOM_CODE, atomCode)
    },
    /**
     * 获取项目下插件
     */
    fetchProjectAtoms: async ({ commit, state }, { projectCode, category, recommendFlag, os, queryProjectAtomFlag }) => {
        // 前提：查询不适用当前job插件
        // 在有编译环境下fitOsFlag传false就代表会把不符合当前job的插件查出来,jobType不传
        // 无编译环境不用传fitOsFlag这个参数，jobType传AGENT
        let jobType = ['WINDOWS', 'MACOS', 'LINUX'].includes(os) ? 'AGENT' : 'AGENT_LESS'
        let queryFitAgentBuildLessAtomFlag
      
        const fitOsFlag = (jobType === 'AGENT' && !recommendFlag) ? false : undefined
        if (fitOsFlag === false) {
            jobType = undefined
        }
        if (!recommendFlag && jobType === 'AGENT_LESS') {
            queryFitAgentBuildLessAtomFlag = false
            jobType = 'AGENT'
        }

        let projectData, unRecommendProjectData, page, pageSize, keyword
        if (recommendFlag) {
            // 适用插件
            projectData = state.projectData || {}
            page = projectData.page || 1
            pageSize = projectData.pageSize || 15
            keyword = projectData.keyword || undefined
        } else {
            // 不适用插件
            unRecommendProjectData = state.unRecommendProjectData || {}
            page = unRecommendProjectData.page || 1
            pageSize = unRecommendProjectData.pageSize || 15
            keyword = unRecommendProjectData.keyword || undefined
            // 无编译不适用情况、有编译不适用情况，category不用传
            category = undefined
            recommendFlag = undefined
        }

        if (category === 'TRIGGER') {
            // 请求触发器类插件时不jobType
            jobType = undefined
        }

        try {
            if (page === 1) {
                commit(FETCHING_ATOM_LIST, true)
            } else {
                recommendFlag ? commit(IS_RECOMMEND_MORE_LOADING, true) : commit(IS_UNRECOMMEND_MORE_LOADING, true)
            }
            const { data: atomList } = await request.get(`${STORE_API_URL_PREFIX}/user/pipeline/atom`, {
                params: {
                    category,
                    projectCode,
                    os,
                    queryProjectAtomFlag,
                    jobType,
                    queryFitAgentBuildLessAtomFlag,
                    fitOsFlag,
                    recommendFlag,
                    page,
                    pageSize,
                    keyword
                }
            })

            const projectAtomMap = getMapByKey(atomList.records, 'atomCode')[1]

            if (page === 1) {
                if (recommendFlag) {
                    commit(SET_PROJECT_ATOMS, {
                        projectRecommendAtomMap: projectAtomMap
                    })
                } else {
                    commit(SET_PROJECT_UNRECOMMEN_ATOMS, {
                        projectUnRecommendAtomMap: projectAtomMap
                    })
                }
            } else {
                if (recommendFlag) {
                    commit(SET_PROJECT_ATOMS, {
                        projectRecommendAtomMap: Object.assign(state.projectRecommendAtomMap, projectAtomMap)
                    })
                } else {
                    commit(SET_PROJECT_UNRECOMMEN_ATOMS, {
                        projectUnRecommendAtomMap: Object.assign(state.projectUnRecommendAtomMap, projectAtomMap)
                    })
                }
            }
            
            if (recommendFlag) {
                // 保存请求页码、搜索关键字
                const isProjectPageOver = Object.keys(state.projectRecommendAtomMap).length === atomList.count
                const projectData = {
                    page: ++page,
                    pageSize,
                    keyword,
                    count: atomList.count
                }
                commit(IS_PROJECT_PAGE_OVER, isProjectPageOver)
                commit(SET_PROJECT_DATA, projectData)
            }
            // 不适用插件请求页码数据保存
            if (!recommendFlag) {
                const unRecommendAtomCount = atomList.count
                const isUnRecommendProjectPageOver = Object.keys(state.projectUnRecommendAtomMap).length === atomList.count
                const curUnRecommendProjectData = {
                    page: ++page,
                    pageSize,
                    keyword
                }
                commit(IS_UNRECOMMEND_PROJECT_PAGE_OVER, isUnRecommendProjectPageOver)
                commit(SET_UNRECOMMEND_PROJECT_DATA, curUnRecommendProjectData)
                commit(SET_UNRECOMMEND_ATOM_COUNT, unRecommendAtomCount)
            }
        } catch (e) {
            rootCommit(commit, FETCH_ERROR, e)
        } finally {
            commit(FETCHING_ATOM_LIST, false)
            commit(IS_RECOMMEND_MORE_LOADING, false)
            commit(IS_UNRECOMMEND_MORE_LOADING, false)
        }
    },
    /**
     * 获取研发商店插件
     */
    fetchStoreAtoms: async ({ commit, state }, { projectCode, classifyId, recommendFlag, category, os, queryProjectAtomFlag }) => {
        // 前提：查询不适用当前job插件
        // 在有编译环境下fitOsFlag传false就代表会把不符合当前job的插件查出来,jobType不传
        // 无编译环境不用传fitOsFlag这个参数，jobType传AGENT
        let jobType = ['WINDOWS', 'MACOS', 'LINUX'].includes(os) ? 'AGENT' : 'AGENT_LESS'
        let queryFitAgentBuildLessAtomFlag

        const fitOsFlag = (jobType === 'AGENT' && !recommendFlag) ? false : undefined
        if (fitOsFlag === false) {
            jobType = undefined
        }
        if (!recommendFlag && jobType === 'AGENT_LESS') {
            queryFitAgentBuildLessAtomFlag = false
            jobType = 'AGENT'
        }

        let storeData, unRecommendStoreData, page, pageSize, keyword
        if (recommendFlag) {
            // 适用插件
            storeData = state.storeData || {}
            page = storeData.page || 1
            pageSize = storeData.pageSize || 15
            keyword = storeData.keyword || undefined
        } else {
            // 不适用插件
            unRecommendStoreData = state.unRecommendStoreData || {}
            page = unRecommendStoreData.page || 1
            pageSize = unRecommendStoreData.pageSize || 15
            keyword = unRecommendStoreData.keyword || undefined
            // 无编译不适用情况、有编译不适用情况，category不用传
            category = undefined
            recommendFlag = undefined
        }
        
        if (category === 'TRIGGER') {
            // 请求触发器类插件时不jobType1
            jobType = undefined
        }

        if (state.storeData && state.storeData.classifyId !== classifyId) {
            page = 1
        }

        try {
            if (page === 1) {
                commit(FETCHING_ATOM_LIST, true)
            } else {
                recommendFlag ? commit(IS_RECOMMEND_MORE_LOADING, true) : commit(IS_UNRECOMMEND_MORE_LOADING, true)
            }

            const { data: atomList } = await request.get(`${STORE_API_URL_PREFIX}/user/pipeline/atom`, {
                params: {
                    projectCode,
                    classifyId,
                    recommendFlag,
                    category,
                    os,
                    queryProjectAtomFlag,
                    jobType,
                    queryFitAgentBuildLessAtomFlag,
                    fitOsFlag,
                    page,
                    pageSize,
                    keyword
                }
            })

            const storeAtomMap = getMapByKey(atomList.records, 'atomCode')[1]
            if (page === 1) {
                if (recommendFlag) {
                    commit(SET_STORE_ATOMS, {
                        storeRecommendAtomMap: storeAtomMap
                    })
                } else {
                    commit(SET_STORE_UNRECOMMEN_ATOMS, {
                        storeUnRecommendAtomMap: storeAtomMap
                    })
                }
            } else {
                if (recommendFlag) {
                    commit(SET_STORE_ATOMS, {
                        storeRecommendAtomMap: Object.assign(state.storeRecommendAtomMap, storeAtomMap)
                    })
                } else {
                    commit(SET_STORE_UNRECOMMEN_ATOMS, {
                        storeUnRecommendAtomMap: Object.assign(state.storeUnRecommendAtomMap, storeAtomMap)
                    })
                }
            }
            // 适用插件请求页码数据保存
            if (recommendFlag) {
                const isStorePageOver = Object.keys(state.storeRecommendAtomMap).length === atomList.count
                const storeData = {
                    page: ++page,
                    pageSize,
                    keyword,
                    classifyId
                }
                commit(IS_STORE_PAGE_OVER, isStorePageOver)
                commit(SET_STORE_DATA, storeData)
            }

            // 不适用插件请求页码数据保存
            if (!recommendFlag) {
                const unRecommendAtomCount = atomList.count
                const isUnRecommendStorePageOver = Object.keys(state.storeUnRecommendAtomMap).length === atomList.count
                const curUnRecommendStoreData = {
                    page: ++page,
                    pageSize,
                    keyword,
                    classifyId
                }
                commit(IS_UNRECOMMEND_STORE_PAGE_OVER, isUnRecommendStorePageOver)
                commit(SET_UNRECOMMEND_STORE_DATA, curUnRecommendStoreData)
                commit(SET_UNRECOMMEND_ATOM_COUNT, unRecommendAtomCount)
            }
        } catch (e) {
            rootCommit(commit, FETCH_ERROR, e)
        } finally {
            commit(FETCHING_ATOM_LIST, false)
            commit(IS_RECOMMEND_MORE_LOADING, false)
            commit(IS_UNRECOMMEND_MORE_LOADING, false)
        }
    },
    fetchAtomModal: async ({ commit, dispatch }, { projectCode, atomCode, version, atomIndex, container }) => {
        try {
            commit(SET_ATOM_MODAL_FETCHING, true)
            const { data: atomModal } = await request.get(`${STORE_API_URL_PREFIX}/user/pipeline/atom/${projectCode}/${atomCode}/${version}`)
            commit(SET_ATOM_MODAL, {
                atomCode,
                version,
                atomModal
            })
            if (container && typeof atomIndex !== 'undefined') { // 获取并更新原子模型
                dispatch('updateAtomType', { container, atomCode, version, atomIndex })
            }
        } catch (error) {
            rootCommit(commit, FETCH_ERROR, error)
        } finally {
            commit(SET_ATOM_MODAL_FETCHING, false)
        }
    },
    fetchAtomVersionList: async ({ commit }, { projectCode, atomCode }) => {
        try {
            commit(FETCHING_ATOM_VERSION, true)
            const { data: versionList } = await request.get(`${STORE_API_URL_PREFIX}/user/pipeline/atom/projectCodes/${projectCode}/atomCodes/${atomCode}/version/list`)

            commit(SET_ATOM_VERSION_LIST, versionList)
        } catch (error) {
            rootCommit(commit, FETCH_ERROR, error)
        } finally {
            commit(FETCHING_ATOM_VERSION, false)
        }
    },
    setInertStageIndex: actionCreator(SET_INSERT_STAGE_INDEX),
    setInsertStageIsFinally: actionCreator(SET_INSERT_STAGE_ISFINALLY),
    toggleStageSelectPopup: actionCreator(CONTAINER_TYPE_SELECTION_VISIBLE),
    addStage: PipelineEditActionCreator(ADD_STAGE),
    deleteStage: ({ commit }, payload) => {
        commit(DELETE_STAGE, payload)
        commit(SET_PIPELINE_EDITING, true)
    },
    addContainer: ({ commit, getters }, { type, ...restPayload }) => {
        const newContainer = getters.getContainerModalByType(type)
        if (newContainer) {
            const { name, required, typeList, type, baseOS, defaultBuildType, defaultPublicBuildResource = '', ...restProps } = newContainer
            const defaultType = (typeList || []).find(type => type.type === defaultBuildType) || {}
            const defaultBuildResource = defaultType.defaultBuildResource || {}
            const baseOSObject = baseOS !== 'NONE' ? { baseOS } : {}
            const isError = ['WINDOWS', 'MACOS'].includes(baseOS)
            commit(ADD_CONTAINER, {
                ...restPayload,
                newContainer: {
                    '@type': type,
                    name,
                    ...restProps,
                    ...baseOSObject,
                    dispatchType: {
                        buildType: defaultBuildType,
                        imageVersion: defaultBuildResource.version || '',
                        value: defaultBuildResource.code || defaultPublicBuildResource || '',
                        imageCode: defaultBuildResource.code || '',
                        imageName: defaultBuildResource.name || '',
                        recommendFlag: defaultBuildResource.recommendFlag,
                        imageType: 'BKSTORE'
                    },
                    elements: [],
                    containerId: `c-${hashID(32)}`,
                    jobId: `job_${randomString(3)}`,
                    nfsSwitch: false,
                    isError
                }
            })
            commit(SET_PIPELINE_EDITING, true)
        }
    },
    deleteContainer: ({ commit }, payload) => {
        commit(DELETE_CONTAINER, payload)
        commit(SET_PIPELINE_EDITING, true)
    },
    updateContainer: PipelineEditActionCreator(UPDATE_CONTAINER),
    updateStage: PipelineEditActionCreator(UPDATE_STAGE),
    addAtom: ({ commit }, { stageIndex, containerIndex, atomIndex, container }) => {
        const insertIndex = atomIndex + 1
        commit(INSERT_ATOM, {
            elements: container.elements,
            insertIndex
        })
        commit(PROPERTY_PANEL_VISIBLE, {
            isShow: true,
            isComplete: false,
            editingElementPos: {
                stageIndex: stageIndex,
                containerIndex: containerIndex,
                elementIndex: insertIndex
            }
        })
        commit(SET_PIPELINE_EDITING, true)
    },
    deleteAtom: ({ commit }, { container, atomIndex }) => {
        commit(DELETE_ATOM, { elements: container.elements, atomIndex })
        commit(SET_PIPELINE_EDITING, true)
    },
    updateAtomType: PipelineEditActionCreator(UPDATE_ATOM_TYPE),
    updateAtom: ({ commit }, { element: atom, newParam }) => {
        PipelineEditActionCreator(UPDATE_ATOM)({ commit }, { atom, newParam })
    },
    updateAtomInput: PipelineEditActionCreator(UPDATE_ATOM_INPUT),
    updateWholeAtomInput: PipelineEditActionCreator(UPDATE_WHOLE_ATOM_INPUT),
    updateAtomOutput: PipelineEditActionCreator(UPDATE_ATOM_OUTPUT),
    updateAtomOutputNameSpace: PipelineEditActionCreator(UPDATE_ATOM_OUTPUT_NAMESPACE),
    deleteAtomProps: PipelineEditActionCreator(DELETE_ATOM_PROP),
    togglePropertyPanel: ({ commit }, payload) => {
        // if (payload.isShow && !payload.editingElementPos) {
        //     return
        // }
        commit(PROPERTY_PANEL_VISIBLE, payload)
    },
    requestPipelineExecDetail: async ({ commit, dispatch }, { projectId, buildNo, pipelineId }) => {
        try {
            const response = await request.get(`${PROCESS_API_URL_PREFIX}/user/builds/${projectId}/${pipelineId}/${buildNo}/detail`)
            dispatch('setPipelineDetail', response.data)
        } catch (e) {
            if (e.code === 403) {
                e.message = ''
            }
            rootCommit(commit, FETCH_ERROR, e)
        }
    },
    requestPipelineExecDetailByBuildNum: async ({ commit, dispatch }, { projectId, buildNum, pipelineId }) => {
        try {
            return request.get(`${PROCESS_API_URL_PREFIX}/user/builds/${projectId}/${pipelineId}/detail/${buildNum}`)
        } catch (e) {
            if (e.code === 403) {
                e.message = ''
            }
            rootCommit(commit, FETCH_ERROR, e)
        }
    },
    setPipelineDetail: actionCreator(SET_PIPELINE_EXEC_DETAIL),
    getRemoteTriggerToken: async ({ commit }, { projectId, pipelineId, element, preToken }) => {
        try {
            const { data: { token } } = await request.put(`${PROCESS_API_URL_PREFIX}/user/pipelines/${projectId}/${pipelineId}/remoteToken`)
            if (preToken !== token) {
                commit(SET_REMOTE_TRIGGER_TOKEN, { atom: element, token })
                commit(SET_PIPELINE_EDITING, true)
            }
        } catch (e) {
            rootCommit(commit, FETCH_ERROR, e)
        }
    },
    requestGlobalEnvs: async ({ commit }) => {
        try {
            const response = await request.get(`/${PROCESS_API_URL_PREFIX}/user/buildParam`)
            commit(SET_GLOBAL_ENVS, response.data)
        } catch (e) {
            rootCommit(commit, FETCH_ERROR, e)
        }
    },
    toggleAtomSelectorPopup: actionCreator(TOGGLE_ATOM_SELECTOR_POPUP),

    // 安装插件
    installAtom ({ dispatch }, param) {
        return request.post(`${STORE_API_URL_PREFIX}/user/market/atom/install`, param).then(() => dispatch('fetchAtoms', { projectCode: param.projectCode[0] }))
    },

    getAtomEnvConfig ({ commit }, atomCode) {
        return request.get(`${STORE_API_URL_PREFIX}/user/market/ATOM/component/${atomCode}/sensitiveConf/list/?types=FRONTEND,ALL`).then((res) => {
            return res.data || []
        })
    },

    // 获取项目下已安装的插件列表
    getInstallAtomList ({ commit }, { projectCode, name }) {
        return request.get(`${STORE_API_URL_PREFIX}/user/pipeline/atom/projectCodes/${projectCode}/installedAtoms/list?page=1&pageSize=15`, {
            params: {
                name
            }
        })
    },

    // 获取已安装的插件详情
    getInstallAtomDetail ({ commit }, { projectCode, atomCode }) {
        return request.get(`${STORE_API_URL_PREFIX}/user/market/atom/statistic/projectCodes/${projectCode}/atomCodes/${atomCode}/pipelines`)
    },

    // 卸载插件
    unInstallAtom ({ commit }, { projectCode, atomCode, reasonList }) {
        return request.delete(`${STORE_API_URL_PREFIX}/user/pipeline/atom/projectCodes/${projectCode}/atoms/${atomCode}`, { data: { reasonList } })
    },

    // 获取卸载原因
    getDeleteReasons () {
        return request.get(`${STORE_API_URL_PREFIX}/user/store/reason/types/UNINSTALLATOM`)
    },

    // 获取分类
    getAtomClassify () {
        return request.get(`${STORE_API_URL_PREFIX}/user/pipeline/atom/classify`)
    },

    // 第一次拉取日志

    getInitLog ({ commit }, { projectId, pipelineId, buildId, tag, currentExe, subTag, debug }) {
        return request.get(`${API_URL_PREFIX}/${LOG_API_URL_PREFIX}/user/logs/${projectId}/${pipelineId}/${buildId}`, {
            params: {
                tag,
                executeCount: currentExe,
                subTag,
                debug
            }
        })
    },

    // 后续拉取日志
    getAfterLog ({ commit }, { projectId, pipelineId, buildId, tag, currentExe, lineNo, subTag, debug }) {
        return request.get(`${API_URL_PREFIX}/${LOG_API_URL_PREFIX}/user/logs/${projectId}/${pipelineId}/${buildId}/after`, {
            params: {
                start: lineNo,
                executeCount: currentExe,
                tag,
                subTag,
                debug
            }
        })
    },

    fetchDevcloudSettings ({ commit }, { projectId, buildType }) {
        return request.get(`/dispatch-docker/api/user/dispatch-docker/resource-config/projects/${projectId}/list?buildType=${buildType}`)
    },

    getLogStatus ({ commit }, { projectId, pipelineId, buildId, tag, executeCount }) {
        return request.get(`${API_URL_PREFIX}/${LOG_API_URL_PREFIX}/user/logs/${projectId}/${pipelineId}/${buildId}/mode`, { params: { tag, executeCount } })
    },

    getDownloadLogFromArtifactory ({ commit }, { projectId, pipelineId, buildId, tag, executeCount }) {
        return request.get(`${API_URL_PREFIX}/artifactory/api/user/artifactories/log/plugin/${projectId}/${pipelineId}/${buildId}/${tag}/${executeCount}`).then((res) => {
            const data = res.data || {}
            return data.url || ''
        })
    },

    getMacSysVersion () {
        return request.get(`${MACOS_API_URL_PREFIX}/user/systemVersions`)
    },

    getMacXcodeVersion () {
        return request.get(`${MACOS_API_URL_PREFIX}/user/xcodeVersions`)
    },
    setImportedPipelineJson ({ commit }, importedJson) {
        commit(SET_IMPORTED_JSON, importedJson)
    },

    pausePlugin ({ commit }, { projectId, pipelineId, buildId, taskId, isContinue, stageId, containerId, element }) {
        return request.post(`${PROCESS_API_URL_PREFIX}/user/builds/projects/${projectId}/pipelines/${pipelineId}/builds/${buildId}/taskIds/${taskId}/execution/pause?isContinue=${isContinue}&stageId=${stageId}&containerId=${containerId}`, element)
    },

    download (_, { url, name }) {
        return fetch(url, { credentials: 'include' }).then((res) => {
            if (res.status >= 200 && res.status < 300) {
                return res.blob()
            } else {
                return res.json().then((result) => Promise.reject(result))
            }
        }).then((blob) => {
            const a = document.createElement('a')
            const url = window.URL || window.webkitURL || window.moxURL
            a.href = url.createObjectURL(blob)
            if (name) a.download = name
            document.body.appendChild(a)
            a.click()
            document.body.removeChild(a)
        })
    }
}
