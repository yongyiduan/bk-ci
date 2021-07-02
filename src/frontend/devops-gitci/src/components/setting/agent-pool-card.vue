<template>
    <section class="agent-pool-card">
        <header class="card-header">
            <h5 class="header-info">
                <span class="info-title">{{ pool.name }}</span>
                <span class="info-num" v-if="editable">Agent：{{ pool.nodeCount }}</span>
            </h5>

            <opt-menu v-if="editable">
                <li @click="goToAgentList">Self-hosted Agents</li>
                <li @click="showDelete = true">Delete Agent Pool</li>
            </opt-menu>
        </header>

        <ul class="card-useages" v-if="!editable">
            <li class="useage-item" v-for="usage in cpuUsages" :key="usage.name">
                <span class="item-header">
                    <span class="header-title">{{ usage.name }}</span>
                    <span class="header-val">{{ usage.showVal }}%</span>
                </span>
                <bk-progress :theme="getTheme(usage.val)" :percent="usage.val" :show-text="false"></bk-progress>
            </li>
        </ul>
        <bk-exception class="exception-wrap-item exception-part card-useages" type="empty" scene="part" v-else>No Data</bk-exception>
        <div v-if="editable" class="operate-btns">
            <bk-button @click="addAgent" class="card-button">Add agent</bk-button>
            <bk-button @click="importNewNode" class="card-button">Import agent</bk-button>
        </div>
        <bk-dialog v-model="showDelete"
            theme="danger"
            :mask-close="false"
            :loading="isDeleteing"
            @confirm="deletePool"
            title="Delete">
            Are you sure to delete【{{pool.name}}】?
        </bk-dialog>

        <node-select :node-select-conf="nodeSelectConf"
            :row-list="importNodeList"
            :select-handlerc-conf="selectHandlercConf"
            :confirm-fn="confirmFn"
            :toggle-all-select="toggleAllSelect"
            :loading="nodeDialogLoading"
            :cancel-fn="cancelFn"
            :query="query">
        </node-select>
    </section>
</template>

