<template>
    <article class="task-list-home">
        <header class="task-head">
            <bk-button theme="primary" @click="addTask">新增方案</bk-button>
            <span class="g-accelerate-gray-font task-head-title">共 {{ turboPlanCount }} 个方案</span>
        </header>

        <main v-bkloading="{ isLoading }">
            <section v-for="task in taskList" :key="task" class="g-accelerate-box task-card">
                <span :class="['open-icon', { 'is-open': task.topStatus === 'true' }]" @click="modifyTurboPlanTopStatus(task)">
                    <logo name="check-1" class="icon-check"></logo>
                </span>

                <h3 class="card-head" @click="toggleShowCard(task)">
                    <p class="task-name">
                        <span class="g-accelerate-deep-black-font name-desc"><span class="g-accelerate-text-overflow">{{ task.planName }}</span><span class="name-detail" @click="$router.push({ name: 'taskDetail', params: { id: task.planId } })">{{ task.engineCode }}</span></span>
                        <span class="g-accelerate-gray-font name-hash g-accelerate-text-overflow">{{ task.planId }}</span>
                    </p>
                    <span class="task-line"></span>
                    <p class="task-rate">
                        <span class="rate-num g-accelerate-deep-black-font">{{ task.instanceNum }}</span>
                        <span class="rate-title g-accelerate-gray-font">实例数</span>
                    </p>
                    <p class="task-rate">
                        <span class="rate-num g-accelerate-deep-black-font">{{ task.executeCount }}</span>
                        <span class="rate-title g-accelerate-gray-font">加速次数</span>
                    </p>
                    <p class="task-rate">
                        <span class="rate-num g-accelerate-deep-black-font">{{ task.estimateTimeHour }}</span>
                        <span class="rate-title g-accelerate-gray-font">未加速耗时(h)</span>
                    </p>
                    <p class="task-rate">
                        <span class="rate-num g-accelerate-deep-black-font">{{ task.executeTimeHour }}</span>
                        <span class="rate-title g-accelerate-gray-font">实际耗时(h)</span>
                    </p>
                    <p class="task-rate">
                        <span class="rate-num g-accelerate-deep-black-font">{{ task.turboRatio }}</span>
                        <span class="rate-title g-accelerate-gray-font">节省率</span>
                    </p>
                    <logo name="right-shape" size="16" :class="showIds.includes(task.planId) ? 'task-right-down task-right-shape' : 'task-right-shape'"></logo>
                </h3>

                <transition name="fade">
                    <bk-table class="task-records g-accelerate-scroll-table" v-if="showIds.includes(task.planId)"
                        :data="task.tableList"
                        :outer-border="false"
                        :header-border="false"
                        :header-cell-style="{ background: '#f5f6fa' }"
                        :pagination="task.pagination"
                        v-bkloading="{ isLoading: task.loading }"
                        @page-change="pageChanged"
                        @page-limit-change="pageLimitChange"
                    >
                        <bk-table-column label="流水线/构建机" prop="pipelineName"></bk-table-column>
                        <bk-table-column label="加速次数" prop="executeCount"></bk-table-column>
                        <bk-table-column label="平均耗时" prop="averageExecuteTimeSecond"></bk-table-column>
                        <bk-table-column label="节省率" prop="turboRatio"></bk-table-column>
                        <bk-table-column label="最新开始时间" prop="latestStartTime"></bk-table-column>
                        <bk-table-column label="最新状态" prop="latestStatus"></bk-table-column>
                    </bk-table>
                </transition>
            </section>
        </main>
    </article>
</template>

