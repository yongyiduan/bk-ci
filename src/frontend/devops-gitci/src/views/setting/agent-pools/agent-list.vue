<template>
    <article class="agent-list-home">
        <header class="agent-list-head">
            <bk-breadcrumb separator-class="bk-icon icon-angle-right">
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
                v-if="agentList.length"
            >
                <bk-table-column label="Name" prop="displayName"></bk-table-column>
                <bk-table-column label="IP" prop="ip"></bk-table-column>
                <bk-table-column label="Operating System" prop="osName"></bk-table-column>
                <bk-table-column label="Source/Importer">
                    <template slot-scope="props">
                        <div v-if="(props.row.nodeType === 'CC' || props.row.nodeType === 'CMDB') && ((props.row.nodeType === 'CC' && props.row.createdUser !== props.row.operator && props.row.createdUser !== props.row.bakOperator)
                            || (props.row.nodeType === 'CMDB' && props.row.createdUser !== props.row.operator && props.row.bakOperator.split(';').indexOf(props.row.createdUser) === -1))">
                            <div class="edit-operator" v-if="userInfo.username === props.row.operator || userInfo.username === props.row.bakOperator">
                                <i class="bk-icon icon-exclamation-circle"></i>
                            </div>
                            <div class="prompt-operator" v-else>
                                <bk-popover placement="top">
                                    <span><i class="bk-icon icon-exclamation-circle"></i>The person in charge has been changed and the use is prohibited</span>
                                    <template slot="content">
                                        <p>Current importer：<span>{{ props.row.createdUser }}</span></p>
                                        <p>Current host owner：<span>{{ props.row.operator }}</span><span v-if="props.row.nodeType === 'CC'">/{{ props.row.bakOperator }}</span></p>
                                        <p>Please contact the person in charge of the host</p>
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
                <bk-table-column label="Status" prop="osName">
                    <template slot-scope="props">
                        <div class="table-node-item node-item-status"
                            v-if="props.row.nodeStatus === 'BUILDING_IMAGE' && props.row.nodeType === 'DEVCLOUD'">
                            <span class="node-status-icon normal-stutus-icon"></span>
                            <span class="node-status">normal</span>
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
                <bk-table-column label="Creation/import time" prop="createTime"></bk-table-column>
                <bk-table-column label="Last Modified" prop="lastModifyTime"></bk-table-column>
                <bk-table-column label="Operation" width="150" class-name="handler-btn">
                    <template slot-scope="props">
                        <span class="update-btn" @click="showDelete(props.row)">Delete</span>
                    </template>
                </bk-table-column>
            </bk-table>
            <section v-else class="table-empty">
                <h3>Import your first agent</h3>
                <h5>Agent can be yourself development machine, or the compile machine of your team</h5>
                <bk-button theme="primary" @click="goToAddAgent">Add Self-hosted agent</bk-button>
            </section>
        </main>

        <bk-dialog v-model="isShowDelete"
            theme="danger"
            :mask-close="false"
            :loading="isDeleteing"
            @confirm="deleteAgent"
            title="Delete">
            Are you sure to delete【{{deleteRow.displayName}}】？
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
                    { link: '', title: this.$route.params.poolName }
                ],
                agentList: [],
                isLoading: false,
                runningStatus: ['CREATING', 'STARTING', 'STOPPING', 'RESTARTING', 'DELETING', 'BUILDING_IMAGE'],
                successStatus: ['NORMAL', 'BUILD_IMAGE_SUCCESS'],
                failStatus: ['ABNORMAL', 'DELETED', 'LOST', 'BUILD_IMAGE_FAILED', 'UNKNOWN', 'RUNNING'],
                nodeTypeMap: {
                    'CC': 'CC',
                    'CMDB': 'CMDB',
                    'BCSVM': 'BCS virtual machine',
                    'THIRDPARTY': 'Third-party build machine',
                    'DEVCLOUD': 'Tencent self-developed cloud (cloud devnet resource)',
                    'TSTACK': 'TStack virtual machine',
                    'OTHER': 'Other',
                    'UNKNOWN': 'Unknow'
                },
                nodeStatusMap: {
                    'NORMAL': 'normal',
                    'ABNORMAL': 'abnormal',
                    'DELETED': 'deleted',
                    'LOST': 'lost',
                    'CREATING': 'creating',
                    'RUNNING': 'Install Agent',
                    'STARTING': 'Booting up',
                    'STOPPING': 'Shutting down',
                    'STOPPED': 'Shut down',
                    'RESTARTING': 'Restarting',
                    'DELETING': 'Being destroyed',
                    'BUILDING_IMAGE': 'Mirroring in progress',
                    'BUILD_IMAGE_SUCCESS': 'Successfully made the mirror',
                    'BUILD_IMAGE_FAILED': 'Mirroring failed',
                    'UNKNOWN': 'Unknow'
                },
                isShowDelete: false,
                isDeleteing: false,
                deleteRow: {}
            }
        },

        computed: {
            ...mapState(['appHeight', 'projectId']),

            tableHeight () {
                return Math.min(this.appHeight - 152, 43 + (this.agentList.length || 4) * 42)
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
