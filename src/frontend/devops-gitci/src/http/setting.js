import api from '../api'
const environmentPerfix = 'ms/environment/api/user/environment'
const ticketPerfix = 'ms/ticket/api/user/credentials'

export default {
    getThirdAgentZoneList (projectId, os) {
        return api.get(`${environmentPerfix}/thirdPartyAgent/projects/${projectId}/os/${os}/gateway`)
    },

    getThirdAgentLink (projectId, os, zone) {
        return api.get(`${environmentPerfix}/thirdPartyAgent/projects/${projectId}/os/${os}/generateLink?zoneName=${zone}`)
    },

    getThirdAgentStatus (projectId, agentId) {
        return api.get(`${environmentPerfix}/thirdPartyAgent/projects/${projectId}/agents/${agentId}/status`)
    },

    getTicketList (projectId, params) {
        return api.get(`${ticketPerfix}/${projectId}`, { params })
    },

    createTicket (projectId, params) {
        return api.post(`${ticketPerfix}/${projectId}`, params)
    },

    modifyTicket (projectId, params, credentialId) {
        return api.put(`${ticketPerfix}/${projectId}/${credentialId}`, params)
    },

    deleteTicket (projectId, credentialId) {
        return api.delete(`${ticketPerfix}/${projectId}/${credentialId}`)
    }
}