<script>
    import { getPlanList, getPlanInstanceDetail, modifyTurboPlanTopStatus } from '@/api'
    import logo from '../../components/logo'

    export default {
        components: {
            logo
        },

        data () {
            return {
                taskList: [],
                showIds: [],
                pageNum: 1,
                loadEnd: false,
                isLoadingMore: false,
                turboPlanCount: 0,
                isLoading: false
            }
        },

        computed: {
            projectId () {
                return this.$route.params.projectId
            }
        },

        created () {
            this.isLoading = true
            this.getPlanList().finally(() => {
                this.isLoading = false
            })
        },

        mounted () {
            const scrollMain = document.querySelector('.task-list-home')
            if (scrollMain) {
                scrollMain.addEventListener('scroll', this.scrollLoadMore, { passive: true })
                this.$once('hook:beforeDestroy', () => {
                    scrollMain.removeEventListener('scroll', this.scrollLoadMore, { passive: true })
                })
            }
        },

        methods: {
            modifyTurboPlanTopStatus (row) {
                const topStatus = row.topStatus === 'true' ? 'false' : 'true'
                modifyTurboPlanTopStatus(row.planId, topStatus).then(() => {
                    row.topStatus = topStatus
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                })
            },

            getPlanList () {
                this.isLoadingMore = true
                return getPlanList(this.projectId, this.pageNum).then((res) => {
                    this.taskList = (res.turboPlanList || []).map((item) => {
                        item.pagination = { current: 1, count: 1, limit: 10 }
                        item.tableList = []
                        item.loading = false
                        return item
                    })
                    this.turboPlanCount = res.turboPlanCount
                    this.loadEnd = (this.pageNum * 40) >= res.turboPlanCount
                    this.pageNum++
                    if (res.turboPlanCount <= 0) this.$router.replace({ name: 'taskInit' })
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoadingMore = false
                })
            },

            toggleShowCard (task) {
                const index = this.showIds.findIndex(x => x === task.planId)
                if (index > -1) {
                    this.showIds.splice(index, 1)
                } else {
                    this.getPlanInstanceDetail(task)
                    this.showIds.push(task.planId)
                }
            },

            getPlanInstanceDetail (task) {
                const pagination = task.pagination || {}
                task.loading = true
                getPlanInstanceDetail(task.planId, pagination.current, pagination.limit).then((res) => {
                    task.tableList = res.records || []
                    task.pagination.count = res.count
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    task.loading = false
                })
            },

            pageChanged (page) {
                console.log(page)
            },

            pageLimitChange (currentLimit, prevLimit) {
                console.log(currentLimit, prevLimit)
            },

            addTask () {
                this.$router.push({
                    name: 'taskCreate'
                })
            },

            scrollLoadMore (event) {
                const target = event.target
                const bottomDis = target.scrollHeight - target.clientHeight - target.scrollTop
                if (bottomDis <= 500 && !this.loadEnd && !this.isLoadingMore) this.getPlanList()
            }
        }
    }
</script>

<style lang="scss" scoped>
    @import '../../assets/scss/var.scss';

    .task-list-home {
        padding: 20px;
        margin: 0 auto;
        .task-head {
            margin-bottom: 20px;
        }
        .task-head-title {
            margin-left: 14px;
            font-size: 12px;
            line-height: 32px;
        }
    }
    .task-card {
        margin-bottom: 10px;
        cursor: pointer;
        position: relative;
        overflow: hidden;
        .open-icon {
            position: absolute;
            left: -16px;
            top: -16px;
            width: 33px;
            height: 33px;
            transform: rotate(45deg);
            background: #DCDEE5;
            &.is-open {
                background: #3a84ff;
            }
            .icon-check {
                color: #fff;
                position: absolute;
                right: 0;
                top: 11px;
                transform: rotate(-45deg);
            }
        }
        .card-head {
            border-bottom: 1px solid #f0f1f5;
            display: flex;
            align-items: center;
            padding: 0 28px 0 43px;
            font-weight: normal;
            height: round(110px * $designToPx);
        }
        .task-name {
            flex: 1;
            max-width: calc(100% - 380px - 3.76rem);
            .name-desc {
                line-height: 22px;
                margin-bottom: 4px;
                display: flex;
                align-items: center;
            }
            .name-hash {
                font-size: 12px;
                line-height: 20px;
            }
            .name-detail {
                background: #e1ecff;
                color: #3a84ff;
                border-radius: 2px;
                padding: 0 9px;
                font-size: 12px;
                line-height: 20px;
                margin: 0 6px;
            }
        }
        .task-line {
            width: 1px;
            height: round(54px * $designToPx);
            background: #e7e8ed;
            margin-right: .52rem;
        }
        .task-rate {
            width: 80px;
            margin-right: .7rem;
            text-align: center;
            &:nth-child(7) {
                margin-right: .36rem;
            }
            .rate-num {
                display: block;
                font-size: 24px;
                line-height: 32px;
                margin-bottom: 2px;
            }
            .rate-title {
                line-height: 20px;
                font-size: 12px;
            }
        }
        .task-right-shape {
            transition: 200ms transform;
            transform: rotate(-90deg);
            color: #979ba5;
            &.task-right-down {
                transform: rotate(90deg);
            }
        }
        .task-records {
            margin: 19px 28px 0;
            padding-bottom: 27px;
            width: calc(100% - 56px);
        }
    }
</style>
