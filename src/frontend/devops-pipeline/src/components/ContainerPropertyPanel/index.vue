<template>
    <bk-sideslider class="bkci-property-panel" width="640" :is-show.sync="visible" :quick-close="true">
        <header class="container-panel-header" slot="header">
            {{ title }}
            <div v-if="showDebugDockerBtn" :class="!editable ? 'control-bar' : 'debug-btn'">
                <bk-button theme="warning" @click="startDebug">{{ $t('editPage.docker.debugConsole') }}</bk-button>
            </div>
        </header>
        <container-content v-bind="$props" slot="content"></container-content>
    </bk-sideslider>
</template>

<script>
    import { mapActions, mapState } from 'vuex'
    import ContainerContent from './ContainerContent'

    export default {
        name: 'container-property-panel',
        components: {
            ContainerContent
        },
        props: {
            containerIndex: Number,
            stageIndex: Number,
            stages: Array,
            editable: Boolean,
            title: String
        },
        computed: {
            ...mapState('atom', [
                'isPropertyPanelVisible'
            ]),
            visible: {
                get () {
                    return this.isPropertyPanelVisible
                },
                set (value) {
                    this.togglePropertyPanel({
                        isShow: value
                    })
                }
            }
        },

        methods: {
            ...mapActions('soda', [
                'startDebugDocker',
                'getContainerInfoByBuildId',
                'startDebugDevcloud'
            ]),

            async startDebug () {
                const vmSeqId = this.getRealSeqId()
                let url = ''
                const tab = window.open('about:blank')
                try {
                    if (this.buildResourceType === 'DOCKER') {
                        // docker 分根据buildId获取容器信息和新启动一个容器
                        if (this.routeName === 'pipelinesDetail' && this.container.status === 'RUNNING') {
                            const res = await this.getContainerInfoByBuildId({
                                projectId: this.projectId,
                                pipelineId: this.pipelineId,
                                buildId: this.buildId,
                                vmSeqId
                            })
                            if (res.containerId && res.address) {
                                url = `${WEB_URL_PIRFIX}/pipeline/${this.projectId}/dockerConsole/?pipelineId=${this.pipelineId}&containerId=${res.containerId}&targetIp=${res.address}`
                            }
                        } else {
                            const res = await this.startDebugDocker({
                                projectId: this.projectId,
                                pipelineId: this.pipelineId,
                                vmSeqId,
                                imageCode: this.buildImageCode,
                                imageVersion: this.buildImageVersion,
                                imageName: this.buildResource,
                                buildEnv: this.container.buildEnv,
                                imageType: this.buildImageType,
                                credentialId: this.buildImageCreId
                            })
                            if (res === true) {
                                url = `${WEB_URL_PIRFIX}/pipeline/${this.projectId}/dockerConsole/?pipelineId=${this.pipelineId}&vmSeqId=${vmSeqId}`
                            }
                        }
                    } else if (this.buildResourceType === 'PUBLIC_DEVCLOUD') {
                        const buildIdStr = this.buildId ? `&buildId=${this.buildId}` : ''
                        url = `${WEB_URL_PIRFIX}/pipeline/${this.projectId}/dockerConsole/?type=DEVCLOUD&pipelineId=${this.pipelineId}&vmSeqId=${vmSeqId}${buildIdStr}`
                    }
                    tab.location = url
                } catch (err) {
                    tab.close()
                    if (err.code === 403) {
                        this.$showAskPermissionDialog({
                            noPermissionList: [{
                                resource: this.$t('pipeline'),
                                option: this.$t('edit')
                            }],
                            applyPermissionUrl: `${PERM_URL_PIRFIX}/backend/api/perm/apply/subsystem/?client_id=pipeline&project_code=${this.projectId}&service_code=pipeline&role_manager=pipeline:${this.pipelineId}`
                        })
                    } else {
                        this.$showTips({
                            theme: 'error',
                            message: err.message || err
                        })
                    }
                }
            },
            getRealSeqId () {
                let i = 0
                let seqId = 0
                this.stages && this.stages.map((stage, sIndex) => {
                    stage.containers.map((container, cIndex) => {
                        if (sIndex === this.stageIndex && cIndex === this.containerIndex) {
                            seqId = i
                        }
                        i++
                    })
                })
                return seqId
            }
        }
    }
</script>

<style lang="scss">
    @import '../AtomPropertyPanel/propertyPanel';
    .container-panel-header {
        display: flex;
        margin-right: 20px;
        justify-content: space-between;
    }
    .container-property-panel {
        font-size: 14px;
        .bk-image {
            display: flex;
            align-items: center;
            margin-top: 15px;
            .image-name {
                width: 50%;
                display: flex;
                align-items: center;
                .not-recommend {
                    text-decoration: line-through;
                }
                .image-named {
                    border: 1px solid #c4c6cc;
                    flex: 1;
                    height: 32px;
                    line-height: 32px;
                    font-size: 12px;
                    color: $fontWeightColor;
                    line-height: 32px;
                    padding-left: 10px;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    white-space: nowrap;
                    &.disable {
                        color: #c4c6cc;
                        cursor: not-allowed;
                    }
                }
            }
            .image-tag {
                width: 50%;
                margin-left: 10px;
            }
        }
        .container-resource-name {
            display: flex;
            align-items: center;
            > input {
                flex: 1;
            }
            .show-build-resource {
                margin-left: 10px;
            }
        }
        .control-bar {
            position: absolute;
            right: 34px;
            top: 12px;
        }
        .debug-btn {
            position: absolute;
            right: 34px;
            top: 12px;
        }
        .accordion-checkbox {
            margin-left: auto;
        }
        .bk-form-content span.bk-form-help {
            padding-top: 5px;
            display: inline-block;
            a {
                color: #3c96ff;
                &:hover {
                    color: #3c96ff;
                }
            }
        }
        form .bk-form-item {
            margin-top: 8px;
        }
    }
    .app-selector-item {
        margin: 10px 0;
        &:last-child {
            .devops-icon.icon-plus {
                display: block;
            }
        }
    }
    .build-path-tips {
        display: flex;
        min-height: 60px;
        margin-top: 8px;
        .tips-icon {
            display: flex;
            width: 44px;
            align-items: center;
            text-align: center;
            border: 1px solid #ffb400;
            background-color: #ffb400;
            i {
                display: inline-block;
                font-size: 18px;
                color: #fff;
                margin: 21px 13px;
            }
        }
        .tips-content {
            flex: 1;
            padding: 0 20px;
            border: 1px solid #e6e6e6;
            border-left: none;
            .tips-title {
                margin: 15px 0;
                font-weight: 600;
            }
            .tips-list {
                margin-bottom: 10px;
            }
        }
    }
</style>
