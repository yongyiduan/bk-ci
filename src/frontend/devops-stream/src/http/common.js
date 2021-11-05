import api from './ajax'
import { PROJECT_PERFIX, STREAM_PERFIX, REPOSITORY_PREFIX } from './perfix'

export default {
    getUserInfo () {
        return api.get(`${PROJECT_PERFIX}/user/users`)
    },

    getProjectInfo (projectPath) {
        return api.get(`${STREAM_PERFIX}/user/gitcode/projects/info?gitProjectId=${projectPath}`)
    },

    oauth (redirectUrl) {
        return api.get(`${REPOSITORY_PREFIX}/user/git/isOauth?redirectUrl=${redirectUrl}`)
    },

    getStreamProjects (type = 'MY_PROJECT', page = 1, limit = 20, search = '') {
        const querySearch = (search && search.trim()) ? `&search=${search.trim()}` : ''
        return api.get(`${STREAM_PERFIX}/user/projects/${type}/list?page=${page}&pageSize=${limit}${querySearch}`)
    },
    
    getRecentProjects (size = 4) {
        return api.get(`${STREAM_PERFIX}/user/projects/history?size=${size}`)
    }
}
