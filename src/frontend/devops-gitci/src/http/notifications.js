import api from './ajax'
import { GITCI_PERFIX } from './perfix'

export default {
    getUserUnreadMessages (current) {
        return api.get(`${GITCI_PERFIX}/user/messages?messageType=REQUEST&haveRead=false&page=${current}&pageSize=10`)
    },

    readAllMessages () {
        return api.put(`${GITCI_PERFIX}/user/messages/read`)
    }
}
