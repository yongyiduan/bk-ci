<template>
    <article class="agent-list-home" v-bkloading="{ isLoading }">
        <header class="agent-list-head">
            <bk-breadcrumb separator-class="bk-icon icon-angle-right">
                <bk-breadcrumb-item v-for="(item,index) in navList" :key="index" :to="item.link">{{item.title}}</bk-breadcrumb-item>
            </bk-breadcrumb>
        </header>

        <main class="agent-list-main" v-if="!isLoading">
            <section v-if="agentList.length">
                <div class="operate-agent">
                    <bk-button theme="primary" @click="goToAddAgent">Add Self-hosted agent</bk-button>
                    <bk-button theme="primary" class="import-agent" @click="importNewNode">Import Self-hosted agent</bk-button>
                </div>
                <bk-table class="agent-table"
                    :data="agentList"
                    :outer-border="false"
                    :header-border="false"
                    :header-cell-style="{ background: '#fafbfd' }"
                    :height="tableHeight"
                >
                    <bk-table-column label="Display Name" prop="displayName">
                        <template slot-scope="props">
                            <span class="update-btn" @click="goToAgentDetail(props.row.nodeHashId)">{{ props.row.displayName}}</span>
                        </template>
                    </bk-table-column>
                    <bk-table-column label="hostName" prop="name"></bk-table-column>
                    <bk-table-column label="Ip" prop="ip"></bk-table-column>
                    <bk-table-column label="OS" prop="osName"></bk-table-column>
                    <bk-table-column label="Status" prop="nodeStatus"></bk-table-column>
                    <bk-table-column label="Operation" width="200" class-name="handler-btn">
                        <template slot-scope="props">
                            <span class="update-btn" @click="showDelete(props.row)">Remove from the pool</span>
                        </template>
                    </bk-table-column>
                </bk-table>
            </section>
            <section v-else class="table-empty">
                <h3>Import your first agent</h3>
                <h5>Agent can be yourself development machine, or the compile machine of your team</h5>
                <div>
                    <bk-button theme="primary" @click="goToAddAgent">Add Self-hosted agent</bk-button>
                    <bk-button theme="primary" class="import-agent" @click="importNewNode">Import Self-hosted agent</bk-button>
                </div>
            </section>
        </main>

        <node-select :node-select-conf="nodeSelectConf"
            :row-list="importNodeList"
            :select-handlerc-conf="selectHandlercConf"
            :confirm-fn="confirmFn"
            :toggle-all-select="toggleAllSelect"
            :loading="nodeDialogLoading"
            :cancel-fn="cancelFn"
            :query="query">
        </node-select>

        <bk-dialog v-model="isShowDelete"
            theme="danger"
            :mask-close="false"
            :loading="isDeleteing"
            @confirm="deleteAgent"
            title="Remove from the pool">
            Are you sure to remove 【{{deleteRow.displayName}}】？
        </bk-dialog>
    </article>
</template>

<script>
    import { mapState } from 'vuex'
    import { setting } from '@/http'
    import nodeSelect from '@/components/setting/node-select-dialog'
    import nodeSelectMixin from '@/components/setting/node-select-mixin.js'

    export default {
        components: {
            nodeSelect
        },

        mixins: [nodeSelectMixin],

        data () {
            return {
                navList: [
                    { link: { name: 'agentPools' }, title: 'Agent Pools' },
                    { link: '', title: this.$route.params.poolName }
                ],
                agentList: [],
                isLoading: false,
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
                setting.getNodeList(this.projectId, this.$route.params.poolId).then((res) => {
                    this.agentList = res
                    this.nodeList = res
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
                setting.deleteEnvNode(this.projectId, this.$route.params.poolId, [this.deleteRow.nodeHashId]).then(() => {
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
            },

            goToAgentDetail (id) {
                this.$router.push({
                    name: 'agentDetail',
                    params: {
                        agentId: id
                    }
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
        padding: 16px;
        .operate-agent {
            margin-bottom: 20px;
        }
        .import-agent {
            margin-left: 10px;
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
