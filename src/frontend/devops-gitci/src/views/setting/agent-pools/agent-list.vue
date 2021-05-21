<template>
    <article class="agent-list-home">
        <header class="agent-list-head">
            <bk-breadcrumb>
                <bk-breadcrumb-item v-for="(item,index) in navList" :key="index" :to="item.link">{{item.title}}</bk-breadcrumb-item>
            </bk-breadcrumb>
        </header>

        <main class="agent-list-main">
            <section>
                <bk-button theme="primary" class="add-agent" @click="goToAddAgent">Add Agent</bk-button>
            </section>
            <bk-table class="agent-table"
                :data="agentList"
                :outer-border="false"
                :header-border="false"
                :header-cell-style="{ background: '#fafbfd' }"
                :height="tableHeight"
                v-bkloading="{ isLoading }"
            >
                <bk-table-column label="别名" prop="displayName"></bk-table-column>
                <bk-table-column label="内网IP" prop="ip"></bk-table-column>
                <bk-table-column label="操作系统" prop="osName"></bk-table-column>
                <bk-table-column label="来源/导入人">
                    <template slot-scope="props">
                        <div v-if="(props.row.nodeType === 'CC' || props.row.nodeType === 'CMDB') && ((props.row.nodeType === 'CC' && props.row.createdUser !== props.row.operator && props.row.createdUser !== props.row.bakOperator)
                            || (props.row.nodeType === 'CMDB' && props.row.createdUser !== props.row.operator && props.row.bakOperator.split(';').indexOf(props.row.createdUser) === -1))">
                            <div class="edit-operator" v-if="userInfo.username === props.row.operator || userInfo.username === props.row.bakOperator">
                                <i class="bk-icon icon-exclamation-circle"></i>
                            </div>
                            <div class="prompt-operator" v-else>
                                <bk-popover placement="top">
                                    <span><i class="bk-icon icon-exclamation-circle"></i>责任人已变更，禁止使用</span>
                                    <template slot="content">
                                        <p>当前导入人：<span>{{ props.row.createdUser }}</span></p>
                                        <p>当前主机责任人：<span>{{ props.row.operator }}</span><span v-if="props.row.nodeType === 'CC'">/{{ props.row.bakOperator }}</span></p>
                                        <p>请联系主机负责人</p>
                                    </template>
                                </bk-popover>
                            </div>
                        </div>
                        <div v-else>
                            <span class="node-name">{{ nodeTypeMap[props.row.nodeType] || '-' }}</span>
                            <span>({{ props.row.createdUser }})</span>
                        </div>
                    </template>
                </bk-table-column>
                <bk-table-column label="状态" prop="osName">
                    <template slot-scope="props">
                        <div class="table-node-item node-item-status"
                            v-if="props.row.nodeStatus === 'BUILDING_IMAGE' && props.row.nodeType === 'DEVCLOUD'">
                            <span class="node-status-icon normal-stutus-icon"></span>
                            <span class="node-status">正常</span>
                        </div>
                        <div class="table-node-item node-item-status">
                            <!-- 状态icon -->
                            <span class="node-status-icon normal-stutus-icon" v-if="successStatus.includes(props.row.nodeStatus)"></span>
                            <span class="node-status-icon abnormal-stutus-icon"
                                v-if="failStatus.includes(props.row.nodeStatus)">
                            </span>
                            <div class="bk-spin-loading bk-spin-loading-mini bk-spin-loading-primary"
                                v-if="runningStatus.includes(props.row.nodeStatus)">
                                <div class="rotate rotate1"></div>
                                <div class="rotate rotate2"></div>
                                <div class="rotate rotate3"></div>
                                <div class="rotate rotate4"></div>
                                <div class="rotate rotate5"></div>
                                <div class="rotate rotate6"></div>
                                <div class="rotate rotate7"></div>
                                <div class="rotate rotate8"></div>
                            </div>
                            <!-- 状态值 -->
                            <span class="install-agent" v-if="props.row.nodeType === 'DEVCLOUD' && props.row.nodeStatus === 'RUNNING'">
                                {{ nodeStatusMap[props.row.nodeStatus] }}
                            </span>
                            <span class="node-status" v-else>{{ nodeStatusMap[props.row.nodeStatus] }}</span>
                        </div>
                    </template>
                </bk-table-column>
                <bk-table-column label="创建/导入时间" prop="createTime"></bk-table-column>
                <bk-table-column label="最后修改时间" prop="lastModifyTime"></bk-table-column>
                <bk-table-column label="操作" width="150" class-name="handler-btn">
                    <template slot-scope="props">
                        <span class="update-btn" @click="showDelete(props.row)">删除</span>
                    </template>
                </bk-table-column>
            </bk-table>
        </main>

        <bk-dialog v-model="isShowDelete"
            theme="danger"
            :mask-close="false"
            :loading="isDeleteing"
            @confirm="deleteAgent"
            title="确认删除">
            是否删除【{{deleteRow.displayName}}】？
        </bk-dialog>
    </article>
