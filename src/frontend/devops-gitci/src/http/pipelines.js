import api from './ajax'
import { LOG_PERFIX, ARTIFACTORY_PREFIX, PROCESS_PREFIX, GITCI_PERFIX } from './perfix'

export default {
    // 第一次拉取日志
    getInitLog ({ projectId, pipelineId, buildId, tag, currentExe, subTag }) {
        return api.get(`${LOG_PERFIX}/user/logs/${projectId}/${pipelineId}/${buildId}`, {
            params: {
                tag,
                executeCount: currentExe,
                subTag
            }
        })
    },

    // 后续拉取日志
    getAfterLog ({ projectId, pipelineId, buildId, tag, currentExe, lineNo, subTag }) {
        return api.get(`${LOG_PERFIX}/user/logs/${projectId}/${pipelineId}/${buildId}/after`, {
            params: {
                start: lineNo,
                executeCount: currentExe,
                tag,
                subTag
            }
        })
    },

    requestPartFile ({ projectId, params }) {
        return api.post(`${ARTIFACTORY_PREFIX}/user/artifactories/${projectId}/search`, params)
    },

    requestExecPipPermission ({ projectId, pipelineId, permission }) {
        return api.get(`${PROCESS_PREFIX}/user/pipelines/${projectId}/${pipelineId}/hasPermission?permission=${permission}`)
    },

    requestDevnetGateway () {
        return api.get(`${ARTIFACTORY_PREFIX}/user/artifactories/checkDevnetGateway`)
    },

    requestDownloadUrl ({ projectId, artifactoryType, path }) {
        return api.post(`${ARTIFACTORY_PREFIX}/user/artifactories/${projectId}/${artifactoryType}/downloadUrl?path=${encodeURIComponent(path)}`)
    },

    requestReportList ({ projectId, pipelineId, buildId }) {
        return api.get(`${GITCI_PERFIX}/user/current/build/projects/${projectId}/pipelines/${pipelineId}/builds/${buildId}/report`)
    },

    getPipelineList (projectId) {
        return api.get(`${GITCI_PERFIX}/user/pipelines/${projectId}/list?page=1&pageSize=50`)
    },

    getPipelineBuildList (projectId, params) {
        return api.post(`${GITCI_PERFIX}/user/history/build/list/${projectId}`, params)
    },

    getPipelineBuildBranchList (projectId, params = {}) {
        return api.get(`${GITCI_PERFIX}/user/history/build/branch/list/${projectId}`, { params })
    },

    getPipelineBuildMemberList (projectId) {
        return api.get(`${GITCI_PERFIX}/user/gitcode/projects/members?projectId=${projectId}`)
    },

    getPipelineBuildDetail (projectId, params) {
        return api.get(`${GITCI_PERFIX}/user/current/build/detail/${projectId}`, { params })
    },

    getPipelineBuildYaml (projectId, buildId) {
        return api.get(`${GITCI_PERFIX}/user/trigger/build/getYaml/${projectId}/${buildId}`)
    },

    addPipelineYamlFile (projectId, params) {
        return api.post(`${GITCI_PERFIX}/user/gitcode/projects/repository/files?projectId=${projectId}`, params)
    },

    getPipelineBranches (params) {
        return api.get(`${GITCI_PERFIX}/user/gitcode/projects/repository/branches`, { params })
    },

    getPipelineCommits (params) {
        return api.get(`${GITCI_PERFIX}/user/gitcode/projects/commits`, { params })
    },

    getPipelineBranchYaml (projectId, pipelineId, params) {
        return api.get(`${GITCI_PERFIX}/user/trigger/build/${projectId}/${pipelineId}/yaml`, { params })
    },

    trigglePipeline (pipelineId, params) {
        return api.post(`${GITCI_PERFIX}/user/trigger/build/${pipelineId}/startup`, params)
    },

    toggleEnablePipeline (projectId, pipelineId, enabled) {
        return api.post(`${GITCI_PERFIX}/user/pipelines/${projectId}/${pipelineId}/enable?enabled=${enabled}`)
    },

    updateRemark (projectId, pipelineId, buildId, remark) {
        return api.post(`${PROCESS_PREFIX}/user/builds/${projectId}/${pipelineId}/${buildId}/updateRemark`, { remark })
    },

    rebuildPipeline (projectId, pipelineId, buildId, params = {}) {
        const queryStr = Object.keys(params).reduce((query, key) => {
            const value = params[key]
            if (value !== undefined) {
                const queryVal = `${key}=${value}`
                query += (query === '' ? '?' : '&')
                query += queryVal
            }
            return query
        }, '')
        return api.post(`${GITCI_PERFIX}/user/builds/${projectId}/${pipelineId}/${buildId}/retry${queryStr}`)
    },

    cancelBuildPipeline (projectId, pipelineId, buildId) {
        return api.delete(`${GITCI_PERFIX}/user/builds/${projectId}/${pipelineId}/${buildId}`)
    }
}
