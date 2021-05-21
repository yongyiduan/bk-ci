<template>
    <article class="credential-home" v-bkloading="{ isLoading }">
        <header class="home-header">
            <bk-button theme="primary" @click="addCredential">新增凭据</bk-button>
        </header>

        <bk-table :data="credentialList"
            :pagination="pagination"
            :outer-border="false"
            :header-border="false"
            :header-cell-style="{ background: '#fafbfd' }"
            :height="tableHeight"
            @page-change="pageChange"
            @page-limit-change="pageLimitChange"
            class="credential-table"
        >
            <bk-table-column label="凭证名称" prop="credentialId"></bk-table-column>
            <bk-table-column label="别名" prop="credentialName"></bk-table-column>
            <bk-table-column label="类型" prop="credentialType"></bk-table-column>
            <bk-table-column label="描述" prop="credentialRemark"></bk-table-column>
            <bk-table-column label="操作" width="150" class-name="handler-btn">
                <template slot-scope="props">
                    <span :class="{ 'update-btn': true, disabled: !props.row.permissions.edit }" @click="editCredential(props.row)">编辑</span>
                    <span :class="{ 'update-btn': true, disabled: !props.row.permissions.delete }" @click="deleteCredential(props.row)">删除</span>
                </template>
            </bk-table-column>
        </bk-table>

        <edit-credential :show="show" :form="form" @hidden="hidden" @success="successOpt"></edit-credential>

        <bk-dialog v-model="deleteObj.show"
            :mask-close="false"
            :loading="isDelLoading"
            theme="danger"
            header-position="left"
            title="删除"
            @confirm="requestDelete">
            确定删除【{{deleteObj.id}}】？
        </bk-dialog>
    </article>
</template>

<script>
    import { mapState } from 'vuex'
    import { setting } from '@/http'
    import EditCredential from '@/components/setting/edit-credential'

    export default {
        components: {
            EditCredential
        },

        data () {
            return {
                credentialList: [],
                pagination: {
                    count: 0,
                    current: 1,
                    limit: 10
                },
                form: {},
                show: false,
                deleteObj: {
                    show: false
                },
                isLoading: false,
                isDelLoading: false
            }
        },

        computed: {
            ...mapState(['appHeight', 'projectId']),

            tableHeight () {
                return Math.min(this.appHeight - 157, 106 + (this.credentialList.length || 3) * 42)
            }
        },

        created () {
            this.getTicketList()
        },

        methods: {
            getTicketList () {
                const params = {
                    page: this.pagination.current,
                    pageSize: this.pagination.limit
                }
                this.isLoading = true
                setting.getTicketList(this.projectId, params).then((res) => {
                    const data = res || {}
                    this.pagination.count = data.count || 0
                    this.credentialList = data.records || []
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                })
            },

            requestDelete () {
                this.isDelLoading = true
                setting.deleteTicket(this.projectId, this.deleteObj.id).then(() => {
                    this.getTicketList()
                    this.deleteObj.show = false
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isDelLoading = false
                })
            },

            pageChange (current) {
                if (current === this.pagination.current) return
                this.pagination.current = current
                this.getTicketList()
            },

            pageLimitChange (limit) {
                if (limit === this.pagination.limit) return
                this.pagination.current = 1
                this.pagination.limit = limit
                this.getTicketList()
            },

            editCredential (row) {
                if (!row.permissions.edit) return
                this.form = row
                this.show = true
            },

            deleteCredential (row) {
                if (!row.permissions.delete) return
                this.deleteObj.show = true
                this.deleteObj.id = row.credentialId
            },

            addCredential () {
                this.show = true
            },

            successOpt () {
                this.hidden()
                this.getTicketList()
            },

            hidden () {
                this.show = false
                this.form = {}
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .credential-home {
        padding: 20px 24px;
    }
    .home-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        margin-bottom: 20px;
    }
</style>
