<template>
    <article class="pipelines-home">
        <aside class="aside-nav section-box" v-bkloading="{ isLoading }">
            <h3 class="nav-title">
                Pipelines
                <div v-bk-tooltips="{ content: 'Permission denied', disabled: permission }" class="nav-button">
                    <bk-button size="small" theme="primary" @click="showAddYml" :disabled="!permission">New</bk-button>
                </div>
            </h3>

            <ul v-if="!isLoading" @scroll.passive="scrollLoadMore">
                <li v-for="(pipeline, index) in pipelineList"
                    :key="index"
                    @click="choosePipeline(pipeline)"
                    :class="{ 'nav-item': true, active: curPipeline.pipelineId === pipeline.pipelineId, disabled: !pipeline.enabled }"
                >
                    <icon :name="pipeline.icon || 'pipeline'" size="24"></icon>
                    <span
                        class="text-ellipsis item-text"
                        v-bk-overflow-tips="{
                            content: pipeline.displayName,
                            placement: 'right'
                        }"
                    >{{ pipeline.displayName }}</span>
                </li>
            </ul>
        </aside>

        <router-view class="pipelines-main" v-if="!isLoading"></router-view>

        <bk-sideslider @hidden="hidden" :is-show.sync="isShowAddYml" :quick-close="true" :width="622" title="New pipeline">
            <bk-form :model="yamlData" ref="yamlForm" slot="content" class="yaml-form" form-type="vertical">
                <bk-form-item label="Name" :rules="[requireRule('Name'), nameRule]" :required="true" property="file_name" error-display-type="normal">
                    <bk-compose-form-item class="yaml-name-container">
                        <bk-input value=".ci / " disabled class="yaml-path"></bk-input>
                        <bk-input v-model="yamlData.file_name" class="yaml-name" placeholder="Please enter yml name，such as build.yml"></bk-input>
                    </bk-compose-form-item>
                </bk-form-item>
                <bk-form-item label="YAML" :rules="[requireRule('Yaml'), checkYaml]" ref="codeSection" :required="true" property="content" error-display-type="normal">
                    <bk-link theme="primary" href="https://iwiki.woa.com/x/OPBUL" target="_blank" class="yaml-examples">YAML Examples</bk-link>
                    <code-section @blur="$refs.codeSection.validate('blur')"
                        @focus="$refs.codeSection.clearValidator()"
                        :code.sync="yamlData.content"
                        :read-only="false"
                        :cursor-blink-rate="530"
                    ></code-section>
                </bk-form-item>
                <bk-form-item label="Branch" :rules="[requireRule('Branch')]" :required="true" property="branch_name" error-display-type="normal">
                    <bk-select v-model="yamlData.branch_name"
                        :remote-method="remoteGetBranchList"
                        :loading="isLoadingBranches"
                        :clearable="false"
                        searchable
                        @toggle="toggleFilterBranch"
                        placeholder="Please select branch"
                    >
                        <bk-option v-for="option in branchList"
                            :key="option"
                            :id="option"
                            :name="option">
                        </bk-option>
                    </bk-select>
                </bk-form-item>
                <bk-form-item label="Commit Message" :rules="[requireRule('commit message')]" :required="true" property="commit_message" error-display-type="normal">
                    <bk-input v-model="yamlData.commit_message" placeholder="Please input commit message"></bk-input>
                </bk-form-item>
                <bk-form-item>
                    <bk-button ext-cls="mr5" theme="primary" title="Submit" @click.stop.prevent="submitData" :loading="isSaving">Submit</bk-button>
                    <bk-button ext-cls="mr5" title="Cancel" @click="hidden" :disabled="isSaving">Cancel</bk-button>
                </bk-form-item>
            </bk-form>
        </bk-sideslider>
    </article>
</template>

