<template>
    <div class="zy-container biz-container bkdevops-history-subpage pipeline-subpages">
        <inner-header class="customer-inner-header">
            <div class="history-bread-crumb" slot="left">
                <bread-crumb class="bread-crumb-comp" separator="/" :value="breadCrumbPath">
                    <template v-if="breadCrumbs && breadCrumbs.length">
                        <bread-crumb-item v-for="(crumb, index) in breadCrumbs" :key="index" v-bind="crumb">
                            <div class="build-num-switcher" v-if="isDetail && index === breadCrumbs.length - 1">
                                <template v-if="execDetail">
                                    <span>#{{ execDetail.buildNum }}</span>
                                    <p>
                                        <i class="devops-icon icon-angle-up" :disabled="execDetail.latestBuildNum === execDetail.buildNum || isLoading" @click="switchBuildNum(1)" />
                                        <i class="devops-icon icon-angle-down" :disabled="1 === execDetail.buildNum || isLoading" @click="switchBuildNum(-1)" />
                                    </p>
                                    <i class="devops-icon icon-txt" :title="$t('history.completedLog')" @click="showLog"></i>
                                </template>
                            </div>
                            <version-sideslider v-else-if="isEdit && index === breadCrumbs.length - 1"></version-sideslider>
                            <a class="bread-crumb-name" v-else @click="onBreadCrumbClick(crumb)">
                                {{ crumb.selectedValue }}
                            </a>
                        </bread-crumb-item>
                    </template>
                    <i v-else class="devops-icon icon-circle-2-1 spin-icon" />
                </bread-crumb>
            </div>
            <template v-if="isPreview" slot="right">
                <router-link :to="{ name: 'zyPipelinesEdit' }"><bk-button>{{ $t('edit') }}</bk-button></router-link>
                <bk-button :disabled="btnDisabled" :icon="executeStatus ? 'loading' : ''" theme="primary" @click="startExcuete">
                    {{ $t('exec') }}
                </bk-button>
            </template>
            <template v-else slot="right">
                <bk-button v-if="isEdit" @click="save" :disabled="saveBtnDisabled" :icon="saveStatus ? 'loading' : ''" theme="primary">
                    {{ $t('save') }}
                </bk-button>
                <router-link v-else :to="{ name: 'zyPipelinesEdit' }"><bk-button>{{ $t('edit') }}</bk-button></router-link>
                <triggers
                    class="bkdevops-header-trigger-btn"
                    :pipeline-id="pipelineId"
                    :status="pipelineStatus"
                    :can-manual-startup="canManualStartup"
                    :before-exec="isSaveAndRun ? save : undefined"
                    @exec="toExecute">
                    <section slot="exec-bar" slot-scope="triggerProps">
                        <bk-button v-if="pipelineStatus !== 'running'" theme="primary" :disabled="btnDisabled || !canManualStartup || triggerProps.isDisable" :icon="executeStatus || triggerProps.isDisable ? 'loading' : ''" :title="canManualStartup ? '' : '不支持手动启动流水线'">
                            {{ isSaveAndRun ? $t('subpage.saveAndExec') : $t('exec') }}
                        </bk-button>
                    </section>
                </triggers>
                <div :class="{ 'more-operation-entry': true, 'active': isDropmenuShow }">
                    <div class="entry-btn">
                        <i class="entry-circle" v-for="i in [1, 2, 3]" :key="i" />
                    </div>
                    <div class="more-operation-dropmenu">
                        <ul>
                            <li @click="renamePipeline">{{ $t('rename') }}</li>
                        </ul>
                        <ul>
                            <li @click="exportPipeline">{{ $t('newlist.exportPipelineJson') }}</li>
                            <li v-if="!isTemplatePipeline" @click="importModifyPipeline">{{ $t('newlist.importModifyPipelineJson') }}</li>
                            <li @click="copyPipeline">{{ $t('newlist.copyAs') }}</li>
                            <li @click="showTemplateDialog">{{ $t('newlist.saveAsTemp') }}</li>
                            <li v-if="isTemplatePipeline" @click="jumpToTemplate">{{ $t('newlist.jumpToTemp') }}</li>
                            <li @click="deletePipeline">{{ $t('delete') }}</li>
                        </ul>
                    </div>
                </div>
            </template>
        </inner-header>
        <router-view
            class="biz-content"
            v-bkloading="{ isLoading }"
            :exec-params="execParams"
            :deploy-label="deployLabel"
            :deploy-types="deployTypes"
        />
        <portal-target name="artifactory-popup"></portal-target>

        <bk-dialog width="500" :loading="dialogConfig.loading" header-position="left" :mask-close="false" v-model="isDialogShow" :title="dialogConfig.title" @confirm="dialogConfig.handleDialogConfirm" @cancel="dialogConfig.handleDialogCancel">
            <bk-form :model="dialogConfig.formData" form-type="vertical" style="padding: 0 10px">
                <bk-form-item v-for="item in dialogConfig.formConfig" :label="item.label" :required="item.required" :rules="item.rules" :property="item.name" :key="item.name">
                    <bk-radio-group v-if="item.component === 'enum-input'" v-model="dialogConfig.formData[item.name]">
                        <bk-radio style="margin-right: 10px;" :value="true">{{ $t('true') }}</bk-radio>
                        <bk-radio :value="false">{{ $t('false') }}</bk-radio>
                    </bk-radio-group>
                    <component v-else :is="item.component" v-model="dialogConfig.formData[item.name]" v-bind="item.bindData"></component>
                </bk-form-item>
            </bk-form>
        </bk-dialog>
        <export-dialog :is-show.sync="showExportDialog"></export-dialog>
        <import-pipeline-popup :handle-import-success="handleImportModifyPipeline" :is-show.sync="showImportDialog"></import-pipeline-popup>
    </div>
