<template>
    <article class="history-list-home">
        <main class="g-accelerate-box history-list-main">
            <header class="filter-area">
                <bk-select multiple
                    :value="turboPlanId"
                    @change="(options) => chooseFilter('turboPlanId', options)"
                    searchable
                    show-select-all
                    class="single-width"
                    :loading="isLoadingSearch"
                >
                    <bk-option v-for="(value, key) in planInfo"
                        :key="key"
                        :id="key"
                        :name="value">
                    </bk-option>
                </bk-select>
                <bk-select multiple
                    :value="pipelineId"
                    @change="(options) => chooseFilter('pipelineId', options)"
                    searchable
                    show-select-all
                    class="single-width"
                    :loading="isLoadingSearch"
                >
                    <bk-option v-for="(value, key) in pipelineInfo"
                        :key="key"
                        :id="key"
                        :name="value">
                    </bk-option>
                </bk-select>
                <bk-select multiple
                    :value="status"
                    @change="(options) => chooseFilter('status', options)"
                    searchable
                    show-select-all
                    class="single-width"
                    :loading="isLoadingSearch"
                >
                    <bk-option v-for="(value, key) in statusInfo"
                        :key="key"
                        :id="key"
                        :name="value">
                    </bk-option>
                </bk-select>
                <bk-date-picker class="single-width" :placeholder="'选择日期范围'" type="daterange" :value="timeRange" @change="(options) => chooseFilter('timeRange', options)"></bk-date-picker>
                <bk-button @click="clearFilter" class="clear-btn">重置</bk-button>
            </header>

            <bk-table class="history-records g-accelerate-scroll-table"
                :data="historyList"
                :outer-border="false"
                :header-border="false"
                :header-cell-style="{ background: '#f5f6fa' }"
                :pagination="pagination"
                v-bkloading="{ isLoading }"
                @page-change="pageChanged"
                @page-limit-change="pageLimitChange"
                @row-click="goToDetail"
                @sort-change="sortChange"
            >
                <bk-table-column label="编号" type="index" width="60"></bk-table-column>
                <bk-table-column label="流水线/构建机" prop="pipeline_name" sortable>
                    <template slot-scope="props">
                        <span>{{ props.row.pipelineName || props.row.clientIp }}</span>
                    </template>
                </bk-table-column>
                <bk-table-column label="未加速耗时" prop="estimateTimeValue" sortable></bk-table-column>
                <bk-table-column label="实际耗时" prop="executeTimeValue" sortable></bk-table-column>
                <bk-table-column label="节省率" prop="turboRatio" sortable></bk-table-column>
                <bk-table-column label="开始时间" prop="startTime" sortable></bk-table-column>
                <bk-table-column label="状态" prop="status" sortable>
                    <template slot-scope="props">
                        <task-status :status="props.row.status"></task-status>
                    </template>
                </bk-table-column>
            </bk-table>
        </main>
    </article>
</template>

<script>
    import { getHistoryList, getHistorySearchList } from '@/api'
    import taskStatus from '../../components/task-status.vue'

    export default {
        components: {
            taskStatus
        },

        data () {
            return {
                pagination: {
                    current: 1,
                    count: 1,
                    limit: 10
                },
                historyList: [],
                turboPlanId: [],
                pipelineId: [],
                status: [],
                startTime: '',
                endTime: '',
                timeRange: [],
                isLoading: false,
                isLoadingSearch: false,
                planInfo: {},
                pipelineInfo: {},
                statusInfo: {},
                sortField: undefined,
                sortType: undefined
            }
        },

        created () {
            this.initFilter()
            this.getHistoryList()
            this.getHistorySearchList()
        },

        methods: {
            sortChange (sort) {
                const sortMap = {
                    ascending: 'ASC',
                    descending: 'DESC'
                }
                this.sortField = sort.prop
                this.sortType = sortMap[sort.order]
                this.getHistoryList()
            },

            initFilter () {
                const query = this.$route.query || {}
                if (query.planId) this.turboPlanId = [query.planId]
                if (query.id) this.pipelineId = [query.id]
            },

            getHistorySearchList () {
                const projectId = this.$route.params.projectId
                this.isLoadingSearch = true
                getHistorySearchList(projectId).then((res) => {
                    this.planInfo = res.planInfo || {}
                    this.pipelineInfo = res.pipelineInfo || {}
                    this.statusInfo = res.statusInfo || {}
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoadingSearch = false
                })
            },

            getHistoryList () {
                this.isLoading = true
                const postData = {
                    startTime: this.startTime,
                    endTime: this.endTime,
                    pipelineId: this.pipelineId,
                    status: this.status,
                    turboPlanId: this.turboPlanId,
                    projectId: this.$route.params.projectId
                }
                const queryData = {
                    pageNum: this.pagination.current,
                    pageSize: this.pagination.limit,
                    sortField: this.sortField,
                    sortType: this.sortType
                }
                getHistoryList(queryData, postData).then((res = {}) => {
                    this.historyList = res.records || []
                    this.pagination.count = res.count
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                })
            },

            clearFilter () {
                this.turboPlanId = []
                this.pipelineId = []
                this.status = []
                this.timeRange = []
                this.getHistoryList()
            },

            chooseFilter (key, value) {
                this[key] = value
                this.startTime = this.timeRange[0]
                this.endTime = this.timeRange[1]
                this.getHistoryList()
            },

            goToDetail (row) {
                this.$router.push({
                    name: 'historyDetail',
                    params: {
                        id: row.id
                    }
                })
            },

            pageChanged (page) {
                if (page) this.pagination.current = page
                this.getHistoryList()
            },

            pageLimitChange (currentLimit) {
                if (currentLimit === this.pagination.limit) return

                this.pagination.current = 1
                this.pagination.limit = currentLimit
                this.getHistoryList()
            }
        }
    }
</script>

<style lang="scss" scoped>
    .history-list-home {
        padding: 20px;
        margin: 0 auto;
    }
    .history-list-main {
        padding: 20px;
    }
    .filter-area {
        display: flex;
        align-items: center;
        margin-bottom: 20px;
    }
    .single-width {
        width: 1.82rem;
        margin-right: 8px;
    }
    .clear-btn {
        margin-left: 8px;
    }
    /deep/ .bk-table-row {
        cursor: pointer;
    }
</style>