<script>
    import { mapState, mapActions } from 'vuex'
    import codeSection from '@/components/code-section'
    import { pipelines } from '@/http'
    import { debounce } from '@/utils'
    import register from '@/utils/websocket-register'
    import validateRule from '@/utils/validate-rule'

    export default {
        components: {
            codeSection
        },

        data () {
            return {
                pipelineList: [],
                branchList: [],
                yamlData: {
                    file_name: '',
                    content: '',
                    branch_name: '',
                    commit_message: ''
                },
                isShowAddYml: this.$route.query.isNew,
                isLoading: false,
                isLoadingBranches: false,
                isSaving: false,
                isLoadingMore: false,
                isLoadEnd: false,
                page: 1,
                pageSize: 100,
                nameRule: {
                    validator (val) {
                        return /^[a-zA-Z0-9_\-\.]+$/.test(val)
                    },
                    message: 'Consists of uppercase and lowercase English letters, numbers, underscores, underscores and dots',
                    trigger: 'blur'
                },
                checkYaml: validateRule.checkYaml,
                allPipeline: { displayName: 'All pipelines', enabled: true, icon: 'all' }
            }
        },

        computed: {
            ...mapState(['projectId', 'curPipeline', 'menuPipelineId', 'permission'])
        },

        watch: {
            '$route.params.pipelineId' () {
                const pipelineId = this.$route.params.pipelineId
                const curPipeline = this.pipelineList.find((pipeline) => (pipeline.pipelineId === pipelineId)) || this.pipelineList[0]
                this.setCurPipeline(curPipeline)
            },
            '$route.name' (val) {
                if (val === 'buildList' && this.$route.params.pipelineId !== this.menuPipelineId) {
                    this.setMenuPipelineId(this.$route.params.pipelineId)
                }
            }

        },

        created () {
            this.initList()
        },

        beforeDestroy () {
            register.unInstallWsMessage('pipelineList')
        },

        methods: {
            ...mapActions(['setCurPipeline', 'setMenuPipelineId']),

            initList () {
                this.isLoading = true
                this.initPipelineList().then(() => {
                    this.pipelineList.unshift(this.allPipeline)
                    this.setDefaultPipeline()
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                })
            },

            scrollLoadMore (event) {
                const target = event.target
                const bottomDis = target.scrollHeight - target.clientHeight - target.scrollTop
                if (bottomDis <= 200 && !this.isLoadEnd && !this.isLoadingMore) this.getPipelineList()
            },

            initPipelineList () {
                register.installWsMessage(this.resetPipelineList, 'IFRAMEstream', 'pipelineList')
                return this.getPipelineList()
            },

            resetPipelineList () {
                this.page = 1
                this.pipelineList = [this.allPipeline]
                this.getPipelineList().catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                })
            },

            getPipelineList () {
                this.isLoadingMore = true
                const params = {
                    projectId: this.projectId,
                    page: this.page,
                    pageSize: this.pageSize
                }
                return pipelines.getPipelineList(params).then((res = {}) => {
                    const pipelines = (res.records || []).map((pipeline) => ({
                        displayName: pipeline.displayName,
                        enabled: pipeline.enabled,
                        pipelineId: pipeline.pipelineId,
                        filePath: pipeline.filePath,
                        branch: pipeline.latestBuildBranch
                    }))
                    this.page++
                    this.pipelineList = [...this.pipelineList, ...pipelines]
                    this.isLoadEnd = this.pipelineList.length > res.count
                }).finally(() => {
                    this.isLoadingMore = false
                })
            },

            setDefaultPipeline () {
                const pipelineId = this.$route.params.pipelineId
                const curPipeline = this.pipelineList.find((pipeline) => (pipeline.pipelineId === pipelineId)) || this.pipelineList[0]
                this.setMenuPipelineId(curPipeline.pipelineId)
                this.setCurPipeline(curPipeline)
                if (this.$route.name === 'pipeline') {
                    this.$router.push({
                        name: 'buildList',
                        params: {
                            pipelineId: curPipeline.pipelineId
                        }
                    })
                }
            },

            choosePipeline (pipeline) {
                this.setCurPipeline(pipeline)
                this.setMenuPipelineId(pipeline.pipelineId)
                this.$router.push({
                    name: 'buildList',
                    params: {
                        pipelineId: pipeline.pipelineId
                    }
                })
            },

            showAddYml () {
                this.isShowAddYml = true
            },

            hidden () {
                this.isShowAddYml = false
                this.yamlData = {
                    file_name: '',
                    content: '',
                    branch_name: '',
                    commit_message: ''
                }
            },

            toggleFilterBranch (isOpen) {
                if (isOpen) {
                    this.isLoadingBranches = true
                    this.getPipelineBranches().then(() => {
                        this.isLoadingBranches = false
                    })
                }
            },

            remoteGetBranchList (search) {
                return new Promise((resolve) => {
                    debounce(() => {
                        this.getPipelineBranches({ search }).then(() => {
                            resolve()
                        })
                    })
                })
            },

            getPipelineBranches (query = {}) {
                const params = {
                    projectId: this.projectId,
                    page: 1,
                    perPage: 100,
                    ...query
                }
                return new Promise((resolve) => {
                    pipelines.getPipelineBranches(params).then((res) => {
                        this.branchList = res || []
                    }).catch((err) => {
                        this.$bkMessage({ theme: 'error', message: err.message || err })
                    }).finally(() => {
                        resolve()
                    })
                })
            },

            submitData () {
                this.$refs.yamlForm.validate().then(() => {
                    const postData = {
                        ...this.yamlData,
                        file_path: `.ci/${this.yamlData.file_name}`
                    }
                    this.isSaving = true
                    pipelines.addPipelineYamlFile(this.projectId, postData).then(() => {
                        this.hidden()
                    }).catch((err) => {
                        this.$bkMessage({ theme: 'error', message: err.message || err })
                    }).finally(() => {
                        this.isSaving = false
                    })
                }, (err) => {
                    this.$bkMessage({ theme: 'error', message: err.content || err })
                })
            },

            requireRule (name) {
                return {
                    required: true,
                    message: name + ' is required',
                    trigger: 'blur'
                }
            }
        }
    }
</script>

<style lang="postcss" scoped>
    .pipelines-home {
        display: flex;
        padding: 25px;
        height: calc(100vh - 60px);
    }
    .nav-title {
        justify-content: space-between;
        margin: 0 12px 10px 20px;
        padding: 10px 0;
        font-size: 16px;
        .nav-button {
            height: 26px;
        }
        button {
            width: 56px;
            min-width: 56px;
            padding: 0;
            vertical-align: top;
        }
    }
    .pipelines-main {
        width: calc(100vw - 290px);
        height: 100%;
        background: #f5f6fa;
    }
    .yaml-form {
        padding: 20px 30px;
        height: calc(100vh - 60px);
        .yaml-path {
            width: 50px;
        }
        .yaml-name-container {
            width: 100%;
            .yaml-name {
                width: calc(100% - 50px);
            }
        }
        .yaml-examples {
            position: absolute;
            top: -26px;
            left: 70px;
            /deep/ .bk-link-text {
                font-size: 12px;
            }
        }
        /deep/ button {
            margin: 8px 10px 0 0;
        }
    }
</style>
