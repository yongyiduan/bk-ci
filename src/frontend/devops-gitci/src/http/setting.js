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

    saveSetting (params) {
        return api.post(`${GITCI_PERFIX}/user/setting/save`, { params })
    },

    getSetting (projectId) {
        return api.get(`${GITCI_PERFIX}/user/setting/${projectId}`)
    },

    getSystemPoolDetail () {
        return api.get(`${DISPATCH_GITCI_PERFIX}/user/dispatch/gitci/dockerhost-load`)
    },

    getEnvironmentList (projectId) {
        return api.get(`${ENVIRNMENT_PERFIX}/user/environment/${projectId}`)
    },

    addEnvironment (projectId, params) {
        return api.post(`${ENVIRNMENT_PERFIX}/user/environment/${projectId}`, params)
    },

    deleteEnvironment (projectId, envHashId) {
        return api.delete(`${ENVIRNMENT_PERFIX}/user/environment/${projectId}/${envHashId}`)
    }
}
