import ajax from '@/utils/request'
import {
    PROCESS_API_URL_PREFIX,
    IAM_PERFIX
} from '@/store/constants'

const prefix = `/${PROCESS_API_URL_PREFIX}/user/pipelines/audit`

const state = {

}

const getters = {

}

const mutations = {

}

const actions = {
    getUserAudit (_, { projectId, userId, resourceName, status, startTime, endTime, current, limit }) {
        return ajax.get(
            `${prefix}/${projectId}/pipeline`, {
                params: {
                    page: current,
                    pageSize: limit,
                    resourceName: resourceName || undefined,
                    userId: userId || undefined,
                    status: status || undefined,
                    startTime: startTime || undefined,
                    endTime: endTime || undefined
                }
            }
        ).then(response => {
            return response.data
        })
    },
    /**
   * 获取是否为资源的管理员
   */
    async fetchHasManagerPermission (params) {
        const { projectCode, resourceType, resourceCode } = params
        return ajax.get(`${IAM_PERFIX}/${projectCode}/${resourceType}/resource/${resourceCode}/hasManagerPermission`)
    },
    /**
   * 是否启用权限管理
   */
    async fetchEnablePermission (params) {
        const { projectCode, resourceType, resourceCode } = params
        return ajax.get(`${IAM_PERFIX}/${projectCode}/${resourceType}/resource/${resourceCode}/isEnablePermission`)
    },
    /**
   * 流水线/流水线组 开启用户组权限管理
   */
    async enableGroupPermission (params) {
        const { projectCode, resourceType, resourceCode } = params
        return ajax.put(`${IAM_PERFIX}/${projectCode}/${resourceType}/resource/${resourceCode}/enable`)
    },
    /**
   * 流水线/流水线组 关闭用户组权限管理
   */
    async disableGroupPermission (params) {
        const { projectCode, resourceType, resourceCode } = params
        return ajax.put(`${IAM_PERFIX}/${projectCode}/${resourceType}/resource/${resourceCode}/disable`)
    },
    /**
   * 获取用户组列表
   */
    async fetchUserGroupList (params) {
        const { projectCode, resourceType, resourceCode } = params
        return ajax.get(`${IAM_PERFIX}/${projectCode}/${resourceType}/resource/${resourceCode}/listGroup`)
    },
    /**
   * 获取用户所属组
   */
    async fetchGroupMember (params) {
        const { projectCode, resourceType, resourceCode } = params
        return ajax.get(`${IAM_PERFIX}/${projectCode}/${resourceType}/resource/${resourceCode}/groupMember`)
    }
}

export default {
    state,
    getters,
    mutations,
    actions
}
