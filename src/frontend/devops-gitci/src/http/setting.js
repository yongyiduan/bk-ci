import api from './ajax'
import { ENVIRNMENT_PERFIX, TICKET_PERFIX, GITCI_PERFIX, DISPATCH_GITCI_PERFIX } from './perfix'

export default {
    getThirdAgentZoneList (projectId, os) {
        return api.get(`${ENVIRNMENT_PERFIX}/user/environment/thirdPartyAgent/projects/${projectId}/os/${os}/gateway`)
    },

    getThirdAgentLink (projectId, os, zone) {
        return api.get(`${ENVIRNMENT_PERFIX}/user/environment/thirdPartyAgent/projects/${projectId}/os/${os}/generateLink?zoneName=${zone}`)
    },

    getThirdAgentStatus (projectId, agentId) {
        return api.get(`${ENVIRNMENT_PERFIX}/user/environment/thirdPartyAgent/projects/${projectId}/agents/${agentId}/status`)
    },

    getTicketList (projectId, params) {
        return api.get(`${TICKET_PERFIX}/user/credentials/${projectId}`, { params })
    },

    createTicket (projectId, params) {
        return api.post(`${TICKET_PERFIX}/user/credentials/${projectId}`, params)
    },

    modifyTicket (projectId, params, credentialId) {
        return api.put(`${TICKET_PERFIX}/user/credentials/${projectId}/${credentialId}`, params)
    },

    deleteTicket (projectId, credentialId) {
        return api.delete(`${TICKET_PERFIX}/user/credentials/${projectId}/${credentialId}`)
    },

    saveSetting (projectId, params) {
        return api.post(`${GITCI_PERFIX}/user/basic/setting/${projectId}/save`, params)
    },

    getSetting (projectId) {
        return api.get(`${GITCI_PERFIX}/user/basic/setting/${projectId}`)
    },

    getSystemPoolDetail () {
        return api.get(`${DISPATCH_GITCI_PERFIX}/user/dispatch/gitci/dockerhost-load`)
    },

    getEnvironmentList (projectId) {
        return api.get(`${ENVIRNMENT_PERFIX}/user/environment/${projectId}`)
    },

    getNodeList (projectId) {
        return api.get(`${ENVIRNMENT_PERFIX}/user/envnode/${projectId}`)
    },

    deleteNode (projectId, params) {
        return api.post(`${ENVIRNMENT_PERFIX}/user/envnode/${projectId}/deleteNodes`, params).then(response => {
            return response
        })
    },

    addEnvironment (projectId, params) {
        return api.post(`${ENVIRNMENT_PERFIX}/user/environment/${projectId}`, params)
    },

    deleteEnvironment (projectId, envHashId) {
        return api.delete(`${ENVIRNMENT_PERFIX}/user/environment/${projectId}/${envHashId}`)
    },

    addNodeToPool (projectId, poolId, params) {
        return api.post(`${ENVIRNMENT_PERFIX}/user/environment/${projectId}/${poolId}/addNodes`, params)
    },

    addNodeToSystem (projectId, agentId) {
        return api.post(`${ENVIRNMENT_PERFIX}/user/environment/thirdPartyAgent/projects/${projectId}/agents/${agentId}/import`)
    },

    toggleEnableCi (enabled, projectInfo) {
        return api.post(`${GITCI_PERFIX}/user/basic/setting/enable?enabled=${enabled}`, projectInfo)
    },

    resetAuthorization (projectId) {
        return api.post(`${GITCI_PERFIX}/user/basic/setting/${projectId}/user`)
    }
}
