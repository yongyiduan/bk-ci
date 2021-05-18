import api from './ajax'
import { PROJECT_PERFIX, GITCI_PERFIX } from './perfix'

export default {
    getUserInfo () {
        return api.get(`${PROJECT_PERFIX}/user/users`)
    },

    getProjectInfo (projectPath) {
        return api.get(`${GITCI_PERFIX}/user/gitcode/projects/info?gitProjectId=${projectPath}`)
    }
}
