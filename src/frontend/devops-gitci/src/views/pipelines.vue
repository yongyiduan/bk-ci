<template>
    <article class="pipelines-home">
        <aside class="aside-nav">
            <h3 class="nav-title">
                Pipelines
                <i class="bk-icon icon-plus"></i>
            </h3>

            <ul>
                <li v-for="pipeline in pipelineList"
                    :key="pipeline.name"
                    @click="choosePipeline(pipeline)"
                    :class="{ 'nav-item': true, active: curPipeline.id === pipeline.id }"
                >
                    <icon :name="pipeline.icon || 'pipeline'" size="20"></icon>
                    {{ pipeline.name }}
                </li>
            </ul>
        </aside>

        <main class="pipelines-main">
            <header class="main-head">
                <span>
                    <span class="pipeline-name">{{ curPipeline.name }}</span>
                    <span class="yml-name">.ci/frontend-ci.yml</span>
                    <icon name="cc-jump-link" size="16"></icon>
                    <span class="pipeline-status">已禁用</span>
                </span>
                <opt-menu>
                    <li>触发构建</li>
                    <li>禁用流水线</li>
                </opt-menu>
            </header>

            <section class="main-body">
                <section class="build-filter">
                    <bk-select v-model="filterData[key]" v-for="(list, key) in filterList" :key="key" class="filter-select" :placeholder="key" multiple>
                        <bk-option v-for="option in list"
                            :key="option.id"
                            :id="option.id"
                            :name="option.name">
                        </bk-option>
                    </bk-select>
                    <bk-button @click="resetFilter">重置</bk-button>
                </section>

                <bk-table :data="buildList"
                    :header-cell-style="{ background: '#fafbfd' }"
                    :height="Math.min(appHeight - 331, 43 + buildList.length * 72)"
                    class="build-table"
                    size="large"
                >
                    <bk-table-column label="Commit message">
                        <template slot-scope="props">
                            <section class="commit-message">
                                <i class="bk-icon icon-check-1"></i>
                                <p>
                                    <span class="message">feat: dockerhost-buildImage支持多个tag，支持--target</span>
                                    <span class="info">#126：Merge requests [!{{props.row}}] opened by xxx</span>
                                </p>
                            </section>
                        </template>
                    </bk-table-column>
                    <bk-table-column label="Branch" prop="branch"></bk-table-column>
                    <bk-table-column label="Consume">
                        <template slot-scope="props">
                            <p class="consume">
                                <span class="consume-item"><i class="bk-icon icon-clock"></i>38 seconds</span>
                                <span class="consume-item"><i class="bk-icon icon-calendar"></i>{{props.row}} days ago</span>
                            </p>
                        </template>
                    </bk-table-column>
                    <bk-table-column label="操作" width="150" class-name="handler-btn">
                        <template slot-scope="props">
                            <opt-menu>
                                <li @click="goToAgentDetail(props.row)">Agent Detail</li>
                                <li>删除</li>
                            </opt-menu>
                        </template>
                    </bk-table-column>
                </bk-table>
                <bk-pagination small
                    :current.sync="compactPaging.current"
                    :count.sync="compactPaging.count"
                    :limit="compactPaging.limit"
                    :show-limit="false"
                    class="build-paging"
                />
            </section>
        </main>
    </article>
</template>

<script>
    import optMenu from '@/components/opt-menu'
    import { mapGetters } from 'vuex'

    export default {
        components: {
            optMenu
        },

        data () {
            return {
                pipelineList: [
                    { name: 'All pipeline', id: 0, icon: 'all' },
                    { name: 'Codecc check', id: 1 },
                    { name: 'Backend CI', id: 2 },
                    { name: 'Forntend CI', id: 3 }
                ],
                buildList: [1, 2, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1],
                compactPaging: {
                    limit: 10,
                    current: 1,
                    count: 100
                },
                filterData: {
                    commit: [],
                    actor: [],
                    branch: [],
                    event: [],
                    status: []
                },
                filterList: {
                    commit: [],
                    actor: [],
                    branch: [],
                    event: [],
                    status: []
                },
                curPipeline: { name: 'All pipeline', id: 0, icon: 'all' }
            }
        },

        computed: {
            ...mapGetters(['appHeight'])
        },

        methods: {
            choosePipeline (pipeline) {
                this.curPipeline = pipeline
            },

            goToAgentDetail (row) {
                console.log(row)
            },

            resetFilter () {
                this.filterData = {
                    commit: [],
                    actor: [],
                    branch: [],
                    event: [],
                    status: []
                }
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .pipelines-home {
        display: flex;
        height: 100%;
    }
    .nav-title {
        justify-content: space-between;
        padding-left: 17px;
        .bk-icon {
            font-size: 30px;
        }
    }
    .pipelines-main {
        width: calc(100vw - 240px);
        height: 100%;
        background: #f5f6fa;
        padding: 20px 25px 36px;
        .main-head {
            height: 64px;
            background: #fff;
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 27px;
            line-height: 20px;
            .pipeline-name {
                color: #313328;
            }
            .yml-name {
                display: inline-block;
                margin: 0 3px 0 16px;
            }
            svg {
                vertical-align: sub;
                cursor: pointer;
            }
            .pipeline-status {
                background: #fafbfd;
                border: 1px solid rgba(151,155,165,0.30);
                border-radius: 11px;
                display: inline-block;
                margin-left: 16px;
                padding: 0 11px;
                line-height: 20px;
                font-size: 12px;
            }
        }

        .main-body {
            margin-top: 18px;
            height: calc(100% - 138px);
            background: #fff;
            padding: 16px 24px 22px;
            .build-filter {
                display: flex;
                align-items: center;
                margin-bottom: 17px;
                .filter-select {
                    width: 160px;
                    margin-right: 8px;
                }
            }
            .build-paging {
                margin: 12px 0 0;
                display: flex;
                align-items: center;
                justify-content: center;
                /deep/ span {
                    outline: none;
                    margin-left: 0;
                }
            }
        }
    }
    .build-table {
        .commit-message {
            display: flex;
            align-items: top;
            font-size: 12px;
            .bk-icon {
                font-size: 32px;
                color: #2dcb56;
                margin-right: 8px;
            }
            .message {
                display: block;
                color: #313328;
                line-height: 24px;
                margin-bottom: 4px;
            }
            .info {
                color: #979ba5;
                line-height: 16px;
            }
        }
        .consume-item {
            display: flex;
            align-items: center;
            font-size: 12px;
            &:first-child {
                margin-bottom: 7px;
            }
            .bk-icon {
                font-size: 14px;
                margin-right: 5px;
            }
            .icon-clock {
                font-size: 15px;
            }
        }
        /deep/ .handler-btn {
            .cell {
                overflow: visible;
            }
        }
    }
</style>