</template>

<script>
    import { mapState } from 'vuex'
    import { setting } from '@/http'

    export default {
        data () {
            return {
                navList: [
                    { link: { name: 'agentPools' }, title: 'Agent Pools' },
                    { link: '', title: 'Agent List' }
                ],
                agentList: [],
                isLoading: false,
                runningStatus: ['CREATING', 'STARTING', 'STOPPING', 'RESTARTING', 'DELETING', 'BUILDING_IMAGE'],
                successStatus: ['NORMAL', 'BUILD_IMAGE_SUCCESS'],
                failStatus: ['ABNORMAL', 'DELETED', 'LOST', 'BUILD_IMAGE_FAILED', 'UNKNOWN', 'RUNNING'],
                nodeTypeMap: {
                    'CC': 'CC',
                    'CMDB': 'CMDB',
                    'BCSVM': 'BCS虚拟机',
                    'THIRDPARTY': '第三方构建机',
                    'DEVCLOUD': '腾讯自研云（云devnet资源）',
                    'TSTACK': 'TStack虚拟机',
                    'OTHER': '其他',
                    'UNKNOWN': '未知'
                },
                nodeStatusMap: {
                    'NORMAL': '正常',
                    'ABNORMAL': '异常',
                    'DELETED': '已删除',
                    'LOST': '失联',
                    'CREATING': '正在创建中',
                    'RUNNING': '安装Agent',
                    'STARTING': '正在开机中',
                    'STOPPING': '正在关机中',
                    'STOPPED': '已关机',
                    'RESTARTING': '正在重启中',
                    'DELETING': '正在销毁中',
                    'BUILDING_IMAGE': '正在制作镜像中',
                    'BUILD_IMAGE_SUCCESS': '制作镜像成功',
                    'BUILD_IMAGE_FAILED': '制作镜像失败',
                    'UNKNOWN': '未知'
                },
                isShowDelete: false,
                isDeleteing: false,
                deleteRow: {}
            }
        },

        computed: {
            ...mapState(['appHeight', 'projectId']),

            tableHeight () {
                return Math.min(this.appHeight - 152, 43 + (this.agentList.length || 3) * 42)
            }
        },

        created () {
            this.getNodeList()
        },

        methods: {
            getNodeList () {
                this.isLoading = true
                setting.getNodeList(this.projectId).then((res) => {
                    this.agentList = res
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                })
            },

            showDelete (row) {
                this.isShowDelete = true
                this.deleteRow = row
            },

            deleteAgent () {
                this.isDeleteing = true
                setting.deleteNode(this.projectId, [this.deleteRow.nodeHashId]).then(() => {
                    this.isShowDelete = false
                    this.getNodeList()
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isDeleteing = false
                })
            },

            goToAddAgent () {
                this.$router.push({
                    name: 'addAgent'
                })
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .agent-list-head {
        height: 49px;
        line-height: 49px;
        background: #fff;
        box-shadow: 0 2px 5px 0 rgba(51,60,72,0.03);
        padding: 0 25.5px;
    }
    .agent-list-main {
        padding: 20px 24px;
        .add-agent {
            margin-bottom: 20px;
        }
    }
    .agent-table {
        .prompt-operator,
        .edit-operator {
            padding-right: 10px;
            color: #ffbf00;
            cursor: pointer;
            .bk-icon {
                margin-right: 6px;
            }
        }
        .node-status-icon {
            display: inline-block;
            margin-left: 2px;
            width: 10px;
            height: 10px;
            border: 2px solid #30D878;
            border-radius: 50%;
            -webkit-border-radius: 50%;
        }
    }
</style>
