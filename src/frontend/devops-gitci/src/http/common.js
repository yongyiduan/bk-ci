import api from './ajax'
import { PROJECT_PERFIX, GITCI_PERFIX, REPOSITORY_PREFIX } from './perfix'

export default {
    getUserInfo () {
        return api.get(`${PROJECT_PERFIX}/user/users`)
    },

    getProjectInfo (projectPath) {
        return api.get(`${GITCI_PERFIX}/user/gitcode/projects/info?gitProjectId=${projectPath}`, { headers: { 'X-DEVOPS-PROJECT-ID': 'gitciproject' } })
    },

    oauth (redirectUrl) {
        return api.get(`${REPOSITORY_PREFIX}/user/git/isOauth?redirectUrl=${redirectUrl}`)
    },

    getGitciProjects (type = 'MY_PROJECT', page = 1, limit = 20, search = '') {
        const querySearch = (search && search.trim()) ? `&search=${search.trim()}` : ''
        return api.get(`${GITCI_PERFIX}/user/projects/${type}/list?page=${page}&pageSize=${limit}${querySearch}`)
    }
}
