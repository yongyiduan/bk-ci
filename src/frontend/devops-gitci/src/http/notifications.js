import api from './ajax'
import { GITCI_PERFIX } from './perfix'

export default {
    getUserMessages (params) {
        return api.get(`${GITCI_PERFIX}/user/messages`, { params })
    },

    readAllMessages () {
        return api.put(`${GITCI_PERFIX}/user/messages/read`)
    }
}
