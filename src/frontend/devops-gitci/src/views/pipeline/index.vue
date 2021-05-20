<template>
    <article class="pipelines-home">
        <aside class="aside-nav" v-bkloading="{ isLoading }">
            <h3 class="nav-title">
                Pipelines
                <i class="bk-icon icon-plus" @click="showAddYml"></i>
            </h3>

            <ul v-if="!isLoading">
                <li v-for="(pipeline, index) in pipelineList"
                    :key="index"
                    @click="choosePipeline(pipeline)"
                    :class="{ 'nav-item': true, active: curPipeline.pipelineId === pipeline.pipelineId }"
                >
                    <icon :name="pipeline.icon || 'pipeline'" size="24"></icon>
                    <span class="text-ellipsis item-text" v-bk-overflow-tips>{{ pipeline.displayName }}</span>
                </li>
            </ul>
        </aside>

        <router-view class="pipelines-main" v-if="!isLoading"></router-view>

        <bk-sideslider @hidden="hidden" :is-show.sync="isShowAddYml" :quick-close="true" :width="622" title="新增流水线">
            <bk-form :model="yamlData" ref="yamlForm" slot="content" class="yaml-form" form-type="vertical">
                <bk-form-item label="名称" :rules="[requireRule('名称'), nameRule]" :required="true" property="file_name" error-display-type="normal">
                    <bk-compose-form-item>
                        <bk-input value=".ci / " disabled class="yaml-path"></bk-input>
                        <bk-input v-model="yamlData.file_name" class="yaml-name"></bk-input>
                    </bk-compose-form-item>
                </bk-form-item>
                <bk-form-item label="yaml" :rules="[requireRule('yaml')]" :required="true" property="content" error-display-type="normal">
                    <code-section :code.sync="yamlData.content" :read-only="false" :cursor-blink-rate="530"></code-section>
                </bk-form-item>
                <bk-form-item label="分支" :rules="[requireRule('分支')]" :required="true" property="branch_name" error-display-type="normal">
                    <bk-select v-model="yamlData.branch_name" :loading="isLoadingBranches" :clearable="false">
                        <bk-option v-for="option in branchList"
                            :key="option"
                            :id="option"
                            :name="option">
                        </bk-option>
                    </bk-select>
                </bk-form-item>
                <bk-form-item label="commit message" :rules="[requireRule('commit message')]" :required="true" property="commit_message" error-display-type="normal">
                    <bk-input v-model="yamlData.commit_message"></bk-input>
                </bk-form-item>
                <bk-form-item>
                    <bk-button ext-cls="mr5" theme="primary" title="提交" @click.stop.prevent="submitData" :loading="isSaving">提交</bk-button>
                    <bk-button ext-cls="mr5" title="取消" @click="hidden" :disabled="isSaving">取消</bk-button>
                </bk-form-item>
            </bk-form>
        </bk-sideslider>
    </article>
</template>

<script>
    import { mapState, mapActions } from 'vuex'
    import codeSection from '@/components/code-section'
    import { pipelines } from '@/http'

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
                isShowAddYml: false,
                isLoading: false,
                isLoadingBranches: false,
                isSaving: false,
                nameRule: {
                    validator (val) {
                        return /^[a-zA-Z0-9_\-\.]+$/.test(val)
                    },
                    message: '由大小写英文字母、数字、下划线、中划线和点组件',
                    trigger: 'blur'
                }
            }
        },

        computed: {
            ...mapState(['projectId', 'curPipeline'])
        },

        created () {
            this.initList()
        },

        methods: {
            ...mapActions(['setCurPipeline']),

            initList () {
                this.isLoading = true
                pipelines.getPipelineList(this.projectId).then((res) => {
                    const allPipeline = { displayName: 'All pipeline', pipelineId: 'all', enabled: true, icon: 'all' }
                    const pipelines = (res.records || []).map((pipeline) => ({
                        displayName: pipeline.displayName,
                        enabled: pipeline.enabled,
                        pipelineId: pipeline.pipelineId,
                        filePath: pipeline.filePath,
                        branch: pipeline.latestBuildInfo.gitRequestEvent.branch
                    }))
                    this.pipelineList = [allPipeline, ...pipelines]
                    this.setDefaultPipeline()
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoading = false
                })
            },

            setDefaultPipeline () {
                const pipelineId = this.$route.params.pipelineId
                const curPipeline = this.pipelineList.find((pipeline) => (pipeline.pipelineId === pipelineId)) || this.pipelineList[0]
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
                this.$router.push({
                    name: 'buildList',
                    params: {
                        pipelineId: pipeline.pipelineId
                    }
                })
            },

            showAddYml () {
                this.isShowAddYml = true
                this.getPipelineBranches()
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

            getPipelineBranches () {
                const params = {
                    projectId: this.projectId,
                    page: 1,
                    perPage: 100
                }
                this.isLoadingBranches = true
                pipelines.getPipelineBranches(params).then((res) => {
                    this.branchList = res
                }).catch((err) => {
                    this.$bkMessage({ theme: 'error', message: err.message || err })
                }).finally(() => {
                    this.isLoadingBranches = false
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
                    message: name + '是必填项',
                    trigger: 'blur'
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
    }
    .yaml-form {
        padding: 20px 30px;
        .yaml-path {
            width: 50px;
        }
        .yaml-name {
            width: 512px;
        }
        /deep/ button {
            margin: 8px 10px 0 0;
        }
    }
</style>
