import api from '../api'
import { LOG_PERFIX, ARTIFACTORY_PREFIX, PROCESS_PREFIX, CHECK_ENV_URL, GITCI_PERFIX } from './perfix'

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
        return api.post(`${ARTIFACTORY_PREFIX}/user/artifactories/${projectId}/search`, params).then(response => {
            return response.data
        })
    },

    requestExecPipPermission ({ projectId, pipelineId, permission }) {
        return api.get(`${PROCESS_PREFIX}/user/pipelines/${projectId}/${pipelineId}/hasPermission?permission=${permission}`).then(response => {
            return response.data
        })
    },

    requestDevnetGateway ({ commit }) {
        const baseUrl = CHECK_ENV_URL
        return api.get(`${ARTIFACTORY_PREFIX}/user/artifactories/checkDevnetGateway`, { baseURL: baseUrl }).then(response => {
            return response.data
        }).catch(e => {
            return false
        })
    },

    requestDownloadUrl ({ projectId, artifactoryType, path }) {
        return api.post(`${ARTIFACTORY_PREFIX}/user/artifactories/${projectId}/${artifactoryType}/downloadUrl?path=${encodeURIComponent(path)}`).then(response => {
            return response.data
        })
    },

    requestReportList ({ projectId, pipelineId, buildId }) {
        return api.get(`${PROCESS_PREFIX}/user/reports/${projectId}/${pipelineId}/${buildId}`).then(response => {
            return response.data
        })
    },

    getPipelineList (projectId) {
        return api.get(`${GITCI_PERFIX}/user/pipelines/${projectId}/listInfo`).then(response => {
            return response.data
        })
    },

    getPipelineBuildList (projectId, params) {
        return api.get(`${GITCI_PERFIX}/user/history/build/list/${projectId}`, { params }).then(response => {
            return response.data
        })
    },

    getPipelineBuildBranchList (projectId, params = {}) {
        return api.get(`${GITCI_PERFIX}/user/history/build/branch/list/${projectId}`, { params }).then(response => {
            return response.data
        })
    },

    getPipelineBuildMemberList (projectId) {
        return api.get(`${GITCI_PERFIX}/user/gitcode/projects/members?projectId=${projectId}`).then(response => {
            return response.data
        })
    },

    getPipelineBuildDetail (projectId, params) {
        return api.get(`${GITCI_PERFIX}/user/current/build/detail/${projectId}`, { params }).then(response => {
            return response.data
        })
    },

    getPipelineBuildYaml (projectId, buildId) {
        return api.get(`${GITCI_PERFIX}/user/trigger/build/getYaml/${projectId}/${buildId}`).then(response => {
            return response.data
        })
    }
}