<script>
    import optMenu from '@/components/opt-menu'
    import nodeSelect from './node-select-dialog'
    import { setting } from '@/http'
    import { mapState } from 'vuex'

    export default {
        components: {
            optMenu,
            nodeSelect
        },

        props: {
            editable: {
                type: Boolean,
                default: true
            },
            pool: Object
        },

        data () {
            return {
                showDelete: false,
                isDeleteing: false,
                nodeList: [], // pool已有节点
                importNodeList: [], // 导入的节点
                nodeDialogLoading: {
                    isLoading: false,
                    title: ''
                },
                // 节点选择弹窗
                nodeSelectConf: {
                    isShow: false,
                    quickClose: false,
                    hasHeader: false,
                    unselected: true
                },
                // 选择节点
                selectHandlercConf: {
                    curTotalCount: 0,
                    curDisplayCount: 0,
                    selectedNodeCount: 0,
                    allNodeSelected: false,
                    searchEmpty: false
                }
            }
        },

        computed: {
            ...mapState(['projectId']),

            cpuUsages () {
                return [
                    { name: 'CPU Usage', showVal: this.pool.averageCpuLoad, val: this.pool.averageCpuLoad / 100 },
                    { name: 'Memory Usage', showVal: this.pool.averageMemLoad, val: this.pool.averageMemLoad / 100 },
                    { name: 'Disk Usage', showVal: this.pool.averageDiskLoad, val: this.pool.averageDiskLoad / 100 }
                ]
            }
        },

        watch: {
            importNodeList: {
                deep: true,
                handler: function (val) {
                    let curCount = 0
                    const isSelected = this.importNodeList.some(item => {
                        return item.isChecked === true && !item.isEixtEnvNode
                    })

                    if (isSelected) {
                        this.nodeSelectConf.unselected = false
                    } else {
                        this.nodeSelectConf.unselected = true
                    }

                    this.importNodeList.filter(item => {
                        if (item.isChecked && !item.isEixtEnvNode) curCount++
                    })

                    this.selectHandlercConf.selectedNodeCount = curCount
                    this.decideToggle()
                }
            }
        },

        created () {
            if (this.pool.nodeCount > 0) {
                setting.getNodeList(this.projectId, this.pool.envHashId).then((res) => {
                    this.nodeList = res
                })
            }
        },

        methods: {
            deletePool () {
                this.isDeleteing = true
                setting.deleteEnvironment(this.projectId, this.pool.envHashId).then(() => {
                    this.showDelete = false
                    this.$emit('refresh')
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isDeleteing = false
                })
            },

            addAgent () {
                this.$router.push({
                    name: 'addAgent',
                    params: {
                        poolId: this.pool.envHashId,
                        poolName: this.pool.name
                    }
                })
            },

            getTheme (val) {
                let theme = 'success'
                if (val > 0.5) {
                    theme = 'warning'
                }
                if (val > 0.8) {
                    theme = 'danger'
                }
                return theme
            },

            goToAgentList () {
                this.$router.push({ name: 'agentList', params: { poolId: this.pool.envHashId, poolName: this.pool.name } })
            },

            importNewNode () {
                this.nodeSelectConf.isShow = true
                this.requestNodeList()
            },

            /**
             * 获取弹窗节点列表
             */
            requestNodeList () {
                this.nodeDialogLoading.isLoading = true

                setting.requestNodeList(this.projectId).then((res) => {
                    this.importNodeList.splice(0, this.importNodeList.length)

                    res.map(item => {
                        item.isChecked = false
                        item.isDisplay = true
                        this.importNodeList.push(item)
                    })
                    this.importNodeList = this.importNodeList.filter(item => (item.nodeType === 'THIRDPARTY'))
                    this.importNodeList.filter(kk => {
                        this.nodeList.filter(vv => {
                            if (vv.nodeHashId === kk.nodeHashId) {
                                kk.isChecked = true
                                kk.isEixtEnvNode = true
                            }
                        })
                    })

                    let curCount = 0

                    this.importNodeList.forEach(item => {
                        if (item.isDisplay) curCount++
                    })

                    this.selectHandlercConf.curTotalCount = curCount

                    const result = this.importNodeList.some(element => {
                        return element.isDisplay
                    })

                    if (result) {
                        this.selectHandlercConf.searchEmpty = false
                    } else {
                        this.selectHandlercConf.searchEmpty = true
                    }
                }).catch((err) => {
                    const message = err.message ? err.message : err
                    const theme = 'error'

                    this.$bkMessage({
                        message,
                        theme
                    })
                }).finally(() => {
                    this.nodeDialogLoading.isLoading = false
                })
            },

            /**
             * 导入节点
             */
            async importEnvNode (nodeArr) {
                let message, theme
                const params = []

                this.nodeDialogLoading.isLoading = true

                nodeArr.map(item => {
                    params.push(item)
                })

                setting.importEnvNode(this.projectId, this.pool.envHashId, params).then(() => {
                    message = 'Import successfully'
                    theme = 'success'
                }).catch((err) => {
                    message = err.message ? err.message : err
                    theme = 'error'
                }).finally(() => {
                    this.$bkMessage({
                        message,
                        theme
                    })

                    this.nodeSelectConf.isShow = false
                    this.nodeDialogLoading.isLoading = false
                    this.$emit('refresh')
                    setting.getNodeList(this.projectId, this.pool.envHashId).then((res) => {
                        this.nodeList = res
                    })
                })
            },

            confirmFn () {
                if (!this.nodeDialogLoading.isLoading) {
                    const nodeArr = []

                    this.importNodeList.forEach(item => {
                        if (item.isChecked && !item.isEixtEnvNode) {
                            nodeArr.push(item.nodeHashId)
                        }
                    })

                    this.importEnvNode(nodeArr)
                }
            },

            cancelFn () {
                if (!this.nodeDialogLoading.isLoading) {
                    this.nodeSelectConf.isShow = false
                    this.selectHandlercConf.searchEmpty = false
                }
            },

            /**
             * 节点全选
             */
            toggleAllSelect () {
                if (this.selectHandlercConf.allNodeSelected) {
                    this.importNodeList.forEach(item => {
                        if (item.isDisplay && !item.isEixtEnvNode) {
                            item.isChecked = true
                        }
                    })
                } else {
                    this.importNodeList.forEach(item => {
                        if (item.isDisplay && !item.isEixtEnvNode) {
                            item.isChecked = false
                        }
                    })
                }
            },
            /**
             * 搜索节点
             */
            query (target) {
                if (target.length) {
                    target.filter(item => {
                        return item && item.length
                    })
                    this.importNodeList.forEach(item => {
                        const str = item.ip
                        for (let i = 0; i < target.length; i++) {
                            // if (target[i] && str === target[i] && item.canUse) {
                            if (target[i] && str === target[i]) {
                                item.isDisplay = true
                                break
                            } else {
                                item.isDisplay = false
                            }
                        }
                    })

                    const result = this.importNodeList.some(element => {
                        return element.isDisplay
                    })

                    if (result) {
                        this.selectHandlercConf.searchEmpty = false
                    } else {
                        this.selectHandlercConf.searchEmpty = true
                    }
                } else {
                    this.selectHandlercConf.searchEmpty = false
                    this.importNodeList.forEach(item => {
                        item.isDisplay = true
                    })
                }

                this.decideToggle()
            },
            /**
             * 弹窗全选联动
             */
            decideToggle () {
                let curCount = 0
                let curCheckCount = 0

                this.importNodeList.forEach(item => {
                    if (item.isDisplay) {
                        curCount++
                        if (item.isChecked) curCheckCount++
                    }
                })

                this.selectHandlercConf.curDisplayCount = curCount

                if (curCount === curCheckCount) {
                    this.selectHandlercConf.allNodeSelected = true
                } else {
                    this.selectHandlercConf.allNodeSelected = false
                }
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .agent-pool-card {
        width: 368px;
        height: 310px;
        background: #FFFFFF;
        border: 1px solid #dde4eb;
        border-radius: 2px;
    }
    .card-header {
        height: 75px;
        padding: 17px 10px 17px 24px;
        display: flex;
        justify-content: space-between;
        align-items: center;
        border-bottom: 1px solid #dde4eb;
        .info-title {
            font-size: 16px;
            color: #333C48;
            line-height: 21px;
            margin-bottom: 4px;
            display: block;
        }
        .info-num {
            font-size: 12px;
            line-height:16px;
            color: #c3cdd7;
        }
    }
    .card-useages {
        padding: 7px 24px 24px;
        .useage-item {
            margin-top: 22px;
            .item-header {
                font-size: 12px;
                line-height: 14px;
                height: 14px;
                color: #7b7d8a;
                display: inline-block;
                width: 100%;
                margin-bottom: 7px;
            }
            .header-val {
                float: right;
                color: #979ba5;
            }
        }
    }
    .operate-btns {
        display: flex;
        justify-content: space-between;
        .card-button {
            margin: 0 24px;
            width: 160px;
            font-size: 12px;
        }
    }
    
</style>
