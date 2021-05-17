import api from '../api'
import { PROJECT_PERFIX } from './perfix'

export default {
    getUserInfo () {
        return api.get(`${PROJECT_PERFIX}/user/users`)
    },

    getProjectId () {
        return api.get(`${PROJECT_PERFIX}/user/users`)
    }
}
