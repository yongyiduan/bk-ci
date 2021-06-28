<template>
    <article class="agent-list-home">
        <header class="agent-list-head">
            <bk-breadcrumb separator-class="bk-icon icon-angle-right">
                <bk-breadcrumb-item v-for="(item,index) in navList" :key="index" :to="item.link">{{item.title}}</bk-breadcrumb-item>
            </bk-breadcrumb>
        </header>

        <main class="agent-list-main">
            <section>
                <bk-button theme="primary" class="add-agent" @click="goToAddAgent" v-if="agentList.length">Add Self-hosted agent</bk-button>
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
                <bk-table-column label="Display Name" prop="displayName">
                    <template slot-scope="props">
                        <span class="update-btn" @click="goToAgentDetail(props.row.nodeHashId)">{{ props.row.displayName}}</span>
                    </template>
                </bk-table-column>
                <bk-table-column label="OS" prop="osName"></bk-table-column>
                <bk-table-column label="Status" prop="nodeStatus"></bk-table-column>
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
