import api from '../api'
const LOG_API_URL_PERFIX = 'ms/log/api/user/logs'
const ARTIFACTORY_API_URL_PREFIX = 'artifactory/api'
const PROCESS_API_URL_PREFIX = 'process/api'
const CHECK_ENV_URL = ''

export default {
    // 第一次拉取日志
    getInitLog ({ projectId, pipelineId, buildId, tag, currentExe, subTag }) {
        return api.get(`${LOG_API_URL_PERFIX}/${projectId}/${pipelineId}/${buildId}`, {
            params: {
                tag,
                executeCount: currentExe,
                subTag
            }
        })
    },

    // 后续拉取日志
    getAfterLog ({ projectId, pipelineId, buildId, tag, currentExe, lineNo, subTag }) {
        return api.get(`${LOG_API_URL_PERFIX}/${projectId}/${pipelineId}/${buildId}/after`, {
            params: {
                start: lineNo,
                executeCount: currentExe,
                tag,
                subTag
            }
        })
    },

    requestPartFile ({ projectId, params }) {
        return api.post(`${ARTIFACTORY_API_URL_PREFIX}/user/artifactories/${projectId}/search`, params).then(response => {
            return response.data
        })
    },

    requestExecPipPermission ({ projectId, pipelineId, permission }) {
        return api.get(`${PROCESS_API_URL_PREFIX}/user/pipelines/${projectId}/${pipelineId}/hasPermission?permission=${permission}`).then(response => {
            return response.data
        })
    },

    requestDevnetGateway ({ commit }) {
        const baseUrl = CHECK_ENV_URL
        return api.get(`${ARTIFACTORY_API_URL_PREFIX}/user/artifactories/checkDevnetGateway`, { baseURL: baseUrl }).then(response => {
            return response.data
        }).catch(e => {
            return false
        })
    },

    requestDownloadUrl ({ projectId, artifactoryType, path }) {
        return api.post(`${ARTIFACTORY_API_URL_PREFIX}/user/artifactories/${projectId}/${artifactoryType}/downloadUrl?path=${encodeURIComponent(path)}`).then(response => {
            return response.data
        })
    },

    requestReportList ({ projectId, pipelineId, buildId }) {
        return api.get(`/${PROCESS_API_URL_PREFIX}/user/reports/${projectId}/${pipelineId}/${buildId}`).then(response => {
            return response.data
        })
    }
}
