const Vue = window.Vue
const vue = new Vue()
const prefix = 'turbo-new/api/user'

module.exports = {
    getOverViewStatData (projectId) {
        return vue.$ajax.get(`${prefix}/turboDaySummary/statisticsRowData/${projectId}`)
    },

    getCompileNumberTrend (dateType, projectId) {
        return vue.$ajax.get(`${prefix}/turboDaySummary/compileNumber/dateType/${dateType}/projectId/${projectId}`)
    },

    getTimeConsumingTrend (dateType, projectId) {
        return vue.$ajax.get(`${prefix}/turboDaySummary/timeConsumingTrend/dateType/${dateType}/projectId/${projectId}`)
    },

    getPlanDetailById (planId) {
        return vue.$ajax.get(`${prefix}/turboPlan/planId/${planId}`)
    },

    getEngineList () {
        return vue.$ajax.get(`${prefix}/turboEngineConfig/list`)
    },

    modifyTaskBasic (form) {
        return vue.$ajax.put(`${prefix}/turboPlan/name/planId/${form.planId}`, form)
    },

    modifyTaskWhiteList (form) {
        return vue.$ajax.put(`${prefix}/turboPlan/whiteList/planId/${form.planId}`, form)
    },

    getHistoryList (pageNum, pageSize, postData) {
        return vue.$ajax.post(`${prefix}/turboRecord/list?pageNum=${pageNum}&pageSize=${pageSize}`, postData)
    },

    getPlanList (projectId, pageNum) {
        return vue.$ajax.get(`${prefix}/turboPlan/detail/projectId/${projectId}`, { params: { pageNum, pageSize: 40 } })
    },

    getPlanInstanceDetail (turboPlanId, pageNum, pageSize) {
        return vue.$ajax.get(`${prefix}/planInstance/detail/turboPlanId/${turboPlanId}`, { params: { pageNum, pageSize } })
    },

    getHistorySearchList (projectId) {
        return vue.$ajax.get(`${prefix}/turboRecord/detail/projectId/${projectId}`)
    },

    modifyConfigParam (form) {
        return vue.$ajax.put(`${prefix}/turboPlan/configParam/planId/${form.planId}`, form)
    },

    addTurboPlan (form) {
        return vue.$ajax.post(`${prefix}/turboPlan`, form)
    },

    getTurboRecord (turboRecordId) {
        return vue.$ajax.get(`${prefix}/turboRecord/id/${turboRecordId}`)
    },

    modifyTurboPlanTopStatus (planId, topStatus) {
        return vue.$ajax.put(`${prefix}/turboPlan/topStatus/planId/${planId}/topStatus/${topStatus}`)
    }
}
