<template>
    <article class="pipelines-main">
        <header class="main-head section-box">
            <span class="head-text">
                <span class="pipeline-name text-ellipsis" v-bk-overflow-tips>{{ curPipeline.displayName }}</span>
                <span class="yml-name text-ellipsis" @click="goToGit" v-if="curPipeline.filePath">
                    <span v-bk-overflow-tips>{{ curPipeline.filePath }}</span>
                    <icon name="cc-jump-link" size="16"></icon>
                </span>
                <span class="pipeline-status" v-if="!curPipeline.enabled">Disabled</span>
            </span>
            <opt-menu v-if="curPipeline.pipelineId">
                <li @click="showTriggleBuild">Trigger build</li>
                <li @click="togglePipelineEnable">{{ curPipeline.enabled ? 'Disable pipeline' : 'Enable pipeline' }}</li>
            </opt-menu>
        </header>

        <section class="main-body section-box">
            <section class="build-filter">
                <bk-input v-model="filterData.commitMsg" class="filter-item" placeholder="Commit message"></bk-input>
                <bk-input v-model="filterData.actor" class="filter-item" placeholder="Actor"></bk-input>
                <bk-select v-model="filterData[filter.id]" v-for="filter in filterList" :key="filter.id" class="filter-item" :placeholder="filter.placeholder" multiple :loading="isLoadingFilter">
                    <bk-option v-for="option in filter.data"
                        :key="option.id"
                        :id="option.id"
                        :name="option.name">
                    </bk-option>
                </bk-select>
                <bk-button @click="resetFilter">Reset</bk-button>
            </section>

            <bk-table :data="buildList"
                :header-cell-style="{ background: '#fafbfd' }"
                :height="tableHeight"
                v-bkloading="{ isLoading }"
                @row-click="goToBuildDetail"
                class="build-table"
                size="large"
            >
                <bk-table-column label="Commit message">
                    <template slot-scope="props">
                        <section class="commit-message">
                            <i :class="getIconClass(props.row.buildHistory.status)"></i>
                            <p>
                                <span class="message">{{ props.row.gitRequestEvent.commitMsg }}</span>
                                <span class="info">#{{ props.row.buildHistory.buildNum }}：{{ getCommitDesc(props.row) }}</span>
                            </p>
                        </section>
                    </template>
                </bk-table-column>
                <bk-table-column label="Branch" width="200">
                    <template slot-scope="props">
                        <span>{{ props.row.gitRequestEvent.branch }}</span>
                    </template>
                </bk-table-column>
                <bk-table-column label="Consume" width="200">
                    <template slot-scope="props">
                        <p class="consume">
                            <span class="consume-item"><i class="bk-icon icon-clock"></i>{{ props.row.buildHistory.totalTime | totalFliter }}</span>
                            <span class="consume-item"><i class="bk-icon icon-calendar"></i>{{ props.row.buildHistory.startTime | timeFilter }}</span>
                        </p>
                    </template>
                </bk-table-column>
                <bk-table-column width="150" class-name="handler-btn">
                    <template slot-scope="props">
                        <opt-menu>
                            <li @click="cancelBuild(props.row)" v-if="['RUNNING', 'PREPARE_ENV', 'QUEUE', 'LOOP_WAITING', 'CALL_WAITING'].includes(props.row.buildHistory.status)">Cancel build</li>
                            <li @click="rebuild(props.row)" v-else>Re-build</li>
                        </opt-menu>
                    </template>
                </bk-table-column>
            </bk-table>
            <bk-pagination small
                :current.sync="compactPaging.current"
                :count.sync="compactPaging.count"
                :limit="compactPaging.limit"
                :show-limit="false"
                @change="pageChange"
                class="build-paging"
            />
        </section>

        <bk-sideslider @hidden="hidden" :is-show.sync="showTriggle" :width="622" :quick-close="true" title="Trigger a custom build">
            <bk-form :model="formData" ref="triggleForm" :label-width="500" slot="content" class="triggle-form" form-type="vertical">
                <bk-form-item label="Branch" :required="true" :rules="[requireRule('Branch')]" property="branch" error-display-type="normal">
                    <bk-select v-model="formData.branch" :clearable="false" :loading="isLoadingBranch" @selected="selectBranch" placeholder="Select a Branch">
                        <bk-option v-for="option in triggerBranches"
                            :key="option"
                            :id="option"
                            :name="option">
                        </bk-option>
                    </bk-select>
                </bk-form-item>
                <bk-form-item class="mt15">
                    <bk-checkbox v-model="formData.useCommitId" @change="getPipelineBranchYaml">Use a specified historical commit to trigger the build</bk-checkbox>
                </bk-form-item>
                <bk-form-item label="Commit" :required="true" :rules="[requireRule('Commit')]" property="commitId" error-display-type="normal" v-if="formData.useCommitId">
                    <bk-select v-model="formData.commitId" :clearable="false" :loading="isLoadingCommit" @selected="getPipelineBranchYaml" placeholder="Select a Commit">
                        <bk-option v-for="option in triggerCommits"
                            :key="option.id"
                            :id="option.id"
                            :name="option.message">
                        </bk-option>
                    </bk-select>
                </bk-form-item>
                <bk-form-item label="Custom Build Message" :required="true" :rules="[requireRule('Message')]" property="customCommitMsg" desc="Custom builds exist only on build history and will not appear in your commit history." error-display-type="normal">
                    <bk-input v-model="formData.customCommitMsg" placeholder="Please enter build message"></bk-input>
                </bk-form-item>
                <bk-form-item label="Yaml" property="yaml" :required="true" :rules="[requireRule('yaml')]" error-display-type="normal" v-bkloading="{ isLoading: isLoadingYaml }">
                    <code-section :code.sync="formData.yaml" :cursor-blink-rate="530" :read-only="false" ref="codeEditor" />
                </bk-form-item>
                <bk-form-item>
                    <bk-button ext-cls="mr5" theme="primary" title="Submit" @click.stop.prevent="submitData" :loading="isTriggering">Submit</bk-button>
                    <bk-button ext-cls="mr5" title="Cancel" @click="hidden" :disabled="isTriggering">Cancel</bk-button>
                </bk-form-item>
            </bk-form>
        </bk-sideslider>
    </article>