</template>

<script>
    import { mapState } from 'vuex'
    import PipelineIndex from '../subpages/index.vue'

    export default {
        name: 'ZyPipeline',
        mixins: [PipelineIndex],
        provide () {
            return {
                sendMessage: this.sendMessage,
                getExecParams: () => this.execParams
            }
        },
        data () {
            const {
                from = '交付流',
                deploy_label: deployLabel = '部署详情',
                deploy_type: deployType = ''
            } = this.$route.query || {}

            const deployTypes = deployType ? deployType.split(',') : undefined

            return {
                from,
                deployLabel,
                deployTypes,
                execParams: {},
                hasParamsButton: false,
                breadCrumbPath: []
            }
        },
        computed: {
            ...mapState('atom', [
                'execDetail',
                'saveStatus',
                'executeStatus'
            ]),
            isEdit () {
                return this.$route?.name === 'zyPipelinesEdit'
            },
            isDetail () {
                return this.$route?.name === 'zyPipelinesDetail'
            },
            isPreview () {
                return this.$route?.name === 'zyPipelinesPreview'
            },
            breadCrumbs () {
                return [{
                    selectedValue: this.from,
                    to: {
                        path: '/'
                    }
                }, {
                    selectedValue: '流水线',
                    path: '/pipeline'
                }, {
                    selectedValue: this.curPipeline.pipelineName || '--',
                    path: '/pipeline/detail'
                }, {
                    selectedValue: this.$route.params.type && this.tabMap[this.$route.params.type]
                        ? this.tabMap[this.$route.params.type]
                        : this.$t(this.$route.name.replace(/^zyPipelines/, 'pipelines'))
                }]
            }
        },
        async mounted () {
            window.addEventListener('message', this.onMessage)
            await this.$nextTick()
        },
        beforeDestroy () {
            window.removeEventListener('message', this.onMessage)
        },
        beforeRouteLeave (to, from, next) {
            const { name, query, params } = to || {}
            const redirectNames = [
                'pipelinesDetail',
                'pipelinesEdit'
            ]

            if (name === 'pipelinesPreview') {
                return this.sendMessage('syncUrl', {
                    url: to.fullPath,
                    refresh: true
                })
            }

            if (!redirectNames.includes(name)) {
                return next()
            }

            return next({
                name: name.replace(/^pipelines/, 'zyPipelines'),
                query,
                params
            })
        },
        methods: {
            onBreadCrumbClick (crumb) {
                this.sendMessage('syncUrl', {
                    url: crumb.path
                })
            },
            onParamsEdit () {
                this.sendMessage('editParams', this.execParams)
            },
            renamePipeline () {
                this.sendMessage('rename', this.curPipeline)
            },
            copyPipeline () {
                this.sendMessage('copy', this.curPipeline)
            },
            deletePipeline () {
                this.sendMessage('delete', this.curPipeline)
            },
            showDeployDetail (params) {
                this.sendMessage('showDeployDetail', params)
            },
            sendMessage (action, params) {
                window.top.postMessage({
                    action,
                    params
                }, '*')
            },
            onMessage (ev) {
                const { action, params } = ev?.data || {}

                if (action === 'syncParams') {
                    this.execParams = params || {}
                }
            }
        }
    }
</script>
<style lang="scss">
.zy-container {
    position: relative;

    .inner-header {
        height: 52px;
        line-height: 32px;
        padding: 8px 24px 8px 14px;
        border-bottom: none;
        box-shadow: none;

        .history-bread-crumb {
            .bread-crumb-name {
                font-size: 14px;
                color: rgba(0, 10, 41, 0.4);
            }

            .bread-crumb-comp {
                flex: 1;
            }

            .bread-crumb-item {
                .devops-icon.icon-angle-right:before {
                    content: '/';
                    color: rgba(0, 10, 41, 0.4);
                }
            }
        }

        .inner-header-right {
            margin-right: -10px;
        }

        .more-operation-entry {
            padding-top: 0;

            &:before {
                top: 0;
            }

            .entry-btn {
                z-index: 1;
            }

            .more-operation-dropmenu {
                top: 38px;
                right: 3px;

                & > ul:nth-child(1) li:nth-child(2) {
                    display: none;
                }
            }
        }

    }

    .biz-content {
        padding: 8px 16px;
    }

    .bkdevops-pipeline-edit-wrapper {
        .bk-tab-header,
        .bk-tab-section
        {
            padding: 0;
        }
    }

    .pipeline-execute-preview .execute-preview-content {
        padding: 0;
    }

    .pipeline-execute-preview {
        .execute-detail-option .scroll-wraper {
            padding: 0;
        }
    }

    .zy-log-action,
    .zy-params-button {
        display: inline-block;
        margin-left: 10px;
        color: #3c96ff;
        cursor: pointer;
        user-select: none;
    }
}
</style>
