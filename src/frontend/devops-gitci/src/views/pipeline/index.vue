<template>
    <article class="pipelines-home">
        <aside class="aside-nav">
            <h3 class="nav-title">
                Pipelines
                <i class="bk-icon icon-plus" @click="showAddYml"></i>
            </h3>

            <ul>
                <li v-for="pipeline in pipelineList"
                    :key="pipeline.name"
                    @click="choosePipeline(pipeline)"
                    :class="{ 'nav-item': true, active: curPipeline.id === pipeline.id }"
                >
                    <icon :name="pipeline.icon || 'pipeline'" size="24"></icon>
                    {{ pipeline.name }}
                </li>
            </ul>
        </aside>

        <router-view class="pipelines-main"></router-view>

        <bk-sideslider :is-show.sync="isShowAddYml" :quick-close="true" :width="622" title="新增流水线">
            <bk-form :model="ymlData" ref="ymlForm" slot="content" class="yml-form" form-type="vertical">
                <bk-form-item label="yml" :rules="[requireRule('yml')]" :required="true" property="branch" error-display-type="normal">
                    <code-section :code="ymlData.yml" :read-only="false" :cursor-blink-rate="530"></code-section>
                </bk-form-item>
                <bk-form-item label="分支" :rules="[requireRule('分支')]" :required="true" property="branch" error-display-type="normal">
                    <bk-select v-model="ymlData.credentialType" :clearable="false">
                        <bk-option v-for="option in branchList"
                            :key="option.id"
                            :id="option.id"
                            :name="option.name">
                        </bk-option>
                    </bk-select>
                </bk-form-item>
                <bk-form-item label="commit message" :rules="[requireRule('commit message')]" :required="true" property="commitMessage" error-display-type="normal">
                    <bk-input v-model="ymlData.commitMessage"></bk-input>
                </bk-form-item>
                <bk-form-item>
                    <bk-button ext-cls="mr5" theme="primary" title="提交" @click.stop.prevent="submitData" :loading="isLoading">提交</bk-button>
                    <bk-button ext-cls="mr5" title="取消" @click="hidden" :disabled="isLoading">取消</bk-button>
                </bk-form-item>
            </bk-form>
        </bk-sideslider>
    </article>
</template>

<script>
    import codeSection from '@/components/code-section'

    export default {
        components: {
            codeSection
        },

        data () {
            return {
                pipelineList: [
                    { name: 'All pipeline', id: 0, icon: 'all' },
                    { name: 'Codecc check', id: 1 },
                    { name: 'Backend CI', id: 2 },
                    { name: 'Forntend CI', id: 3 }
                ],
                curPipeline: { name: 'All pipeline', id: 0, icon: 'all' },
                isShowAddYml: false,
                branchList: [],
                ymlData: {},
                isLoading: false
            }
        },

        methods: {
            choosePipeline (pipeline) {
                this.curPipeline = pipeline
                this.$router.push({
                    name: 'buildList',
                    params: this.$route.params
                })
            },

            showAddYml () {
                this.isShowAddYml = true
            },

            submitData () {

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
    .yml-form {
        padding: 20px 30px;
        /deep/ button {
            margin: 8px 10px 0 0;
        }
    }
</style>