</template>

<script>
    import { mapState, mapActions } from 'vuex'
    import { pipelines } from '@/http'
    import { goYaml, preciseDiff, timeFormatter } from '@/utils'
    import optMenu from '@/components/opt-menu'
    import codeSection from '@/components/code-section'
    import { getPipelineStatusClass, getPipelineStatusCircleIconCls } from '@/components/status'

    export default {
        components: {
            optMenu,
            codeSection
        },

        filters: {
            timeFilter (val) {
                return timeFormatter(val)
            },

            totalFliter (val) {
                return preciseDiff(val)
            }
        },

        data () {
            return {
                buildList: [],
                compactPaging: {
                    limit: 10,
                    current: 1,
                    count: 0
                },
                filterData: {
                    commitMsg: '',
                    actor: '',
                    branch: [],
                    event: [],
                    status: []
                },
                filterList: [
                    {
                        id: 'branch',
                        placeholder: 'Branch',
                        data: []
                    },
                    {
                        id: 'event',
                        placeholder: 'Event',
                        data: []
                    },
                    {
                        id: 'status',
                        placeholder: 'Status',
                        data: []
                    }
                ],
                isLoadingFilter: false,
                isLoading: false,
                isLoadingBranch: false,
                isLoadingCommit: false,
                isLoadingYaml: false,
                isTriggering: false,
                showTriggle: false,
                formData: {
                    branch: '',
                    useCommitId: false,
                    commitId: '',
                    customCommitMsg: '',
                    yaml: ''
                },
                triggerBranches: [],
                triggerCommits: []
            }
        },

        computed: {
            ...mapState(['appHeight', 'curPipeline', 'projectId', 'projectInfo']),

            tableHeight () {
                return Math.min(this.appHeight - 331, 43 + (this.buildList.length || 3) * 72)
            }
        },

        watch: {
            curPipeline: {
                handler () {
                    this.getFilterData()
                    this.cleanFilterData()
                    this.initBuildData()
                },
                immediate: true
            },
            filterData: {
                handler () {
                    this.initBuildData()
                },
                deep: true
            }
        },

        created () {
            this.loopGetList()
        },

        beforeDestroy () {
            clearTimeout(this.loopGetList.loopId)
        },

        methods: {
            ...mapActions(['setCurPipeline']),

            getIconClass (status) {
                return [getPipelineStatusClass(status), ...getPipelineStatusCircleIconCls(status)]
            },

            getFilterData () {
                this.isLoadingFilter = true
                pipelines.getPipelineBuildBranchList(this.projectId).then((branchInfo) => {
                    this.filterList[0].data = (branchInfo.records || []).map((branch) => ({ name: branch.branchName, id: branch.branchName }))
                    this.filterList[1].data = [
                        { name: 'Push', id: 'PUSH' },
                        { name: 'Tag push', id: 'TAG' },
                        { name: 'Merge request', id: 'MERGE' },
                        { name: 'Manual trigger', id: 'MANUAL' }
                    ]
                    this.filterList[2].data = [
                        { name: 'Succeed', id: 'SUCCEED' },
                        { name: 'Failed', id: 'FAILED' },
                        { name: 'Cancelled', id: 'CANCELED' }
                    ]
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoadingFilter = false
                })
            },

            cleanFilterData () {
                this.filterData = {
                    commitMsg: '',
                    actor: '',
                    branch: [],
                    event: [],
                    status: []
                }
            },

            initBuildData () {
                this.isLoading = true
                this.getBuildData().finally(() => {
                    this.isLoading = false
                })
            },

            pageChange (page) {
                this.compactPaging.current = page
                this.initBuildData()
            },

            loopGetList () {
                clearTimeout(this.loopGetList.loopId)
                this.loopGetList.loopId = setTimeout(() => {
                    this.getBuildData()
                    this.loopGetList()
                }, 5000)
            },

            getBuildData () {
                const params = {
                    page: this.compactPaging.current,
                    pageSize: this.compactPaging.limit,
                    pipelineId: this.curPipeline.pipelineId,
                    triggerUser: this.filterData.actor.split(';').filter(v => v),
                    ...this.filterData
                }
                return pipelines.getPipelineBuildList(this.projectId, params).then((res = {}) => {
                    this.buildList = (res.records || []).map((build) => {
                        return {
                            ...build,
                            buildHistory: build.buildHistory || {},
                            gitRequestEvent: build.gitRequestEvent || {}
                        }
                    })
                    this.compactPaging.count = res.count
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                })
            },

            getCommitDesc ({ gitRequestEvent }) {
                let res = ''
                switch (gitRequestEvent.objectKind) {
                    case 'push':
                    case 'tag_push':
                        res = `Commit ${gitRequestEvent.commitId.slice(0, 9)} pushed by ${gitRequestEvent.userId}`
                        break
                    case 'merge_request':
                        const actionMap = {
                            'push-update': 'updated',
                            'reopen': 'reopened',
                            'open': 'opened'
                        }
                        res = `Merge requests [!${gitRequestEvent.mergeRequestId}] ${actionMap[gitRequestEvent.extensionAction]} by ${gitRequestEvent.userId}`
                        break
                    case 'manual':
                        res = `Manual by ${gitRequestEvent.userId}`
                        break
                }
                return res
            },

            togglePipelineEnable () {
                this.clickEmpty()
                pipelines.toggleEnablePipeline(this.projectId, this.curPipeline.pipelineId, !this.curPipeline.enabled).then(() => {
                    const pipeline = {
                        ...this.curPipeline,
                        enabled: !this.curPipeline.enabled
                    }
                    this.setCurPipeline(pipeline)
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                })
            },

            showTriggleBuild () {
                this.showTriggle = true
                this.getPipelineBranches()
            },

            getPipelineBranches (query = {}) {
                const params = {
                    page: 1,
                    perPage: 100,
                    projectId: this.projectId,
                    ...query
                }
                this.isLoadingBranch = true
                return pipelines.getPipelineBranches(params).then((res) => {
                    this.triggerBranches = res || []
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoadingBranch = false
                })
            },

            hidden () {
                this.showTriggle = false
                this.triggerCommits = []
                this.triggerBranches = []
                this.formData = {
                    branch: '',
                    useCommitId: false,
                    commitId: '',
                    customCommitMsg: '',
                    yaml: ''
                }
            },

            selectBranch () {
                this.getBranchCommits()
                this.getPipelineBranchYaml()
            },

            getBranchCommits (value, options, query = {}) {
                const params = {
                    page: 1,
                    perPage: 100,
                    projectId: this.projectId,
                    branch: this.formData.branch,
                    ...query
                }
                this.isLoadingCommit = true
                return pipelines.getPipelineCommits(params).then((res) => {
                    this.triggerCommits = res || []
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoadingCommit = false
                })
            },

            getPipelineBranchYaml () {
                const params = {
                    branchName: this.formData.branch,
                    commitId: this.formData.useCommitId ? this.formData.commitId : undefined
                }
                this.isLoadingYaml = true
                return pipelines.getPipelineBranchYaml(this.projectId, this.curPipeline.pipelineId, params).then((res) => {
                    this.formData.yaml = res || ''
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoadingYaml = false
                })
            },

            goToGit () {
                goYaml(this.projectInfo.web_url, this.curPipeline.branch, this.curPipeline.filePath)
            },

            cancelBuild (row) {
                this.clickEmpty()
                pipelines.cancelBuildPipeline(this.projectId, row.pipelineId, row.buildHistory.id).then(() => {
                    this.getBuildData()
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                })
            },

            clickEmpty () {
                const button = document.createElement('input')
                button.type = 'button'
                document.body.appendChild(button)
                button.click()
            },

            rebuild (row) {
                this.clickEmpty()
                pipelines.rebuildPipeline(this.projectId, row.pipelineId, row.buildHistory.id).then(() => {
                    this.getBuildData()
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                })
            },

            submitData () {
                this.$refs.triggleForm.validate().then(() => {
                    const postData = {
                        ...this.formData,
                        projectId: this.projectId,
                        commitId: this.formData.useCommitId ? this.formData.commitId : undefined
                    }
                    this.isTriggering = true
                    pipelines.trigglePipeline(this.curPipeline.pipelineId, postData).then(() => {
                        this.$bkMessage({ theme: 'success', message: 'Submitted successfully' })
                        this.hidden()
                        this.initBuildData()
                    }).catch((err) => {
                        this.$bkMessage({ theme: 'error', message: err.message || err })
                    }).finally(() => {
                        this.isTriggering = false
                    })
                }, (err) => {
                    this.$bkMessage({ theme: 'error', message: err.content || err })
                })
            },

            goToBuildDetail (row) {
                this.$router.push({
                    name: 'buildDetail',
                    params: {
                        buildId: row.buildHistory.id,
                        pipelineId: row.pipelineId,
                        buildNum: row.buildHistory.buildNum
                    }
                })
            },

            resetFilter () {
                this.filterData = {
                    commitMsg: '',
                    actor: '',
                    branch: [],
                    event: [],
                    status: []
                }
            },

            requireRule (name) {
                return {
                    required: true,
                    message: name + 'is required',
                    trigger: 'blur'
                }
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .pipelines-main {
        padding-left: 25px;
        .main-head {
            height: 64px;
            background: #fff;
            display: flex;
            align-items: center;
            justify-content: space-between;
            padding: 0 27px;
            line-height: 20px;
            .head-text {
                display: flex;
                align-items: center;
            }
            .pipeline-name {
                color: #313328;
                max-width: 300px;
                display: inline-block;
            }
            .yml-name {
                display: inline-block;
                margin: 0 3px 0 16px;
                max-width: 300px;
                cursor: pointer;
            }
            svg {
                vertical-align: sub;
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
            height: calc(100% - 82px);
            background: #fff;
            padding: 16px 24px 22px;
            .build-filter {
                display: flex;
                align-items: center;
                margin-bottom: 17px;
                .filter-item {
                    width: 200px;
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
                width: 32px;
                height: 32px;
                font-size: 32px;
                margin-right: 8px;
                line-height: 32px;
                &.executing {
                    font-size: 14px;
                }
                &.icon-exclamation, &.icon-exclamation-triangle, &.icon-clock {
                    font-size: 24px;
                }
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
        /deep/ .bk-table-row {
            cursor: pointer;
        }
        /deep/ .handler-btn {
            .cell {
                overflow: visible;
            }
        }
    }
    .triggle-form {
        padding: 20px 30px;
        /deep/ button {
            margin: 8px 10px 0 0;
        }
    }
</style>
