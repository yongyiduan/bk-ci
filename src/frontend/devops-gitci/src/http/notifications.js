import api from './ajax'
import { GITCI_PERFIX } from './perfix'

export default {
    getUserMessages (params) {
        return api.get(`${GITCI_PERFIX}/user/messages`, { params })
    },

    readAllMessages () {
        return api.put(`${GITCI_PERFIX}/user/messages/read`)
    },

    readMessage (id) {
        return api.put(`${GITCI_PERFIX}/user/messages/${id}/read`)
    },

    getUnreadNotificationNum () {
        return api.get(`${GITCI_PERFIX}/user/messages/noread`)
    }
}
