<template>
    <article class="pipelines-main">
        <header class="main-head">
            <span class="head-text">
                <span class="pipeline-name text-ellipsis" v-bk-overflow-tips>{{ curPipeline.displayName }}</span>
                <template v-if="curPipeline.filePath">
                    <span class="yml-name text-ellipsis" v-bk-overflow-tips>{{ curPipeline.filePath }}</span>
                    <icon name="cc-jump-link" size="16" @click.native="goToGit"></icon>
                </template>
                <span class="pipeline-status" v-if="!curPipeline.enabled">已禁用</span>
            </span>
            <opt-menu v-if="curPipeline.pipelineId !== 'all'">
                <li @click="showTriggleBuild">触发构建</li>
                <li @click="togglePipelineEnable">{{ curPipeline.enabled ? '禁用流水线' : '启用流水线' }}</li>
            </opt-menu>
        </header>

        <section class="main-body">
            <section class="build-filter" v-bkloading="{ isLoading: isLoadingFilter }">
                <bk-input v-model="filterData.commitMsg" class="filter-item" placeholder="Commit Message"></bk-input>
                <bk-select v-model="filterData[key]" v-for="(list, key) in filterList" :key="key" class="filter-item" :placeholder="key" multiple>
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
                v-bkloading="{ isLoading }"
                @row-click="goToBuildDetail"
                class="build-table"
                size="large"
            >
                <bk-table-column label="Commit message">
                    <template slot-scope="props">
                        <section class="commit-message">
                            <build-status :status="props.row.buildHistory.status"></build-status>
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
                <bk-table-column label="操作" width="150" class-name="handler-btn">
                    <template slot-scope="props">
                        <opt-menu>
                            <li @click="cancelBuild(props.row)" v-if="['RUNNING', 'PREPARE_ENV', 'QUEUE', 'LOOP_WAITING', 'CALL_WAITING'].includes(props.row.buildHistory.status)">取消构建</li>
                            <li @click="rebuild(props.row)" v-else>重新构建</li>
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
                <bk-form-item label="Select a Branch" :required="true" :rules="[requireRule('分支')]" property="branch" error-display-type="normal">
                    <bk-select v-model="formData.branch" :clearable="false" :loading="isLoadingBranch" @selected="selectBranch">
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
                <bk-form-item label="Select historical commit" :required="true" :rules="[requireRule('commit')]" property="commitId" error-display-type="normal" v-if="formData.useCommitId">
                    <bk-select v-model="formData.commitId" :clearable="false" :loading="isLoadingCommit" @selected="getPipelineBranchYaml">
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
                <bk-form-item label="yaml" property="yaml" :required="true" :rules="[requireRule('yaml')]" error-display-type="normal" v-bkloading="{ isLoading: isLoadingYaml }">
                    <code-section :code.sync="formData.yaml" :cursor-blink-rate="530" :read-only="false" ref="codeEditor" />
                </bk-form-item>
                <bk-form-item>
                    <bk-button ext-cls="mr5" theme="primary" title="提交" @click.stop.prevent="submitData" :loading="isTriggering">提交</bk-button>
                    <bk-button ext-cls="mr5" title="取消" @click="hidden" :disabled="isTriggering">取消</bk-button>
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
    import buildStatus from '@/components/build-status'

    export default {
        components: {
            optMenu,
            codeSection,
            buildStatus
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
                    triggerUser: [],
                    branch: [],
                    event: [],
                    status: []
                },
                filterList: {
                    triggerUser: [],
                    branch: [],
                    event: [],
                    status: []
                },
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
            ...mapState(['appHeight', 'curPipeline', 'projectId', 'projectInfo'])
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

            getFilterData () {
                this.isLoadingFilter = true
                Promise.all([
                    pipelines.getPipelineBuildBranchList(this.projectId),
                    pipelines.getPipelineBuildMemberList(this.projectId)
                ]).then(([branchInfo, memberInfo]) => {
                    this.filterList.branch = (branchInfo.records || []).map((branch) => ({ name: branch.branchName, id: branch.branchName }))
                    this.filterList.triggerUser = (memberInfo || []).map((member) => ({ name: member.username, id: member.username }))
                    this.filterList.event = [
                        { name: 'PUSH', id: 'PUSH' },
                        { name: 'TAG', id: 'TAG' },
                        { name: 'MERGE', id: 'MERGE' },
                        { name: 'MANUAL', id: 'MANUAL' }
                    ]
                    this.filterList.status = [
                        { name: '成功', id: 'SUCCEED' },
                        { name: '失败', id: 'FAILED' },
                        { name: '取消', id: 'CANCELED' }
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
                    triggerUser: [],
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
                    ...this.filterData
                }
                if (this.curPipeline.pipelineId !== 'all') {
                    params.pipelineId = this.curPipeline.pipelineId
                }
                return pipelines.getPipelineBuildList(this.projectId, params).then((res) => {
                    this.buildList = res.records || []
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
                    this.formData.yaml = res
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
                        this.$bkMessage({ theme: 'success', message: '触发成功' })
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
                        pipelineId: row.pipelineId
                    }
                })
            },

            resetFilter () {
                this.filterData = {
                    commitMsg: '',
                    triggerUser: [],
                    branch: [],
                    event: [],
                    status: []
                }
            },

            requireRule (name) {
                return {
                    required: true,
                    message: name + '是必填项',
                    trigger: 'blur'
                }
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .pipelines-main {
        padding: 20px 25px 36px;
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
                .filter-item {
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
