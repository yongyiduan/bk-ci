import api from './ajax'
import { GITCI_PERFIX } from './perfix'

export default {
    getUserMessages (params) {
        return api.get(`${GITCI_PERFIX}/user/messages`, { params })
    },

    readAllMessages (projectId) {
        return api.put(`${GITCI_PERFIX}/user/messages/read?projectId=${projectId}`)
    },

    readMessage (id) {
        return api.put(`${GITCI_PERFIX}/user/messages/${id}/read`)
    },

    getUnreadNotificationNum (projectId) {
        return api.get(`${GITCI_PERFIX}/user/messages/noread`, { params: { projectId } })
    }
}
