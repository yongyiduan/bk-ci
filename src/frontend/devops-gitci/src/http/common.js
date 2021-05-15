import api from '../api'
const PROJECT_API_URL_PERFIX = 'ms/project/api/user'

export default {
    getUserInfo () {
        return api.get(`${PROJECT_API_URL_PERFIX}/users`)
    }
}
