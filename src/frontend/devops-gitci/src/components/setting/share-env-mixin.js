import { common, setting } from '@/http'

const shareEnvMixin = {
    data () {
        return {
            page: 1,
            pageSize: 100,
            hasNext: false,
            searchStr: '',
            shareList: [], // 已有共享的项目列表
            curPageList: [], // 当前页项目列表
            totalList: [], // 每页列表之和
            shareDialogLoading: {
                isLoading: false,
                title: ''
            },
            // 节点选择弹窗
            shareSelectConf: {
                isShow: false,
                quickClose: false,
                unselected: false
            },
            // 选择节点
            shareHandlerConf: {
                curTotalCount: 0,
                curDisplayCount: 0,
                selectedNodeCount: 0,
                allSelected: false,
                searchEmpty: false
            }
        }
    },
    computed: {
        pageConfig () {
            const { page, pageSize, hasNext } = this
            return {
                page,
                pageSize,
                hasNext
            }
        }
    },
    watch: {
        curPageList: {
            deep: true,
            handler: function (val) {
                this.decideToggle()
            }
        }
    },
    methods: {
        toLinkShare () {
            this.shareSelectConf.isShow = true
            this.requestProjectList()
        },

        requestProjectList () {
            this.shareDialogLoading.isLoading = true

            common.getGitciProjects('MY_PROJECT', this.page, this.pageSize, this.searchStr).then((res) => {
                this.hasNext = res.hasNext || false
                res = res.records || []
                this.curPageList = []
                res.map(item => {
                    // 在这里判断一下totalList里面能不能找到这一项，是否isCheck
                    if (item.projectCode !== this.projectId) {
                        item.isChecked = false
                        this.curPageList.push(item)
                        if (!this.totalList.find(totalListItem => totalListItem.id === item.id)) {
                            this.totalList.push(item)
                        }
                    }
                })
                this.curPageList.filter(kk => {
                    this.shareList.filter(vv => {
                        if (vv.gitProjectId === `git_${kk.id}`) {
                            kk.isChecked = true
                            kk.isEixt = true
                        }
                    })
                })
                this.shareHandlerConf.searchEmpty = this.curPageList.length <= 0
            }).catch((err) => {
                const message = err.message ? err.message : err
                const theme = 'error'

                this.$bkMessage({
                    message,
                    theme
                })
            }).finally(() => {
                this.shareDialogLoading.isLoading = false
            })
        },

        async confirmShare (params) {
            let message, theme

            this.shareDialogLoading.isLoading = true

            setting.setSharePool(this.projectId, this.envHashId, params).then(() => {
                message = 'Share successfully'
                theme = 'success'

                this.getShareList()
            }).catch((err) => {
                message = err.message ? err.message : err
                theme = 'error'
            }).finally(() => {
                this.$bkMessage({
                    message,
                    theme
                })

                this.shareSelectConf.isShow = false
                this.shareDialogLoading.isLoading = false
                this.searchStr = ''
                this.page = 1
                this.totalList = []
            })
        },

        confirmFn () {
            if (!this.shareDialogLoading.isLoading) {
                let selectItems = this.totalList.filter(item => item.isChecked && !item.isEixt)
                if (selectItems.length <= 0) {
                    this.$bkMessage({
                        theme: 'error',
                        message: 'No project selected'
                    })
                    return
                }
                selectItems = selectItems.map(item => {
                    return {
                        gitProjectId: `git_${item.id}`,
                        name: item.nameWithNamespace,
                        type: 'PROJECT'
                    }
                })
                const params = Object.assign({}, { sharedProjects: selectItems })
                this.confirmShare(params)
            }
        },

        cancelFn () {
            if (!this.shareDialogLoading.isLoading) {
                this.searchStr = ''
                this.page = 1
                this.totalList = []
                this.shareSelectConf.isShow = false
                this.shareHandlerConf.searchEmpty = false
            }
        },

        /**
         * 节点全选
         */
        toggleAllSelect () {
            if (this.shareHandlerConf.allSelected) {
                this.curPageList.forEach(item => {
                    if (!item.isEixt) {
                        item.isChecked = true
                    }
                })
            } else {
                this.curPageList.forEach(item => {
                    if (!item.isEixt) {
                        item.isChecked = false
                    }
                })
            }
        },
        
        // 搜索
        query (inputVal) {
            if (this.searchStr !== inputVal) {
                this.searchStr = inputVal
                this.page = 1
                this.requestProjectList()
            }
        },
        // 翻页
        updatePage (page) {
            this.page = page
            this.requestProjectList()
        },
        // 弹窗全选联动
        decideToggle () {
            let curCount = 0
            let curCheckCount = 0

            this.curPageList.forEach(item => {
                curCount++
                if (item.isChecked) curCheckCount++
            })

            this.shareHandlerConf.curDisplayCount = curCount

            if (curCount === curCheckCount) {
                this.shareHandlerConf.allSelected = true
            } else {
                this.shareHandlerConf.allSelected = false
            }
        }
    }
}

export default shareEnvMixin
